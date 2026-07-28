package com.rememberflash.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_schedules",
    foreignKeys = [
        ForeignKey(
            entity = ContestEntity::class,
            parentColumns = ["id"],
            childColumns = ["contest_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contest_id")]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "contest_id")
    val contestId: Long,

    @ColumnInfo(name = "exam_date")
    val examDate: Long,

    @ColumnInfo(name = "available_hours_per_day")
    val availableHoursPerDay: Double,

    @ColumnInfo(name = "rest_days_per_week")
    val restDaysPerWeek: Int = 1,

    @ColumnInfo(name = "tokens_spent")
    val tokensSpent: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_recalculated_at")
    val lastRecalculatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_goals",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DisciplineEntity::class,
            parentColumns = ["id"],
            childColumns = ["discipline_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("schedule_id"), Index("discipline_id"), Index("date")]
)
data class DailyGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "schedule_id")
    val scheduleId: Long,

    val date: Long,

    @ColumnInfo(name = "discipline_id")
    val disciplineId: Long,

    @ColumnInfo(name = "target_minutes")
    val targetMinutes: Int,

    @ColumnInfo(name = "completed_minutes")
    val completedMinutes: Int = 0,

    @ColumnInfo(name = "flashcards_target")
    val flashcardsTarget: Int = 0,

    @ColumnInfo(name = "flashcards_completed")
    val flashcardsCompleted: Int = 0
)
