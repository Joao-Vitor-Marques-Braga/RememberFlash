package com.rememberflash.app.presentation.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.StudySchedule
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.ScheduleRepository
import com.rememberflash.app.domain.usecase.schedule.GenerateStudyScheduleUseCase
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ScheduleUiState(
    val contestId: Long = 0L,
    val contestTitle: String = "",
    val allContests: List<Contest> = emptyList(),
    val schedule: StudySchedule? = null,
    val dailyGoals: List<DailyGoal> = emptyList(),
    val disciplinesMap: Map<Long, String> = emptyMap(),
    val selectedDate: Long = System.currentTimeMillis().toMidnight(),
    val selectedDateGoals: List<DailyGoal> = emptyList(),
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val cronogramaWarning: String? = null,

    // Form parameters
    val examDate: Long? = null,
    val minutesPerDay: Int = 120,
    val maxSubjectsPerDay: Int = 2,
    val availableDays: List<String> = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"),
    val isEditingForm: Boolean = false
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val contestRepository: ContestRepository,
    private val disciplineRepository: DisciplineRepository,
    private val scheduleRepository: ScheduleRepository,
    private val generateStudyScheduleUseCase: GenerateStudyScheduleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private var goalsJob: Job? = null

    init {
        val paramContestId = savedStateHandle.get<Long>("contestId") ?: 0L
        loadAllContests(paramContestId)
    }

    fun onContestSelected(contestId: Long) {
        if (contestId <= 0) return
        loadContestAndSchedule(contestId)
    }

    private fun loadAllContests(initialContestId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val sessionResult = authRepository.getCurrentSession()
            if (sessionResult !is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuário não autenticado."
                )
                return@launch
            }
            val userId = sessionResult.data.id

            try {
                val list = contestRepository.getActiveContestsByUser(userId).first()
                _uiState.value = _uiState.value.copy(
                    allContests = list,
                    isLoading = false
                )
                // Se houver um contestId inicial, carrega-o
                val targetId = if (initialContestId > 0L) {
                    initialContestId
                } else {
                    list.firstOrNull()?.id ?: 0L
                }
                if (targetId > 0L) {
                    loadContestAndSchedule(targetId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro ao carregar concursos: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun loadContestAndSchedule(contestId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, contestId = contestId, error = null)
            
            // 1. Carrega dados do concurso
            val contestResult = contestRepository.getById(contestId)
            if (contestResult is Result.Success) {
                val contest = contestResult.data
                _uiState.value = _uiState.value.copy(contestTitle = contest.title)
                
                // Extrai aviso (warning) anterior se houver
                val warning = if (contest.description.contains("[WarningCronograma]:")) {
                    contest.description.substringAfter("[WarningCronograma]:").trim()
                } else {
                    null
                }
                _uiState.value = _uiState.value.copy(cronogramaWarning = warning)
            }

            // 2. Carrega mapa de disciplinas
            try {
                val disciplines = disciplineRepository.getByContest(contestId).first()
                val map = disciplines.associate { it.id to it.name }
                _uiState.value = _uiState.value.copy(disciplinesMap = map)
            } catch (e: Exception) {
                android.util.Log.e("ScheduleViewModel", "Erro ao carregar mapa de disciplinas", e)
            }

            // 3. Carrega o cronograma existente
            when (val scheduleResult = scheduleRepository.getByContest(contestId)) {
                is Result.Success -> {
                    val schedule = scheduleResult.data
                    if (schedule != null) {
                        // Preenche os parâmetros do formulário com os valores já salvos do cronograma
                        val days = decodeDaysBitmask(schedule.restDaysPerWeek)
                        _uiState.value = _uiState.value.copy(
                            schedule = schedule,
                            examDate = schedule.examDate,
                            minutesPerDay = (schedule.availableHoursPerDay * 60).toInt(),
                            availableDays = days,
                            isEditingForm = false,
                            isLoading = false
                        )
                        observeDailyGoals(schedule.id)
                    } else {
                        // Não há cronograma, pré-carrega a data da prova do edital se cadastrada
                        val contest = (contestResult as? Result.Success)?.data
                        val parsedExamDate = parseExamDateStr(contest?.examDateStr)
                        _uiState.value = _uiState.value.copy(
                            schedule = null,
                            examDate = parsedExamDate,
                            isEditingForm = true,
                            isLoading = false
                        )
                        goalsJob?.cancel()
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro ao buscar cronograma: ${scheduleResult.message}"
                    )
                }
                else -> {}
            }
        }
    }

    private fun observeDailyGoals(scheduleId: Long) {
        goalsJob?.cancel()
        goalsJob = viewModelScope.launch {
            scheduleRepository.getDailyGoalsBySchedule(scheduleId).collect { goals ->
                _uiState.value = _uiState.value.copy(dailyGoals = goals)
                filterGoalsForSelectedDate()
            }
        }
    }

    private fun filterGoalsForSelectedDate() {
        val state = _uiState.value
        val normalizedSelected = state.selectedDate.toMidnight()
        val filtered = state.dailyGoals.filter { it.date.toMidnight() == normalizedSelected }
        _uiState.value = _uiState.value.copy(selectedDateGoals = filtered)
    }

    fun onSelectDate(date: Long) {
        _uiState.value = _uiState.value.copy(selectedDate = date.toMidnight())
        filterGoalsForSelectedDate()
    }

    fun onExamDateChanged(date: Long) {
        _uiState.value = _uiState.value.copy(examDate = date, error = null)
    }

    fun onMinutesPerDayChanged(minutes: Int) {
        _uiState.value = _uiState.value.copy(minutesPerDay = minutes)
    }

    fun onMaxSubjectsPerDayChanged(max: Int) {
        _uiState.value = _uiState.value.copy(maxSubjectsPerDay = max)
    }

    fun toggleAvailableDay(day: String) {
        val current = _uiState.value.availableDays.toMutableList()
        if (current.contains(day)) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _uiState.value = _uiState.value.copy(availableDays = current)
    }

    fun onRecalculateClicked() {
        _uiState.value = _uiState.value.copy(isEditingForm = true)
    }

    fun onCancelEditFormClicked() {
        if (_uiState.value.schedule != null) {
            _uiState.value = _uiState.value.copy(isEditingForm = false)
        }
    }

    fun onGenerateScheduleClicked() {
        val state = _uiState.value
        val examDate = state.examDate
        val todayMidnight = System.currentTimeMillis().toMidnight()

        // Validação A1: data da prova vazia ou igual/menor ao dia de hoje
        if (examDate == null || examDate <= todayMidnight) {
            _uiState.value = _uiState.value.copy(error = "A data da prova deve ser uma data futura válida.")
            return
        }

        if (state.availableDays.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Selecione pelo menos um dia disponível para estudar.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, error = null)
            val result = generateStudyScheduleUseCase(
                contestId = state.contestId,
                examDateLong = examDate,
                minutesPerDay = state.minutesPerDay,
                maxSubjectsPerDay = state.maxSubjectsPerDay,
                availableDaysOfWeek = state.availableDays
            )

            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isGenerating = false)
                    // Recarrega o cronograma atualizado
                    loadContestAndSchedule(state.contestId)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = result.message
                    )
                }
                else -> {}
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

    // Helpers de parsing de data e bitmask

    private fun parseExamDateStr(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        val formats = listOf("dd/MM/yyyy", "yyyy-MM-dd")
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                val parsed = sdf.parse(dateStr)
                if (parsed != null && parsed.time > System.currentTimeMillis()) {
                    return parsed.time.toMidnight()
                }
            } catch (e: Exception) {
                // continua tentando
            }
        }
        return null
    }

    private fun decodeDaysBitmask(mask: Int): List<String> {
        val days = mutableListOf<String>()
        if (mask and 1 != 0) days.add("Segunda")
        if (mask and 2 != 0) days.add("Terça")
        if (mask and 4 != 0) days.add("Quarta")
        if (mask and 8 != 0) days.add("Quinta")
        if (mask and 16 != 0) days.add("Sexta")
        if (mask and 32 != 0) days.add("Sábado")
        if (mask and 64 != 0) days.add("Domingo")
        return days
    }

}

private fun Long.toMidnight(): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
