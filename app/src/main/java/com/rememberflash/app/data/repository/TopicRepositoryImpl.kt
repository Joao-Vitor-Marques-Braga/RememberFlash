package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.database.dao.DisciplineDao
import com.rememberflash.app.data.local.database.dao.FlashcardDao
import com.rememberflash.app.data.local.database.dao.QuestionDao
import com.rememberflash.app.data.local.database.dao.TopicDao
import com.rememberflash.app.data.mapper.toDomain
import com.rememberflash.app.data.mapper.toEntity
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Topic
import com.rememberflash.app.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepositoryImpl @Inject constructor(
    private val topicDao: TopicDao,
    private val disciplineDao: DisciplineDao,
    private val flashcardDao: FlashcardDao,
    private val questionDao: QuestionDao
) : TopicRepository {

    override suspend fun insert(topic: Topic): Result<Long> {
        return try {
            val id = topicDao.insert(topic.toEntity())
            syncDisciplineTopicCounters(topic.disciplineId)
            Result.success(id)
        } catch (e: Exception) {
            Result.error("Erro ao inserir subtópico: ${e.localizedMessage}", e)
        }
    }

    override suspend fun insertAll(topics: List<Topic>): Result<List<Long>> {
        return try {
            val ids = topicDao.insertAll(topics.map { it.toEntity() })
            if (topics.isNotEmpty()) {
                syncDisciplineTopicCounters(topics.first().disciplineId)
            }
            Result.success(ids)
        } catch (e: Exception) {
            Result.error("Erro ao inserir lote de subtópicos: ${e.localizedMessage}", e)
        }
    }

    override suspend fun update(topic: Topic): Result<Unit> {
        return try {
            topicDao.update(topic.toEntity())
            syncDisciplineTopicCounters(topic.disciplineId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao atualizar subtópico: ${e.localizedMessage}", e)
        }
    }

    override suspend fun delete(topicId: Long): Result<Unit> {
        return try {
            val topic = topicDao.getById(topicId)
            topicDao.delete(topicId)
            if (topic != null) {
                syncDisciplineTopicCounters(topic.disciplineId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao excluir subtópico: ${e.localizedMessage}", e)
        }
    }

    override suspend fun deleteByDiscipline(disciplineId: Long): Result<Unit> {
        return try {
            topicDao.deleteByDiscipline(disciplineId)
            syncDisciplineTopicCounters(disciplineId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao excluir tópicos da disciplina: ${e.localizedMessage}", e)
        }
    }

    override fun getByDisciplineFlow(disciplineId: Long): Flow<List<Topic>> {
        return combine(
            topicDao.getByDisciplineFlow(disciplineId),
            flashcardDao.getByDiscipline(disciplineId),
            questionDao.getByDiscipline(disciplineId)
        ) { topics, flashcards, questions ->
            topics.map { topicEntity ->
                val fCount = flashcards.count { it.topicId == topicEntity.id }
                val qCount = questions.count { it.topicId == topicEntity.id }
                topicEntity.toDomain(flashcardsCount = fCount, questionsCount = qCount)
            }
        }
    }

    override suspend fun getByDiscipline(disciplineId: Long): Result<List<Topic>> {
        return try {
            val entities = topicDao.getByDiscipline(disciplineId)
            val domainList = entities.map { it.toDomain() }
            Result.success(domainList)
        } catch (e: Exception) {
            Result.error("Erro ao buscar subtópicos: ${e.localizedMessage}", e)
        }
    }

    override fun getByContestFlow(contestId: Long): Flow<List<Topic>> {
        return topicDao.getByContestFlow(contestId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(topicId: Long): Result<Topic> {
        return try {
            val entity = topicDao.getById(topicId)
                ?: return Result.error("Subtópico não encontrado")
            val fCount = flashcardDao.countByTopic(topicId)
            val qCount = questionDao.countByTopic(topicId)
            Result.success(entity.toDomain(flashcardsCount = fCount, questionsCount = qCount))
        } catch (e: Exception) {
            Result.error("Erro ao obter subtópico: ${e.localizedMessage}", e)
        }
    }

    override suspend fun syncDisciplineTopicCounters(disciplineId: Long): Result<Unit> {
        return try {
            val discEntity = disciplineDao.getById(disciplineId) ?: return Result.success(Unit)
            val total = topicDao.countTotalByDiscipline(disciplineId)
            disciplineDao.update(
                discEntity.copy(
                    totalTopics = total
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao sincronizar contadores da disciplina: ${e.localizedMessage}", e)
        }
    }
}
