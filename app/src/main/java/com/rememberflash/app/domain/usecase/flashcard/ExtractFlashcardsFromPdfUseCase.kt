package com.rememberflash.app.domain.usecase.flashcard

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.FlashcardSource
import com.rememberflash.app.domain.repository.FlashcardRepository
import javax.inject.Inject

/**
 * Contrato de interface para extração automatizada de flashcards via PDF.
 * O fluxo completo é: PDF → OCR/Parsing → texto bruto → IA Generativa → flashcards estruturados.
 * Esta classe define a orquestração; a implementação concreta do parsing e da IA
 * reside na camada data.
 */
class ExtractFlashcardsFromPdfUseCase @Inject constructor(
    private val flashcardRepository: FlashcardRepository
) {
    /**
     * Recebe o texto já extraído de um PDF e os flashcards gerados pela IA,
     * persistindo-os com a source [FlashcardSource.PDF_EXTRACT].
     */
    suspend operator fun invoke(
        disciplineId: Long,
        extractedFlashcards: List<Pair<String, String>>
    ): Result<List<Long>> {
        if (extractedFlashcards.isEmpty()) {
            return Result.error("Nenhum flashcard extraído do PDF")
        }
        if (disciplineId <= 0L) {
            return Result.error("Disciplina inválida para vinculação dos flashcards")
        }
        return try {
            val flashcards = extractedFlashcards.map { (front, back) ->
                Flashcard(
                    disciplineId = disciplineId,
                    front = front.trim(),
                    back = back.trim(),
                    source = FlashcardSource.PDF_EXTRACT,
                    createdAt = System.currentTimeMillis()
                )
            }
            flashcardRepository.insertAll(flashcards)
        } catch (e: Exception) {
            Result.error("Falha ao persistir flashcards extraídos: ${e.localizedMessage}", e)
        }
    }
}
