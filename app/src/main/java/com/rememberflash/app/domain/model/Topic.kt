package com.rememberflash.app.domain.model

/**
 * Representa um subtópico ou subpasta temática dentro de uma disciplina.
 * Permite decompor o conteúdo programático do edital em unidades de estudo
 * menores, facilitando o acompanhamento granular de progresso e organização
 * de flashcards e questões.
 */
data class Topic(
    val id: Long = 0L,
    val disciplineId: Long,
    val contestId: Long,
    val name: String,
    val description: String? = null,
    val orderIndex: Int = 0,
    val isSynced: Boolean = false,
    val flashcardsCount: Int = 0,
    val questionsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
