package com.rememberflash.app.presentation.flashcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.FlashcardSource
import com.rememberflash.app.domain.usecase.discipline.GetDisciplineByIdUseCase
import com.rememberflash.app.domain.usecase.flashcard.CreateFlashcardUseCase
import com.rememberflash.app.domain.usecase.flashcard.DeleteFlashcardUseCase
import com.rememberflash.app.domain.usecase.flashcard.GetFlashcardsByDisciplineUseCase
import com.rememberflash.app.domain.usecase.flashcard.UpdateFlashcardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashcardDeckViewModel @Inject constructor(
    private val getDisciplineByIdUseCase: GetDisciplineByIdUseCase,
    private val getFlashcardsByDisciplineUseCase: GetFlashcardsByDisciplineUseCase,
    private val createFlashcardUseCase: CreateFlashcardUseCase,
    private val updateFlashcardUseCase: UpdateFlashcardUseCase,
    private val deleteFlashcardUseCase: DeleteFlashcardUseCase,
    private val syncManager: com.rememberflash.app.data.sync.SyncManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardDeckUiState())
    val uiState: StateFlow<FlashcardDeckUiState> = _uiState.asStateFlow()

    private val disciplineId: Long = checkNotNull(savedStateHandle["disciplineId"]) {
        "disciplineId é obrigatório"
    }

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing

    init {
        loadDiscipline()
        observeFlashcards()
        syncManager.triggerSync()
    }

    fun syncNow(onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            when (val res = syncManager.syncNow()) {
                is Result.Success -> onResult("Sincronização concluída com sucesso!")
                is Result.Error -> onResult("Falha na sincronização: ${res.message}")
                else -> onResult("Sincronização finalizada.")
            }
        }
    }

    private fun loadDiscipline() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = getDisciplineByIdUseCase(disciplineId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        discipline = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun observeFlashcards() {
        viewModelScope.launch {
            getFlashcardsByDisciplineUseCase(disciplineId).collectLatest { flashcards ->
                _uiState.value = _uiState.value.copy(flashcards = flashcards)
            }
        }
    }

    suspend fun createFlashcard(front: String, back: String): Result<Long> {
        if (front.isBlank() || back.isBlank()) {
            return Result.error("A frente e o verso do cartão são obrigatórios.")
        }
        val card = Flashcard(
            disciplineId = disciplineId,
            front = front,
            back = back,
            source = FlashcardSource.MANUAL,
            isSynced = false
        )
        val res = createFlashcardUseCase(card)
        if (res is Result.Success) {
            syncManager.triggerSync()
        }
        return res
    }

    suspend fun updateFlashcard(card: Flashcard, front: String, back: String): Result<Unit> {
        if (front.isBlank() || back.isBlank()) {
            return Result.error("A frente e o verso do cartão são obrigatórios.")
        }
        val updatedCard = card.copy(
            front = front,
            back = back,
            isSynced = false // resets to unsynced so that it triggers sync
        )
        val res = updateFlashcardUseCase(updatedCard)
        if (res is Result.Success) {
            syncManager.triggerSync()
        }
        return res
    }

    suspend fun deleteFlashcard(flashcardId: Long): Result<Unit> {
        syncManager.deleteFlashcardRemote(flashcardId)
        return deleteFlashcardUseCase(flashcardId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
