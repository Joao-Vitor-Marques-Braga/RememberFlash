package com.rememberflash.app.presentation.auth

data class RegisterUiState(
    val name: String = "",
    val cpf: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
