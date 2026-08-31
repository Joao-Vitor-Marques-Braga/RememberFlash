package com.rememberflash.app.domain.usecase.essay

import android.net.Uri
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Extrai texto de uma imagem de redação manuscrita.
 * Prioriza transcrição multimodal inteligente com Gemini para caligrafia cursiva (se chave configurada)
 * e utiliza o OCR local do ML Kit como fallback/offline.
 */
class ExtractTextFromImageUseCase @Inject constructor(
    private val textExtractor: TextExtractor,
    private val handwrittenTranscriber: HandwrittenTranscriber,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(imageUri: Uri): Result<String> {
        // 1. Se o usuário tiver chave de API do Gemini configurada, transcreve com IA (ideal para cursiva)
        if (authRepository.hasGeminiApiKey()) {
            try {
                val aiTranscribedText = handwrittenTranscriber.transcribeHandwritten(imageUri)
                if (aiTranscribedText.isNotBlank()) {
                    return Result.success(aiTranscribedText.trim())
                }
            } catch (e: Exception) {
                android.util.Log.w("ExtractTextUseCase", "Falha na transcrição por IA, recorrendo ao OCR local", e)
            }
        }

        // 2. Fallback para ML Kit local
        return try {
            val extractedText = textExtractor.extractText(imageUri)
            if (extractedText.isBlank()) {
                Result.error("Nenhum texto identificado na imagem. Verifique o enquadramento ou a iluminação da foto.")
            } else {
                Result.success(extractedText)
            }
        } catch (e: Exception) {
            Result.error("Falha na extração de texto via OCR: ${e.localizedMessage}", e)
        }
    }

    /** Contrato de extração de texto local — implementado pelo ML Kit */
    interface TextExtractor {
        suspend fun extractText(imageUri: Uri): String
    }

    /** Contrato de transcrição de manuscrito por IA — implementado pelo GeminiClient */
    interface HandwrittenTranscriber {
        suspend fun transcribeHandwritten(imageUri: Uri): String
    }
}
