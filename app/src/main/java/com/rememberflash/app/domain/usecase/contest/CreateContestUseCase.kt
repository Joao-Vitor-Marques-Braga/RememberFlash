package com.rememberflash.app.domain.usecase.contest

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.repository.ContestRepository
import javax.inject.Inject

class CreateContestUseCase @Inject constructor(
    private val contestRepository: ContestRepository
) {
    suspend operator fun invoke(contest: Contest): Result<Long> {
        if (contest.title.isBlank()) {
            return Result.error("O título do concurso é obrigatório")
        }
        if (contest.userId.isBlank()) {
            return Result.error("Usuário não identificado")
        }
        return try {
            val now = System.currentTimeMillis()
            val contestToInsert = contest.copy(
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
            contestRepository.insert(contestToInsert)
        } catch (e: Exception) {
            Result.error("Falha ao criar concurso: ${e.localizedMessage}", e)
        }
    }
}
