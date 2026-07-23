package com.rememberflash.app.presentation.tutor

data class TutorMessage(
    val text: String,
    val isUser: Boolean
)

data class TutorChatUiState(
    val messages: List<TutorMessage> = emptyList(),
    val input: String = "",
    val isThinking: Boolean = false,
    val error: String? = null,
    val activeContextTitle: String = "Tutor Particular"
)
