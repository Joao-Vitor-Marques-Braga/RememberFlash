package com.rememberflash.app.domain.usecase.essay

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.EssayRepository
import javax.inject.Inject

class DeleteEssayUseCase @Inject constructor(
    private val essayRepository: EssayRepository
) {
    suspend operator fun invoke(essayId: Long): Result<Unit> {
        return essayRepository.delete(essayId)
    }
}
