package com.rememberflash.app.domain.usecase.contest

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.repository.ContestRepository
import javax.inject.Inject

class UpdateContestUseCase @Inject constructor(
    private val contestRepository: ContestRepository
) {
    suspend operator fun invoke(contest: Contest): Result<Unit> {
        if (contest.id == 0L) {
            return Result.error("Concurso sem ID válido para atualização")
        }
        if (contest.title.isBlank()) {
            return Result.error("O título do concurso é obrigatório")
        }
        return try {
            val updatedContest = contest.copy(updatedAt = System.currentTimeMillis())
            contestRepository.update(updatedContest)
        } catch (e: Exception) {
            Result.error("Falha ao atualizar concurso: ${e.localizedMessage}", e)
        }
    }
}
