package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.rememberflash.app.data.local.database.entity.DailyGoalEntity
import com.rememberflash.app.data.local.database.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Upsert
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Query("SELECT * FROM study_schedules WHERE contest_id = :contestId LIMIT 1")
    suspend fun getByContest(contestId: Long): ScheduleEntity?

    @Upsert
    suspend fun insertDailyGoals(goals: List<DailyGoalEntity>)

    @Query("SELECT * FROM daily_goals WHERE schedule_id = :scheduleId ORDER BY date ASC, discipline_id ASC")
    fun getDailyGoalsBySchedule(scheduleId: Long): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM daily_goals")
    fun getAllDailyGoalsFlow(): Flow<List<DailyGoalEntity>>

    @Query(
        """
        UPDATE daily_goals SET 
            completed_minutes = :completedMinutes, 
            flashcards_completed = :flashcardsCompleted 
        WHERE id = :goalId
        """
    )
    suspend fun updateDailyGoalProgress(
        goalId: Long,
        completedMinutes: Int,
        flashcardsCompleted: Int
    )

    @Query("DELETE FROM daily_goals WHERE schedule_id = :scheduleId")
    suspend fun clearDailyGoalsBySchedule(scheduleId: Long)
}
