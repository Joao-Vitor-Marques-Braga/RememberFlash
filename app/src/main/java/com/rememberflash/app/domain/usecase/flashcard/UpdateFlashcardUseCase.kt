package com.rememberflash.app.domain.usecase.flashcard

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.repository.FlashcardRepository
import javax.inject.Inject

class UpdateFlashcardUseCase @Inject constructor(
    private val flashcardRepository: FlashcardRepository
) {
    suspend operator fun invoke(flashcard: Flashcard): Result<Unit> {
        if (flashcard.front.isBlank()) {
            return Result.error("A frente do flashcard é obrigatória")
        }
        if (flashcard.back.isBlank()) {
            return Result.error("O verso do flashcard é obrigatório")
        }
        if (flashcard.id <= 0L) {
            return Result.error("Flashcard inválido para atualização")
        }
        return try {
            flashcardRepository.update(flashcard)
        } catch (e: Exception) {
            Result.error("Falha ao atualizar flashcard: ${e.localizedMessage}", e)
        }
    }
}
