package com.rememberflash.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = DisciplineEntity::class,
            parentColumns = ["id"],
            childColumns = ["discipline_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topic_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("discipline_id"), Index("topic_id")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "discipline_id")
    val disciplineId: Long,

    @ColumnInfo(name = "topic_id")
    val topicId: Long? = null,

    val statement: String,

    @ColumnInfo(name = "options_json")
    val optionsJson: String,

    @ColumnInfo(name = "correct_index")
    val correctIndex: Int,

    val explanation: String? = null,

    val source: String = "MANUAL",

    @ColumnInfo(name = "chosen_option")
    val chosenOption: Int? = null,

    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean? = null,

    @ColumnInfo(name = "answered_at")
    val answeredAt: Long? = null,

    @ColumnInfo(name = "tokens_spent")
    val tokensSpent: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
