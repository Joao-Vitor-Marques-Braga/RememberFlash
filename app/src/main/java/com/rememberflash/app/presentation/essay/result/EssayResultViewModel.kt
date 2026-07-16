package com.rememberflash.app.presentation.essay.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.EssayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EssayResultViewModel @Inject constructor(
    private val essayRepository: EssayRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EssayResultUiState())
    val uiState: StateFlow<EssayResultUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    init {
        savedStateHandle.get<Long>("essayId")?.let { id ->
            loadEssay(id)
        } ?: run {
            _uiState.value = _uiState.value.copy(error = "Identificador da redação inválido.")
        }
    }

    private fun loadEssay(essayId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = essayRepository.getById(essayId)
            
            when (result) {
                is Result.Success -> {
                    val essay = result.data
                    val parsed = essay.aiFeedbackJson?.let { parseFeedbackJson(it) }
                    
                    _uiState.value = _uiState.value.copy(
                        essay = essay,
                        parsedFeedback = parsed,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    private fun parseFeedbackJson(json: String): ParsedEssayFeedback? {
        return try {
            val type = object : TypeToken<AiFeedbackRaw>() {}.type
            val raw: AiFeedbackRaw = gson.fromJson(json, type) ?: return null
            
            ParsedEssayFeedback(
                overallScore = raw.nota ?: 0.0,
                competencies = raw.competencias?.map {
                    CompetenceFeedback(
                        name = it.nome ?: "Critério",
                        score = it.nota ?: 0.0,
                        comment = it.comentario ?: ""
                    )
                } ?: emptyList(),
                strengths = raw.pontos_fortes ?: emptyList(),
                improvements = raw.sugestoes_melhoria ?: emptyList()
            )
        } catch (e: Exception) {
            android.util.Log.e("EssayResultViewModel", "Erro ao fazer parse do feedback JSON", e)
            null
        }
    }

    private data class AiFeedbackRaw(
        val nota: Double? = null,
        val competencias: List<CompetenceRaw>? = null,
        val pontos_fortes: List<String>? = null,
        val sugestoes_melhoria: List<String>? = null
    )

    private data class CompetenceRaw(
        val nome: String? = null,
        val nota: Double? = null,
        val comentario: String? = null
    )
}
