package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rememberflash.app.data.local.database.entity.MockExamAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MockExamAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: MockExamAttemptEntity): Long

    @Query("SELECT * FROM mock_exam_attempts WHERE discipline_id = :disciplineId ORDER BY created_at DESC")
    fun getAttemptsByDiscipline(disciplineId: Long): Flow<List<MockExamAttemptEntity>>

    @Query("SELECT * FROM mock_exam_attempts WHERE contest_id = :contestId ORDER BY created_at DESC")
    fun getAttemptsByContest(contestId: Long): Flow<List<MockExamAttemptEntity>>
}
