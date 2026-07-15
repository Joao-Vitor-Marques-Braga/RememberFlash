package com.rememberflash.app.domain.usecase.discipline

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.repository.DisciplineRepository
import javax.inject.Inject

class CreateDisciplineUseCase @Inject constructor(
    private val disciplineRepository: DisciplineRepository
) {
    suspend operator fun invoke(discipline: Discipline): Result<Long> {
        if (discipline.name.isBlank()) {
            return Result.error("O nome da disciplina é obrigatório")
        }
        if (discipline.contestId <= 0L) {
            return Result.error("Disciplina deve estar vinculada a um concurso válido")
        }
        if (discipline.weight <= 0.0) {
            return Result.error("O peso da disciplina deve ser maior que zero")
        }
        return try {
            disciplineRepository.insert(discipline)
        } catch (e: Exception) {
            Result.error("Falha ao criar disciplina: ${e.localizedMessage}", e)
        }
    }
}
