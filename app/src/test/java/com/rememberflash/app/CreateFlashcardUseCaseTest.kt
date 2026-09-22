package com.rememberflash.app

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.FlashcardSource
import com.rememberflash.app.domain.repository.FlashcardRepository
import com.rememberflash.app.domain.usecase.flashcard.CreateFlashcardUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateFlashcardUseCaseTest {

    private lateinit var fakeFlashcardRepository: FakeFlashcardRepository
    private lateinit var useCase: CreateFlashcardUseCase

    class FakeFlashcardRepository : FlashcardRepository {
        val flashcards = mutableListOf<Flashcard>()
        private val flow = MutableStateFlow<List<Flashcard>>(emptyList())

        override suspend fun insert(flashcard: Flashcard): Result<Long> {
            val id = (flashcards.size + 1).toLong()
            flashcards.add(flashcard.copy(id = id))
            flow.value = flashcards.toList()
            return Result.success(id)
        }

        override suspend fun insertAll(flashcardsToInsert: List<Flashcard>): Result<List<Long>> {
            val ids = mutableListOf<Long>()
            flashcardsToInsert.forEach {
                val id = (flashcards.size + 1).toLong()
                flashcards.add(it.copy(id = id))
                ids.add(id)
            }
            flow.value = flashcards.toList()
            return Result.success(ids)
        }

        override suspend fun update(flashcard: Flashcard): Result<Unit> = Result.success(Unit)
        override suspend fun delete(flashcardId: Long): Result<Unit> = Result.success(Unit)
        override fun getByDiscipline(disciplineId: Long): Flow<List<Flashcard>> = flow
        override fun getDueForReview(disciplineId: Long, now: Long): Flow<List<Flashcard>> = flow
        override suspend fun updateReviewMetrics(
            flashcardId: Long,
            easeFactor: Double,
            interval: Int,
            repetitions: Int,
            nextReviewAt: Long
        ): Result<Unit> = Result.success(Unit)
        override fun getByTopic(topicId: Long): Flow<List<Flashcard>> = flow
        override suspend fun countByTopic(topicId: Long): Result<Int> = Result.success(flashcards.size)
    }

    @Before
    fun setup() {
        fakeFlashcardRepository = FakeFlashcardRepository()
        useCase = CreateFlashcardUseCase(fakeFlashcardRepository)
    }

    @Test
    fun `createFlashcard should fail when front is blank`() = runTest {
        val flashcard = Flashcard(
            disciplineId = 1L,
            front = "   ",
            back = "Verso válido",
            source = FlashcardSource.MANUAL
        )

        val result = useCase(flashcard)

        assertTrue(result is Result.Error)
        assertEquals("A frente do flashcard é obrigatória", (result as Result.Error).message)
        assertEquals(0, fakeFlashcardRepository.flashcards.size)
    }

    @Test
    fun `createFlashcard should fail when back is blank`() = runTest {
        val flashcard = Flashcard(
            disciplineId = 1L,
            front = "Frente válida",
            back = "",
            source = FlashcardSource.MANUAL
        )

        val result = useCase(flashcard)

        assertTrue(result is Result.Error)
        assertEquals("O verso do flashcard é obrigatório", (result as Result.Error).message)
        assertEquals(0, fakeFlashcardRepository.flashcards.size)
    }

    @Test
    fun `createFlashcard should fail when disciplineId is invalid`() = runTest {
        val flashcard = Flashcard(
            disciplineId = 0L,
            front = "Frente válida",
            back = "Verso válido",
            source = FlashcardSource.MANUAL
        )

        val result = useCase(flashcard)

        assertTrue(result is Result.Error)
        assertEquals("Flashcard deve estar vinculado a uma disciplina válida", (result as Result.Error).message)
        assertEquals(0, fakeFlashcardRepository.flashcards.size)
    }

    @Test
    fun `createFlashcard should succeed with valid data and initialize default SM-2 metrics`() = runTest {
        val flashcard = Flashcard(
            disciplineId = 10L,
            topicId = 5L,
            front = "O que é ato administrativo discricionário?",
            back = "Aquele em que a Administração possui certa margem de conveniência e oportunidade.",
            source = FlashcardSource.MANUAL
        )

        val result = useCase(flashcard)

        assertTrue(result is Result.Success)
        assertEquals(1, fakeFlashcardRepository.flashcards.size)

        val saved = fakeFlashcardRepository.flashcards[0]
        assertEquals(10L, saved.disciplineId)
        assertEquals(5L, saved.topicId)
        assertEquals(FlashcardSource.MANUAL, saved.source)
        assertEquals(2.5, saved.easeFactor, 0.001)
        assertEquals(0, saved.interval)
        assertEquals(0, saved.repetitions)
    }
}
