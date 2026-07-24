package com.rememberflash.app.data.remote.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.domain.usecase.essay.EvaluateEssayUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cliente Gemini com modelo BYOK (Bring Your Own Key) conforme RN08.
 * Bloqueia todas as requisições de geração até que uma API Key válida
 * seja informada nas preferências do usuário.
 *
 * Implementa [EvaluateEssayUseCase.EssayEvaluator] para avaliação de redações.
 */
@Singleton
class GeminiClient @Inject constructor(
    private val preferencesManager: SecurePreferencesManager
) : EvaluateEssayUseCase.EssayEvaluator {

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
                    temperature = 0.3f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 4096
                }
            )
        }

        return cachedModel!!
    }

    suspend fun generateContent(prompt: String): String {
        val model = getOrCreateModel()
        val response = model.generateContent(prompt)
        return response.text
            ?: throw IllegalStateException("Resposta vazia do modelo Gemini")
    }

    override suspend fun evaluate(essayText: String, theme: String): String {
        val rigor = preferencesManager.getRigor()
        val tone = preferencesManager.getTone()
        val prompt = PromptTemplates.buildEssayEvaluationPrompt(essayText, theme, rigor, tone)
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

    suspend fun generateStudySchedule(
        prompt: String
    ): String {
        return generateContent(prompt)
    }

    companion object {
        const val MODEL_NAME = "gemini-3.1-flash-lite"
    }
}
