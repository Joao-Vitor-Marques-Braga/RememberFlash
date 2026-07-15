package com.rememberflash.app.domain.usecase.contest

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.ContestRepository
import javax.inject.Inject

/**
 * Exclusão lógica (Soft Delete) conforme RN06.
 * Inativa a pasta do concurso preservando todo o histórico de desempenho
 * (flashcards, questões, redações) vinculado ao aluno.
 */
class SoftDeleteContestUseCase @Inject constructor(
    private val contestRepository: ContestRepository
) {
    suspend operator fun invoke(contestId: Long): Result<Unit> {
        if (contestId <= 0L) {
            return Result.error("ID de concurso inválido")
        }
        return try {
            contestRepository.softDelete(contestId)
        } catch (e: Exception) {
            Result.error("Falha ao inativar concurso: ${e.localizedMessage}", e)
        }
    }
}
