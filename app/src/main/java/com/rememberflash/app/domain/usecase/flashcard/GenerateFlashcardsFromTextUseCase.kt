package com.rememberflash.app.domain.usecase.flashcard

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.data.remote.gemini.GeminiTokenTracker
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.FlashcardSource
import com.rememberflash.app.domain.repository.FlashcardRepository
import javax.inject.Inject

/**
 * Caso de uso para gerar flashcards estruturados a partir do texto extraído de um PDF
 * utilizando a IA do Google Gemini e persistir diretamente no Room vinculado à Disciplina/Tópico.
 */
class GenerateFlashcardsFromTextUseCase @Inject constructor(
    private val geminiClient: GeminiClient,
    private val flashcardRepository: FlashcardRepository
) {
    private val gson = Gson()

    suspend operator fun invoke(
        text: String,
        disciplineId: Long,
        quantity: Int,
        topicId: Long? = null
    ): Result<List<Long>> {
        if (text.isBlank() || text.length < 50) {
            return Result.error("O PDF selecionado não contém texto legível (documento escaneado). Envie um PDF com camada de texto")
        }
        if (disciplineId <= 0L) {
            return Result.error("Disciplina inválida para vinculação dos flashcards")
        }

        return try {
            val jsonResponse = geminiClient.extractFlashcardsFromText(text)
            val type = object : TypeToken<Map<String, List<RawFlashcard>>>() {}.type
            val data: Map<String, List<RawFlashcard>> = gson.fromJson(jsonResponse, type)
            val rawCards = data["flashcards"]

            if (rawCards.isNullOrEmpty()) {
                return Result.error("A criação automática demorou a responder ou o texto é muito complexo. Tente novamente em instantes.")
            }

            val cardsToSave = rawCards.take(quantity).map { raw ->
                Flashcard(
                    disciplineId = disciplineId,
                    topicId = topicId,
                    front = raw.frente.trim(),
                    back = raw.verso.trim(),
                    source = FlashcardSource.PDF_EXTRACT,
                    tokensSpent = GeminiTokenTracker.lastTotalTokens,
                    createdAt = System.currentTimeMillis()
                )
            }

            flashcardRepository.insertAll(cardsToSave)
        } catch (e: Exception) {
            android.util.Log.e("GenerateFlashcardsFromTextUseCase", "Erro ao gerar flashcards a partir do texto", e)
            Result.error(e.message ?: "Falha ao gerar flashcards com a Inteligência Artificial.", e)
        }
    }

    private data class RawFlashcard(
        val frente: String,
        val verso: String
    )
}
