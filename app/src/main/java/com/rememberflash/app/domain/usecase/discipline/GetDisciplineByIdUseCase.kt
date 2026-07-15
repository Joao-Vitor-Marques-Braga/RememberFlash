package com.rememberflash.app.domain.usecase.discipline

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.repository.DisciplineRepository
import javax.inject.Inject

class GetDisciplineByIdUseCase @Inject constructor(
    private val disciplineRepository: DisciplineRepository
) {
    suspend operator fun invoke(disciplineId: Long): Result<Discipline> {
        if (disciplineId <= 0L) {
            return Result.error("ID de disciplina inválido")
        }
        return try {
            disciplineRepository.getById(disciplineId)
        } catch (e: Exception) {
            Result.error("Falha ao buscar disciplina: ${e.localizedMessage}", e)
        }
    }
}
