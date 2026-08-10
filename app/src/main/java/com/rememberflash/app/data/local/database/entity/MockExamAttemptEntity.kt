package com.rememberflash.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_exam_attempts")
data class MockExamAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "contest_id")
    val contestId: Long? = null,

    @ColumnInfo(name = "discipline_id")
    val disciplineId: Long? = null,

    val score: Int,

    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int,

    @ColumnInfo(name = "answers_json")
    val answersJson: String,

    @ColumnInfo(name = "times_json")
    val timesJson: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
