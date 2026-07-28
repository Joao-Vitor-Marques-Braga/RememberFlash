package com.rememberflash.app.presentation.discipline

import android.net.Uri
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.Question

data class DisciplineUiState(
    val discipline: Discipline? = null,
    val flashcards: List<Flashcard> = emptyList(),
    val questions: List<Question> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,

    // PDF States
    val pdfUri: Uri? = null,
    val pdfName: String = "",
    val pdfSize: Long = 0L,
    val pdfError: String? = null,
    val isGeneratingFlashcards: Boolean = false,

    // Question Gen States
    val isGeneratingQuestions: Boolean = false,
    val isQuestionsGeneratedSuccess: Boolean = false,

    val attempts: List<com.rememberflash.app.domain.model.MockExamAttempt> = emptyList()
)
