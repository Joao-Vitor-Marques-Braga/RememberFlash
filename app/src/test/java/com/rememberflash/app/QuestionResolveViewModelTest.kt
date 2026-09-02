package com.rememberflash.app

import androidx.lifecycle.SavedStateHandle
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.MockExamAttempt
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import com.rememberflash.app.domain.usecase.discipline.GetDisciplineByIdUseCase
import com.rememberflash.app.domain.usecase.question.GetQuestionsByDisciplineUseCase
import com.rememberflash.app.presentation.question.resolve.QuestionResolveViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionResolveViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDiscipline = Discipline(
        id = 10L,
        contestId = 1L,
        name = "Direito Constitucional",
        weight = 5.0
    )

    private val fakeQuestions = listOf(
        Question(
            id = 101L,
            disciplineId = 10L,
            statement = "A Constituição Federal é de 1988?",
            options = listOf("Sim", "Não"),
            correctIndex = 0,
            explanation = "Promulgada em 1988."
        ),
        Question(
            id = 102L,
            disciplineId = 10L,
            statement = "O Brasil é uma monarquia?",
            options = listOf("Sim", "Não"),
            correctIndex = 1,
            explanation = "O Brasil é uma República."
        )
    )

    class FakeDisciplineRepository(val discipline: Discipline) : DisciplineRepository {
        override suspend fun insert(discipline: Discipline): Result<Long> = Result.success(discipline.id)
        override suspend fun update(discipline: Discipline): Result<Unit> = Result.success(Unit)
        override suspend fun delete(disciplineId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun getById(disciplineId: Long): Result<Discipline> = Result.success(discipline)
        override fun getByContest(contestId: Long): Flow<List<Discipline>> = flowOf(listOf(discipline))
        override fun getAllDisciplines(): Flow<List<Discipline>> = flowOf(listOf(discipline))
    }

    class FakeQuestionRepository(val questions: List<Question>) : QuestionRepository {
        var lastAnsweredQuestionId: Long? = null
        var lastAnsweredOption: Int? = null
        var lastAnsweredIsCorrect: Boolean? = null
        var savedAttempt: MockExamAttempt? = null
        var resetCalled = false

        override fun getQuestionsByDiscipline(disciplineId: Long): Flow<List<Question>> = flowOf(questions)
        override suspend fun saveQuestions(questions: List<Question>): Result<Unit> = Result.success(Unit)
        override suspend fun clearQuestionsByDiscipline(disciplineId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun answerQuestion(questionId: Long, chosenOption: Int, isCorrect: Boolean): Result<Unit> {
            lastAnsweredQuestionId = questionId
            lastAnsweredOption = chosenOption
            lastAnsweredIsCorrect = isCorrect
            return Result.success(Unit)
        }
        override fun getAllQuestions(): Flow<List<Question>> = flowOf(questions)
        override suspend fun getQuestionById(questionId: Long): Result<Question> = Result.success(questions.first { it.id == questionId })
        override fun getAttemptsByDiscipline(disciplineId: Long): Flow<List<MockExamAttempt>> = flowOf(emptyList())
        override fun getAttemptsByContest(contestId: Long): Flow<List<MockExamAttempt>> = flowOf(emptyList())
        override fun getAllAttempts(): Flow<List<MockExamAttempt>> = flowOf(emptyList())
        override suspend fun saveMockExamAttempt(attempt: MockExamAttempt): Result<Long> {
            savedAttempt = attempt
            return Result.success(1L)
        }
        override suspend fun resetQuestionsForDiscipline(disciplineId: Long): Result<Unit> {
            resetCalled = true
            return Result.success(Unit)
        }
        override suspend fun resetQuestionsForContest(contestId: Long): Result<Unit> = Result.success(Unit)
    }

    private lateinit var fakeDisciplineRepo: FakeDisciplineRepository
    private lateinit var fakeQuestionRepo: FakeQuestionRepository
    private lateinit var viewModel: QuestionResolveViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDisciplineRepo = FakeDisciplineRepository(fakeDiscipline)
        fakeQuestionRepo = FakeQuestionRepository(fakeQuestions)

        val getDisciplineByIdUseCase = GetDisciplineByIdUseCase(fakeDisciplineRepo)
        val getQuestionsByDisciplineUseCase = GetQuestionsByDisciplineUseCase(fakeQuestionRepo)
        val savedStateHandle = SavedStateHandle(mapOf("disciplineId" to 10L))

        viewModel = QuestionResolveViewModel(
            getDisciplineByIdUseCase = getDisciplineByIdUseCase,
            getQuestionsByDisciplineUseCase = getQuestionsByDisciplineUseCase,
            questionRepository = fakeQuestionRepo,
            savedStateHandle = savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load discipline and questions correctly`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Direito Constitucional", state.disciplineName)
        assertEquals(2, state.questions.size)
        assertEquals(0, state.currentIndex)
        assertTrue(fakeQuestionRepo.resetCalled)
    }

    @Test
    fun `selectOption should update selectedAnswers map`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.selectOption(0)
        val state = viewModel.uiState.value
        assertEquals(0, state.selectedAnswers[0])
    }

    @Test
    fun `submitAnswer should verify correct answer and increment score`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.selectOption(0) // Option 0 is correct for question 1
        viewModel.submitAnswer()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.score)
        assertTrue(state.submittedAnswers.contains(0))
        assertEquals(101L, fakeQuestionRepo.lastAnsweredQuestionId)
        assertEquals(0, fakeQuestionRepo.lastAnsweredOption)
        assertEquals(true, fakeQuestionRepo.lastAnsweredIsCorrect)
    }

    @Test
    fun `submitAnswer should handle incorrect answer without incrementing score`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.selectOption(1) // Option 1 is wrong for question 1
        viewModel.submitAnswer()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.score)
        assertTrue(state.submittedAnswers.contains(0))
        assertEquals(false, fakeQuestionRepo.lastAnsweredIsCorrect)
    }

    @Test
    fun `nextQuestion should advance to next question and complete on last question`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        // Question 1
        viewModel.selectOption(0)
        viewModel.submitAnswer()
        viewModel.nextQuestion()

        var state = viewModel.uiState.value
        assertEquals(1, state.currentIndex)
        assertFalse(state.isFinished)

        // Question 2
        viewModel.selectOption(1)
        viewModel.submitAnswer()
        viewModel.nextQuestion() // Should finish the mock exam

        testScheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(2, state.score)
        assertNotNull(fakeQuestionRepo.savedAttempt)
        assertEquals(2, fakeQuestionRepo.savedAttempt?.score)
        assertEquals(2, fakeQuestionRepo.savedAttempt?.totalQuestions)
    }
}
