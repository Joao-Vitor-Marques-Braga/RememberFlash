package com.rememberflash.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: SecurePreferencesManager,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val diff = preferencesManager.getDifficulty()
        val rig = preferencesManager.getRigor()
        val ton = preferencesManager.getTone()
        val key = preferencesManager.getGeminiApiKey() ?: ""
        _uiState.value = SettingsUiState(
            difficulty = diff,
            rigor = rig,
            tone = ton,
            apiKey = key
        )
    }

    fun onDifficultyChanged(value: String) {
        _uiState.value = _uiState.value.copy(difficulty = value, isSuccess = false, error = null)
    }

    fun onRigorChanged(value: String) {
        _uiState.value = _uiState.value.copy(rigor = value, isSuccess = false, error = null)
    }

    fun onToneChanged(value: String) {
        _uiState.value = _uiState.value.copy(tone = value, isSuccess = false, error = null)
    }

    fun onApiKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value, isSuccess = false, error = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    fun saveSettings() {
        val state = _uiState.value
        if (state.difficulty.isBlank() || state.rigor.isBlank() || state.tone.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Preencha todos os campos obrigatórios")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)

            // Salva localmente primeiro (garante retenção e persistência criptografada)
            preferencesManager.saveDifficulty(state.difficulty)
            preferencesManager.saveRigor(state.rigor)
            preferencesManager.saveTone(state.tone)
            preferencesManager.saveGeminiApiKey(state.apiKey)

            // Simula envio para a nuvem
            val isOnline = syncManager.isOnline.value
            delay(1000) // Simular latência de rede de 1 segundo

            if (!isOnline) {
                // Fluxo Alternativo A1 - Falha de Conexão Nuvem
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Não foi possível salvar suas preferências na nuvem. Verifique sua conexão e tente novamente.",
                    isSuccess = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            }
        }
    }
}
