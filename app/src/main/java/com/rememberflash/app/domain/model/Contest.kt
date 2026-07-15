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
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
