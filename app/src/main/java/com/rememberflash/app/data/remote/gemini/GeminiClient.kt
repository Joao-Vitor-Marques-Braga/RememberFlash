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
        return executeWithRetry(actionName = "generateContent") { attempt ->
            val model = getOrCreateModel()
            val inputTokens = try {
                model.countTokens(prompt).totalTokens
            } catch (ex: Exception) {
                -1
            }
            android.util.Log.d("GeminiClient", "Geração do Gemini (tentativa $attempt/3) - Tokens de entrada: $inputTokens")
            
            // Inicializa o tracker com a estimativa de entrada
            GeminiTokenTracker.reset()
            if (inputTokens > 0) {
                GeminiTokenTracker.lastInputTokens = inputTokens
            }
            
            val response = model.generateContent(prompt)
            
            // Grava os dados oficiais de uso retornados pelo Gemini
            response.usageMetadata?.let { usage ->
                GeminiTokenTracker.lastInputTokens = usage.promptTokenCount
                GeminiTokenTracker.lastOutputTokens = usage.candidatesTokenCount
            }
            
            response.text ?: throw IllegalStateException("Resposta vazia do modelo Gemini")
        }
    }

    suspend fun generateContentWithImage(prompt: String, bitmap: Bitmap): String {
        return executeWithRetry(actionName = "generateContentWithImage") { attempt ->
            val model = getOrCreateModel()
            GeminiTokenTracker.reset()
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

            response.text ?: throw IllegalStateException("Resposta vazia do modelo Gemini ao processar a imagem")
        }
    }

    /**
     * Executa uma chamada à API do Gemini com tentativas automáticas (retry)
     * e tratamento de exceção com mensagens amigáveis em português.
     */
    private suspend fun <T> executeWithRetry(
        actionName: String,
        maxAttempts: Int = 3,
        initialDelayMs: Long = 2000L,
        block: suspend (attempt: Int) -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Throwable? = null

        for (attempt in 1..maxAttempts) {
            try {
                if (attempt > 1) {
                    android.util.Log.i("GeminiClient", "Tentativa $attempt de $maxAttempts para: $actionName")
                }
                return block(attempt)
            } catch (e: Throwable) {
                lastException = e
                val message = e.message ?: ""
                val lower = message.lowercase()

                // Se for chave não configurada ou explicitamente negada, não faz sentido tentar de novo
                val isNonRetryable = lower.contains("api key do gemini não configurada") ||
                        (lower.contains("api key") && (lower.contains("not valid") || lower.contains("invalid") || lower.contains("permission_denied") || lower.contains("403")))

                if (isNonRetryable || attempt == maxAttempts) {
                    android.util.Log.e("GeminiClient", "Erro fatal ou limite de tentativas atingido para $actionName", e)
                    break
                }

                android.util.Log.w(
                    "GeminiClient",
                    "Falha na tentativa $attempt/$maxAttempts para $actionName (${e.javaClass.simpleName}: ${e.message}). Aguardando ${currentDelay}ms para nova tentativa..."
                )
                kotlinx.coroutines.delay(currentDelay)
                currentDelay = (currentDelay * 1.5).toLong()
            }
        }

        val friendlyMessage = formatFriendlyErrorMessage(lastException ?: IllegalStateException("Erro desconhecido na comunicação com a IA"))
        throw IllegalStateException(friendlyMessage, lastException)
    }

    /**
     * Converte erros técnicos do SDK do Gemini e respostas de erro da rede
     * em mensagens claras, humanas e orientativas para o usuário.
     */
    private fun formatFriendlyErrorMessage(e: Throwable): String {
        val message = e.message ?: ""
        val lower = message.lowercase()

        return when {
            lower.contains("503") || lower.contains("high demand") || lower.contains("unavailable") ->
                "Os servidores do Google Gemini estão com alta demanda temporária no momento (código 503). Foram realizadas 3 tentativas automáticas de reconexão sem sucesso. Por favor, aguarde alguns instantes e tente novamente."

            lower.contains("429") || lower.contains("resource_exhausted") || lower.contains("quota") || lower.contains("rate limit") ->
                "O limite de requisições por minuto da sua chave Gemini foi atingido temporariamente (código 429). Por favor, aguarde cerca de 1 minuto antes de tentar novamente."

            lower.contains("api key") && (lower.contains("not valid") || lower.contains("invalid") || lower.contains("permission_denied") || lower.contains("403")) ->
                "Sua chave de API do Gemini parece ser inválida ou não possui as permissões necessárias. Por favor, revise sua chave em Configurações > Gemini API."

            lower.contains("api key do gemini não configurada") ->
                "API Key do Gemini não configurada. Acesse as Configurações do aplicativo e insira sua chave pessoal (BYOK)."

            lower.contains("timeout") || lower.contains("unable to resolve host") || lower.contains("unknownhost") || lower.contains("connectexception") || lower.contains("socket") ->
                "Não foi possível conectar aos servidores do Google Gemini. Verifique sua conexão com a internet e tente novamente."

            lower.contains("safety") || lower.contains("blocked") || lower.contains("harm_category") ->
                "A resposta foi bloqueada pelas diretrizes de segurança do modelo de IA. Tente reformular ou ajustar o texto enviado."

            else -> {
                val cleaned = if (message.contains("{") && message.contains("}")) {
                    message.substringBefore("{").trim().ifBlank { "Instabilidade momentânea nos servidores da IA" }
                } else {
                    message.take(200)
                }
                "Falha ao comunicar com a Inteligência Artificial (Gemini): $cleaned. Por favor, tente novamente."
            }
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
