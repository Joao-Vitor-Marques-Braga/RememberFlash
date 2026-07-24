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

data class PdfAttachment(
    val uri: String,
    val name: String
)

data class ContestFormUiState(
    val id: Long? = null,
    val title: String = "",
    val organizerName: String = "",
    val questionType: String = "Múltipla Escolha",
    val pdfAttachments: List<PdfAttachment> = emptyList(),
    val jobPosition: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val savingStep: String = ""
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
                    val attachments = contest.syllabusPdfUri?.split("|")
                        ?.filter { it.isNotBlank() }
                        ?.map { uriStr ->
                            val decodedUri = android.net.Uri.parse(uriStr)
                            val name = decodedUri.lastPathSegment ?: "anexo.pdf"
                            PdfAttachment(uriStr, name)
                        } ?: emptyList()

                    val jobFromDesc = if (contest.description.startsWith("Cargo: ")) {
                        contest.description.substringAfter("Cargo: ").substringBefore("\n\n").trim()
                    } else {
                        ""
                    }
                    _uiState.value = _uiState.value.copy(
                        id = contest.id,
                        title = contest.title,
                        organizerName = contest.organizerName,
                        questionType = contest.questionType,
                        pdfAttachments = attachments,
                        jobPosition = jobFromDesc,
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

    fun onJobPositionChanged(position: String) {
        _uiState.value = _uiState.value.copy(jobPosition = position, error = null)
    }

    fun onQuestionTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(questionType = type)
    }

    fun addPdfAttachment(uri: String, name: String) {
        val current = _uiState.value.pdfAttachments.toMutableList()
        if (current.none { it.uri == uri }) {
            current.add(PdfAttachment(uri, name))
            _uiState.value = _uiState.value.copy(
                pdfAttachments = current,
                error = null
            )
        }
    }

    fun removePdfAttachment(uri: String) {
        val current = _uiState.value.pdfAttachments.filter { it.uri != uri }
        _uiState.value = _uiState.value.copy(
            pdfAttachments = current,
            error = null
        )
    }

    fun onPdfError(errorMsg: String?) {
        _uiState.value = _uiState.value.copy(error = errorMsg)
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (state.pdfAttachments.isNotEmpty() && state.jobPosition.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Informe o cargo pretendido para podermos extrair as disciplinas corretas do edital.")
            return
        }
        if ((state.title.isBlank() || state.organizerName.isBlank()) && state.pdfAttachments.isEmpty()) {
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

            val syllabusPdfUriMerged = if (state.pdfAttachments.isNotEmpty()) {
                state.pdfAttachments.joinToString("|") { it.uri }
            } else {
                null
            }

            val contest = Contest(
                id = state.id ?: 0L,
                userId = userId,
                title = state.title,
                description = "Cargo: ${state.jobPosition}",
                organizerName = state.organizerName,
                questionType = state.questionType,
                syllabusPdfUri = syllabusPdfUriMerged
            )

            val result = if (state.id == null) {
                createContestUseCase(contest) { step ->
                    _uiState.value = _uiState.value.copy(savingStep = step)
                }
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
