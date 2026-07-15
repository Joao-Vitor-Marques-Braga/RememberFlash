package com.rememberflash.app.domain.usecase.question

import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuestionsByDisciplineUseCase @Inject constructor(
    private val questionRepository: QuestionRepository
) {
    operator fun invoke(disciplineId: Long): Flow<List<Question>> {
        return questionRepository.getQuestionsByDiscipline(disciplineId)
    }
}
