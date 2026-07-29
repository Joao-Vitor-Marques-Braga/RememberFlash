package com.rememberflash.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Essay
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.EssayRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import com.rememberflash.app.domain.repository.ScheduleRepository
import com.rememberflash.app.domain.usecase.contest.GetActiveContestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.rememberflash.app.presentation.home.util.DailyGoalsProgressCalculator

data class HomeUiState(
    val user: User? = null,
    val activeContests: List<Contest> = emptyList(),
    val essays: List<Essay> = emptyList(),
    val allQuestions: List<Question> = emptyList(),
    val allDisciplines: List<Discipline> = emptyList(),
    val dailyGoalProgress: Float = 0f,
    val todayGoals: List<DailyGoal> = emptyList(),
    val allDailyGoals: List<DailyGoal> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getActiveContestsUseCase: GetActiveContestsUseCase,
    private val essayRepository: EssayRepository,
    private val questionRepository: QuestionRepository,
    private val disciplineRepository: DisciplineRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val sessionResult = authRepository.getCurrentSession()) {
                is Result.Success -> onSessionReady(sessionResult.data)
                else -> _uiState.update { it.copy(isLoading = false, error = "Sessão inválida") }
            }
        }
    }

    private fun onSessionReady(user: User) {
        _uiState.update { it.copy(user = user) }
        observeActiveContests(user.id)
        observeEssays(user.id)
        observeQuestions()
        observeDisciplines()
        observeDailyGoals()
    }

    private fun observeActiveContests(userId: String) {
        viewModelScope.launch {
            getActiveContestsUseCase(userId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao carregar concursos: ${e.localizedMessage}"
                        )
                    }
                }
                .collect { contests ->
                    _uiState.update { it.copy(activeContests = contests, isLoading = false) }
                }
        }
    }

    private fun observeEssays(userId: String) {
        viewModelScope.launch {
            essayRepository.getByUser(userId)
                .catch { }
                .collect { essays -> _uiState.update { it.copy(essays = essays) } }
        }
    }

    private fun observeQuestions() {
        viewModelScope.launch {
            questionRepository.getAllQuestions()
                .catch { }
                .collect { questions -> _uiState.update { it.copy(allQuestions = questions) } }
        }
    }

    private fun observeDisciplines() {
        viewModelScope.launch {
            disciplineRepository.getAllDisciplines()
                .catch { }
                .collect { disciplines -> _uiState.update { it.copy(allDisciplines = disciplines) } }
        }
    }

    private fun observeDailyGoals() {
        viewModelScope.launch {
            scheduleRepository.getAllDailyGoalsFlow()
                .catch { }
                .collect { goals ->
                    val summary = DailyGoalsProgressCalculator.summarize(goals)
                    _uiState.update {
                        it.copy(
                            allDailyGoals = goals,
                            todayGoals = summary.todayGoals,
                            dailyGoalProgress = summary.progress
                        )
                    }
                }
        }
    }

    fun updateGoalProgress(goal: DailyGoal, completedMinutes: Int, flashcardsCompleted: Int) {
        viewModelScope.launch {
            scheduleRepository.updateDailyGoalProgress(
                goalId = goal.id,
                completedMinutes = completedMinutes.coerceAtLeast(0),
                flashcardsCompleted = flashcardsCompleted.coerceAtLeast(0)
            )
        }
    }
}
