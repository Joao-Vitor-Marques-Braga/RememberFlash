package com.rememberflash.app

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.StudySchedule
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.ScheduleRepository
import com.rememberflash.app.domain.usecase.schedule.ProposeScheduleRecalculationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ScheduleRecalculationTest {

    private lateinit var scheduleRepository: FakeScheduleRepository
    private lateinit var disciplineRepository: FakeDisciplineRepository
    private lateinit var useCase: ProposeScheduleRecalculationUseCase

    private val fakeSchedule = StudySchedule(
        id = 1L,
        contestId = 100L,
        examDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L, // 30 dias futuros
        availableHoursPerDay = 2.0, // 120 minutos por dia
        restDaysPerWeek = 1
    )

    private val fakeDisciplines = listOf(
        Discipline(id = 10L, contestId = 100L, name = "Língua Portuguesa", weight = 1.0),
        Discipline(id = 20L, contestId = 100L, name = "Direito Constitucional", weight = 1.0)
    )

    class FakeScheduleRepository(var currentSchedule: StudySchedule?) : ScheduleRepository {
        override suspend fun insert(schedule: StudySchedule): Result<Long> = Result.success(1L)
        override suspend fun update(schedule: StudySchedule): Result<Unit> = Result.success(Unit)
        override suspend fun getByContest(contestId: Long): Result<StudySchedule?> = Result.success(currentSchedule)
        override fun getDailyGoalsBySchedule(scheduleId: Long): Flow<List<DailyGoal>> = flowOf(emptyList())
        override fun getAllDailyGoalsFlow(): Flow<List<DailyGoal>> = flowOf(emptyList())
        override suspend fun insertDailyGoals(goals: List<DailyGoal>): Result<Unit> = Result.success(Unit)
        override suspend fun updateDailyGoalProgress(goalId: Long, completedMinutes: Int, flashcardsCompleted: Int): Result<Unit> = Result.success(Unit)
        override suspend fun clearDailyGoalsBySchedule(scheduleId: Long): Result<Unit> = Result.success(Unit)
    }

    class FakeDisciplineRepository(var disciplines: List<Discipline>) : DisciplineRepository {
        override suspend fun insert(discipline: Discipline): Result<Long> = Result.success(1L)
        override suspend fun update(discipline: Discipline): Result<Unit> = Result.success(Unit)
        override suspend fun delete(disciplineId: Long): Result<Unit> = Result.success(Unit)
        override fun getByContest(contestId: Long): Flow<List<Discipline>> = flowOf(disciplines)
        override suspend fun getById(disciplineId: Long): Result<Discipline> =
            Result.success(disciplines.first { it.id == disciplineId })
        override fun getAllDisciplines(): Flow<List<Discipline>> = flowOf(disciplines)
    }

    @Before
    fun setUp() {
        scheduleRepository = FakeScheduleRepository(fakeSchedule)
        disciplineRepository = FakeDisciplineRepository(fakeDisciplines)
        useCase = ProposeScheduleRecalculationUseCase(
            scheduleRepository = scheduleRepository,
            disciplineRepository = disciplineRepository
        )
    }

    @Test
    fun `proposeRecalculation should identify deficit below 70 percent and apply adaptive difficulty multiplier`() = runTest {
        // Simulado: Língua Portuguesa com 50% de acertos (5 de 10) -> Déficit (< 70%)
        // Direito Constitucional com 90% de acertos (9 de 10) -> Satisfatório (>= 70%)
        val performanceMap = mapOf(
            10L to Pair(5, 10), // 50% acertos
            20L to Pair(9, 10)  // 90% acertos
        )

        val result = useCase(contestId = 100L, disciplinePerformance = performanceMap)

        assertTrue(result is Result.Success)
        val proposal = (result as Result.Success).data

        // 1. Deve identificar 1 disciplina em dificuldade (Língua Portuguesa)
        assertEquals(1, proposal.difficulties.size)
        val difficulty = proposal.difficulties.first()
        assertEquals(10L, difficulty.disciplineId)
        assertEquals("Língua Portuguesa", difficulty.disciplineName)
        assertEquals(50.0, difficulty.accuracy, 0.01)

        // 2. Verifica a redistribuição na lista de comparação
        val compPortuguese = proposal.comparisonList.first { it.disciplineId == 10L }
        val compConst = proposal.comparisonList.first { it.disciplineId == 20L }

        // Antes: 60 min para cada (pesos iguais 1.0 vs 1.0)
        assertEquals(60, compPortuguese.currentMinutesPerDay)
        assertEquals(60, compConst.currentMinutesPerDay)

        // Depois: Multiplicador adaptativo = 2.0 - (50 / 100) = 1.5x para Português
        // Português ganha mais tempo diário que Direito Constitucional
        assertTrue(compPortuguese.proposedMinutesPerDay > compConst.proposedMinutesPerDay)
        assertEquals(72, compPortuguese.proposedMinutesPerDay)
        assertEquals(48, compConst.proposedMinutesPerDay)

        // 3. Verifica se as metas diárias foram geradas com o novo balanceamento
        assertTrue(proposal.proposedSchedule.dailyGoals.isNotEmpty())
    }

    @Test
    fun `proposeRecalculation should retain base schedule when all disciplines have accuracy above 70 percent`() = runTest {
        val performanceMap = mapOf(
            10L to Pair(8, 10), // 80% acertos
            20L to Pair(9, 10)  // 90% acertos
        )

        val result = useCase(contestId = 100L, disciplinePerformance = performanceMap)

        assertTrue(result is Result.Success)
        val proposal = (result as Result.Success).data

        // Nenhuma disciplina abaixo de 70%
        assertTrue(proposal.difficulties.isEmpty())

        // Ambas continuam com 60 min
        val compPortuguese = proposal.comparisonList.first { it.disciplineId == 10L }
        val compConst = proposal.comparisonList.first { it.disciplineId == 20L }
        assertEquals(60, compPortuguese.proposedMinutesPerDay)
        assertEquals(60, compConst.proposedMinutesPerDay)
    }

    @Test
    fun `proposeRecalculation should fail when no active schedule exists for contest`() = runTest {
        scheduleRepository.currentSchedule = null

        val result = useCase(contestId = 100L, disciplinePerformance = emptyMap())

        assertTrue(result is Result.Error)
        assertEquals("Não há um cronograma ativo para este concurso.", (result as Result.Error).message)
    }

    @Test
    fun `proposeRecalculation should fail when contest has no disciplines registered`() = runTest {
        disciplineRepository.disciplines = emptyList()

        val result = useCase(contestId = 100L, disciplinePerformance = emptyMap())

        assertTrue(result is Result.Error)
        assertEquals("Não foram encontradas disciplinas para este concurso.", (result as Result.Error).message)
    }
}
