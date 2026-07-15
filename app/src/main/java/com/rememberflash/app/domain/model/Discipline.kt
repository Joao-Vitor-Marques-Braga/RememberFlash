package com.rememberflash.app.domain.model

/**
 * Disciplina associada a um concurso. Permite reaproveitamento entre
 * múltiplos concursos de mesma matéria conforme DER.
 * O [weight] representa o peso da disciplina na prova para distribuição
 * proporcional de tempo no cronograma dinâmico (RN05).
 */
data class Discipline(
    val id: Long = 0L,
    val contestId: Long,
    val name: String,
    val weight: Double = 1.0,
    val totalTopics: Int = 0,
    val completedTopics: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercentage: Double
        get() = if (totalTopics > 0) (completedTopics.toDouble() / totalTopics) * 100.0 else 0.0
}
