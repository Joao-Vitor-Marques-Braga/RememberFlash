package com.rememberflash.app.data.remote.gemini

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.domain.usecase.essay.EvaluateEssayUseCase
import com.rememberflash.app.domain.usecase.essay.ExtractTextFromImageUseCase
import com.rememberflash.app.presentation.essay.capture.util.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cliente Gemini com modelo BYOK (Bring Your Own Key) conforme RN08.
 * Bloqueia todas as requisições de geração até que uma API Key válida
 * seja informada nas preferências do usuário.
 *
 * Implementa [EvaluateEssayUseCase.EssayEvaluator] para avaliação de redações
 * e [ExtractTextFromImageUseCase.HandwrittenTranscriber] para transcrição OCR de caligrafia cursiva.
 */
@Singleton
class GeminiClient @Inject constructor(
    private val preferencesManager: SecurePreferencesManager,
    @ApplicationContext private val context: Context
) : EvaluateEssayUseCase.EssayEvaluator,
    GeminiScheduleClient,
    ExtractTextFromImageUseCase.HandwrittenTranscriber {

    private var cachedModel: GenerativeModel? = null
    private var cachedApiKey: String? = null

    private fun getOrCreateModel(): GenerativeModel {
        val apiKey = preferencesManager.getGeminiApiKey()
            ?: throw IllegalStateException(
                "API Key do Gemini não configurada. " +
                "Acesse as Preferências e informe sua chave pessoal (BYOK)."
            )

        // Recria o modelo se a key mudou
        if (apiKey != cachedApiKey) {
            cachedApiKey = apiKey
            cachedModel = GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.4f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 10000

                }
            )
        }

        return cachedModel!!
    }

    suspend fun generateContent(prompt: String): String {
        val model = getOrCreateModel()
        val inputTokens = try {
            model.countTokens(prompt).totalTokens
        } catch (ex: Exception) {
            -1
        }
        android.util.Log.d("GeminiClient", "Geração do Gemini - Tokens de entrada: $inputTokens")
        
        // Inicializa o tracker com a estimativa de entrada
        GeminiTokenTracker.reset()
        if (inputTokens > 0) {
            GeminiTokenTracker.lastInputTokens = inputTokens
        }
        
        try {
            val response = model.generateContent(prompt)
            
            // Grava os dados oficiais de uso retornados pelo Gemini
            response.usageMetadata?.let { usage ->
                GeminiTokenTracker.lastInputTokens = usage.promptTokenCount
                GeminiTokenTracker.lastOutputTokens = usage.candidatesTokenCount
            }
            
            return response.text
                ?: throw IllegalStateException("Resposta vazia do modelo Gemini")
        } catch (e: Exception) {
            android.util.Log.e("GeminiClient", "Erro na geração do Gemini - Tokens de entrada: $inputTokens", e)
            throw e
        }
    }

    suspend fun generateContentWithImage(prompt: String, bitmap: Bitmap): String {
        val model = getOrCreateModel()
        GeminiTokenTracker.reset()
        try {
            val contentInput = content {
                image(bitmap)
                text(prompt)
            }
            val response = model.generateContent(contentInput)
            val inTokens = response.usageMetadata?.promptTokenCount ?: 0
            val outTokens = response.usageMetadata?.candidatesTokenCount ?: 0

            GeminiTokenTracker.lastInputTokens = inTokens
            GeminiTokenTracker.lastOutputTokens = outTokens
            GeminiTokenTracker.recordOcrUsage(inTokens, outTokens)

            return response.text
                ?: throw IllegalStateException("Resposta vazia do modelo Gemini ao processar a imagem")
        } catch (e: Exception) {
            android.util.Log.e("GeminiClient", "Erro na geração com imagem do Gemini", e)
            throw e
        }
    }

    suspend fun transcribeHandwrittenEssay(bitmap: Bitmap): String {
        val prompt = PromptTemplates.buildEssayTranscribePrompt()
        return generateContentWithImage(prompt, bitmap)
    }

    override suspend fun transcribeHandwritten(imageUri: Uri): String {
        val bitmap = ImageUtils.loadRotatedBitmap(context, imageUri)
            ?: throw IllegalArgumentException("Não foi possível carregar a imagem da redação.")
        return transcribeHandwrittenEssay(bitmap)
    }

    override suspend fun evaluate(
        essayText: String,
        theme: String,
        banca: String,
        rigor: String,
        tone: String
    ): String {
        val prompt = PromptTemplates.buildEssayEvaluationPrompt(
            essayText = essayText,
            theme = theme,
            banca = banca,
            rigor = rigor,
            tone = tone
        )
        return generateContent(prompt)
    }

    suspend fun extractFlashcardsFromText(rawText: String): String {
        val difficulty = preferencesManager.getDifficulty()
        val tone = preferencesManager.getTone()
        val prompt = PromptTemplates.buildFlashcardExtractionPrompt(rawText, difficulty, tone)
        return generateContent(prompt)
    }

    suspend fun generateQuestions(
        disciplineName: String,
        banca: String,
        format: String,
        difficulty: String,
        quantity: Int,
        theme: String?
    ): String {
        val prompt = PromptTemplates.buildQuestionGenerationPrompt(
            disciplineName = disciplineName,
            banca = banca,
            format = format,
            difficulty = difficulty,
            quantity = quantity,
            theme = theme
        )
        return generateContent(prompt)
    }

    suspend fun parseFullEditalText(
        editalText: String,
        jobPosition: String
    ): String {
        val prompt = PromptTemplates.buildFullEditalParsingPrompt(editalText, jobPosition)
        return generateContent(prompt)
    }

    override suspend fun generateStudySchedule(
        prompt: String
    ): String {
        return generateContent(prompt)
    }

    companion object {
        const val MODEL_NAME = "gemini-3.1-flash-lite"
    }
}
