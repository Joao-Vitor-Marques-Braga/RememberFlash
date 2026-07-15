package com.rememberflash.app.domain.repository

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    suspend fun insert(flashcard: Flashcard): Result<Long>
    suspend fun insertAll(flashcards: List<Flashcard>): Result<List<Long>>
    suspend fun update(flashcard: Flashcard): Result<Unit>
    suspend fun delete(flashcardId: Long): Result<Unit>
    fun getByDiscipline(disciplineId: Long): Flow<List<Flashcard>>
    fun getDueForReview(disciplineId: Long, now: Long): Flow<List<Flashcard>>
    suspend fun updateReviewMetrics(
        flashcardId: Long,
        easeFactor: Double,
        interval: Int,
        repetitions: Int,
        nextReviewAt: Long
    ): Result<Unit>
}
