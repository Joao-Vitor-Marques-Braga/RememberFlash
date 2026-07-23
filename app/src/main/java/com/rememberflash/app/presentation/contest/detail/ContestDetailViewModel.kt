package com.rememberflash.app.presentation.contest.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.usecase.contest.GetContestByIdUseCase
import com.rememberflash.app.domain.usecase.discipline.CreateDisciplineUseCase
import com.rememberflash.app.domain.usecase.discipline.DeleteDisciplineUseCase
import com.rememberflash.app.domain.usecase.discipline.GetDisciplinesByContestUseCase
import com.rememberflash.app.domain.usecase.discipline.UpdateDisciplineUseCase
import com.rememberflash.app.domain.usecase.question.GenerateContestMockExamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContestDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getContestByIdUseCase: GetContestByIdUseCase,
    private val getDisciplinesByContestUseCase: GetDisciplinesByContestUseCase,
    private val createDisciplineUseCase: CreateDisciplineUseCase,
    private val updateDisciplineUseCase: UpdateDisciplineUseCase,
    private val deleteDisciplineUseCase: DeleteDisciplineUseCase,
    private val generateContestMockExamUseCase: GenerateContestMockExamUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContestDetailUiState())
    val uiState: StateFlow<ContestDetailUiState> = _uiState.asStateFlow()

    private var contestId: Long = 0L

    init {
        savedStateHandle.get<String>("contestId")?.toLongOrNull()?.let { id ->
            contestId = id
            loadContest(id)
            observeDisciplines(id)
        } ?: run {
            _uiState.value = _uiState.value.copy(error = "Código de concurso inválido")
        }
    }

    private fun loadContest(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = getContestByIdUseCase(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        contest = result.data,
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

    private fun observeDisciplines(id: Long) {
        viewModelScope.launch {
            getDisciplinesByContestUseCase(id).collect { list ->
                _uiState.value = _uiState.value.copy(disciplines = list)
            }
        }
    }

    fun clearDisciplineNameError() {
        _uiState.value = _uiState.value.copy(disciplineNameError = null)
    }

    fun validateDisciplineName(name: String, editingDisciplineId: Long? = null): Boolean {
        if (name.trim().isEmpty()) {
            _uiState.value = _uiState.value.copy(disciplineNameError = "O nome não pode estar vazio")
            return false
        }

        val isDuplicate = _uiState.value.disciplines.any {
            it.name.trim().equals(name.trim(), ignoreCase = true) && it.id != editingDisciplineId
        }

        if (isDuplicate) {
            _uiState.value = _uiState.value.copy(
                disciplineNameError = "Já existe uma disciplina com este nome neste concurso."
            )
            return false
        }

        _uiState.value = _uiState.value.copy(disciplineNameError = null)
        return true
    }

    fun saveDiscipline(name: String, editingDisciplineId: Long? = null, onSuccess: () -> Unit) {
        if (!validateDisciplineName(name, editingDisciplineId)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = if (editingDisciplineId == null) {
                val newDiscipline = Discipline(
                    contestId = contestId,
                    name = name.trim()
                )
                createDisciplineUseCase(newDiscipline)
            } else {
                val existing = _uiState.value.disciplines.firstOrNull { it.id == editingDisciplineId }
                if (existing != null) {
                    val updated = existing.copy(name = name.trim())
                    updateDisciplineUseCase(updated)
                } else {
                    Result.error("Disciplina não encontrada para edição")
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is Result.Success -> {
                    clearDisciplineNameError()
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteDiscipline(disciplineId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = deleteDisciplineUseCase(disciplineId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
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

    fun generateMockExam(onFinished: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingMock = true,
                mockGenerationProgress = 0f,
                mockGenerationStatus = "Iniciando geração..."
            )
            
            val result = generateContestMockExamUseCase(
                contestId = contestId,
                onProgress = { current, total, disciplineName ->
                    val progress = current.toFloat() / total.toFloat()
                    _uiState.value = _uiState.value.copy(
                        mockGenerationProgress = progress,
                        mockGenerationStatus = "Gerando questões para $disciplineName ($current/$total)..."
                    )
                }
            )
            
            _uiState.value = _uiState.value.copy(isGeneratingMock = false)
            
            when (result) {
                is Result.Success -> {
                    onFinished()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }
}
