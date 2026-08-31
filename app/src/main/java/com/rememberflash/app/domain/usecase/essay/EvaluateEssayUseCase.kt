package com.rememberflash.app.domain.usecase.essay

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Essay
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.EssayRepository
import javax.inject.Inject

/**
 * Avalia uma redação enviando o texto extraído (OCR) para a IA Generativa (Gemini)
 * com o contexto do concurso pai (banca examinadora, rigor e tom) e persiste o feedback no banco local.
 */
class EvaluateEssayUseCase @Inject constructor(
    private val essayRepository: EssayRepository,
    private val contestRepository: ContestRepository,
    private val essayEvaluator: EssayEvaluator
) {
    suspend operator fun invoke(essay: Essay): Result<String> {
        if (essay.extractedText.isNullOrBlank()) {
            return Result.error("Texto da redação não extraído. Execute o OCR primeiro.")
        }
        return try {
            // Obtém os parâmetros específicos do Concurso Pai
            val contestResult = contestRepository.getById(essay.contestId)
            val (banca, rigor, tone) = when (contestResult) {
                is Result.Success -> Triple(
                    contestResult.data.organizerName.ifBlank { "Geral" },
                    contestResult.data.aiRigor,
                    contestResult.data.aiTone
                )
                else -> Triple("Geral", "Padrão", "Explicativo")
            }

            val feedbackJson = essayEvaluator.evaluate(
                essayText = essay.extractedText,
                theme = essay.theme,
                banca = banca,
                rigor = rigor,
                tone = tone
            )
            essayRepository.updateWithAiFeedback(
                essayId = essay.id,
                feedbackJson = feedbackJson,
                score = extractScoreFromFeedback(feedbackJson),
                tokensSpent = com.rememberflash.app.data.remote.gemini.GeminiTokenTracker.lastTotalTokens
            )
            Result.success(feedbackJson)
        } catch (e: Exception) {
            Result.error("Falha na avaliação da redação: ${e.localizedMessage}", e)
        }
    }

    private fun extractScoreFromFeedback(feedbackJson: String): Double {
        // Parsing simplificado — extrai nota do JSON de feedback da IA
        val scoreRegex = """"nota"\s*:\s*(\d+\.?\d*)""".toRegex()
        return scoreRegex.find(feedbackJson)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    /** Contrato de avaliação de redação — implementado na camada data via Gemini */
    interface EssayEvaluator {
        suspend fun evaluate(
            essayText: String,
            theme: String,
            banca: String,
            rigor: String,
            tone: String
        ): String
    }
}
