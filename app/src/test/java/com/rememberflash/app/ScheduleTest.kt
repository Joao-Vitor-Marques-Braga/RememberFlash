package com.rememberflash.app

import androidx.lifecycle.SavedStateHandle
import com.rememberflash.app.data.remote.gemini.GeminiScheduleClient
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.StudySchedule
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.ScheduleRepository
import com.rememberflash.app.domain.usecase.schedule.GenerateStudyScheduleUseCase
import com.rememberflash.app.presentation.schedule.ScheduleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleTest {

    private val testDispatcher = StandardTestDispatcher()

    // Pure Kotlin Fakes
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var contestRepository: FakeContestRepository
    private lateinit var disciplineRepository: FakeDisciplineRepository
    private lateinit var scheduleRepository: FakeScheduleRepository
    private lateinit var geminiClient: FakeGeminiScheduleClient

    private lateinit var useCase: GenerateStudyScheduleUseCase
    private lateinit var viewModel: ScheduleViewModel

    private val fakeUser = User(id = "user123", name = "Test User", cpf = "12345678900", email = "test@user.com")
    private val fakeContest = Contest(id = 1L, userId = "user123", title = "Concurso Teste", description = "Cargo: Auditor")
    private val fakeDisciplines = listOf(
        Discipline(id = 10L, contestId = 1L, name = "Língua Portuguesa", weight = 10.0),
        Discipline(id = 20L, contestId = 1L, name = "Direito Constitucional", weight = 8.0)
    )

    // Fakes Implementations
    class FakeAuthRepository(val fakeUser: User) : AuthRepository {
        override suspend fun saveSession(token: String, user: User) {}
        override suspend fun getCurrentSession(): Result<User> = Result.success(fakeUser)
        override suspend fun clearSession() {}
        override suspend fun isSessionValid(): Boolean = true
        override suspend fun saveGeminiApiKey(apiKey: String) {}
        override suspend fun getGeminiApiKey(): String? = "fake_key"
        override suspend fun hasGeminiApiKey(): Boolean = true
        override suspend fun registerUser(name: String, cpf: String, email: String, passwordKey: String): Result<User> = Result.success(fakeUser)
        override suspend fun authenticateUser(email: String, passwordKey: String): Result<User> = Result.success(fakeUser)
    }

    class FakeContestRepository(var contest: Contest) : ContestRepository {
        var lastInsertedContest: Contest? = null
        override suspend fun insert(contest: Contest): Result<Long> {
            lastInsertedContest = contest
            this.contest = contest
            return Result.success(contest.id)
        }
        override suspend fun update(contest: Contest): Result<Unit> {
            this.contest = contest
            return Result.success(Unit)
        }
        override suspend fun softDelete(contestId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun getById(contestId: Long): Result<Contest> = Result.success(contest)
        override fun getActiveContestsByUser(userId: String): Flow<List<Contest>> = flowOf(listOf(contest))
        override fun getAllByUser(userId: String): Flow<List<Contest>> = flowOf(listOf(contest))
    }

    class FakeDisciplineRepository(val list: List<Discipline>) : DisciplineRepository {
        override suspend fun insert(discipline: Discipline): Result<Long> = Result.success(1L)
        override suspend fun update(discipline: Discipline): Result<Unit> = Result.success(Unit)
        override suspend fun delete(disciplineId: Long): Result<Unit> = Result.success(Unit)
        override fun getByContest(contestId: Long): Flow<List<Discipline>> = flowOf(list)
        override suspend fun getById(disciplineId: Long): Result<Discipline> = Result.success(list.first())
        override fun getAllDisciplines(): Flow<List<Discipline>> = flowOf(list)
    }

    class FakeScheduleRepository(var existingSchedule: StudySchedule?) : ScheduleRepository {
        var clearGoalsCalledWithId: Long? = null
        val insertedGoals = mutableListOf<DailyGoal>()
        var lastInsertedSchedule: StudySchedule? = null

        override suspend fun insert(schedule: StudySchedule): Result<Long> {
            lastInsertedSchedule = schedule
            val id = if (schedule.id == 0L) 100L else schedule.id
            return Result.success(id)
        }
        override suspend fun update(schedule: StudySchedule): Result<Unit> = Result.success(Unit)
        override suspend fun getByContest(contestId: Long): Result<StudySchedule?> = Result.success(existingSchedule)
        override fun getDailyGoalsBySchedule(scheduleId: Long): Flow<List<DailyGoal>> = flowOf(insertedGoals)
        override suspend fun insertDailyGoals(goals: List<DailyGoal>): Result<Unit> {
            insertedGoals.addAll(goals)
            return Result.success(Unit)
        }
        override suspend fun updateDailyGoalProgress(goalId: Long, completedMinutes: Int, flashcardsCompleted: Int): Result<Unit> = Result.success(Unit)
        override suspend fun clearDailyGoalsBySchedule(scheduleId: Long): Result<Unit> {
            clearGoalsCalledWithId = scheduleId
            insertedGoals.clear()
            return Result.success(Unit)
        }
    }

    class FakeGeminiScheduleClient : GeminiScheduleClient {
        var resultJson: String = ""
        override suspend fun generateStudySchedule(prompt: String): String = resultJson
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        authRepository = FakeAuthRepository(fakeUser)
        contestRepository = FakeContestRepository(fakeContest)
        disciplineRepository = FakeDisciplineRepository(fakeDisciplines)
        scheduleRepository = FakeScheduleRepository(null)
        geminiClient = FakeGeminiScheduleClient()

        useCase = GenerateStudyScheduleUseCase(
            context = mock(),
            contestRepository = contestRepository,
            disciplineRepository = disciplineRepository,
            scheduleRepository = scheduleRepository,
            geminiClient = geminiClient
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModelValidatesFutureExamDate() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("contestId" to 1L))
        viewModel = ScheduleViewModel(
            savedStateHandle = savedStateHandle,
            authRepository = authRepository,
            contestRepository = contestRepository,
            disciplineRepository = disciplineRepository,
            scheduleRepository = scheduleRepository,
            generateStudyScheduleUseCase = useCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Ontem
        val pastDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        viewModel.onExamDateChanged(pastDate)

        viewModel.onGenerateScheduleClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("A data da prova deve ser uma data futura válida.", viewModel.uiState.value.error)
    }

    @Test
    fun testGenerateScheduleSuccessfully() = runTest {
        val fakeGeminiJson = """
            {
              "warning": null,
              "sessions": [
                {
                  "date": "2026-08-01",
                  "disciplineName": "Língua Portuguesa",
                  "minutes": 60
                },
                {
                  "date": "2026-08-01",
                  "disciplineName": "Direito Constitucional",
                  "minutes": 60
                }
              ]
            }
        """.trimIndent()

        geminiClient.resultJson = fakeGeminiJson

        val futureExamDate = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L
        val result = useCase(
            contestId = 1L,
            examDateLong = futureExamDate,
            minutesPerDay = 120,
            maxSubjectsPerDay = 2,
            availableDaysOfWeek = listOf("Segunda", "Terça", "Quarta")
        )

        assertTrue(result.isSuccess)
        assertEquals(100L, result.getOrNull())

        assertEquals(2, scheduleRepository.insertedGoals.size)
        assertEquals(60, scheduleRepository.insertedGoals[0].targetMinutes)
        assertEquals(60, scheduleRepository.insertedGoals[1].targetMinutes)
    }

    @Test
    fun testGenerateScheduleClearsOldGoals() = runTest {
        val existingSchedule = StudySchedule(
            id = 55L,
            contestId = 1L,
            examDate = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L,
            availableHoursPerDay = 2.0
        )
        scheduleRepository.existingSchedule = existingSchedule

        val fakeGeminiJson = """
            {
              "warning": null,
              "sessions": [
                {
                  "date": "2026-08-01",
                  "disciplineName": "Língua Portuguesa",
                  "minutes": 90
                }
              ]
            }
        """.trimIndent()
        geminiClient.resultJson = fakeGeminiJson

        val futureExamDate = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L
        val result = useCase(
            contestId = 1L,
            examDateLong = futureExamDate,
            minutesPerDay = 120,
            maxSubjectsPerDay = 2,
            availableDaysOfWeek = listOf("Segunda")
        )

        assertTrue(result.isSuccess)
        assertEquals(55L, scheduleRepository.clearGoalsCalledWithId)
    }

    @Test
    fun testGenerateScheduleHandlesInsufficientTimeWarning() = runTest {
        val warningMessage = "O tempo selecionado é insuficiente para cobrir todas as matérias até a data da prova."
        val fakeGeminiJson = """
            {
              "warning": "$warningMessage",
              "sessions": [
                {
                  "date": "2026-08-01",
                  "disciplineName": "Língua Portuguesa",
                  "minutes": 120
                }
              ]
            }
        """.trimIndent()

        geminiClient.resultJson = fakeGeminiJson

        val futureExamDate = System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000L
        val result = useCase(
            contestId = 1L,
            examDateLong = futureExamDate,
            minutesPerDay = 120,
            maxSubjectsPerDay = 2,
            availableDaysOfWeek = listOf("Segunda")
        )

        assertTrue(result.isSuccess)
        val savedDescription = contestRepository.contest.description
        assertNotNull(savedDescription)
        assertTrue(savedDescription.contains("[WarningCronograma]:"))
        assertTrue(savedDescription.contains(warningMessage))
    }
}
