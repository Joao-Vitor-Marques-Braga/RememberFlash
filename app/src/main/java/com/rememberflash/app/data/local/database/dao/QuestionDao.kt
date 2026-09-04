package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rememberflash.app.data.local.database.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>): List<Long>

    @Query("SELECT * FROM questions WHERE discipline_id = :disciplineId ORDER BY created_at DESC")
    fun getByDiscipline(disciplineId: Long): Flow<List<QuestionEntity>>

    @Query("DELETE FROM questions WHERE discipline_id = :disciplineId")
    suspend fun deleteByDiscipline(disciplineId: Long)

    @Query("UPDATE questions SET chosen_option = :chosenOption, is_correct = :isCorrect, answered_at = :answeredAt WHERE id = :questionId")
    suspend fun updateAnswer(questionId: Long, chosenOption: Int, isCorrect: Boolean, answeredAt: Long)

    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :questionId LIMIT 1")
    suspend fun getById(questionId: Long): QuestionEntity?

    @Query("UPDATE questions SET chosen_option = NULL, is_correct = NULL, answered_at = NULL WHERE discipline_id = :disciplineId")
    suspend fun resetAnswersForDiscipline(disciplineId: Long)

    @Query(
        """
        UPDATE questions SET chosen_option = NULL, is_correct = NULL, answered_at = NULL 
        WHERE discipline_id IN (SELECT id FROM disciplines WHERE contest_id = :contestId)
        """
    )
    suspend fun resetAnswersForContest(contestId: Long)

    @Query("SELECT * FROM questions WHERE topic_id = :topicId ORDER BY created_at DESC")
    fun getByTopic(topicId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions WHERE topic_id = :topicId")
    suspend fun countByTopic(topicId: Long): Int
}
