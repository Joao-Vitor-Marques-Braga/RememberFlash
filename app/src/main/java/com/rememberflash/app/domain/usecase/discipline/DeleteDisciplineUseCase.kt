package com.rememberflash.app.domain.usecase.discipline

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.DisciplineRepository
import javax.inject.Inject

class DeleteDisciplineUseCase @Inject constructor(
    private val disciplineRepository: DisciplineRepository
) {
    suspend operator fun invoke(disciplineId: Long): Result<Unit> {
        if (disciplineId <= 0L) {
            return Result.error("ID de disciplina inválido")
        }
        return try {
            disciplineRepository.delete(disciplineId)
        } catch (e: Exception) {
            Result.error("Falha ao inibir disciplina: ${e.localizedMessage}", e)
        }
    }
}
