package com.rememberflash.app

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Topic
import com.rememberflash.app.domain.repository.TopicRepository
import com.rememberflash.app.domain.usecase.topic.CreateTopicUseCase
import com.rememberflash.app.domain.usecase.topic.DeleteTopicUseCase
import com.rememberflash.app.domain.usecase.topic.GetTopicsByDisciplineUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TopicUseCaseTest {

    private lateinit var fakeTopicRepository: FakeTopicRepository
    private lateinit var createTopicUseCase: CreateTopicUseCase
    private lateinit var getTopicsByDisciplineUseCase: GetTopicsByDisciplineUseCase
    private lateinit var deleteTopicUseCase: DeleteTopicUseCase

    class FakeTopicRepository : TopicRepository {
        val topics = mutableListOf<Topic>()
        private val flowState = MutableStateFlow<List<Topic>>(emptyList())

        private fun notifyChange() {
            flowState.value = topics.toList()
        }

        override suspend fun insert(topic: Topic): Result<Long> {
            val id = if (topic.id == 0L) (topics.size + 1).toLong() else topic.id
            val inserted = topic.copy(id = id)
            topics.add(inserted)
            notifyChange()
            return Result.success(id)
        }

        override suspend fun insertAll(topicsToInsert: List<Topic>): Result<List<Long>> {
            val ids = mutableListOf<Long>()
            topicsToInsert.forEach {
                val id = (topics.size + 1).toLong()
                topics.add(it.copy(id = id))
                ids.add(id)
            }
            notifyChange()
            return Result.success(ids)
        }

        override suspend fun update(topic: Topic): Result<Unit> {
            val index = topics.indexOfFirst { it.id == topic.id }
            if (index != -1) {
                topics[index] = topic
                notifyChange()
            }
            return Result.success(Unit)
        }

        override suspend fun delete(topicId: Long): Result<Unit> {
            topics.removeAll { it.id == topicId }
            notifyChange()
            return Result.success(Unit)
        }

        override suspend fun deleteByDiscipline(disciplineId: Long): Result<Unit> {
            topics.removeAll { it.disciplineId == disciplineId }
            notifyChange()
            return Result.success(Unit)
        }

        override fun getByDisciplineFlow(disciplineId: Long): Flow<List<Topic>> {
            return flowState.map { list -> list.filter { it.disciplineId == disciplineId } }
        }

        override suspend fun getByDiscipline(disciplineId: Long): Result<List<Topic>> {
            return Result.success(topics.filter { it.disciplineId == disciplineId })
        }

        override fun getByContestFlow(contestId: Long): Flow<List<Topic>> {
            return flowState.map { list -> list.filter { it.contestId == contestId } }
        }

        override suspend fun getById(topicId: Long): Result<Topic> {
            val item = topics.find { it.id == topicId }
            return if (item != null) Result.success(item) else Result.error("Não encontrado")
        }

        override suspend fun syncDisciplineTopicCounters(disciplineId: Long): Result<Unit> {
            return Result.success(Unit)
        }
    }

    @Before
    fun setup() {
        fakeTopicRepository = FakeTopicRepository()
        createTopicUseCase = CreateTopicUseCase(fakeTopicRepository)
        getTopicsByDisciplineUseCase = GetTopicsByDisciplineUseCase(fakeTopicRepository)
        deleteTopicUseCase = DeleteTopicUseCase(fakeTopicRepository)
    }

    @Test
    fun `createTopicUseCase returns error when topic name is blank`() = runTest {
        val result = createTopicUseCase(
            disciplineId = 1L,
            contestId = 10L,
            name = "   "
        )
        assertTrue(result is Result.Error)
        assertEquals("O nome do subtópico não pode estar em branco", (result as Result.Error).message)
    }

    @Test
    fun `createTopicUseCase inserts topic successfully when name is valid`() = runTest {
        val result = createTopicUseCase(
            disciplineId = 1L,
            contestId = 10L,
            name = "Modelagem de Dados e SQL",
            description = "Conceitos de MER, Normalização e DDL/DML"
        )

        assertTrue(result is Result.Success)
        val createdId = (result as Result.Success).data
        assertEquals(1L, createdId)
        assertEquals(1, fakeTopicRepository.topics.size)
        assertEquals("Modelagem de Dados e SQL", fakeTopicRepository.topics.first().name)
    }

    @Test
    fun `getTopicsByDisciplineUseCase emits topics for specific discipline`() = runTest {
        createTopicUseCase(disciplineId = 1L, contestId = 10L, name = "SQL")
        createTopicUseCase(disciplineId = 1L, contestId = 10L, name = "NoSQL")
        createTopicUseCase(disciplineId = 2L, contestId = 10L, name = "Direito Constitucional")

        val emitted = getTopicsByDisciplineUseCase(disciplineId = 1L).first()

        assertEquals(2, emitted.size)
        assertEquals("SQL", emitted[0].name)
        assertEquals("NoSQL", emitted[1].name)
    }

    @Test
    fun `deleteTopicUseCase removes topic from repository`() = runTest {
        val createResult = createTopicUseCase(1L, 10L, "Tópico a Excluir")
        val topicId = (createResult as Result.Success).data
        assertEquals(1, fakeTopicRepository.topics.size)

        val deleteResult = deleteTopicUseCase(topicId)

        assertTrue(deleteResult is Result.Success)
        assertEquals(0, fakeTopicRepository.topics.size)
    }
}
