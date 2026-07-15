package com.rememberflash.app.domain.usecase.auth

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Falha ao encerrar sessão: ${e.localizedMessage}", e)
        }
    }
}
