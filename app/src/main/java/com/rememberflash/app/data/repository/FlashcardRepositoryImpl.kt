package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.database.dao.FlashcardDao
import com.rememberflash.app.data.mapper.toDomain
import com.rememberflash.app.data.mapper.toEntity
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepositoryImpl @Inject constructor(
    private val flashcardDao: FlashcardDao
) : FlashcardRepository {

    override suspend fun insert(flashcard: Flashcard): Result<Long> {
        return try {
            val id = flashcardDao.insert(flashcard.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Result.error("Erro ao inserir flashcard: ${e.localizedMessage}", e)
        }
    }

    override suspend fun insertAll(flashcards: List<Flashcard>): Result<List<Long>> {
        return try {
            val ids = flashcardDao.insertAll(flashcards.map { it.toEntity() })
            Result.success(ids)
        } catch (e: Exception) {
            Result.error("Erro ao inserir flashcards em lote: ${e.localizedMessage}", e)
        }
    }

    override suspend fun update(flashcard: Flashcard): Result<Unit> {
        return try {
            flashcardDao.update(flashcard.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao atualizar flashcard: ${e.localizedMessage}", e)
        }
    }

    override suspend fun delete(flashcardId: Long): Result<Unit> {
        return try {
            flashcardDao.delete(flashcardId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao remover flashcard: ${e.localizedMessage}", e)
        }
    }

    override fun getByDiscipline(disciplineId: Long): Flow<List<Flashcard>> {
        return flashcardDao.getByDiscipline(disciplineId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDueForReview(disciplineId: Long, now: Long): Flow<List<Flashcard>> {
        return flashcardDao.getDueForReview(disciplineId, now).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateReviewMetrics(
        flashcardId: Long,
        easeFactor: Double,
        interval: Int,
        repetitions: Int,
        nextReviewAt: Long
    ): Result<Unit> {
        return try {
            flashcardDao.updateReviewMetrics(flashcardId, easeFactor, interval, repetitions, nextReviewAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao atualizar métricas de revisão: ${e.localizedMessage}", e)
        }
    }

    override fun getByTopic(topicId: Long): Flow<List<Flashcard>> {
        return flashcardDao.getByTopic(topicId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun countByTopic(topicId: Long): Result<Int> {
        return try {
            val count = flashcardDao.countByTopic(topicId)
            Result.success(count)
        } catch (e: Exception) {
            Result.error("Erro ao contar flashcards do tópico: ${e.localizedMessage}", e)
        }
    }
}
