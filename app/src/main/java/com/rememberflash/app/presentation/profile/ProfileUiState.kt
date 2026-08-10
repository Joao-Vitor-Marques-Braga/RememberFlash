package com.rememberflash.app.presentation.profile

import com.rememberflash.app.domain.model.User

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val currentPasswordText: String = "",
    val newPasswordText: String = "",
    val confirmPasswordText: String = "",
    val isChangingPasswordLoading: Boolean = false,
    val passwordChangeError: String? = null,
    val passwordChangeSuccess: Boolean = false,
    val isLoggedOut: Boolean = false
)
