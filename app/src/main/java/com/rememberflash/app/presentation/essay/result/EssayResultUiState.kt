package com.rememberflash.app.presentation.essay.result

import com.rememberflash.app.domain.model.Essay

data class EssayResultUiState(
    val essay: Essay? = null,
    val parsedFeedback: ParsedEssayFeedback? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ParsedEssayFeedback(
    val overallScore: Double = 0.0,
    val competencies: List<CompetenceFeedback> = emptyList(),
    val strengths: List<String> = emptyList(),
    val improvements: List<String> = emptyList()
)

data class CompetenceFeedback(
    val name: String,
    val score: Double,
    val comment: String
)
