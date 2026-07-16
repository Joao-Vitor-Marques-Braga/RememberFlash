package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.database.dao.DisciplineDao
import com.rememberflash.app.data.mapper.toDomain
import com.rememberflash.app.data.mapper.toEntity
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.repository.DisciplineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisciplineRepositoryImpl @Inject constructor(
    private val disciplineDao: DisciplineDao
) : DisciplineRepository {

    override suspend fun insert(discipline: Discipline): Result<Long> {
        return try {
            val id = disciplineDao.insert(discipline.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Result.error("Erro ao inserir disciplina: ${e.localizedMessage}", e)
        }
    }

    override suspend fun update(discipline: Discipline): Result<Unit> {
        return try {
            disciplineDao.update(discipline.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao atualizar disciplina: ${e.localizedMessage}", e)
        }
    }

    override suspend fun delete(disciplineId: Long): Result<Unit> {
        return try {
            disciplineDao.delete(disciplineId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao remover disciplina: ${e.localizedMessage}", e)
        }
    }

    override fun getByContest(contestId: Long): Flow<List<Discipline>> {
        return disciplineDao.getByContest(contestId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(disciplineId: Long): Result<Discipline> {
        return try {
            val entity = disciplineDao.getById(disciplineId)
                ?: return Result.error("Disciplina não encontrada")
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.error("Erro ao buscar disciplina: ${e.localizedMessage}", e)
        }
    }

    override fun getAllDisciplines(): Flow<List<Discipline>> {
        return disciplineDao.getAllDisciplines().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
