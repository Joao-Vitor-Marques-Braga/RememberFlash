package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.database.dao.QuestionDao
import com.rememberflash.app.data.mapper.toDomain
import com.rememberflash.app.data.mapper.toEntity
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao
) : QuestionRepository {

    override fun getQuestionsByDiscipline(disciplineId: Long): Flow<List<Question>> {
        return questionDao.getByDiscipline(disciplineId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveQuestions(questions: List<Question>): Result<Unit> {
        return try {
            val entities = questions.map { it.toEntity() }
            questionDao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Falha ao salvar lote de questões: ${e.localizedMessage}", e)
        }
    }

    override suspend fun clearQuestionsByDiscipline(disciplineId: Long): Result<Unit> {
        return try {
            questionDao.deleteByDiscipline(disciplineId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Falha ao limpar questões: ${e.localizedMessage}", e)
        }
    }

    override suspend fun answerQuestion(questionId: Long, chosenOption: Int, isCorrect: Boolean): Result<Unit> {
        return try {
            questionDao.updateAnswer(questionId, chosenOption, isCorrect, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Falha ao registrar resposta da questão: ${e.localizedMessage}", e)
        }
    }

    override fun getAllQuestions(): Flow<List<Question>> {
        return questionDao.getAllQuestions().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
