package com.rememberflash.app.domain.usecase.contest

import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.repository.ContestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveContestsUseCase @Inject constructor(
    private val contestRepository: ContestRepository
) {
    operator fun invoke(userId: String): Flow<List<Contest>> {
        return contestRepository.getActiveContestsByUser(userId)
    }
}
