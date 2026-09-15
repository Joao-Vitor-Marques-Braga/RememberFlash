package com.rememberflash.app.presentation.auth

data class ResetPasswordUiState(
    val email: String = "",
    val securityCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
