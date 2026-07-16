package com.rememberflash.app.domain.repository

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Discipline
import kotlinx.coroutines.flow.Flow

interface DisciplineRepository {
    suspend fun insert(discipline: Discipline): Result<Long>
    suspend fun update(discipline: Discipline): Result<Unit>
    suspend fun delete(disciplineId: Long): Result<Unit>
    fun getByContest(contestId: Long): Flow<List<Discipline>>
    suspend fun getById(disciplineId: Long): Result<Discipline>
    fun getAllDisciplines(): Flow<List<Discipline>>
}
