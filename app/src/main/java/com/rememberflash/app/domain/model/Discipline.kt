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
    val isActive: Boolean = true,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
