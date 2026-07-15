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
}
