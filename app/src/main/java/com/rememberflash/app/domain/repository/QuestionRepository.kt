package com.rememberflash.app.domain.repository

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    fun getQuestionsByDiscipline(disciplineId: Long): Flow<List<Question>>
    suspend fun saveQuestions(questions: List<Question>): Result<Unit>
    suspend fun clearQuestionsByDiscipline(disciplineId: Long): Result<Unit>
}
