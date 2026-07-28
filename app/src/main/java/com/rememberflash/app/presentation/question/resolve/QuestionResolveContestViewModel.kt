package com.rememberflash.app.presentation.question.resolve

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionWithDiscipline(
    val question: Question,
    val disciplineName: String
)

data class QuestionResolveContestUiState(
    val questions: List<QuestionWithDiscipline> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(), // index -> chosen option
    val submittedAnswers: Set<Int> = emptySet(), // index set
    val score: Int = 0,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val contestTitle: String = "Simulado do Edital"
)

@HiltViewModel
class QuestionResolveContestViewModel @Inject constructor(
    private val contestRepository: ContestRepository,
    private val disciplineRepository: DisciplineRepository,
    private val questionRepository: QuestionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionResolveContestUiState())
    val uiState: StateFlow<QuestionResolveContestUiState> = _uiState.asStateFlow()

    private val contestId: Long = checkNotNull(savedStateHandle["contestId"]) {
        "contestId é obrigatório"
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Reseta respostas anteriores para iniciar simulado novo limpo
            questionRepository.resetQuestionsForContest(contestId)

            // Carrega Concurso
            val contestResult = contestRepository.getById(contestId)
            val title = when (contestResult) {
                is Result.Success -> contestResult.data.title
                else -> "Simulado do Edital"
            }
            _uiState.value = _uiState.value.copy(contestTitle = title)

            try {
                // Carrega todas as disciplinas do concurso
                val disciplines = disciplineRepository.getByContest(contestId).first()
                    .filter { it.isActive }

                val unifiedQuestions = mutableListOf<QuestionWithDiscipline>()

                // Carrega as questões de cada disciplina
                disciplines.forEach { discipline ->
                    val questions = questionRepository.getQuestionsByDiscipline(discipline.id).first()
                    questions.forEach { question ->
                        unifiedQuestions.add(QuestionWithDiscipline(question, discipline.name))
                    }
                }

                _uiState.value = _uiState.value.copy(
                    questions = unifiedQuestions.sortedBy { it.question.createdAt },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erro ao carregar simulado: ${e.localizedMessage}",
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
            _uiState.value.questions.getOrNull(index)?.question?.id ?: 0L
        }
        val answersJsonString = com.google.gson.Gson().toJson(answersMap)

        viewModelScope.launch {
            val attempt = com.rememberflash.app.domain.model.MockExamAttempt(
                contestId = contestId,
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
        if (hasSubmitted) return

        val updatedMap = _uiState.value.selectedAnswers.toMutableMap()
        updatedMap[currentIndex] = optionIndex
        _uiState.value = _uiState.value.copy(selectedAnswers = updatedMap)
    }

    fun submitAnswer() {
        val currentIndex = _uiState.value.currentIndex
        val selectedIndex = _uiState.value.selectedAnswers[currentIndex] ?: return
        val item = _uiState.value.questions.getOrNull(currentIndex) ?: return
        val question = item.question

        val isCorrect = selectedIndex == question.correctIndex
        val updatedSet = _uiState.value.submittedAnswers.toMutableSet()
        updatedSet.add(currentIndex)

        val newScore = if (isCorrect) _uiState.value.score + 1 else _uiState.value.score

        _uiState.value = _uiState.value.copy(
            submittedAnswers = updatedSet,
            score = newScore
        )

        // Persistência local
        viewModelScope.launch {
            questionRepository.answerQuestion(question.id, selectedIndex, isCorrect)
        }
    }

    fun nextQuestion() {
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex < _uiState.value.questions.size) {
            _uiState.value = _uiState.value.copy(currentIndex = nextIndex)
        } else {
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
