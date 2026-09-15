package com.rememberflash.app.presentation.discipline

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.data.local.pdf.LocalPdfExtractor
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.FlashcardSource
import com.rememberflash.app.domain.usecase.discipline.GetDisciplineByIdUseCase
import com.rememberflash.app.domain.usecase.flashcard.CreateFlashcardUseCase
import com.rememberflash.app.domain.usecase.flashcard.DeleteFlashcardUseCase
import com.rememberflash.app.domain.usecase.flashcard.GenerateFlashcardsFromTextUseCase
import com.rememberflash.app.domain.usecase.flashcard.GetFlashcardsByDisciplineUseCase
import com.rememberflash.app.domain.usecase.flashcard.UpdateFlashcardUseCase
import com.rememberflash.app.domain.usecase.question.GenerateQuestionsUseCase
import com.rememberflash.app.domain.usecase.question.GetQuestionsByDisciplineUseCase
import com.rememberflash.app.domain.usecase.topic.CreateTopicUseCase
import com.rememberflash.app.domain.usecase.topic.DeleteTopicUseCase
import com.rememberflash.app.domain.usecase.topic.GetTopicsByDisciplineUseCase
import com.rememberflash.app.domain.usecase.topic.UpdateTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DisciplineViewModel @Inject constructor(
    private val getDisciplineByIdUseCase: GetDisciplineByIdUseCase,
    private val getTopicsByDisciplineUseCase: GetTopicsByDisciplineUseCase,
    private val createTopicUseCase: CreateTopicUseCase,
    private val deleteTopicUseCase: DeleteTopicUseCase,
    private val updateTopicUseCase: UpdateTopicUseCase,
    private val getFlashcardsByDisciplineUseCase: GetFlashcardsByDisciplineUseCase,
    private val getQuestionsByDisciplineUseCase: GetQuestionsByDisciplineUseCase,
    private val createFlashcardUseCase: CreateFlashcardUseCase,
    private val updateFlashcardUseCase: UpdateFlashcardUseCase,
    private val deleteFlashcardUseCase: DeleteFlashcardUseCase,
    private val generateFlashcardsFromTextUseCase: GenerateFlashcardsFromTextUseCase,
    private val generateQuestionsUseCase: GenerateQuestionsUseCase,
    private val questionRepository: com.rememberflash.app.domain.repository.QuestionRepository,
    private val syncManager: com.rememberflash.app.data.sync.SyncManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisciplineUiState())
    val uiState: StateFlow<DisciplineUiState> = _uiState.asStateFlow()

    val disciplineId: Long = checkNotNull(savedStateHandle["disciplineId"]) {
        "disciplineId é obrigatório"
    }

    val isSyncing = syncManager.isSyncing

    fun syncNow(onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            when (val res = syncManager.syncNow()) {
                is Result.Success -> onResult(res.data)
                is Result.Error -> onResult(res.message)
                else -> {}
            }
        }
    }

    init {
        loadDiscipline()
        observeData()
        syncManager.triggerSync()
    }

    private fun loadDiscipline() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = getDisciplineByIdUseCase(disciplineId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        discipline = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            getTopicsByDisciplineUseCase(disciplineId).collectLatest { topics ->
                _uiState.value = _uiState.value.copy(topics = topics)
                // Recarrega os dados da disciplina para refletir contadores atualizados
                when (val result = getDisciplineByIdUseCase(disciplineId)) {
                    is Result.Success -> _uiState.value = _uiState.value.copy(discipline = result.data)
                    else -> {}
                }
            }
        }
        viewModelScope.launch {
            getFlashcardsByDisciplineUseCase(disciplineId).collectLatest { flashcards ->
                _uiState.value = _uiState.value.copy(flashcards = flashcards)
            }
        }
        viewModelScope.launch {
            getQuestionsByDisciplineUseCase(disciplineId).collectLatest { questions ->
                _uiState.value = _uiState.value.copy(questions = questions)
            }
        }
        viewModelScope.launch {
            questionRepository.getAttemptsByDiscipline(disciplineId).collectLatest { attempts ->
                _uiState.value = _uiState.value.copy(attempts = attempts)
            }
        }
    }

    fun selectTopic(topicId: Long?) {
        _uiState.value = _uiState.value.copy(selectedTopicId = topicId)
    }

    fun createTopic(name: String, description: String? = null, onSuccess: () -> Unit = {}) {
        val contestId = _uiState.value.discipline?.contestId ?: return
        viewModelScope.launch {
            when (val result = createTopicUseCase(disciplineId, contestId, name, description)) {
                is Result.Success -> {
                    syncManager.triggerSync()
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteTopic(topicId: Long) {
        viewModelScope.launch {
            syncManager.deleteTopicRemote(topicId)
            deleteTopicUseCase(topicId)
            if (_uiState.value.selectedTopicId == topicId) {
                _uiState.value = _uiState.value.copy(selectedTopicId = null)
            }
        }
    }

    fun onPdfSelected(uri: Uri, name: String, size: Long) {
        _uiState.value = _uiState.value.copy(
            pdfUri = uri,
            pdfName = name,
            pdfSize = size,
            pdfError = null
        )
    }

    fun setPdfError(errorMsg: String) {
        _uiState.value = _uiState.value.copy(
            pdfUri = null,
            pdfName = "",
            pdfSize = 0L,
            pdfError = errorMsg
        )
    }

    fun clearPdf() {
        _uiState.value = _uiState.value.copy(
            pdfUri = null,
            pdfName = "",
            pdfSize = 0L,
            pdfError = null
        )
    }

    // PDF Geração automatizada (RF007)
    fun generateFlashcardsFromPdf(quantity: Int) {
        val uri = _uiState.value.pdfUri
        if (uri == null) {
            _uiState.value = _uiState.value.copy(error = "Selecione um arquivo PDF primeiro")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingFlashcards = true, error = null)

            val extractedText = withContext(Dispatchers.IO) {
                LocalPdfExtractor.extractText(context, uri)
            }

            if (extractedText.isBlank() || extractedText.length < 50) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingFlashcards = false,
                    error = "O PDF selecionado não contém texto legível (documento escaneado). Envie um PDF com camada de texto"
                )
                return@launch
            }

            when (val saveResult = generateFlashcardsFromTextUseCase(
                text = extractedText,
                disciplineId = disciplineId,
                quantity = quantity,
                topicId = null
            )) {
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
                else -> {
                    _uiState.value = _uiState.value.copy(isGeneratingFlashcards = false)
                }
            }
        }
    }

    // Questões Geração (RF008)
    fun generateQuestionsIA(quantity: Int, theme: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingQuestions = true, error = null, isQuestionsGeneratedSuccess = false)

            when (val result = generateQuestionsUseCase(disciplineId, quantity, theme)) {
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

    // Manual CRUD (RF006)
    suspend fun createManualFlashcard(front: String, back: String, topicId: Long? = null): Result<Long> {
        if (front.isBlank() || back.isBlank()) {
            return Result.error("A frente e o verso do cartão são obrigatórios.")
        }
        val card = Flashcard(
            disciplineId = disciplineId,
            topicId = topicId,
            front = front,
            back = back,
            source = FlashcardSource.MANUAL,
            isSynced = false
        )
        val res = createFlashcardUseCase(card)
        if (res is Result.Success) {
            syncManager.triggerSync()
        }
        return res
    }

    suspend fun updateManualFlashcard(card: Flashcard, front: String, back: String, topicId: Long? = null): Result<Unit> {
        if (front.isBlank() || back.isBlank()) {
            return Result.error("A frente e o verso do cartão são obrigatórios.")
        }
        val updatedCard = card.copy(
            front = front,
            back = back,
            topicId = topicId ?: card.topicId,
            isSynced = false
        )
        val res = updateFlashcardUseCase(updatedCard)
        if (res is Result.Success) {
            syncManager.triggerSync()
        }
        return res
    }

    suspend fun deleteManualFlashcard(flashcardId: Long): Result<Unit> {
        syncManager.deleteFlashcardRemote(flashcardId)
        return deleteFlashcardUseCase(flashcardId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetQuestionsSuccess() {
        _uiState.value = _uiState.value.copy(isQuestionsGeneratedSuccess = false)
    }
}
