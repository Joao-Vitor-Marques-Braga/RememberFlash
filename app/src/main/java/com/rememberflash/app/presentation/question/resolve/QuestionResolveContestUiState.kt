package com.rememberflash.app.presentation.question.resolve

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.StudySchedule
import com.rememberflash.app.domain.usecase.schedule.DifficultyItem
import com.rememberflash.app.domain.usecase.schedule.ScheduleComparisonItem

/**
 * Estado da UI do simulado completo por concurso.
 *
 * [ITEM 1.7 — LOCALIZAÇÃO CORRIGIDA]
 * [QuestionResolveUiState] já tinha arquivo próprio. Esta classe estava incrustada
 * em [QuestionResolveContestViewModel], criando inconsistência de organização dentro
 * da mesma feature. Agora ambas as UiStates têm arquivos dedicados.
 */
data class QuestionResolveContestUiState(
    val questions: List<QuestionWithDiscipline> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),       // questionIndex → chosenOptionIndex
    val submittedAnswers: Set<Int> = emptySet(),            // questionIndex
    val score: Int = 0,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val contestTitle: String = "Simulado do Edital",
    val difficulties: List<DifficultyItem> = emptyList(),
    val comparisonList: List<ScheduleComparisonItem> = emptyList(),
    val proposedSchedule: StudySchedule? = null,
    val showRecalculationProposal: Boolean = false,
    val isSavingProposal: Boolean = false,
    val proposalSaveResult: Result<Unit>? = null,
    val questionTimes: Map<Long, Int> = emptyMap(),         // questionId → timeSpentSeconds
    val currentQuestionStartTime: Long = 0L
)
