package com.rememberflash.app.domain.usecase.discipline

import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.repository.DisciplineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDisciplinesByContestUseCase @Inject constructor(
    private val disciplineRepository: DisciplineRepository
) {
    operator fun invoke(contestId: Long): Flow<List<Discipline>> {
        return disciplineRepository.getByContest(contestId)
    }
}
