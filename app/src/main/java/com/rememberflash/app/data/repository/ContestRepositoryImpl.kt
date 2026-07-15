package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.database.dao.ContestDao
import com.rememberflash.app.data.mapper.toDomain
import com.rememberflash.app.data.mapper.toEntity
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.repository.ContestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContestRepositoryImpl @Inject constructor(
    private val contestDao: ContestDao
) : ContestRepository {

    override suspend fun insert(contest: Contest): Result<Long> {
        return try {
            val id = contestDao.insert(contest.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Result.error("Erro ao inserir concurso: ${e.localizedMessage}", e)
        }
    }

    override suspend fun update(contest: Contest): Result<Unit> {
        return try {
            contestDao.update(contest.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao atualizar concurso: ${e.localizedMessage}", e)
        }
    }

    override suspend fun softDelete(contestId: Long): Result<Unit> {
        return try {
            contestDao.softDelete(contestId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao inativar concurso: ${e.localizedMessage}", e)
        }
    }

    override fun getActiveContestsByUser(userId: String): Flow<List<Contest>> {
        return contestDao.getActiveByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(contestId: Long): Result<Contest> {
        return try {
            val entity = contestDao.getById(contestId)
                ?: return Result.error("Concurso não encontrado")
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.error("Erro ao buscar concurso: ${e.localizedMessage}", e)
        }
    }

    override fun getAllByUser(userId: String): Flow<List<Contest>> {
        return contestDao.getAllByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
