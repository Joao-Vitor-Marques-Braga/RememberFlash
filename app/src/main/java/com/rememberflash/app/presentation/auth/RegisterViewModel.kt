package com.rememberflash.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onCpfChanged(cpf: String) {
        // Remove não numéricos antes de atualizar o estado do modelo (a máscara é visual)
        val rawCpf = cpf.filter { it.isDigit() }
        if (rawCpf.length <= 11) {
            _uiState.value = _uiState.value.copy(cpf = rawCpf, error = null)
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password, error = null)
    }

    fun onRegisterClicked() {
        val state = _uiState.value

        if (state.name.isBlank() || state.cpf.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Preencha todos os campos")
            return
        }

        if (state.password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "A senha deve ter no mínimo 6 caracteres")
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "As senhas não coincidem")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = registerUseCase(
                name = state.name,
                cpf = state.cpf,
                email = state.email,
                password = state.password
            )

            when (result) {
                is com.rememberflash.app.domain.common.Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is com.rememberflash.app.domain.common.Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }
}
