package com.rememberflash.app.domain.usecase.topic

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Topic
import com.rememberflash.app.domain.repository.TopicRepository
import javax.inject.Inject

class CreateTopicUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(
        disciplineId: Long,
        contestId: Long,
        name: String,
        description: String? = null
    ): Result<Long> {
        if (name.isBlank()) {
            return Result.error("O nome do subtópico não pode estar em branco")
        }
        val topic = Topic(
            disciplineId = disciplineId,
            contestId = contestId,
            name = name.trim(),
            description = description?.trim(),
            createdAt = System.currentTimeMillis()
        )
        return topicRepository.insert(topic)
    }
}
