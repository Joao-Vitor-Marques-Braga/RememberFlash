package com.rememberflash.app.domain.usecase.tutor

import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.data.remote.gemini.PromptTemplates
import javax.inject.Inject

/**
 * Caso de uso para a Tutoria Interativa Contextual (RF014 / RN07).
 * Encapsula a construção rígida de prompts com guardrails pedagógicos,
 * garantindo que a IA atue estritamente dentro do contexto do erro/acerto ou edital.
 */
class AskTutorUseCase @Inject constructor(
    private val geminiClient: GeminiClient
) {
    suspend operator fun invoke(
        contextType: String,
        contextDetails: String,
        tone: String,
        history: List<Pair<String, String>>,
        latestMessage: String
    ): String {
        val prompt = PromptTemplates.buildTutorChatPrompt(
            contextType = contextType,
            contextDetails = contextDetails,
            tone = tone,
            history = history,
            latestMessage = latestMessage
        )
        return geminiClient.generateContent(prompt)
    }
}
