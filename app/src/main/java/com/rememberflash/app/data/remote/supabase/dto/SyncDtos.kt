package com.rememberflash.app.data.remote.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSupabaseDto(
    val id: String,
    val name: String,
    val email: String,
    val cpf: String,
    @SerialName("password_hash") val passwordHash: String,
    @SerialName("gemini_api_key") val geminiApiKey: String? = null
)

@Serializable
data class ContestSupabaseDto(
    val id: Long? = null,
    @SerialName("user_id") val userId: String,
    val title: String,
    val description: String? = "",
    @SerialName("organizer_name") val organizerName: String? = "",
    @SerialName("question_type") val questionType: String? = "Múltipla Escolha",
    @SerialName("syllabus_pdf_uri") val syllabusPdfUri: String? = null,
    @SerialName("exam_date_str") val examDateStr: String? = null,
    @SerialName("exam_location") val examLocation: String? = null,
    @SerialName("allowed_pen") val allowedPen: String? = null,
    @SerialName("allowed_items") val allowedItems: String? = null,
    @SerialName("prohibited_items") val prohibitedItems: String? = null,
    @SerialName("ai_difficulty") val aiDifficulty: String? = "Médio",
    @SerialName("ai_rigor") val aiRigor: String? = "Padrão",
    @SerialName("ai_tone") val aiTone: String? = "Explicativo",
    @SerialName("is_active") val isActive: Boolean? = true
)

@Serializable
data class DisciplineSupabaseDto(
    val id: Long? = null,
    @SerialName("contest_id") val contestId: Long,
    val name: String,
    val weight: Double? = 1.0,
    @SerialName("total_topics") val totalTopics: Int? = 0,
    @SerialName("completed_topics") val completedTopics: Int? = 0,
    @SerialName("is_active") val isActive: Boolean? = true
)

@Serializable
data class TopicSupabaseDto(
    val id: Long? = null,
    @SerialName("discipline_id") val disciplineId: Long,
    @SerialName("contest_id") val contestId: Long,
    val name: String,
    val description: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean? = false,
    @SerialName("order_index") val orderIndex: Int? = 0,
    @SerialName("created_at") val createdAt: Long? = System.currentTimeMillis()
)

@Serializable
data class FlashcardSupabaseDto(
    val id: Long? = null,
    @SerialName("discipline_id") val disciplineId: Long,
    @SerialName("topic_id") val topicId: Long? = null,
    val front: String,
    val back: String,
    val source: String? = "MANUAL",
    @SerialName("next_review_at") val nextReviewAt: Long? = null,
    @SerialName("ease_factor") val easeFactor: Double? = 2.5,
    val interval: Int? = 0,
    val repetitions: Int? = 0,
    @SerialName("tokens_spent") val tokensSpent: Int? = 0,
    @SerialName("created_at") val createdAt: Long? = System.currentTimeMillis()
)

@Serializable
data class QuestionSupabaseDto(
    val id: Long? = null,
    @SerialName("discipline_id") val disciplineId: Long,
    @SerialName("topic_id") val topicId: Long? = null,
    val statement: String,
    @SerialName("options_json") val optionsJson: String,
    @SerialName("correct_index") val correctIndex: Int,
    val explanation: String? = null,
    val source: String? = "MANUAL",
    @SerialName("chosen_option") val chosenOption: Int? = null,
    @SerialName("is_correct") val isCorrect: Boolean? = null,
    @SerialName("answered_at") val answeredAt: Long? = null,
    @SerialName("tokens_spent") val tokensSpent: Int? = 0,
    @SerialName("created_at") val createdAt: Long? = System.currentTimeMillis()
)

@Serializable
data class StudyScheduleSupabaseDto(
    val id: Long? = null,
    @SerialName("contest_id") val contestId: Long,
    @SerialName("exam_date") val examDate: String? = null,
    @SerialName("available_hours_per_day") val availableHoursPerDay: Double? = 2.0,
    @SerialName("rest_days_per_week") val restDaysPerWeek: Int? = 1,
    @SerialName("tokens_spent") val tokensSpent: Int? = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_recalculated_at") val lastRecalculatedAt: String? = null
)

@Serializable
data class DailyGoalSupabaseDto(
    val id: Long? = null,
    @SerialName("schedule_id") val scheduleId: Long,
    @SerialName("discipline_id") val disciplineId: Long,
    val date: String,
    @SerialName("target_minutes") val targetMinutes: Int,
    @SerialName("completed_minutes") val completedMinutes: Int? = 0,
    @SerialName("flashcards_target") val flashcardsTarget: Int? = 0,
    @SerialName("flashcards_completed") val flashcardsCompleted: Int? = 0
)
