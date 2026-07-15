package com.rememberflash.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "disciplines",
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
data class DisciplineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "contest_id")
    val contestId: Long,

    val name: String,

    val weight: Double = 1.0,

    @ColumnInfo(name = "total_topics")
    val totalTopics: Int = 0,

    @ColumnInfo(name = "completed_topics")
    val completedTopics: Int = 0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
