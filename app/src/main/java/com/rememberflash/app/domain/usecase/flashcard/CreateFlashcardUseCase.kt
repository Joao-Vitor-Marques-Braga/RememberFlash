package com.rememberflash.app.domain.usecase.flashcard

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.FlashcardSource
import com.rememberflash.app.domain.repository.FlashcardRepository
import javax.inject.Inject

class CreateFlashcardUseCase @Inject constructor(
    private val flashcardRepository: FlashcardRepository
) {
    suspend operator fun invoke(flashcard: Flashcard): Result<Long> {
        if (flashcard.front.isBlank()) {
            return Result.error("A frente do flashcard é obrigatória")
        }
        if (flashcard.back.isBlank()) {
            return Result.error("O verso do flashcard é obrigatório")
        }
        if (flashcard.disciplineId <= 0L) {
            return Result.error("Flashcard deve estar vinculado a uma disciplina válida")
        }
        return try {
            val cardToInsert = flashcard.copy(
                source = FlashcardSource.MANUAL,
                easeFactor = 2.5,
                interval = 0,
                repetitions = 0,
                createdAt = System.currentTimeMillis()
            )
            flashcardRepository.insert(cardToInsert)
        } catch (e: Exception) {
            Result.error("Falha ao criar flashcard: ${e.localizedMessage}", e)
        }
    }
}
