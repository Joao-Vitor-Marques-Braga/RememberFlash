package com.rememberflash.app.domain.usecase.schedule

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.StudySchedule
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Algoritmo reativo de cronograma dinâmico (RN05 / RF013).
 *
 * Recalcula a distribuição diária de estudo com base em:
 * - Data futura da prova
 * - Horas disponíveis por dia do usuário
 * - Pesos relativos das disciplinas
 * - Progresso atual (tópicos concluídos) de cada disciplina
 * - Dias de descanso configurados
 *
 * O algoritmo prioriza disciplinas com maior peso e menor progresso,
 * alocando proporcionalmente o tempo disponível até a data da prova.
 */
class RecalculateDynamicScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val disciplineRepository: DisciplineRepository
) {
    suspend operator fun invoke(schedule: StudySchedule): Result<StudySchedule> {
        if (schedule.examDate <= System.currentTimeMillis()) {
            return Result.error("A data da prova deve ser futura")
        }
        if (schedule.availableHoursPerDay <= 0.0) {
            return Result.error("Horas disponíveis por dia devem ser maiores que zero")
        }
        if (schedule.contestId <= 0L) {
            return Result.error("Cronograma deve estar vinculado a um concurso válido")
        }

        return try {
            val disciplines = disciplineRepository.getByContest(schedule.contestId).first()
            if (disciplines.isEmpty()) {
                return Result.error("Cadastre disciplinas antes de gerar o cronograma")
            }

            val dailyGoals = calculateDailyDistribution(schedule, disciplines)

            // Limpa goals antigos e insere os recalculados
            if (schedule.id > 0L) {
                scheduleRepository.clearDailyGoalsBySchedule(schedule.id)
            }

            val updatedSchedule = schedule.copy(
                dailyGoals = dailyGoals,
                lastRecalculatedAt = System.currentTimeMillis()
            )

            if (schedule.id == 0L) {
                val newId = scheduleRepository.insert(updatedSchedule).getOrThrow()
                val goalsWithScheduleId = dailyGoals.map { it.copy(scheduleId = newId) }
                scheduleRepository.insertDailyGoals(goalsWithScheduleId)
                Result.success(updatedSchedule.copy(id = newId, dailyGoals = goalsWithScheduleId))
            } else {
                scheduleRepository.update(updatedSchedule)
                scheduleRepository.insertDailyGoals(dailyGoals)
                Result.success(updatedSchedule)
            }
        } catch (e: Exception) {
            Result.error("Falha ao recalcular cronograma: ${e.localizedMessage}", e)
        }
    }

    private fun calculateDailyDistribution(
        schedule: StudySchedule,
        disciplines: List<Discipline>
    ): List<DailyGoal> {
        val now = System.currentTimeMillis()
        val totalDays = TimeUnit.MILLISECONDS.toDays(schedule.examDate - now).toInt()
        if (totalDays <= 0) return emptyList()

        val availableMinutesPerDay = (schedule.availableHoursPerDay * 60).toInt()

        // Calcula peso ajustado: peso da disciplina × (1 - progresso%)
        // Disciplinas com menor progresso e maior peso recebem mais tempo
        val adjustedWeights = disciplines.map { discipline ->
            val progressFactor = 1.0 - (discipline.progressPercentage / 100.0)
            val adjustedWeight = discipline.weight * progressFactor.coerceAtLeast(0.1)
            discipline to adjustedWeight
        }

        val totalWeight = adjustedWeights.sumOf { it.second }.coerceAtLeast(0.01)

        val goals = mutableListOf<DailyGoal>()
        val oneDayMillis = TimeUnit.DAYS.toMillis(1)

        for (dayOffset in 0 until totalDays) {
            // Respeita dias de descanso (distribui proporcionalmente na semana)
            val dayOfWeek = ((now + dayOffset * oneDayMillis) / oneDayMillis % 7).toInt()
            if (schedule.restDaysPerWeek > 0 && dayOfWeek == 0) continue // Domingo como descanso padrão

            val dayTimestamp = now + dayOffset * oneDayMillis

            for ((discipline, weight) in adjustedWeights) {
                val proportion = weight / totalWeight
                val targetMinutes = (availableMinutesPerDay * proportion).toInt().coerceAtLeast(10)
                val flashcardsTarget = (targetMinutes / 5).coerceAtLeast(2) // ~5 min por flashcard

                goals.add(
                    DailyGoal(
                        scheduleId = schedule.id,
                        date = dayTimestamp,
                        disciplineId = discipline.id,
                        targetMinutes = targetMinutes,
                        flashcardsTarget = flashcardsTarget
                    )
                )
            }
        }

        return goals
    }
}
