package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.database.dao.ScheduleDao
import com.rememberflash.app.data.local.database.entity.DailyGoalEntity
import com.rememberflash.app.data.local.database.entity.ScheduleEntity
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.StudySchedule
import com.rememberflash.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override suspend fun insert(schedule: StudySchedule): Result<Long> {
        return try {
            val id = scheduleDao.insertSchedule(schedule.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Result.error("Erro ao inserir cronograma: ${e.localizedMessage}", e)
        }
    }

    override suspend fun update(schedule: StudySchedule): Result<Unit> {
        return try {
            scheduleDao.updateSchedule(schedule.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao atualizar cronograma: ${e.localizedMessage}", e)
        }
    }

    override suspend fun getByContest(contestId: Long): Result<StudySchedule?> {
        return try {
            val entity = scheduleDao.getByContest(contestId)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.error("Erro ao buscar cronograma: ${e.localizedMessage}", e)
        }
    }

    override fun getDailyGoalsBySchedule(scheduleId: Long): Flow<List<DailyGoal>> {
        return scheduleDao.getDailyGoalsBySchedule(scheduleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllDailyGoalsFlow(): Flow<List<DailyGoal>> {
        return scheduleDao.getAllDailyGoalsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertDailyGoals(goals: List<DailyGoal>): Result<Unit> {
        return try {
            scheduleDao.insertDailyGoals(goals.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao inserir metas diárias: ${e.localizedMessage}", e)
        }
    }

    override suspend fun updateDailyGoalProgress(
        goalId: Long,
        completedMinutes: Int,
        flashcardsCompleted: Int
    ): Result<Unit> {
        return try {
            scheduleDao.updateDailyGoalProgress(goalId, completedMinutes, flashcardsCompleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao atualizar progresso: ${e.localizedMessage}", e)
        }
    }

    override suspend fun clearDailyGoalsBySchedule(scheduleId: Long): Result<Unit> {
        return try {
            scheduleDao.clearDailyGoalsBySchedule(scheduleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao limpar metas do cronograma: ${e.localizedMessage}", e)
        }
    }

    // Mappers locais (Schedule/DailyGoal — mantidos aqui por simplicidade)

    private fun StudySchedule.toEntity() = ScheduleEntity(
        id = id,
        contestId = contestId,
        examDate = examDate,
        availableHoursPerDay = availableHoursPerDay,
        restDaysPerWeek = restDaysPerWeek,
        tokensSpent = tokensSpent,
        createdAt = createdAt,
        lastRecalculatedAt = lastRecalculatedAt
    )

    private fun ScheduleEntity.toDomain() = StudySchedule(
        id = id,
        contestId = contestId,
        examDate = examDate,
        availableHoursPerDay = availableHoursPerDay,
        restDaysPerWeek = restDaysPerWeek,
        tokensSpent = tokensSpent,
        createdAt = createdAt,
        lastRecalculatedAt = lastRecalculatedAt
    )

    private fun DailyGoal.toEntity() = DailyGoalEntity(
        id = id,
        scheduleId = scheduleId,
        date = date,
        disciplineId = disciplineId,
        targetMinutes = targetMinutes,
        completedMinutes = completedMinutes,
        flashcardsTarget = flashcardsTarget,
        flashcardsCompleted = flashcardsCompleted
    )

    private fun DailyGoalEntity.toDomain() = DailyGoal(
        id = id,
        scheduleId = scheduleId,
        date = date,
        disciplineId = disciplineId,
        targetMinutes = targetMinutes,
        completedMinutes = completedMinutes,
        flashcardsTarget = flashcardsTarget,
        flashcardsCompleted = flashcardsCompleted
    )
}
