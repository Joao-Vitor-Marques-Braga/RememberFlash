package com.rememberflash.app.presentation.essay.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Essay
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.EssayRepository
import com.rememberflash.app.domain.usecase.essay.EvaluateEssayUseCase
import com.rememberflash.app.domain.usecase.essay.ExtractTextFromImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EssayCaptureViewModel @Inject constructor(
    private val extractTextFromImageUseCase: ExtractTextFromImageUseCase,
    private val evaluateEssayUseCase: EvaluateEssayUseCase,
    private val essayRepository: EssayRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EssayCaptureUiState())
    val uiState: StateFlow<EssayCaptureUiState> = _uiState.asStateFlow()

    fun onMethodSelected(method: SubmissionMethod) {
        _uiState.value = _uiState.value.copy(method = method, error = null)
    }

    fun onThemeChanged(theme: String) {
        _uiState.value = _uiState.value.copy(theme = theme, error = null)
    }

    fun onTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(text = text, error = null)
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(imageUri = uri, isOcrLoading = true, error = null)
            
            val result = extractTextFromImageUseCase(uri)
            
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        text = result.data,
                        isOcrLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isOcrLoading = false,
                        error = "Não foi possível ler o texto com clareza. Certifique-se de que a foto está bem iluminada e que a folha não está amassada."
                    )
                }
                else -> {}
            }
        }
    }

    fun onPdfSelected(context: android.content.Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pdfUri = uri, isOcrLoading = true, error = null)
            
            val rawText = com.rememberflash.app.data.local.pdf.LocalPdfExtractor.extractText(context, uri)
            
            if (rawText.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isOcrLoading = false,
                    error = "O PDF selecionado não contém texto extraível ou está protegido/criptografado."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    text = rawText,
                    isOcrLoading = false
                )
            }
        }
    }

    fun onEvaluateClicked() {
        val state = _uiState.value
        
        if (state.theme.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Por favor, defina o tema da redação.")
            return
        }

        // Validação Heurística Local de Tamanho Mínimo (RF011 - A3)
        if (state.text.trim().length < 150) {
            _uiState.value = _uiState.value.copy(
                error = "Texto muito curto. Uma redação exige um mínimo de desenvolvimento para ser avaliada."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEvaluating = true, error = null)
            
            val sessionResult = authRepository.getCurrentSession()
            if (sessionResult !is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    isEvaluating = false,
                    error = "Usuário não autenticado no aplicativo."
                )
                return@launch
            }
            
            val userId = sessionResult.data.id
            val essay = Essay(
                userId = userId,
                title = "Redação: ${state.theme.take(30)}...",
                theme = state.theme,
                imageUri = state.pdfUri?.toString() ?: state.imageUri?.toString() ?: "",
                extractedText = state.text
            )

            // Salva a redação localmente para gerar o ID
            val insertResult = essayRepository.insert(essay)
            
            if (insertResult is Result.Success) {
                val essayId = insertResult.data
                
                // Solicita a avaliação do Gemini
                val evalResult = evaluateEssayUseCase(essay.copy(id = essayId))
                
                when (evalResult) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isEvaluating = false,
                            successEssayId = essayId
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isEvaluating = false,
                            error = evalResult.message
                        )
                    }
                    else -> {}
                }
            } else if (insertResult is Result.Error) {
                _uiState.value = _uiState.value.copy(
                    isEvaluating = false,
                    error = insertResult.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
