package com.rememberflash.app.domain.model

enum class QuestionSource {
    MANUAL,
    AI_GENERATED
}

data class Question(
    val id: Long = 0L,
    val disciplineId: Long,
    val statement: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null,
    val source: QuestionSource = QuestionSource.MANUAL,
    val createdAt: Long = System.currentTimeMillis()
)
