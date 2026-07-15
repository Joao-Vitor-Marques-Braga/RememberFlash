package com.rememberflash.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.usecase.contest.GetActiveContestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val activeContests: List<Contest> = emptyList(),
    val dailyGoalProgress: Float = 0.65f, // Mock conforme protótipo
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getActiveContestsUseCase: GetActiveContestsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Carrega usuário da sessão
            val sessionResult = authRepository.getCurrentSession()
            if (sessionResult is Result.Success) {
                val user = sessionResult.data
                _uiState.value = _uiState.value.copy(user = user)
                
                // Carrega os concursos ativos
                getActiveContestsUseCase(user.id)
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Erro ao carregar concursos: ${e.localizedMessage}"
                        )
                    }
                    .collect { contests ->
                        _uiState.value = _uiState.value.copy(
                            activeContests = contests,
                            isLoading = false
                        )
                    }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Sessão inválida"
                )
            }
        }
    }
}
