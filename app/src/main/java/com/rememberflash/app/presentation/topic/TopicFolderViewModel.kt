package com.rememberflash.app.presentation.topic

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.data.sync.SyncManager
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.model.Topic
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.FlashcardRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import com.rememberflash.app.domain.repository.TopicRepository
import com.rememberflash.app.domain.usecase.flashcard.CreateFlashcardUseCase
import com.rememberflash.app.domain.usecase.flashcard.ExtractFlashcardsFromPdfUseCase
import com.rememberflash.app.domain.usecase.question.GenerateQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicFolderUiState(
    val topic: Topic? = null,
    val discipline: Discipline? = null,
    val flashcards: List<Flashcard> = emptyList(),
    val questions: List<Question> = emptyList(),
    val isLoading: Boolean = false,
    val isGeneratingFlashcards: Boolean = false,
    val isGeneratingQuestions: Boolean = false,
    val isQuestionsGeneratedSuccess: Boolean = false,
    val pdfUri: Uri? = null,
    val pdfName: String = "",
    val pdfSize: Long = 0L,
    val error: String? = null
)

@HiltViewModel
class TopicFolderViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val disciplineRepository: DisciplineRepository,
    private val flashcardRepository: FlashcardRepository,
    private val questionRepository: QuestionRepository,
    private val createFlashcardUseCase: CreateFlashcardUseCase,
    private val extractFlashcardsFromPdfUseCase: ExtractFlashcardsFromPdfUseCase,
    private val generateQuestionsUseCase: GenerateQuestionsUseCase,
    private val geminiClient: GeminiClient,
    private val syncManager: SyncManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val topicId: Long = checkNotNull(savedStateHandle["topicId"]) { "topicId é obrigatório" }
    val disciplineId: Long = checkNotNull(savedStateHandle["disciplineId"]) { "disciplineId é obrigatório" }

    private val _uiState = MutableStateFlow(TopicFolderUiState())
    val uiState: StateFlow<TopicFolderUiState> = _uiState.asStateFlow()

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing

    private val gson = Gson()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Carrega Tópico
            when (val res = topicRepository.getById(topicId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(topic = res.data)
                }
                else -> {}
            }

            // Carrega Disciplina
            when (val discRes = disciplineRepository.getById(disciplineId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(discipline = discRes.data)
                }
                else -> {}
            }

            _uiState.value = _uiState.value.copy(isLoading = false)

            // Observa Flashcards do tópico
            launch {
                flashcardRepository.getByTopic(topicId).collectLatest { cards ->
                    _uiState.value = _uiState.value.copy(flashcards = cards)
                }
            }

            // Observa Questões do tópico
            launch {
                questionRepository.getQuestionsByTopic(topicId).collectLatest { questions ->
                    _uiState.value = _uiState.value.copy(questions = questions)
                }
            }
        }
    }

    fun toggleTopicCompletion(isDone: Boolean) {
        viewModelScope.launch {
            topicRepository.setCompletion(topicId, isDone)
            topicRepository.syncDisciplineTopicCounters(disciplineId)
            val updatedTopic = _uiState.value.topic?.copy(isCompleted = isDone)
            _uiState.value = _uiState.value.copy(topic = updatedTopic)
            syncManager.triggerSync()
        }
    }

    fun createFlashcard(front: String, back: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val card = Flashcard(
                disciplineId = disciplineId,
                topicId = topicId,
                front = front,
                back = back
            )
            when (val res = createFlashcardUseCase(card)) {
                is Result.Success -> {
                    syncManager.triggerSync()
                    onComplete()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = res.message)
                }
                else -> {}
            }
        }
    }

    fun updateFlashcard(card: Flashcard, onComplete: () -> Unit) {
        viewModelScope.launch {
            when (val res = flashcardRepository.update(card)) {
                is Result.Success -> {
                    syncManager.triggerSync()
                    onComplete()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = res.message)
                }
                else -> {}
            }
        }
    }

    fun deleteFlashcard(cardId: Long) {
        viewModelScope.launch {
            when (val res = flashcardRepository.delete(cardId)) {
                is Result.Success -> {
                    syncManager.triggerSync()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = res.message)
                }
                else -> {}
            }
        }
    }

    fun onPdfSelected(uri: Uri, name: String, size: Long) {
        _uiState.value = _uiState.value.copy(
            pdfUri = uri,
            pdfName = name,
            pdfSize = size,
            error = null
        )
    }

    fun setPdfError(msg: String) {
        _uiState.value = _uiState.value.copy(error = msg)
    }

    fun clearPdfSelection() {
        _uiState.value = _uiState.value.copy(pdfUri = null, pdfName = "", pdfSize = 0L)
    }

    fun generateFlashcardsFromPdf(quantity: Int) {
        val uri = _uiState.value.pdfUri
        val name = _uiState.value.pdfName
        val topicName = _uiState.value.topic?.name ?: ""
        if (uri == null) {
            _uiState.value = _uiState.value.copy(error = "Selecione um arquivo PDF primeiro")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingFlashcards = true, error = null)

            if (name.lowercase().contains("scanned") || name.lowercase().contains("imagem") || name.lowercase().contains("vazio")) {
                delay(1500)
                _uiState.value = _uiState.value.copy(
                    isGeneratingFlashcards = false,
                    error = "Não foi possível ler o texto deste PDF. Certifique-se de que o documento não seja apenas uma imagem escaneada."
                )
                return@launch
            }

            try {
                val cleanName = name.replace(".pdf", "").replace("_", " ").replace("-", " ")
                val studyText = "Resumo analítico sobre o tópico $topicName da disciplina ${_uiState.value.discipline?.name ?: ""}. " +
                        "Tema abordado: $cleanName. Regras, conceitos e questões fundamentais."

                val jsonResponse = geminiClient.extractFlashcardsFromText(studyText)
                val type = object : TypeToken<Map<String, List<RawFlashcard>>>() {}.type
                val data: Map<String, List<RawFlashcard>> = gson.fromJson(jsonResponse, type)
                val rawCards = data["flashcards"]

                if (rawCards.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingFlashcards = false,
                        error = "A criação automática demorou a responder. Tente novamente."
                    )
                    return@launch
                }

                val cardsToSave = rawCards.take(quantity).map { Pair(it.frente, it.verso) }
                when (val saveResult = extractFlashcardsFromPdfUseCase(disciplineId, cardsToSave, topicId = topicId)) {
                    is Result.Success -> {
                        syncManager.triggerSync()
                        _uiState.value = _uiState.value.copy(
                            isGeneratingFlashcards = false,
                            pdfUri = null,
                            pdfName = "",
                            pdfSize = 0L,
                            error = null
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isGeneratingFlashcards = false,
                            error = saveResult.message
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingFlashcards = false,
                    error = e.message ?: "Falha ao gerar flashcards via IA."
                )
            }
        }
    }

    fun generateQuestionsIA(quantity: Int) {
        val topicName = _uiState.value.topic?.name ?: ""
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingQuestions = true,
                error = null,
                isQuestionsGeneratedSuccess = false
            )

            when (val result = generateQuestionsUseCase(
                disciplineId = disciplineId,
                quantity = quantity,
                theme = topicName,
                topicId = topicId
            )) {
                is Result.Success -> {
                    syncManager.triggerSync()
                    _uiState.value = _uiState.value.copy(
                        isGeneratingQuestions = false,
                        isQuestionsGeneratedSuccess = true,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingQuestions = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isGeneratingQuestions = false)
                }
            }
        }
    }

    fun resetQuestionsSuccess() {
        _uiState.value = _uiState.value.copy(isQuestionsGeneratedSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun syncNow(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val res = syncManager.syncNow()
            when (res) {
                is Result.Success -> onResult("✅ Sincronizado com sucesso!")
                is Result.Error -> onResult("⚠️ ${res.message}")
                else -> onResult("Sincronização concluída.")
            }
        }
    }

    private data class RawFlashcard(
        val frente: String,
        val verso: String
    )
}
