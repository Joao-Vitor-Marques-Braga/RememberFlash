package com.rememberflash.app.domain.model

/**
 * Representa um concurso público cadastrado pelo usuário.
 * Implementa exclusão lógica (Soft Delete) via [isActive] para preservar
 * o histórico global de desempenho conforme RN06.
 */
data class Contest(
    val id: Long = 0L,
    val userId: String,
    val title: String,
    val description: String = "",
    val organizerName: String = "",
    val questionType: String = "Múltipla Escolha",
    val syllabusPdfUri: String? = null,
    val examDate: Long? = null,
    val examDateStr: String? = null,
    val examLocation: String? = null,
    val allowedPen: String? = null,
    val allowedItems: List<String> = emptyList(),
    val prohibitedItems: List<String> = emptyList(),
    val aiDifficulty: String = "Médio",
    val aiRigor: String = "Padrão",
    val aiTone: String = "Explicativo",
    val isActive: Boolean = true,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
