package com.rememberflash.app.domain.usecase.contest

import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.repository.ContestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para consultar a lista reativa de concursos inativos/arquivados do usuário (RF003 / RN06).
 */
class GetArchivedContestsUseCase @Inject constructor(
    private val contestRepository: ContestRepository
) {
    operator fun invoke(userId: String): Flow<List<Contest>> =
        contestRepository.getArchivedContestsByUser(userId)
}
