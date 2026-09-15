package com.rememberflash.app.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.usecase.auth.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val emailArg: String = savedStateHandle.get<String>("email") ?: ""

    private val _uiState = MutableStateFlow(ResetPasswordUiState(email = emailArg))
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun initEmail(email: String) {
        if (_uiState.value.email.isBlank()) {
            _uiState.value = _uiState.value.copy(email = email)
        }
    }

    fun onSecurityCodeChanged(code: String) {
        val digitsOnly = code.filter { it.isDigit() }
        if (digitsOnly.length <= 6) {
            _uiState.value = _uiState.value.copy(securityCode = digitsOnly, error = null)
        }
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(newPassword = password, error = null)
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password, error = null)
    }

    fun toggleNewPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isNewPasswordVisible = !_uiState.value.isNewPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
    }

    fun resetPassword(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.securityCode.length != 6) {
            _uiState.value = _uiState.value.copy(error = "O código deve ter 6 dígitos")
            return
        }

        if (state.newPassword.isBlank() || state.confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Preencha todos os campos de senha")
            return
        }

        if (state.newPassword != state.confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "As senhas não coincidem")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = resetPasswordUseCase(
                email = state.email,
                otp = state.securityCode,
                newPassword = state.newPassword,
                confirmPassword = state.confirmPassword
            )

            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
