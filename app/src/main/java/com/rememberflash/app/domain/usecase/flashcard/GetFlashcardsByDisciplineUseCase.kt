package com.rememberflash.app.domain.usecase.flashcard

import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFlashcardsByDisciplineUseCase @Inject constructor(
    private val flashcardRepository: FlashcardRepository
) {
    operator fun invoke(disciplineId: Long): Flow<List<Flashcard>> {
        return flashcardRepository.getByDiscipline(disciplineId)
    }
}
