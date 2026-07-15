package com.rememberflash.app.domain.usecase.essay

import android.net.Uri
import com.rememberflash.app.domain.common.Result
import javax.inject.Inject

/**
 * Extrai texto de uma imagem de redação manuscrita utilizando OCR local (ML Kit).
 * O processamento é 100% on-device sem tráfego de rede (RF011).
 *
 * A interface [TextExtractor] é definida no domínio e implementada na camada data
 * pelo MlKitTextExtractor, mantendo a inversão de dependência.
 */
class ExtractTextFromImageUseCase @Inject constructor(
    private val textExtractor: TextExtractor
) {
    suspend operator fun invoke(imageUri: Uri): Result<String> {
        return try {
            val extractedText = textExtractor.extractText(imageUri)
            if (extractedText.isBlank()) {
                Result.error("Nenhum texto identificado na imagem. Verifique a qualidade da foto.")
            } else {
                Result.success(extractedText)
            }
        } catch (e: Exception) {
            Result.error("Falha na extração de texto via OCR: ${e.localizedMessage}", e)
        }
    }

    /** Contrato de extração de texto — implementado na camada data pelo ML Kit */
    interface TextExtractor {
        suspend fun extractText(imageUri: Uri): String
    }
}
