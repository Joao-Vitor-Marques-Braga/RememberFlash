package com.rememberflash.app.domain.model

enum class QuestionSource {
    MANUAL,
    AI_GENERATED
}

data class Question(
    val id: Long = 0L,
    val disciplineId: Long,
    val topicId: Long? = null,
    val statement: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null,
    val source: QuestionSource = QuestionSource.MANUAL,
    val chosenOption: Int? = null,
    val isCorrect: Boolean? = null,
    val answeredAt: Long? = null,
    val tokensSpent: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
