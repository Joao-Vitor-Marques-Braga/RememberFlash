package com.rememberflash.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contests")
data class ContestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: String,

    val title: String,

    val description: String = "",

    @ColumnInfo(name = "organizer_name")
    val organizerName: String = "",

    @ColumnInfo(name = "question_type", defaultValue = "'Múltipla Escolha'")
    val questionType: String = "Múltipla Escolha",

    @ColumnInfo(name = "syllabus_pdf_uri")
    val syllabusPdfUri: String? = null,

    @ColumnInfo(name = "exam_date")
    val examDate: Long? = null,

    @ColumnInfo(name = "exam_date_str")
    val examDateStr: String? = null,

    @ColumnInfo(name = "exam_location")
    val examLocation: String? = null,

    @ColumnInfo(name = "allowed_pen")
    val allowedPen: String? = null,

    @ColumnInfo(name = "allowed_items")
    val allowedItems: String? = null,

    @ColumnInfo(name = "prohibited_items")
    val prohibitedItems: String? = null,

    @ColumnInfo(name = "ai_difficulty", defaultValue = "'Médio'")
    val aiDifficulty: String = "Médio",

    @ColumnInfo(name = "ai_rigor", defaultValue = "'Padrão'")
    val aiRigor: String = "Padrão",

    @ColumnInfo(name = "ai_tone", defaultValue = "'Explicativo'")
    val aiTone: String = "Explicativo",

    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
