package com.rememberflash.app.domain.repository

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.StudySchedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    suspend fun insert(schedule: StudySchedule): Result<Long>
    suspend fun update(schedule: StudySchedule): Result<Unit>
    suspend fun getByContest(contestId: Long): Result<StudySchedule?>
    fun getDailyGoalsBySchedule(scheduleId: Long): Flow<List<DailyGoal>>
    fun getAllDailyGoalsFlow(): Flow<List<DailyGoal>>
    suspend fun insertDailyGoals(goals: List<DailyGoal>): Result<Unit>
    suspend fun updateDailyGoalProgress(
        goalId: Long,
        completedMinutes: Int,
        flashcardsCompleted: Int
    ): Result<Unit>
    suspend fun clearDailyGoalsBySchedule(scheduleId: Long): Result<Unit>
}
