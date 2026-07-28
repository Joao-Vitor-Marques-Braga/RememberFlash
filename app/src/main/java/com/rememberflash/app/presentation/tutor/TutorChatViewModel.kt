package com.rememberflash.app.presentation.tutor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.data.remote.gemini.PromptTemplates
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.EssayRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorChatViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val essayRepository: EssayRepository,
    private val contestRepository: com.rememberflash.app.domain.repository.ContestRepository,
    private val geminiClient: GeminiClient,
    private val preferencesManager: SecurePreferencesManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TutorChatUiState())
    val uiState: StateFlow<TutorChatUiState> = _uiState.asStateFlow()

    private val type: String = checkNotNull(savedStateHandle["type"]) { "Parâmetro type é obrigatório" }
    private val id: Long = checkNotNull(savedStateHandle["id"]) { "Parâmetro id é obrigatório" }

    private var activeContextDetails: String = ""

    init {
        loadActiveContext()
    }

    private fun loadActiveContext() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isThinking = true)
            if (type == "question") {
                val result = questionRepository.getQuestionById(id)
                if (result is Result.Success) {
                    val question = result.data
                    activeContextDetails = """
                        |Tipo: Questão Resolvida
                        |Enunciado: ${question.statement}
                        |Opções:
                        |${question.options.mapIndexed { idx, opt -> "  ${('A' + idx)}) $opt" }.joinToString("\n")}
                        |Alternativa Correta: ${('A' + question.correctIndex)}
                        |Alternativa Marcada pelo Estudante: ${question.chosenOption?.let { ('A' + it) } ?: "Nenhuma"}
                        |Justificativa Técnica: ${question.explanation ?: "Sem justificativa cadastrada."}
                    """.trimMargin()
                    
                    _uiState.value = _uiState.value.copy(
                        activeContextTitle = "Dúvida sobre Questão",
                        isThinking = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Erro ao carregar contexto da questão.",
                        isThinking = false
                    )
                }
            } else if (type == "essay") {
                val result = essayRepository.getById(id)
                if (result is Result.Success) {
                    val essay = result.data
                    activeContextDetails = """
                        |Tipo: Correção de Redação
                        |Tema: ${essay.theme}
                        |Redação do Estudante:
                        |${essay.extractedText}
                        |
                        |Nota Global: ${essay.score ?: 0.0} / 10.0
                        |Feedback de IA:
                        |${essay.aiFeedbackJson ?: "Sem feedback cadastrado."}
                    """.trimMargin()
                    
                    _uiState.value = _uiState.value.copy(
                        activeContextTitle = "Explicação da Redação",
                        isThinking = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Erro ao carregar contexto da redação.",
                        isThinking = false
                    )
                }
            } else if (type == "edital") {
                val result = contestRepository.getById(id)
                if (result is Result.Success) {
                    val contest = result.data
                    val pdfUriStr = contest.syllabusPdfUri ?: ""
                    val pdfUris = pdfUriStr.split("|").filter { it.isNotBlank() }
                    
                    val combinedTextBuilder = StringBuilder()
                    pdfUris.forEach { uriStr ->
                        try {
                            val pdfUri = android.net.Uri.parse(uriStr)
                            val extracted = com.rememberflash.app.data.local.pdf.LocalPdfExtractor.extractText(context, pdfUri)
                            if (extracted.isNotBlank()) {
                                combinedTextBuilder.append(extracted).append("\n\n")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("TutorChatViewModel", "Erro ao extrair pdf no tutor", e)
                        }
                    }
                    val editalText = combinedTextBuilder.toString().trim()
                    
                    activeContextDetails = """
                        |Tipo: Análise de Edital (Dúvidas do Edital)
                        |Concurso: ${contest.title}
                        |Banca: ${contest.organizerName}
                        |Conteúdo Extraído do Edital e Anexos:
                        |${if (editalText.isNotBlank()) editalText.take(50000) else "Nenhum texto extraído dos PDFs."}
                    """.trimMargin()
                    
                    _uiState.value = _uiState.value.copy(
                        activeContextTitle = "Tutor do Edital",
                        isThinking = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Erro ao carregar edital do concurso.",
                        isThinking = false
                    )
                }
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(input = text)
    }

    fun sendMessage() {
        val state = _uiState.value
        val messageText = state.input.trim()
        if (messageText.isBlank() || state.isThinking) return

        // Adiciona mensagem do usuário ao chat local
        val updatedMessages = state.messages + TutorMessage(text = messageText, isUser = true)
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            input = "",
            isThinking = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val tone = preferencesManager.getTone()
                val history = updatedMessages.dropLast(1).chunked(2).mapNotNull { chunk ->
                    val userMsg = chunk.firstOrNull { it.isUser }?.text
                    val tutorMsg = chunk.firstOrNull { !it.isUser }?.text
                    if (userMsg != null && tutorMsg != null) {
                        Pair(userMsg, tutorMsg)
                    } else null
                }

                val prompt = PromptTemplates.buildTutorChatPrompt(
                    contextType = type,
                    contextDetails = activeContextDetails,
                    tone = tone,
                    history = history,
                    latestMessage = messageText
                )

                val reply = geminiClient.generateContent(prompt)
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + TutorMessage(text = reply, isUser = false),
                    isThinking = false
                )
            } catch (e: Exception) {
                // RF014 - A2: Caso falhe, restaura o input para o usuário não perder a digitação e exibe erro
                _uiState.value = _uiState.value.copy(
                    input = messageText,
                    isThinking = false,
                    error = "Seu tutor virtual está indisponível no momento devido a falhas na rede. Tente enviar a mensagem novamente em alguns segundos."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
