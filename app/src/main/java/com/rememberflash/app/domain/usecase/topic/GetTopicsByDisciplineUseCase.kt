package com.rememberflash.app.domain.usecase.topic

import com.rememberflash.app.domain.model.Topic
import com.rememberflash.app.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTopicsByDisciplineUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    operator fun invoke(disciplineId: Long): Flow<List<Topic>> {
        return topicRepository.getByDisciplineFlow(disciplineId)
    }
}
