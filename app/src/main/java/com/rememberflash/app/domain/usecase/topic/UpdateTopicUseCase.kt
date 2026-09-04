package com.rememberflash.app.domain.usecase.topic

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Topic
import com.rememberflash.app.domain.repository.TopicRepository
import javax.inject.Inject

class UpdateTopicUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(topic: Topic): Result<Unit> {
        if (topic.name.isBlank()) {
            return Result.error("O nome do subtópico não pode estar em branco")
        }
        return topicRepository.update(topic)
    }
}
