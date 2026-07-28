package com.rememberflash.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = DisciplineEntity::class,
            parentColumns = ["id"],
            childColumns = ["discipline_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("discipline_id"), Index("next_review_at")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "discipline_id")
    val disciplineId: Long,

    val front: String,

    val back: String,

    val source: String = "MANUAL",

    @ColumnInfo(name = "next_review_at")
    val nextReviewAt: Long? = null,

    @ColumnInfo(name = "ease_factor")
    val easeFactor: Double = 2.5,

    val interval: Int = 0,

    val repetitions: Int = 0,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "tokens_spent", defaultValue = "0")
    val tokensSpent: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
