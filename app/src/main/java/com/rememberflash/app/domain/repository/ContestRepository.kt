package com.rememberflash.app.domain.repository

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import kotlinx.coroutines.flow.Flow

interface ContestRepository {
    suspend fun insert(contest: Contest): Result<Long>
    suspend fun update(contest: Contest): Result<Unit>
    suspend fun softDelete(contestId: Long): Result<Unit>
    fun getActiveContestsByUser(userId: String): Flow<List<Contest>>
    suspend fun getById(contestId: Long): Result<Contest>
    fun getAllByUser(userId: String): Flow<List<Contest>>
}
