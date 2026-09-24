package com.rememberflash.app

import android.net.Uri
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.Essay
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.EssayRepository
import com.rememberflash.app.domain.usecase.essay.EvaluateEssayUseCase
import com.rememberflash.app.domain.usecase.essay.ExtractTextFromImageUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class EssayTest {

    private lateinit var mockTextExtractor: ExtractTextFromImageUseCase.TextExtractor
    private lateinit var mockHandwrittenTranscriber:
        ExtractTextFromImageUseCase.HandwrittenTranscriber
    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var extractUseCase: ExtractTextFromImageUseCase

    private lateinit var fakeEssayRepository: FakeEssayRepository
    private lateinit var fakeContestRepository: FakeContestRepository
    private lateinit var mockEssayEvaluator: EvaluateEssayUseCase.EssayEvaluator
    private lateinit var evaluateUseCase: EvaluateEssayUseCase

    private val mockUri = mock(Uri::class.java)

    class FakeContestRepository(var contest: Contest) : ContestRepository {
        override suspend fun insert(contest: Contest): Result<Long> = Result.success(contest.id)

        override suspend fun update(contest: Contest): Result<Unit> = Result.success(Unit)

        override suspend fun delete(contestId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun softDelete(contestId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun getById(contestId: Long): Result<Contest> = Result.success(contest)

        override fun getActiveContestsByUser(userId: String): Flow<List<Contest>> =
            flowOf(listOf(contest))

        override fun getArchivedContestsByUser(userId: String): Flow<List<Contest>> =
            flowOf(emptyList())

        override suspend fun reactivate(contestId: Long): Result<Unit> = Result.success(Unit)

        override fun getAllByUser(userId: String): Flow<List<Contest>> = flowOf(listOf(contest))
    }

    class FakeEssayRepository : EssayRepository {
        val essays = mutableListOf<Essay>()
        var lastUpdatedEssayId: Long? = null
        var lastUpdatedFeedbackJson: String? = null
        var lastUpdatedScore: Double? = null

        override suspend fun insert(essay: Essay): Result<Long> {
            val id = (essays.size + 1).toLong()
            essays.add(essay.copy(id = id))
            return Result.success(id)
        }

        override suspend fun updateWithExtractedText(essayId: Long, text: String): Result<Unit> =
            Result.success(Unit)

        override suspend fun updateWithAiFeedback(
            essayId: Long,
            feedbackJson: String,
            score: Double,
            tokensSpent: Int,
        ): Result<Unit> {
            lastUpdatedEssayId = essayId
            lastUpdatedFeedbackJson = feedbackJson
            lastUpdatedScore = score
            return Result.success(Unit)
        }

        override fun getByUser(userId: String): Flow<List<Essay>> = MutableStateFlow(essays)

        override fun getByContest(contestId: Long): Flow<List<Essay>> = MutableStateFlow(essays)

        override suspend fun getById(essayId: Long): Result<Essay> =
            Result.success(essays.first { it.id == essayId })
    }

    @Before
    fun setup() {
        mockTextExtractor = mock(ExtractTextFromImageUseCase.TextExtractor::class.java)
        mockHandwrittenTranscriber =
            mock(ExtractTextFromImageUseCase.HandwrittenTranscriber::class.java)
        mockAuthRepository = mock(AuthRepository::class.java)

        extractUseCase =
            ExtractTextFromImageUseCase(
                textExtractor = mockTextExtractor,
                handwrittenTranscriber = mockHandwrittenTranscriber,
                authRepository = mockAuthRepository,
            )

        val contest =
            Contest(
                id = 1L,
                userId = "user_123",
                title = "Concurso TJ-GO",
                organizerName = "FGV",
                aiRigor = "Rigoroso",
                aiTone = "Analítico e Crítico",
            )
        fakeContestRepository = FakeContestRepository(contest)
        fakeEssayRepository = FakeEssayRepository()
        mockEssayEvaluator = mock(EvaluateEssayUseCase.EssayEvaluator::class.java)

        evaluateUseCase =
            EvaluateEssayUseCase(
                essayRepository = fakeEssayRepository,
                contestRepository = fakeContestRepository,
                essayEvaluator = mockEssayEvaluator,
            )
    }

    @Test
    fun `extractText should prioritize AI handwriting transcription when Gemini API key is configured`() =
        runTest {
            `when`(mockAuthRepository.hasGeminiApiKey()).thenReturn(true)
            `when`(mockHandwrittenTranscriber.transcribeHandwritten(mockUri))
                .thenReturn(
                    "O Estado Democrático de Direito assegura os direitos sociais e individuais."
                )

            val result = extractUseCase(mockUri)

            assertTrue(result is Result.Success)
            assertEquals(
                "O Estado Democrático de Direito assegura os direitos sociais e individuais.",
                (result as Result.Success).data,
            )
        }

    @Test
    fun `extractText should fallback to local OCR when AI transcription fails or throws exception`() =
        runTest {
            `when`(mockAuthRepository.hasGeminiApiKey()).thenReturn(true)
            `when`(mockHandwrittenTranscriber.transcribeHandwritten(mockUri))
                .thenThrow(RuntimeException("Timeout de conexão na nuvem"))
            `when`(mockTextExtractor.extractText(mockUri))
                .thenReturn("Texto extraído com sucesso pelo ML Kit local.")

            val result = extractUseCase(mockUri)

            assertTrue(result is Result.Success)
            assertEquals(
                "Texto extraído com sucesso pelo ML Kit local.",
                (result as Result.Success).data,
            )
        }

    @Test
    fun `extractText should return friendly error when no text is found in image`() = runTest {
        `when`(mockAuthRepository.hasGeminiApiKey()).thenReturn(false)
        `when`(mockTextExtractor.extractText(mockUri)).thenReturn("   ")

        val result = extractUseCase(mockUri)

        assertTrue(result is Result.Error)
        assertEquals(
            "Nenhum texto identificado na imagem. Verifique o enquadramento ou a iluminação da foto.",
            (result as Result.Error).message,
        )
    }

    @Test
    fun `evaluateEssay should inject parent contest banca, rigor and tone into AI evaluator and persist feedback with score`() =
        runTest {
            val essay =
                Essay(
                    id = 50L,
                    userId = "user_123",
                    contestId = 1L,
                    title = "Desafios da Segurança Pública",
                    theme = "A segurança pública e os direitos fundamentais no Brasil",
                    imageUri = "file://image.jpg",
                    extractedText =
                        "A segurança pública, dever do Estado, direito e responsabilidade de todos, é exercida para a preservação da ordem pública.",
                )

            val mockAiFeedbackJson =
                """
                {
                    "nota": 8.5,
                    "notaMaxima": 10.0,
                    "criterios": [
                        {"nome": "Domínio da Norma Culta", "nota": 2.0, "peso": 2.0, "comentario": "Excelente uso de vocabulário e concordância."},
                        {"nome": "Estrutura Dissertativa-Argumentativa", "nota": 3.0, "peso": 3.0, "comentario": "Tese bem delimitada com argumentos consistentes."},
                        {"nome": "Proposta de Intervenção", "nota": 3.5, "peso": 5.0, "comentario": "Proposta plausível, porém carece de detalhamento dos agentes."}
                    ],
                    "pontosFortes": ["Coesão impecável", "Fundamentação constitucional"],
                    "oportunidadesMelhoria": ["Detalhamento prático da intervenção"],
                    "comentarioGeral": "Excelente produção textual no perfil FGV, mantendo tom analítico e rigoroso."
                }
                """
                    .trimIndent()

            `when`(
                    mockEssayEvaluator.evaluate(
                        essayText = essay.extractedText!!,
                        theme = essay.theme,
                        banca = "FGV",
                        rigor = "Rigoroso",
                        tone = "Analítico e Crítico",
                    )
                )
                .thenReturn(mockAiFeedbackJson)

            val result = evaluateUseCase(essay)

            assertTrue(result is Result.Success)
            assertEquals(50L, fakeEssayRepository.lastUpdatedEssayId)
            assertEquals(8.5, fakeEssayRepository.lastUpdatedScore)
            assertEquals(mockAiFeedbackJson, fakeEssayRepository.lastUpdatedFeedbackJson)
        }

    @Test
    fun `evaluateEssay should fail when essay extractedText is blank`() = runTest {
        val essay =
            Essay(
                id = 51L,
                userId = "user_123",
                contestId = 1L,
                title = "Redação sem OCR",
                theme = "Tema Livre",
                imageUri = "file://image.jpg",
                extractedText = "   ",
            )

        val result = evaluateUseCase(essay)

        assertTrue(result is Result.Error)
        assertEquals(
            "Texto da redação não extraído. Execute o OCR primeiro.",
            (result as Result.Error).message,
        )
    }
}
