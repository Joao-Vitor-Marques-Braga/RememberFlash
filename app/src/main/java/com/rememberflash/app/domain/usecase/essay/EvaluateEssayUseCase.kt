package com.rememberflash.app.domain.usecase.essay

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Essay
import com.rememberflash.app.domain.repository.EssayRepository
import javax.inject.Inject

/**
 * Avalia uma redação enviando o texto extraído (OCR) para a IA Generativa (Gemini)
 * e persiste o feedback JSON estruturado no banco local (RF012).
 *
 * A interface [EssayEvaluator] é definida no domínio e implementada na camada data
 * pelo GeminiClient com prompt template rígido para mitigar alucinações (RN04).
 */
class EvaluateEssayUseCase @Inject constructor(
    private val essayRepository: EssayRepository,
    private val essayEvaluator: EssayEvaluator
) {
    suspend operator fun invoke(essay: Essay): Result<String> {
        if (essay.extractedText.isNullOrBlank()) {
            return Result.error("Texto da redação não extraído. Execute o OCR primeiro.")
        }
        return try {
            val feedbackJson = essayEvaluator.evaluate(
                essayText = essay.extractedText,
                theme = essay.theme
            )
            essayRepository.updateWithAiFeedback(
                essayId = essay.id,
                feedbackJson = feedbackJson,
                score = extractScoreFromFeedback(feedbackJson)
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
        suspend fun evaluate(essayText: String, theme: String): String
    }
}
