package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.rememberflash.app.data.local.database.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    @Upsert
    suspend fun insert(topic: TopicEntity): Long

    @Upsert
    suspend fun insertAll(topics: List<TopicEntity>): List<Long>

    @Update
    suspend fun update(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun delete(topicId: Long)

    @Query("DELETE FROM topics WHERE discipline_id = :disciplineId")
    suspend fun deleteByDiscipline(disciplineId: Long)

    @Query("DELETE FROM topics WHERE contest_id = :contestId")
    suspend fun deleteByContest(contestId: Long)

    @Query("SELECT * FROM topics WHERE discipline_id = :disciplineId ORDER BY order_index ASC, id ASC")
    fun getByDisciplineFlow(disciplineId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE discipline_id = :disciplineId ORDER BY order_index ASC, id ASC")
    suspend fun getByDiscipline(disciplineId: Long): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE contest_id = :contestId ORDER BY discipline_id ASC, order_index ASC")
    fun getByContestFlow(contestId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :topicId LIMIT 1")
    suspend fun getById(topicId: Long): TopicEntity?

    @Query("SELECT COUNT(*) FROM topics WHERE discipline_id = :disciplineId")
    suspend fun countTotalByDiscipline(disciplineId: Long): Int

    @Query("SELECT * FROM topics WHERE is_synced = 0")
    suspend fun getUnsyncedTopics(): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<TopicEntity>

    @Query("SELECT * FROM topics")
    suspend fun getAll(): List<TopicEntity>

    @Query("UPDATE topics SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markTopicsAsSynced(ids: List<Long>)
}
