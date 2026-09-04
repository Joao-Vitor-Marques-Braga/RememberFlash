package com.rememberflash.app.domain.usecase.topic

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.TopicRepository
import javax.inject.Inject

class DeleteTopicUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(topicId: Long): Result<Unit> {
        return topicRepository.delete(topicId)
    }
}
