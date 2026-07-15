package com.rememberflash.app.presentation.flashcard

import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Flashcard

data class FlashcardDeckUiState(
    val discipline: Discipline? = null,
    val flashcards: List<Flashcard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
