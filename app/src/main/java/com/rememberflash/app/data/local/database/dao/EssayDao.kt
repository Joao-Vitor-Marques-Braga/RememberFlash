package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rememberflash.app.data.local.database.entity.EssayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EssayDao {

    @Upsert
    suspend fun insert(essay: EssayEntity): Long

    @Query("UPDATE essays SET extracted_text = :text WHERE id = :essayId")
    suspend fun updateExtractedText(essayId: Long, text: String)

    @Query(
        """
        UPDATE essays SET 
            ai_feedback_json = :feedbackJson, 
            score = :score,
            tokens_spent = :tokensSpent
        WHERE id = :essayId
        """
    )
    suspend fun updateAiFeedback(essayId: Long, feedbackJson: String, score: Double, tokensSpent: Int)

    @Query("SELECT * FROM essays WHERE user_id = :userId ORDER BY created_at DESC")
    fun getByUser(userId: String): Flow<List<EssayEntity>>

    @Query("SELECT * FROM essays WHERE id = :essayId LIMIT 1")
    suspend fun getById(essayId: Long): EssayEntity?

    @Query("SELECT * FROM essays WHERE contest_id = :contestId ORDER BY created_at DESC")
    fun getByContest(contestId: Long): Flow<List<EssayEntity>>
}
