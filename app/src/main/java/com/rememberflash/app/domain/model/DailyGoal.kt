package com.rememberflash.app.domain.model

/**
 * Meta diária gerada pelo algoritmo de cronograma dinâmico.
 * Cada [DailyGoal] representa a alocação de tempo e flashcards
 * para uma disciplina específica em uma data específica.
 */
data class DailyGoal(
    val id: Long = 0L,
    val scheduleId: Long,
    val date: Long,
    val disciplineId: Long,
    val targetMinutes: Int,
    val completedMinutes: Int = 0,
    val flashcardsTarget: Int = 0,
    val flashcardsCompleted: Int = 0
) {
    val isCompleted: Boolean
        get() = completedMinutes >= targetMinutes && flashcardsCompleted >= flashcardsTarget
}
