package com.rememberflash.app.domain.usecase.topic

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.TopicRepository
import javax.inject.Inject

class ToggleTopicCompletionUseCase @Inject constructor(
    private val topicRepository: TopicRepository
) {
    suspend operator fun invoke(topicId: Long, isCompleted: Boolean): Result<Unit> {
        return topicRepository.setCompletion(topicId, isCompleted)
    }
}
