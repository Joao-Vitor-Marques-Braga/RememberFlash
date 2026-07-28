package com.rememberflash.app.domain.model

data class MockExamAttempt(
    val id: Long = 0L,
    val contestId: Long? = null,
    val disciplineId: Long? = null,
    val score: Int,
    val totalQuestions: Int,
    val answersJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
