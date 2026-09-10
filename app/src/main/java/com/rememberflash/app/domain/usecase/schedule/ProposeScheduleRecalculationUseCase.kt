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

data class DifficultyItem(
    val disciplineId: Long,
    val disciplineName: String,
    val accuracy: Double,
    val correctAnswers: Int,
    val totalQuestions: Int
)

data class ProposedScheduleProposal(
    val proposedSchedule: StudySchedule,
    val difficulties: List<DifficultyItem>,
    val comparisonList: List<ScheduleComparisonItem>
)

data class ScheduleComparisonItem(
    val disciplineId: Long,
    val disciplineName: String,
    val currentMinutesPerDay: Int,
    val proposedMinutesPerDay: Int
)

class ProposeScheduleRecalculationUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val disciplineRepository: DisciplineRepository
) {
    suspend operator fun invoke(
        contestId: Long,
        disciplinePerformance: Map<Long, Pair<Int, Int>> // disciplineId -> (correct, total)
    ): Result<ProposedScheduleProposal> {
        return try {
            val scheduleResult = scheduleRepository.getByContest(contestId)
            val currentSchedule = scheduleResult.getOrNull() 
                ?: return Result.error("Não há um cronograma ativo para este concurso.")

            val disciplines = disciplineRepository.getByContest(contestId).first()
            if (disciplines.isEmpty()) {
                return Result.error("Não foram encontradas disciplinas para este concurso.")
            }

            // 1. Identificar dificuldades (acertos < 70%)
            val difficulties = mutableListOf<DifficultyItem>()
            val accuracyMap = mutableMapOf<Long, Double>()

            disciplinePerformance.forEach { (disciplineId, stats) ->
                val correct = stats.first
                val total = stats.second
                val accuracy = if (total > 0) (correct.toDouble() / total.toDouble()) * 100.0 else 100.0
                accuracyMap[disciplineId] = accuracy
                
                val disciplineName = disciplines.firstOrNull { it.id == disciplineId }?.name ?: "Disciplina"
                if (accuracy < 70.0 && total > 0) {
                    difficulties.add(
                        DifficultyItem(
                            disciplineId = disciplineId,
                            disciplineName = disciplineName,
                            accuracy = accuracy,
                            correctAnswers = correct,
                            totalQuestions = total
                        )
                    )
                }
            }

            // 2. Calcular pesos ajustados com base no progresso e multiplicador de dificuldade
            val availableMinutesPerDay = (currentSchedule.availableHoursPerDay * 60).toInt()
            
            // Peso base proporcional atual (para comparação)
            val currentTotalWeight = disciplines.sumOf { it.weight }.coerceAtLeast(0.01)
            val currentMinutesMap = disciplines.associate { discipline ->
                val proportion = discipline.weight / currentTotalWeight
                val currentMins = (availableMinutesPerDay * proportion).toInt().coerceAtLeast(10)
                discipline.id to currentMins
            }

            // Pesos propostos: aplica multiplicador de dificuldade se acertos < 70%
            val proposedWeights = disciplines.map { discipline ->
                val accuracy = accuracyMap[discipline.id]
                val difficultyMultiplier = if (accuracy != null && accuracy < 70.0) {
                    // Quanto pior a nota, maior o foco (multiplicador entre 1.3 e 2.0)
                    2.0 - (accuracy / 100.0).coerceIn(0.0, 0.7)
                } else {
                    1.0
                }
                val adjustedWeight = discipline.weight.coerceAtLeast(0.1) * difficultyMultiplier
                discipline to adjustedWeight
            }

            val proposedTotalWeight = proposedWeights.sumOf { it.second }.coerceAtLeast(0.01)
            val proposedMinutesMap = proposedWeights.associate { (discipline, weight) ->
                val proportion = weight / proposedTotalWeight
                val proposedMins = (availableMinutesPerDay * proportion).toInt().coerceAtLeast(10)
                discipline.id to proposedMins
            }

            // 3. Gerar lista de comparação
            val comparisonList = disciplines.map { discipline ->
                ScheduleComparisonItem(
                    disciplineId = discipline.id,
                    disciplineName = discipline.name,
                    currentMinutesPerDay = currentMinutesMap[discipline.id] ?: 0,
                    proposedMinutesPerDay = proposedMinutesMap[discipline.id] ?: 0
                )
            }

            // 4. Gerar lista de metas diárias propostas
            val now = System.currentTimeMillis()
            val totalDays = TimeUnit.MILLISECONDS.toDays(currentSchedule.examDate - now).toInt()
            val goals = mutableListOf<DailyGoal>()
            
            if (totalDays > 0) {
                val oneDayMillis = TimeUnit.DAYS.toMillis(1)
                for (dayOffset in 0 until totalDays) {
                    val dayTimestamp = now + dayOffset * oneDayMillis
                    // Bitmask de descanso
                    val dayOfWeek = ((dayTimestamp) / oneDayMillis % 7).toInt()
                    if (currentSchedule.restDaysPerWeek > 0 && dayOfWeek == 0) continue

                    proposedWeights.forEach { (discipline, weight) ->
                        val proportion = weight / proposedTotalWeight
                        val targetMinutes = (availableMinutesPerDay * proportion).toInt().coerceAtLeast(10)
                        val flashcardsTarget = (targetMinutes / 5).coerceAtLeast(2)

                        goals.add(
                            DailyGoal(
                                scheduleId = currentSchedule.id,
                                date = dayTimestamp,
                                disciplineId = discipline.id,
                                targetMinutes = targetMinutes,
                                flashcardsTarget = flashcardsTarget
                            )
                        )
                    }
                }
            }

            val proposedSchedule = currentSchedule.copy(
                dailyGoals = goals,
                lastRecalculatedAt = System.currentTimeMillis()
            )

            Result.success(
                ProposedScheduleProposal(
                    proposedSchedule = proposedSchedule,
                    difficulties = difficulties,
                    comparisonList = comparisonList
                )
            )
        } catch (e: Exception) {
            Result.error("Erro ao propor recalculo de cronograma: ${e.localizedMessage}", e)
        }
    }
}
