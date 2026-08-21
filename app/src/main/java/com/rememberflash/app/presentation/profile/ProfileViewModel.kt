package com.rememberflash.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserSession()
    }

    private fun loadUserSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.getCurrentSession()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    user = result.getOrNull()
                )
            }
        }
    }

    fun onCurrentPasswordChange(text: String) {
        _uiState.update { it.copy(currentPasswordText = text, passwordChangeError = null) }
    }

    fun onNewPasswordChange(text: String) {
        _uiState.update { it.copy(newPasswordText = text, passwordChangeError = null) }
    }

    fun onConfirmPasswordChange(text: String) {
        _uiState.update { it.copy(confirmPasswordText = text, passwordChangeError = null) }
    }

    fun changePassword() {
        val state = _uiState.value
        if (state.currentPasswordText.isBlank() || state.newPasswordText.isBlank() || state.confirmPasswordText.isBlank()) {
            _uiState.update { it.copy(passwordChangeError = "Todos os campos de senha são obrigatórios.") }
            return
        }
        if (state.newPasswordText != state.confirmPasswordText) {
            _uiState.update { it.copy(passwordChangeError = "A nova senha e a confirmação não coincidem.") }
            return
        }
        if (state.newPasswordText.length < 6) {
            _uiState.update { it.copy(passwordChangeError = "A nova senha deve ter pelo menos 6 caracteres.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChangingPasswordLoading = true,
                    passwordChangeError = null,
                    passwordChangeSuccess = false
                )
            }
            val result = authRepository.changePassword(
                currentPasswordKey = state.currentPasswordText,
                newPasswordKey = state.newPasswordText
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isChangingPasswordLoading = false,
                        passwordChangeSuccess = true,
                        currentPasswordText = "",
                        newPasswordText = "",
                        confirmPasswordText = ""
                    )
                }
            } else {
                val errorMsg = (result as? com.rememberflash.app.domain.common.Result.Error)?.message ?: "Erro ao alterar a senha."
                _uiState.update {
                    it.copy(
                        isChangingPasswordLoading = false,
                        passwordChangeError = errorMsg
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update {
            it.copy(
                passwordChangeError = null,
                passwordChangeSuccess = false,
                emailChangeError = null,
                emailChangeSuccess = false
            )
        }
    }

    fun onNewEmailChange(text: String) {
        _uiState.update { it.copy(newEmailText = text, emailChangeError = null) }
    }

    fun onConfirmPasswordForEmailChange(text: String) {
        _uiState.update { it.copy(confirmPasswordForEmailText = text, emailChangeError = null) }
    }

    fun changeEmail() {
        val state = _uiState.value
        if (state.newEmailText.isBlank() || state.confirmPasswordForEmailText.isBlank()) {
            _uiState.update { it.copy(emailChangeError = "E-mail e senha de confirmação são obrigatórios.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.newEmailText).matches()) {
            _uiState.update { it.copy(emailChangeError = "Formato de e-mail inválido.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChangingEmailLoading = true,
                    emailChangeError = null,
                    emailChangeSuccess = false
                )
            }
            val result = authRepository.changeEmail(
                newEmail = state.newEmailText,
                passwordKey = state.confirmPasswordForEmailText
            )
            if (result.isSuccess) {
                loadUserSession()
                _uiState.update {
                    it.copy(
                        isChangingEmailLoading = false,
                        emailChangeSuccess = true,
                        newEmailText = "",
                        confirmPasswordForEmailText = ""
                    )
                }
            } else {
                val errorMsg = (result as? com.rememberflash.app.domain.common.Result.Error)?.message ?: "Erro ao alterar o e-mail."
                _uiState.update {
                    it.copy(
                        isChangingEmailLoading = false,
                        emailChangeError = errorMsg
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.clearSession()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
