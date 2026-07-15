package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rememberflash.app.data.local.database.entity.ContestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contest: ContestEntity): Long

    @Update
    suspend fun update(contest: ContestEntity)

    @Query("UPDATE contests SET is_active = 0, updated_at = :updatedAt WHERE id = :contestId")
    suspend fun softDelete(contestId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM contests WHERE user_id = :userId AND is_active = 1 ORDER BY updated_at DESC")
    fun getActiveByUser(userId: String): Flow<List<ContestEntity>>

    @Query("SELECT * FROM contests WHERE user_id = :userId ORDER BY updated_at DESC")
    fun getAllByUser(userId: String): Flow<List<ContestEntity>>

    @Query("SELECT * FROM contests WHERE id = :contestId LIMIT 1")
    suspend fun getById(contestId: Long): ContestEntity?

    @Query("SELECT * FROM contests WHERE is_synced = 0")
    suspend fun getUnsyncedContests(): List<ContestEntity>

    @Query("UPDATE contests SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markContestsAsSynced(ids: List<Long>)
}
