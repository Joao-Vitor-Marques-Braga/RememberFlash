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

    private val _uiState = MutableStateFlow(QuestionResolveUiState())
    val uiState: StateFlow<QuestionResolveUiState> = _uiState.asStateFlow()

    private val disciplineId: Long = checkNotNull(savedStateHandle["disciplineId"]) {
        "disciplineId é obrigatório"
    }

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

            // Carrega questões
            getQuestionsByDisciplineUseCase(disciplineId).collectLatest { list ->
                _uiState.value = _uiState.value.copy(
                    questions = list.sortedBy { it.createdAt },
                    isLoading = false
                )
            }
        }
    }

    private fun saveAttempt() {
        val score = _uiState.value.score
        val totalQuestions = _uiState.value.questions.size
        
        // Mapeia questionId -> chosenOptionIndex
        val answersMap = _uiState.value.selectedAnswers.mapKeys { (index, _) ->
            _uiState.value.questions.getOrNull(index)?.id ?: 0L
        }
        val answersJsonString = com.google.gson.Gson().toJson(answersMap)

        viewModelScope.launch {
            val attempt = com.rememberflash.app.domain.model.MockExamAttempt(
                disciplineId = disciplineId,
                score = score,
                totalQuestions = totalQuestions,
                answersJson = answersJsonString
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

        val newScore = if (isCorrect) _uiState.value.score + 1 else _uiState.value.score

        _uiState.value = _uiState.value.copy(
            submittedAnswers = updatedSet,
            score = newScore
        )

        // Persistência local no banco de dados
        viewModelScope.launch {
            questionRepository.answerQuestion(question.id, selectedIndex, isCorrect)
        }
    }

    fun nextQuestion() {
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex < _uiState.value.questions.size) {
            _uiState.value = _uiState.value.copy(currentIndex = nextIndex)
        } else {
            // Fim do Simulado
            _uiState.value = _uiState.value.copy(isFinished = true)
            saveAttempt()
        }
    }

    fun previousQuestion() {
        val prevIndex = _uiState.value.currentIndex - 1
        if (prevIndex >= 0) {
            _uiState.value = _uiState.value.copy(currentIndex = prevIndex)
        }
    }

    fun finishPractice() {
        _uiState.value = _uiState.value.copy(isFinished = true)
        saveAttempt()
    }
}
