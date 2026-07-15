package com.rememberflash.app.domain.usecase.flashcard

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.FlashcardRepository
import javax.inject.Inject

class DeleteFlashcardUseCase @Inject constructor(
    private val flashcardRepository: FlashcardRepository
) {
    suspend operator fun invoke(flashcardId: Long): Result<Unit> {
        if (flashcardId <= 0L) {
            return Result.error("ID de flashcard inválido")
        }
        return try {
            flashcardRepository.delete(flashcardId)
        } catch (e: Exception) {
            Result.error("Falha ao excluir flashcard: ${e.localizedMessage}", e)
        }
    }
}
