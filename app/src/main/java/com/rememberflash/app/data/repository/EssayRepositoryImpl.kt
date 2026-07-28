package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.database.dao.EssayDao
import com.rememberflash.app.data.mapper.toDomain
import com.rememberflash.app.data.mapper.toEntity
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Essay
import com.rememberflash.app.domain.repository.EssayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EssayRepositoryImpl @Inject constructor(
    private val essayDao: EssayDao
) : EssayRepository {

    override suspend fun insert(essay: Essay): Result<Long> {
        return try {
            val id = essayDao.insert(essay.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Result.error("Erro ao inserir redação: ${e.localizedMessage}", e)
        }
    }

    override suspend fun updateWithExtractedText(essayId: Long, text: String): Result<Unit> {
        return try {
            essayDao.updateExtractedText(essayId, text)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao atualizar texto extraído: ${e.localizedMessage}", e)
        }
    }

    override suspend fun updateWithAiFeedback(
        essayId: Long,
        feedbackJson: String,
        score: Double,
        tokensSpent: Int
    ): Result<Unit> {
        return try {
            essayDao.updateAiFeedback(essayId, feedbackJson, score, tokensSpent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao atualizar feedback da IA: ${e.localizedMessage}", e)
        }
    }

    override fun getByUser(userId: String): Flow<List<Essay>> {
        return essayDao.getByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getByContest(contestId: Long): Flow<List<Essay>> {
        return essayDao.getByContest(contestId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(essayId: Long): Result<Essay> {
        return try {
            val entity = essayDao.getById(essayId)
                ?: return Result.error("Redação não encontrada")
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.error("Erro ao buscar redação: ${e.localizedMessage}", e)
        }
    }
}
