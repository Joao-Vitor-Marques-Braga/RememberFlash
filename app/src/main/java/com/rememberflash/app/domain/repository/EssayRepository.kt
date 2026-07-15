package com.rememberflash.app.domain.repository

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Essay
import kotlinx.coroutines.flow.Flow

interface EssayRepository {
    suspend fun insert(essay: Essay): Result<Long>
    suspend fun updateWithExtractedText(essayId: Long, text: String): Result<Unit>
    suspend fun updateWithAiFeedback(
        essayId: Long,
        feedbackJson: String,
        score: Double
    ): Result<Unit>
    fun getByUser(userId: String): Flow<List<Essay>>
    fun getByContest(contestId: Long): Flow<List<Essay>>
    suspend fun getById(essayId: Long): Result<Essay>
}
