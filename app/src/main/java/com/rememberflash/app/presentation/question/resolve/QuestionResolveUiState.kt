package com.rememberflash.app.presentation.question.resolve

import com.rememberflash.app.domain.model.Question

data class QuestionResolveUiState(
    val disciplineName: String = "",
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> chosenIndex
    val submittedAnswers: Set<Int> = emptySet(), // questionIndex -> hasChecked
    val score: Int = 0,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val questionTimes: Map<Long, Int> = emptyMap(), // questionId -> timeSpentSeconds
    val currentQuestionStartTime: Long = 0L
)
