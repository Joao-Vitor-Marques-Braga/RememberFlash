package com.rememberflash.app.presentation.settings

data class SettingsUiState(
    val difficulty: String = "Médio",
    val rigor: String = "Padrão",
    val tone: String = "Explicativo",
    val apiKey: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
