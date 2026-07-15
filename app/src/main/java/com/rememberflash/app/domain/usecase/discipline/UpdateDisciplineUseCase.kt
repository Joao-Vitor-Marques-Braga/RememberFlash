package com.rememberflash.app.domain.usecase.discipline

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.repository.DisciplineRepository
import javax.inject.Inject

class UpdateDisciplineUseCase @Inject constructor(
    private val disciplineRepository: DisciplineRepository
) {
    suspend operator fun invoke(discipline: Discipline): Result<Unit> {
        if (discipline.name.isBlank()) {
            return Result.error("O nome da disciplina é obrigatório")
        }
        if (discipline.id <= 0L) {
            return Result.error("Disciplina inválida para atualização")
        }
        return try {
            disciplineRepository.update(discipline)
        } catch (e: Exception) {
            Result.error("Falha ao atualizar disciplina: ${e.localizedMessage}", e)
        }
    }
}
