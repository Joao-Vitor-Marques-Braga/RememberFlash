package com.rememberflash.app.presentation.auth

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val generatedOtp: String? = null,
    val error: String? = null
)
