package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rememberflash.app.data.local.database.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<FlashcardEntity>): List<Long>

    @Update
    suspend fun update(flashcard: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :flashcardId")
    suspend fun delete(flashcardId: Long)

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
            next_review_at = :nextReviewAt 
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
}
