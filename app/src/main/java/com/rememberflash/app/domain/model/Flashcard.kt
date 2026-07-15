package com.rememberflash.app.domain.model

enum class FlashcardSource {
    MANUAL,
    PDF_EXTRACT,
    AI_GENERATED
}

/**
 * Flashcard de revisão espaçada associado a uma [Discipline].
 * Os campos [easeFactor], [interval] e [repetitions] suportam o algoritmo
 * de repetição espaçada (inspirado no SM-2) para agendamento inteligente.
 */
data class Flashcard(
    val id: Long = 0L,
    val disciplineId: Long,
    val front: String,
    val back: String,
    val source: FlashcardSource = FlashcardSource.MANUAL,
    val nextReviewAt: Long? = null,
    val easeFactor: Double = 2.5,
    val interval: Int = 0,
    val repetitions: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
