package com.rememberflash.app.presentation.contest.detail

import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.Discipline

data class ContestDetailUiState(
    val isLoading: Boolean = false,
    val contest: Contest? = null,
    val disciplines: List<Discipline> = emptyList(),
    val error: String? = null,
    val disciplineNameError: String? = null
)
