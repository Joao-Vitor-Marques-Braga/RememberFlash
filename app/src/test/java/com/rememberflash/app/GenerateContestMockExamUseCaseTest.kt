package com.rememberflash.app

import android.content.Context
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.MockExamAttempt
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import com.rememberflash.app.domain.usecase.question.GenerateContestMockExamUseCase
import com.rememberflash.app.domain.usecase.question.GenerateQuestionsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GenerateContestMockExamUseCaseTest {

    private lateinit var fakeContestRepository: FakeContestRepository
    private lateinit var fakeDisciplineRepository: FakeDisciplineRepository
    private lateinit var fakeQuestionRepository: FakeQuestionRepository
    private lateinit var mockGenerateQuestionsUseCase: GenerateQuestionsUseCase
    private lateinit var mockContext: Context
    private lateinit var useCase: GenerateContestMockExamUseCase

    class FakeContestRepository : ContestRepository {
        val contests = mutableListOf<Contest>()
        private val flow = MutableStateFlow<List<Contest>>(emptyList())

        override suspend fun insert(contest: Contest): Result<Long> {
            val id = (contests.size + 1).toLong()
            contests.add(contest.copy(id = id))
            flow.value = contests.toList()
            return Result.success(id)
        }

        override suspend fun update(contest: Contest): Result<Unit> = Result.success(Unit)

        override suspend fun softDelete(contestId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun delete(contestId: Long): Result<Unit> = Result.success(Unit)

        override fun getActiveContestsByUser(userId: String): Flow<List<Contest>> = flow

        override fun getArchivedContestsByUser(userId: String): Flow<List<Contest>> = flow

        override suspend fun reactivate(contestId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun getById(contestId: Long): Result<Contest> {
            val c = contests.firstOrNull { it.id == contestId }
            return if (c != null) Result.success(c) else Result.error("Não encontrado")
        }

        override fun getAllByUser(userId: String): Flow<List<Contest>> = flow
    }

    class FakeDisciplineRepository : DisciplineRepository {
        val disciplines = mutableListOf<Discipline>()
        private val flow = MutableStateFlow<List<Discipline>>(emptyList())

        override suspend fun insert(discipline: Discipline): Result<Long> {
            val id = (disciplines.size + 1).toLong()
            disciplines.add(discipline.copy(id = id))
            flow.value = disciplines.toList()
            return Result.success(id)
        }

        override suspend fun update(discipline: Discipline): Result<Unit> = Result.success(Unit)

        override suspend fun delete(disciplineId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun getById(disciplineId: Long): Result<Discipline> {
            val d = disciplines.firstOrNull { it.id == disciplineId }
            return if (d != null) Result.success(d) else Result.error("Não encontrado")
        }

        override fun getByContest(contestId: Long): Flow<List<Discipline>> = flow

        override fun getAllDisciplines(): Flow<List<Discipline>> = flow
    }

    class FakeQuestionRepository : QuestionRepository {
        val savedAttempts = mutableListOf<MockExamAttempt>()

        override fun getQuestionsByDiscipline(disciplineId: Long): Flow<List<Question>> =
            MutableStateFlow(emptyList())

        override suspend fun saveQuestions(questions: List<Question>): Result<Unit> =
            Result.success(Unit)

        override suspend fun clearQuestionsByDiscipline(disciplineId: Long): Result<Unit> =
            Result.success(Unit)

        override suspend fun answerQuestion(
            questionId: Long,
            chosenOption: Int,
            isCorrect: Boolean,
        ): Result<Unit> = Result.success(Unit)

        override fun getAllQuestions(): Flow<List<Question>> = MutableStateFlow(emptyList())

        override suspend fun getQuestionById(questionId: Long): Result<Question> =
            Result.error("Não encontrado")

        override fun getAttemptsByDiscipline(disciplineId: Long): Flow<List<MockExamAttempt>> =
            MutableStateFlow(savedAttempts)

        override fun getAttemptsByContest(contestId: Long): Flow<List<MockExamAttempt>> =
            MutableStateFlow(savedAttempts)

        override fun getAllAttempts(): Flow<List<MockExamAttempt>> = MutableStateFlow(savedAttempts)

        override suspend fun saveMockExamAttempt(attempt: MockExamAttempt): Result<Long> {
            val id = (savedAttempts.size + 1).toLong()
            savedAttempts.add(attempt.copy(id = id))
            return Result.success(id)
        }

        override suspend fun resetQuestionsForDiscipline(disciplineId: Long): Result<Unit> =
            Result.success(Unit)

        override suspend fun resetQuestionsForContest(contestId: Long): Result<Unit> =
            Result.success(Unit)

        override fun getQuestionsByTopic(topicId: Long): Flow<List<Question>> =
            MutableStateFlow(emptyList())

        override suspend fun clearQuestionsByTopic(topicId: Long): Result<Unit> =
            Result.success(Unit)
    }

    @Before
    fun setup() {
        fakeContestRepository = FakeContestRepository()
        fakeDisciplineRepository = FakeDisciplineRepository()
        fakeQuestionRepository = FakeQuestionRepository()
        mockGenerateQuestionsUseCase = mock(GenerateQuestionsUseCase::class.java)
        mockContext = mock(Context::class.java)

        useCase =
            GenerateContestMockExamUseCase(
                contestRepository = fakeContestRepository,
                disciplineRepository = fakeDisciplineRepository,
                questionRepository = fakeQuestionRepository,
                generateQuestionsUseCase = mockGenerateQuestionsUseCase,
                context = mockContext,
            )
    }

    @Test
    fun `generateContestMockExam should orchestrate full mock exam with questions per discipline defined by exam board`() =
        runTest {
            val contest =
                Contest(
                    id = 1L,
                    userId = "user_123",
                    title = "Concurso TJ-GO",
                    organizerName = "FGV",
                    syllabusPdfUri = null,
                )
            fakeContestRepository.insert(contest)

            fakeDisciplineRepository.insert(
                Discipline(id = 10L, contestId = 1L, name = "Direito Constitucional", weight = 10.0)
            )
            fakeDisciplineRepository.insert(
                Discipline(id = 20L, contestId = 1L, name = "Direito Administrativo", weight = 10.0)
            )

            `when`(mockGenerateQuestionsUseCase(10L, 10, null)).thenReturn(Result.success(Unit))
            `when`(mockGenerateQuestionsUseCase(20L, 10, null)).thenReturn(Result.success(Unit))

            var progressCalls = 0
            val result =
                useCase(1L) { current, total, _ ->
                    progressCalls++
                    assertEquals(2, total)
                }

            assertTrue(result is Result.Success)
            assertEquals(2, progressCalls)
        }

    @Test
    fun `recordAttempt should persist attempt in repository with answers history and per-question times`() =
        runTest {
            val answersMap = mapOf(101L to 2, 102L to 0, 103L to 3)
            val timesMap = mapOf(101L to 15000L, 102L to 22000L, 103L to 18500L)

            val saveResult =
                useCase.recordAttempt(
                    contestId = 1L,
                    score = 2,
                    totalQuestions = 3,
                    answersMap = answersMap,
                    timesMap = timesMap,
                )

            assertTrue(saveResult is Result.Success)
            assertEquals(1, fakeQuestionRepository.savedAttempts.size)

            val saved = fakeQuestionRepository.savedAttempts[0]
            assertEquals(1L, saved.contestId)
            assertEquals(2, saved.score)
            assertEquals(3, saved.totalQuestions)
            assertNotNull(saved.answersJson)
            assertTrue(saved.answersJson.contains("\"101\":2"))
            assertNotNull(saved.timesJson)
            assertTrue(saved.timesJson!!.contains("\"101\":15000"))
        }
}
