package com.rememberflash.app.domain.usecase.contest

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.ContestRepository
import javax.inject.Inject

/**
 * Caso de uso para reativar um concurso previamente inativado/arquivado (RF003 / RN06 / Fluxo A5).
 */
class ReactivateContestUseCase @Inject constructor(
    private val contestRepository: ContestRepository
) {
    suspend operator fun invoke(contestId: Long): Result<Unit> =
        contestRepository.reactivate(contestId)
}
