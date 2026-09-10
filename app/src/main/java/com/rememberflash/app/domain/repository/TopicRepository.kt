package com.rememberflash.app.domain.repository

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Topic
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    suspend fun insert(topic: Topic): Result<Long>
    suspend fun insertAll(topics: List<Topic>): Result<List<Long>>
    suspend fun update(topic: Topic): Result<Unit>
    suspend fun delete(topicId: Long): Result<Unit>
    suspend fun deleteByDiscipline(disciplineId: Long): Result<Unit>
    fun getByDisciplineFlow(disciplineId: Long): Flow<List<Topic>>
    suspend fun getByDiscipline(disciplineId: Long): Result<List<Topic>>
    fun getByContestFlow(contestId: Long): Flow<List<Topic>>
    suspend fun getById(topicId: Long): Result<Topic>
    suspend fun syncDisciplineTopicCounters(disciplineId: Long): Result<Unit>
}
