package com.rememberflash.app.presentation.contest.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.usecase.contest.CreateContestUseCase
import com.rememberflash.app.domain.usecase.contest.GetContestByIdUseCase
import com.rememberflash.app.domain.usecase.contest.UpdateContestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContestFormUiState(
    val id: Long? = null,
    val title: String = "",
    val organizerName: String = "",
    val questionType: String = "Múltipla Escolha",
    val syllabusPdfUri: String? = null,
    val pdfFileName: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContestFormViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val createContestUseCase: CreateContestUseCase,
    private val updateContestUseCase: UpdateContestUseCase,
    private val getContestByIdUseCase: GetContestByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContestFormUiState())
    val uiState: StateFlow<ContestFormUiState> = _uiState.asStateFlow()

    init {
        val contestId = savedStateHandle.get<String>("contestId")?.toLongOrNull()
        if (contestId != null && contestId > 0) {
            loadContest(contestId)
        }
    }

    private fun loadContest(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = getContestByIdUseCase(id)) {
                is Result.Success -> {
                    val contest = result.data
                    _uiState.value = _uiState.value.copy(
                        id = contest.id,
                        title = contest.title,
                        organizerName = contest.organizerName,
                        questionType = contest.questionType,
                        syllabusPdfUri = contest.syllabusPdfUri,
                        pdfFileName = contest.syllabusPdfUri?.substringAfterLast("/"), // mock name display
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro ao carregar concurso: ${result.message}"
                    )
                }
                else -> {}
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title, error = null)
    }

    fun onOrganizerChanged(organizer: String) {
        _uiState.value = _uiState.value.copy(organizerName = organizer, error = null)
    }

    fun onQuestionTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(questionType = type)
    }

    fun onPdfSelected(uri: String, name: String) {
        _uiState.value = _uiState.value.copy(
            syllabusPdfUri = uri,
            pdfFileName = name,
            error = null
        )
    }

    fun onPdfError(errorMsg: String) {
        _uiState.value = _uiState.value.copy(error = errorMsg)
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if ((state.title.isBlank() || state.organizerName.isBlank()) && state.syllabusPdfUri.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(error = "Preencha o título e a banca examinadora, ou selecione um edital para preenchimento automático.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val sessionResult = authRepository.getCurrentSession()
            if (sessionResult !is Result.Success) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Usuário não autenticado.")
                return@launch
            }
            val userId = sessionResult.data.id

            val contest = Contest(
                id = state.id ?: 0L,
                userId = userId,
                title = state.title,
                organizerName = state.organizerName,
                questionType = state.questionType,
                syllabusPdfUri = state.syllabusPdfUri
            )

            val result = if (state.id == null) {
                createContestUseCase(contest)
            } else {
                updateContestUseCase(contest)
            }

            when (result) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }
}
