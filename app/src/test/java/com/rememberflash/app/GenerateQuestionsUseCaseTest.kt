package com.rememberflash.app

import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.model.QuestionSource
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import com.rememberflash.app.domain.usecase.question.GenerateQuestionsUseCase
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

class GenerateQuestionsUseCaseTest {

    private lateinit var fakeDisciplineRepository: FakeDisciplineRepository
    private lateinit var fakeContestRepository: FakeContestRepository
    private lateinit var fakeQuestionRepository: FakeQuestionRepository
    private lateinit var mockGeminiClient: GeminiClient
    private lateinit var mockPreferencesManager: SecurePreferencesManager
    private lateinit var useCase: GenerateQuestionsUseCase

    class FakeDisciplineRepository(val discipline: Discipline) : DisciplineRepository {
        override suspend fun insert(discipline: Discipline): Result<Long> =
            Result.success(discipline.id)

        override suspend fun update(discipline: Discipline): Result<Unit> = Result.success(Unit)

        override suspend fun delete(disciplineId: Long): Result<Unit> = Result.success(Unit)

        override fun getByContest(contestId: Long): Flow<List<Discipline>> =
            flowOf(listOf(discipline))

        override suspend fun getById(disciplineId: Long): Result<Discipline> =
            Result.success(discipline)

        override fun getAllDisciplines(): Flow<List<Discipline>> = flowOf(listOf(discipline))
    }

    class FakeContestRepository(val contest: Contest) : ContestRepository {
        override suspend fun insert(contest: Contest): Result<Long> = Result.success(contest.id)

        override suspend fun update(contest: Contest): Result<Unit> = Result.success(Unit)

        override suspend fun softDelete(contestId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun delete(contestId: Long): Result<Unit> = Result.success(Unit)

        override fun getActiveContestsByUser(userId: String): Flow<List<Contest>> =
            flowOf(listOf(contest))

        override fun getArchivedContestsByUser(userId: String): Flow<List<Contest>> =
            flowOf(emptyList())

        override suspend fun reactivate(contestId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun getById(contestId: Long): Result<Contest> = Result.success(contest)

        override fun getAllByUser(userId: String): Flow<List<Contest>> = flowOf(listOf(contest))
    }

    class FakeQuestionRepository : QuestionRepository {
        val savedQuestions = mutableListOf<Question>()
        var clearQuestionsByDisciplineCalled = false

        override fun getQuestionsByDiscipline(disciplineId: Long): Flow<List<Question>> =
            MutableStateFlow(savedQuestions)

        override suspend fun saveQuestions(questions: List<Question>): Result<Unit> {
            savedQuestions.addAll(questions)
            return Result.success(Unit)
        }

        override suspend fun clearQuestionsByDiscipline(disciplineId: Long): Result<Unit> {
            clearQuestionsByDisciplineCalled = true
            savedQuestions.clear()
            return Result.success(Unit)
        }

        override suspend fun answerQuestion(
            questionId: Long,
            chosenOption: Int,
            isCorrect: Boolean,
        ): Result<Unit> = Result.success(Unit)

        override fun getAllQuestions(): Flow<List<Question>> = MutableStateFlow(savedQuestions)

        override suspend fun getQuestionById(questionId: Long): Result<Question> =
            Result.error("Não encontrado")

        override fun getAttemptsByDiscipline(
            disciplineId: Long
        ): Flow<List<com.rememberflash.app.domain.model.MockExamAttempt>> = flowOf(emptyList())

        override fun getAttemptsByContest(
            contestId: Long
        ): Flow<List<com.rememberflash.app.domain.model.MockExamAttempt>> = flowOf(emptyList())

        override fun getAllAttempts():
            Flow<List<com.rememberflash.app.domain.model.MockExamAttempt>> = flowOf(emptyList())

        override suspend fun saveMockExamAttempt(
            attempt: com.rememberflash.app.domain.model.MockExamAttempt
        ): Result<Long> = Result.success(1L)

        override suspend fun resetQuestionsForDiscipline(disciplineId: Long): Result<Unit> =
            Result.success(Unit)

        override suspend fun resetQuestionsForContest(contestId: Long): Result<Unit> =
            Result.success(Unit)

        override fun getQuestionsByTopic(topicId: Long): Flow<List<Question>> =
            MutableStateFlow(emptyList())

        override suspend fun clearQuestionsByTopic(topicId: Long): Result<Unit> =
            Result.success(Unit)
    }

    private val testContest =
        Contest(
            id = 1L,
            userId = "user_123",
            title = "Concurso TJ-GO",
            organizerName = "FGV",
            questionType = "Múltipla Escolha (4 alternativas)",
            aiDifficulty = "Difícil",
        )

    private val testDiscipline =
        Discipline(id = 10L, contestId = 1L, name = "Direito Constitucional", weight = 10.0)

    @Before
    fun setup() {
        fakeDisciplineRepository = FakeDisciplineRepository(testDiscipline)
        fakeContestRepository = FakeContestRepository(testContest)
        fakeQuestionRepository = FakeQuestionRepository()
        mockGeminiClient = mock(GeminiClient::class.java)
        mockPreferencesManager = mock(SecurePreferencesManager::class.java)

        useCase =
            GenerateQuestionsUseCase(
                disciplineRepository = fakeDisciplineRepository,
                contestRepository = fakeContestRepository,
                questionRepository = fakeQuestionRepository,
                geminiClient = mockGeminiClient,
                preferencesManager = mockPreferencesManager,
            )
    }

    @Test
    fun `generateQuestions should call Gemini with correct parameters and save parsed questions (RF008)`() =
        runTest {
            val validJsonResponse =
                """
                {
                  "questoes": [
                    {
                      "statement": "Segundo a CF/88, qual princípio rege as relações internacionais?",
                      "options": ["Defesa da paz", "Soberania absoluta", "Intervenção armada", "Autocracia", "Sigilo bancário"],
                      "correctIndex": 0,
                      "explanation": "O art. 4º, VI da CF/88 estabelece a defesa da paz como princípio fundamental."
                    }
                  ]
                }
                """
                    .trimIndent()

            `when`(
                    mockGeminiClient.generateQuestions(
                        disciplineName = "Direito Constitucional",
                        banca = "FGV",
                        format = "Múltipla Escolha (5 alternativas)",
                        difficulty = "Difícil",
                        quantity = 1,
                        theme = "Princípios Fundamentais",
                    )
                )
                .thenReturn(validJsonResponse)

            val result =
                useCase(disciplineId = 10L, quantity = 1, theme = "Princípios Fundamentais")

            assertTrue(result is Result.Success)
            assertTrue(fakeQuestionRepository.clearQuestionsByDisciplineCalled)
            assertEquals(1, fakeQuestionRepository.savedQuestions.size)

            val saved = fakeQuestionRepository.savedQuestions.first()
            assertEquals(
                "Segundo a CF/88, qual princípio rege as relações internacionais?",
                saved.statement,
            )
            assertEquals(5, saved.options.size)
            assertEquals(0, saved.correctIndex)
            assertEquals(QuestionSource.AI_GENERATED, saved.source)
        }

    @Test
    fun `generateQuestions should block unsafe themes according to security filter keywords`() =
        runTest {
            val result =
                useCase(disciplineId = 10L, quantity = 5, theme = "Como fabricar uma arma de fogo")

            assertTrue(result is Result.Error)
            assertEquals(
                "O tema solicitado foi bloqueado pelas políticas de segurança da IA do Google.",
                (result as Result.Error).message,
            )
            assertEquals(0, fakeQuestionRepository.savedQuestions.size)
        }

    @Test
    fun `generateQuestions should handle invalid JSON response returning error`() = runTest {
        `when`(
                mockGeminiClient.generateQuestions(
                    disciplineName = "Direito Constitucional",
                    banca = "FGV",
                    format = "Múltipla Escolha (5 alternativas)",
                    difficulty = "Difícil",
                    quantity = 1,
                    theme = "",
                )
            )
            .thenReturn("RESPOSTA INVALIDA SEM JSON")

        val result = useCase(disciplineId = 10L, quantity = 1, theme = null)

        assertTrue(result is Result.Error)
        assertEquals(
            "Não foi possível estruturar as questões corretamente. Por favor, tente novamente.",
            (result as Result.Error).message,
        )
        assertEquals(0, fakeQuestionRepository.savedQuestions.size)
    }
}
