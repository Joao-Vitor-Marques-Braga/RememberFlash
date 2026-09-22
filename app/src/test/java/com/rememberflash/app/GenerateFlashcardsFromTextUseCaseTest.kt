package com.rememberflash.app

import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.FlashcardSource
import com.rememberflash.app.domain.repository.FlashcardRepository
import com.rememberflash.app.domain.usecase.flashcard.GenerateFlashcardsFromTextUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GenerateFlashcardsFromTextUseCaseTest {

    private lateinit var fakeFlashcardRepository: FakeFlashcardRepository
    private lateinit var mockGeminiClient: GeminiClient
    private lateinit var useCase: GenerateFlashcardsFromTextUseCase

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
            nextReviewAt: Long,
        ): Result<Unit> = Result.success(Unit)

        override fun getByTopic(topicId: Long): Flow<List<Flashcard>> = flow

        override suspend fun countByTopic(topicId: Long): Result<Int> =
            Result.success(flashcards.size)
    }

    @Before
    fun setup() {
        fakeFlashcardRepository = FakeFlashcardRepository()
        mockGeminiClient = mock(GeminiClient::class.java)
        useCase = GenerateFlashcardsFromTextUseCase(mockGeminiClient, fakeFlashcardRepository)
    }

    @Test
    fun `useCase should return error for text shorter than 50 chars`() = runTest {
        val shortText = "Texto curto de PDF escaneado"
        val result = useCase(text = shortText, disciplineId = 1L, quantity = 5, topicId = 2L)

        assertTrue(result is Result.Error)
        assertEquals(
            "O PDF selecionado não contém texto legível (documento escaneado). Envie um PDF com camada de texto",
            (result as Result.Error).message,
        )
        assertEquals(0, fakeFlashcardRepository.flashcards.size)
    }

    @Test
    fun `useCase should generate flashcards and persist with correct discipline and topic`() =
        runTest {
            val validText =
                """
                A República Federativa do Brasil rege-se nas suas relações internacionais pelos seguintes princípios:
                soberania, dignidade da pessoa humana e valores sociais do trabalho.
                """
            val mockJsonResponse =
                """
                {
                    "flashcards": [
                        {"frente": "O que é a República Federativa?", "verso": "Forma de Estado brasileira baseada na união indissolúvel."},
                        {"frente": "Quais são os princípios fundamentais?", "verso": "Soberania, dignidade humana, valores sociais do trabalho."}
                    ]
                }
                """
                    .trimIndent()

            `when`(mockGeminiClient.extractFlashcardsFromText(validText))
                .thenReturn(mockJsonResponse)

            val result = useCase(text = validText, disciplineId = 10L, quantity = 5, topicId = 20L)

            assertTrue(result is Result.Success)
            assertEquals(2, fakeFlashcardRepository.flashcards.size)
            assertEquals(10L, fakeFlashcardRepository.flashcards[0].disciplineId)
            assertEquals(20L, fakeFlashcardRepository.flashcards[0].topicId)
            assertEquals(FlashcardSource.PDF_EXTRACT, fakeFlashcardRepository.flashcards[0].source)
            assertEquals(
                "O que é a República Federativa?",
                fakeFlashcardRepository.flashcards[0].front,
            )
        }

    @Test
    fun `useCase should respect quantity limit`() = runTest {
        val validText =
            "Este é um texto analítico longo sobre direito administrativo com detalhes suficientes para ultrapassar 50 caracteres."
        val mockJsonResponse =
            """
            {
                "flashcards": [
                    {"frente": "Card 1", "verso": "Resp 1"},
                    {"frente": "Card 2", "verso": "Resp 2"},
                    {"frente": "Card 3", "verso": "Resp 3"}
                ]
            }
            """
                .trimIndent()

        `when`(mockGeminiClient.extractFlashcardsFromText(validText)).thenReturn(mockJsonResponse)

        val result = useCase(text = validText, disciplineId = 1L, quantity = 1, topicId = null)

        assertTrue(result is Result.Success)
        assertEquals(1, fakeFlashcardRepository.flashcards.size)
        assertEquals("Card 1", fakeFlashcardRepository.flashcards[0].front)
    }
}
