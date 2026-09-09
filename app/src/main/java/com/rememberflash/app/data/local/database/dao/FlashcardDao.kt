package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.rememberflash.app.data.local.database.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Upsert
    suspend fun insert(flashcard: FlashcardEntity): Long

    @Upsert
    suspend fun insertAll(flashcards: List<FlashcardEntity>): List<Long>

    @Update
    suspend fun update(flashcard: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :flashcardId")
    suspend fun delete(flashcardId: Long)

    @Query("DELETE FROM flashcards WHERE discipline_id = :disciplineId")
    suspend fun deleteByDiscipline(disciplineId: Long)

    @Query("SELECT * FROM flashcards WHERE discipline_id = :disciplineId ORDER BY created_at DESC")
    fun getByDiscipline(disciplineId: Long): Flow<List<FlashcardEntity>>

    @Query(
        """
        SELECT * FROM flashcards 
        WHERE discipline_id = :disciplineId 
        AND (next_review_at IS NULL OR next_review_at <= :now)
        ORDER BY next_review_at ASC
        """
    )
    fun getDueForReview(disciplineId: Long, now: Long): Flow<List<FlashcardEntity>>

    @Query(
        """
        UPDATE flashcards SET 
            ease_factor = :easeFactor, 
            interval = :interval, 
            repetitions = :repetitions, 
            next_review_at = :nextReviewAt,
            is_synced = 0 
        WHERE id = :flashcardId
        """
    )
    suspend fun updateReviewMetrics(
        flashcardId: Long,
        easeFactor: Double,
        interval: Int,
        repetitions: Int,
        nextReviewAt: Long
    )

    @Query("SELECT * FROM flashcards WHERE is_synced = 0")
    suspend fun getUnsyncedFlashcards(): List<FlashcardEntity>

    @Query("UPDATE flashcards SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markFlashcardsAsSynced(ids: List<Long>)

    @Query("SELECT * FROM flashcards WHERE topic_id = :topicId ORDER BY created_at DESC")
    fun getByTopic(topicId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE topic_id = :topicId")
    suspend fun countByTopic(topicId: Long): Int
}
