package com.rememberflash.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "essays",
    foreignKeys = [
        ForeignKey(
            entity = ContestEntity::class,
            parentColumns = ["id"],
            childColumns = ["contest_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contest_id"), Index("user_id")]
)
data class EssayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "contest_id")
    val contestId: Long,

    val title: String,

    val theme: String = "",

    @ColumnInfo(name = "image_uri")
    val imageUri: String,

    @ColumnInfo(name = "extracted_text")
    val extractedText: String? = null,

    @ColumnInfo(name = "ai_feedback_json")
    val aiFeedbackJson: String? = null,

    val score: Double? = null,

    @ColumnInfo(name = "tokens_spent")
    val tokensSpent: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
