package com.rememberflash.app.domain.usecase.question

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.DisciplineRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GenerateContestMockExamUseCase @Inject constructor(
    private val disciplineRepository: DisciplineRepository,
    private val generateQuestionsUseCase: GenerateQuestionsUseCase
) {

    suspend operator fun invoke(
        contestId: Long,
        onProgress: (Int, Int, String) -> Unit
    ): Result<Unit> {
        return try {
            // Obtém todas as disciplinas ativas do concurso
            val disciplines = disciplineRepository.getByContest(contestId).first()
                .filter { it.isActive }

            if (disciplines.isEmpty()) {
                return Result.error("O concurso não possui nenhuma disciplina cadastrada.")
            }

            val total = disciplines.size
            disciplines.forEachIndexed { index, discipline ->
                val step = index + 1
                onProgress(step, total, discipline.name)

                // Quantidade proporcional do edital (weight) ou fallback para 5
                val quantity = if (discipline.weight > 0.0) discipline.weight.toInt() else 5
                
                // Dispara a geração de questões daquela disciplina
                val result = generateQuestionsUseCase(
                    disciplineId = discipline.id,
                    quantity = quantity,
                    theme = null
                )

                if (result is Result.Error) {
                    return Result.error("Falha ao gerar questões para ${discipline.name}: ${result.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Erro ao gerar simulado completo: ${e.localizedMessage}", e)
        }
    }
}
