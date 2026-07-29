package com.rememberflash.app.presentation.home.util

import com.rememberflash.app.domain.model.DailyGoal

data class DailyGoalsSummary(
    val todayGoals: List<DailyGoal> = emptyList(),
    val progress: Float = 0f
)

/**
 * Calcula, a partir de todas as metas diárias, quais pertencem a "hoje" e
 * qual a fração concluída (minutos de estudo + flashcards, combinados).
 *
 * Extraído do HomeViewModel: é lógica pura (sem StateFlow, sem coroutine),
 * então pode ser testada com um simples teste unitário passando uma lista
 * de DailyGoal e conferindo o resultado.
 */
object DailyGoalsProgressCalculator {

    fun summarize(
        allGoals: List<DailyGoal>,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): DailyGoalsSummary {
        val todayStart = referenceTimeMillis.startOfDayMillis()
        val todayGoals = allGoals.filter { it.date.startOfDayMillis() == todayStart }

        val totalMinutes = todayGoals.sumOf { it.targetMinutes }
        val completedMinutes = todayGoals.sumOf { it.completedMinutes }
        val totalCards = todayGoals.sumOf { it.flashcardsTarget }
        val completedCards = todayGoals.sumOf { it.flashcardsCompleted }

        val progress = ratioOf(
            part = completedMinutes + completedCards,
            total = totalMinutes + totalCards
        )

        return DailyGoalsSummary(todayGoals = todayGoals, progress = progress)
    }
}
