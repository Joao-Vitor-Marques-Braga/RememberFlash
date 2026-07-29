package com.rememberflash.app.domain.usecase.schedule

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.StudySchedule
import com.rememberflash.app.domain.repository.ScheduleRepository
import javax.inject.Inject

class AcceptProposedScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(
        schedule: StudySchedule,
        goals: List<DailyGoal>
    ): Result<Unit> {
        return try {
            // 1. Limpa as metas diárias antigas
            scheduleRepository.clearDailyGoalsBySchedule(schedule.id)

            // 2. Atualiza o cronograma principal (nova data de recalculação, etc.)
            scheduleRepository.update(schedule)

            // 3. Insere as novas metas propostas
            val goalsWithCorrectScheduleId = goals.map { it.copy(scheduleId = schedule.id) }
            scheduleRepository.insertDailyGoals(goalsWithCorrectScheduleId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Falha ao salvar cronograma aceito: ${e.localizedMessage}", e)
        }
    }
}
