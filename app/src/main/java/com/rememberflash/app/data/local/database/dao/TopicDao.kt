package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rememberflash.app.data.local.database.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(topics: List<TopicEntity>): List<Long>

    @Update
    suspend fun update(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun delete(topicId: Long)

    @Query("DELETE FROM topics WHERE discipline_id = :disciplineId")
    suspend fun deleteByDiscipline(disciplineId: Long)

    @Query("SELECT * FROM topics WHERE discipline_id = :disciplineId ORDER BY order_index ASC, id ASC")
    fun getByDisciplineFlow(disciplineId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE discipline_id = :disciplineId ORDER BY order_index ASC, id ASC")
    suspend fun getByDiscipline(disciplineId: Long): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE contest_id = :contestId ORDER BY discipline_id ASC, order_index ASC")
    fun getByContestFlow(contestId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :topicId LIMIT 1")
    suspend fun getById(topicId: Long): TopicEntity?

    @Query("UPDATE topics SET is_completed = :isCompleted WHERE id = :topicId")
    suspend fun setCompletion(topicId: Long, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM topics WHERE discipline_id = :disciplineId")
    suspend fun countTotalByDiscipline(disciplineId: Long): Int

    @Query("SELECT COUNT(*) FROM topics WHERE discipline_id = :disciplineId AND is_completed = 1")
    suspend fun countCompletedByDiscipline(disciplineId: Long): Int
}
