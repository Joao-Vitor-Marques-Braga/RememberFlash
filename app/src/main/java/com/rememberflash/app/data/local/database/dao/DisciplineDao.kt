package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rememberflash.app.data.local.database.entity.DisciplineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DisciplineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(discipline: DisciplineEntity): Long

    @Update
    suspend fun update(discipline: DisciplineEntity)

    @Query("UPDATE disciplines SET is_active = 0 WHERE id = :disciplineId")
    suspend fun delete(disciplineId: Long)

    @Query("SELECT * FROM disciplines WHERE contest_id = :contestId AND is_active = 1 ORDER BY weight DESC, name ASC")
    fun getByContest(contestId: Long): Flow<List<DisciplineEntity>>

    @Query("SELECT * FROM disciplines WHERE id = :disciplineId LIMIT 1")
    suspend fun getById(disciplineId: Long): DisciplineEntity?

    @Query("SELECT * FROM disciplines WHERE is_synced = 0")
    suspend fun getUnsyncedDisciplines(): List<DisciplineEntity>

    @Query("UPDATE disciplines SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markDisciplinesAsSynced(ids: List<Long>)
}
