package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.rememberflash.app.data.local.database.entity.DisciplineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DisciplineDao {

    @Upsert
    suspend fun insert(discipline: DisciplineEntity): Long

    @Upsert
    suspend fun insertAll(disciplines: List<DisciplineEntity>): List<Long>

    @Update
    suspend fun update(discipline: DisciplineEntity)

    @Query("DELETE FROM disciplines WHERE id = :disciplineId")
    suspend fun delete(disciplineId: Long)

    @Query("DELETE FROM disciplines WHERE contest_id = :contestId")
    suspend fun deleteByContest(contestId: Long)

    @Query("SELECT * FROM disciplines WHERE contest_id = :contestId AND is_active = 1 ORDER BY weight DESC, name ASC")
    fun getByContest(contestId: Long): Flow<List<DisciplineEntity>>

    @Query("SELECT * FROM disciplines WHERE id = :disciplineId LIMIT 1")
    suspend fun getById(disciplineId: Long): DisciplineEntity?

    @Query("SELECT * FROM disciplines WHERE is_synced = 0")
    suspend fun getUnsyncedDisciplines(): List<DisciplineEntity>

    @Query("SELECT * FROM disciplines WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<DisciplineEntity>

    @Query("SELECT * FROM disciplines")
    suspend fun getAll(): List<DisciplineEntity>

    @Query("UPDATE disciplines SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markDisciplinesAsSynced(ids: List<Long>)

    @Query("SELECT * FROM disciplines WHERE is_active = 1")
    fun getAllDisciplines(): Flow<List<DisciplineEntity>>
}
