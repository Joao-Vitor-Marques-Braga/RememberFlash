package com.rememberflash.app.domain.model

/**
 * Cronograma de estudos dinâmico vinculado a um [Contest].
 * O algoritmo reativo (RN05) recalcula as [DailyGoal] com base em:
 * - Data da prova ([examDate])
 * - Horas disponíveis por dia ([availableHoursPerDay])
 * - Pesos e progresso das disciplinas
 * - Restrições de tempo do usuário
 */
data class StudySchedule(
    val id: Long = 0L,
    val contestId: Long,
    val examDate: Long,
    val availableHoursPerDay: Double,
    val restDaysPerWeek: Int = 1,
    val dailyGoals: List<DailyGoal> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastRecalculatedAt: Long = System.currentTimeMillis()
)
