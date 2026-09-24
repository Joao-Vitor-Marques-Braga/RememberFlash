package com.rememberflash.app

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.Essay
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.EssayRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import com.rememberflash.app.presentation.tutor.TutorChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class TutorChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockQuestionRepository: QuestionRepository
    private lateinit var mockEssayRepository: EssayRepository
    private lateinit var mockContestRepository: ContestRepository
    private lateinit var mockGeminiClient: GeminiClient
    private lateinit var mockPreferencesManager: SecurePreferencesManager
    private lateinit var mockContext: Context

    private val fakeQuestion = Question(
        id = 42L,
        disciplineId = 1L,
        statement = "Qual é o princípio constitucional implícito?",
        options = listOf("Legalidade", "Proporcionalidade", "Impessoalidade", "Moralidade", "Publicidade"),
        correctIndex = 1,
        chosenOption = 0,
        explanation = "A proporcionalidade é um princípio implícito decorrente do devido processo legal."
    )

    private val fakeEssay = Essay(
        id = 88L,
        userId = "user_123",
        contestId = 10L,
        title = "Redação Modelo",
        theme = "Os impactos da IA no serviço público",
        imageUri = "file://image.jpg",
        extractedText = "A inteligência artificial tem potencializado a celeridade administrativa...",
        score = 8.5,
        aiFeedbackJson = "Excelente domínio temático e norma culta."
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockQuestionRepository = mock(QuestionRepository::class.java)
        mockEssayRepository = mock(EssayRepository::class.java)
        mockContestRepository = mock(ContestRepository::class.java)
        mockGeminiClient = mock(GeminiClient::class.java)
        mockPreferencesManager = mock(SecurePreferencesManager::class.java)
        mockContext = mock(Context::class.java)

        `when`(mockPreferencesManager.getTone()).thenReturn("Direto e Motivador")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadActiveContext should bind question context and set title to Duvida sobre Questao`() = runTest {
        `when`(mockQuestionRepository.getQuestionById(42L)).thenReturn(Result.success(fakeQuestion))

        val savedStateHandle = SavedStateHandle(mapOf("type" to "question", "id" to 42L))
        val viewModel = TutorChatViewModel(
            questionRepository = mockQuestionRepository,
            essayRepository = mockEssayRepository,
            contestRepository = mockContestRepository,
            geminiClient = mockGeminiClient,
            preferencesManager = mockPreferencesManager,
            context = mockContext,
            savedStateHandle = savedStateHandle
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Dúvida sobre Questão", state.activeContextTitle)
        assertFalse(state.isThinking)
        assertNull(state.error)
    }

    @Test
    fun `loadActiveContext should bind essay context and set title to Explicacao da Redacao`() = runTest {
        `when`(mockEssayRepository.getById(88L)).thenReturn(Result.success(fakeEssay))

        val savedStateHandle = SavedStateHandle(mapOf("type" to "essay", "id" to 88L))
        val viewModel = TutorChatViewModel(
            questionRepository = mockQuestionRepository,
            essayRepository = mockEssayRepository,
            contestRepository = mockContestRepository,
            geminiClient = mockGeminiClient,
            preferencesManager = mockPreferencesManager,
            context = mockContext,
            savedStateHandle = savedStateHandle
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Explicação da Redação", state.activeContextTitle)
        assertFalse(state.isThinking)
        assertNull(state.error)
    }

    @Test
    fun `sendMessage should append user message and invoke Gemini with bounded context and append tutor reply`() = runTest {
        `when`(mockQuestionRepository.getQuestionById(42L)).thenReturn(Result.success(fakeQuestion))
        val elaborateAiReply =
            """
            A alternativa A cita a Legalidade, que é um princípio expresso no art. 37 da CF/88. A questão exigia um princípio implícito (a Proporcionalidade, derivado do devido processo legal substantivo), tornando a alternativa B o gabarito correto.
            """.trimIndent()
        `when`(mockGeminiClient.generateContent(anyString()))
            .thenReturn(elaborateAiReply)

        val savedStateHandle = SavedStateHandle(mapOf("type" to "question", "id" to 42L))
        val viewModel = TutorChatViewModel(
            questionRepository = mockQuestionRepository,
            essayRepository = mockEssayRepository,
            contestRepository = mockContestRepository,
            geminiClient = mockGeminiClient,
            preferencesManager = mockPreferencesManager,
            context = mockContext,
            savedStateHandle = savedStateHandle
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onInputChanged("Por que a alternativa A está incorreta?")
        viewModel.sendMessage()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        // 1ª mensagem: do usuário
        assertEquals("Por que a alternativa A está incorreta?", state.messages[0].text)
        assertTrue(state.messages[0].isUser)
        // 2ª mensagem: do tutor (IA)
        assertEquals(elaborateAiReply, state.messages[1].text)
        assertFalse(state.messages[1].isUser)
        assertEquals("", state.input)
        assertFalse(state.isThinking)
        assertNull(state.error)
    }

    @Test
    fun `sendMessage should restore user input and set friendly error message on network failure`() = runTest {
        `when`(mockQuestionRepository.getQuestionById(42L)).thenReturn(Result.success(fakeQuestion))
        `when`(mockGeminiClient.generateContent(anyString()))
            .thenThrow(RuntimeException("Timeout de conexão na nuvem"))

        val savedStateHandle = SavedStateHandle(mapOf("type" to "question", "id" to 42L))
        val viewModel = TutorChatViewModel(
            questionRepository = mockQuestionRepository,
            essayRepository = mockEssayRepository,
            contestRepository = mockContestRepository,
            geminiClient = mockGeminiClient,
            preferencesManager = mockPreferencesManager,
            context = mockContext,
            savedStateHandle = savedStateHandle
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val userQuestion = "Explique a fundamentação da banca"
        viewModel.onInputChanged(userQuestion)
        viewModel.sendMessage()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        // RF014 - A2: Restaura o input para o estudante não perder o texto digitado
        assertEquals(userQuestion, state.input)
        assertFalse(state.isThinking)
        assertNotNull(state.error)
        assertEquals(
            "Seu tutor virtual está indisponível no momento devido a falhas na rede. Tente enviar a mensagem novamente em alguns segundos.",
            state.error
        )
    }
}
