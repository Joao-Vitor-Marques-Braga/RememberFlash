package com.rememberflash.app.domain.usecase.contest

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.repository.ContestRepository
import javax.inject.Inject

class GetContestByIdUseCase @Inject constructor(
    private val contestRepository: ContestRepository
) {
    suspend operator fun invoke(contestId: Long): Result<Contest> {
        return contestRepository.getById(contestId)
    }
}
