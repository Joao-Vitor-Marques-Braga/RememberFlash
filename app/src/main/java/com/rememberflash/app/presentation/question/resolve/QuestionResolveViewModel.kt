package com.rememberflash.app.presentation.question.resolve

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.usecase.discipline.GetDisciplineByIdUseCase
import com.rememberflash.app.domain.usecase.question.GetQuestionsByDisciplineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionResolveViewModel @Inject constructor(
    private val getDisciplineByIdUseCase: GetDisciplineByIdUseCase,
    private val getQuestionsByDisciplineUseCase: GetQuestionsByDisciplineUseCase,
    private val questionRepository: com.rememberflash.app.domain.repository.QuestionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // [ITEM 1.8 — FIX] Instância única reutilizada em vez de `Gson()` criada a cada
    // chamada de saveAttempt(). Gson é thread-safe e tem custo de inicialização.
    private val gson = com.google.gson.Gson()

    private val _uiState = MutableStateFlow(QuestionResolveUiState())
    val uiState: StateFlow<QuestionResolveUiState> = _uiState.asStateFlow()

    private val disciplineId: Long = checkNotNull(
        savedStateHandle.get<Long>("disciplineId") ?: savedStateHandle.get<String>("disciplineId")?.toLongOrNull()
    ) {
        "disciplineId é obrigatório"
    }

    private val topicId: Long? = savedStateHandle.get<Long>("topicId")
        ?: savedStateHandle.get<String>("topicId")?.toLongOrNull()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Reseta respostas anteriores para iniciar simulado novo limpo
            questionRepository.resetQuestionsForDiscipline(disciplineId)

            // Carrega disciplina
            val disciplineName = when (val discResult = getDisciplineByIdUseCase(disciplineId)) {
                is Result.Success -> discResult.data.name
                else -> "Simulado"
            }
            _uiState.value = _uiState.value.copy(disciplineName = disciplineName)

            // Carrega questões (do tópico específico ou gerais da disciplina)
            val questionsFlow = if (topicId != null && topicId > 0L) {
                questionRepository.getQuestionsByTopic(topicId)
            } else {
                getQuestionsByDisciplineUseCase(disciplineId)
            }

            questionsFlow.collectLatest { list ->
                // Não sobrescreve currentQuestionStartTime em emissões subsequentes
                val currentStartTime = _uiState.value.currentQuestionStartTime
                _uiState.value = _uiState.value.copy(
                    questions = list.sortedBy { it.createdAt },
                    isLoading = false,
                    currentQuestionStartTime = if (currentStartTime > 0L) currentStartTime
                    else System.currentTimeMillis()
                )
            }
        }
    }

    private fun updateTimeSpentForCurrentQuestion() {
        val currentIndex = _uiState.value.currentIndex
        val question = _uiState.value.questions.getOrNull(currentIndex) ?: return
        val hasSubmitted = _uiState.value.submittedAnswers.contains(currentIndex)
        if (hasSubmitted) return

        // [ITENS 1.1/1.2 — DUPLICAÇÃO REMOVIDA] Cálculo extraído para
        // elapsedSecondsFrom() e accumulateQuestionTime() em QuestionResolveTimeUtils.kt.
        val elapsed = elapsedSecondsFrom(_uiState.value.currentQuestionStartTime)
        if (elapsed == 0) return

        _uiState.value = _uiState.value.copy(
            questionTimes = accumulateQuestionTime(_uiState.value.questionTimes, question.id, elapsed),
            currentQuestionStartTime = System.currentTimeMillis()
        )
    }

    private fun saveAttempt() {
        val score = _uiState.value.score
        val totalQuestions = _uiState.value.questions.size
        
        // Mapeia questionId -> chosenOptionIndex
        val answersMap = _uiState.value.selectedAnswers.mapKeys { (index, _) ->
            _uiState.value.questions.getOrNull(index)?.id ?: 0L
        }
        val answersJsonString = gson.toJson(answersMap)
        val timesJsonString = gson.toJson(_uiState.value.questionTimes)

        viewModelScope.launch {
            val attempt = com.rememberflash.app.domain.model.MockExamAttempt(
                disciplineId = disciplineId,
                score = score,
                totalQuestions = totalQuestions,
                answersJson = answersJsonString,
                timesJson = timesJsonString
            )
            questionRepository.saveMockExamAttempt(attempt)
        }
    }

    fun selectOption(optionIndex: Int) {
        val currentIndex = _uiState.value.currentIndex
        val hasSubmitted = _uiState.value.submittedAnswers.contains(currentIndex)
        if (hasSubmitted) return // can't change answer after submitting!

        val updatedMap = _uiState.value.selectedAnswers.toMutableMap()
        updatedMap[currentIndex] = optionIndex
        _uiState.value = _uiState.value.copy(selectedAnswers = updatedMap)
    }

    fun submitAnswer() {
        val currentIndex = _uiState.value.currentIndex
        val selectedIndex = _uiState.value.selectedAnswers[currentIndex] ?: return
        val question = _uiState.value.questions.getOrNull(currentIndex) ?: return

        val isCorrect = selectedIndex == question.correctIndex
        val updatedSet = _uiState.value.submittedAnswers.toMutableSet()
        updatedSet.add(currentIndex)

        // Trava o tempo da questão no momento em que responde
        // [ITENS 1.1/1.2 — DUPLICAÇÃO REMOVIDA] Usa elapsedSecondsFrom() e
        // accumulateQuestionTime() de QuestionResolveTimeUtils.kt.
        val elapsedSeconds = elapsedSecondsFrom(_uiState.value.currentQuestionStartTime)
        val updatedTimes = accumulateQuestionTime(_uiState.value.questionTimes, question.id, elapsedSeconds)

        val newScore = if (isCorrect) _uiState.value.score + 1 else _uiState.value.score

        _uiState.value = _uiState.value.copy(
            submittedAnswers = updatedSet,
            score = newScore,
            questionTimes = updatedTimes
        )

        // Persistência local no banco de dados
        viewModelScope.launch {
            questionRepository.answerQuestion(question.id, selectedIndex, isCorrect)
        }
    }

    fun nextQuestion() {
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex < _uiState.value.questions.size) {
            updateTimeSpentForCurrentQuestion()
            _uiState.value = _uiState.value.copy(
                currentIndex = nextIndex,
                currentQuestionStartTime = System.currentTimeMillis()
            )
        } else {
            // Fim do Simulado
            updateTimeSpentForCurrentQuestion()
            _uiState.value = _uiState.value.copy(isFinished = true)
            saveAttempt()
        }
    }

    fun previousQuestion() {
        val prevIndex = _uiState.value.currentIndex - 1
        if (prevIndex >= 0) {
            updateTimeSpentForCurrentQuestion()
            _uiState.value = _uiState.value.copy(
                currentIndex = prevIndex,
                currentQuestionStartTime = System.currentTimeMillis()
            )
        }
    }

    fun finishPractice() {
        // [BUG #4 — FIX] Não acumula tempo da questão atual antes de encerrar.
        // - Se a questão foi submetida: submitAnswer() já travou o tempo exato.
        //   Chamar updateTimeSpentForCurrentQuestion() seria no-op (o guard interno
        //   retorna cedo), mas evitamos a chamada por clareza.
        // - Se a questão NÃO foi submetida: o tempo não deve entrar nas estatísticas,
        //   pois a questão ficou sem resposta e incluí-la distorceria a média.
        _uiState.value = _uiState.value.copy(isFinished = true)
        saveAttempt()
    }
}
