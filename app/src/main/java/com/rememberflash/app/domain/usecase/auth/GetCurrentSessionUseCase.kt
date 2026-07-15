package com.rememberflash.app.domain.usecase.auth

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return try {
            if (!authRepository.isSessionValid()) {
                return Result.error("Sessão expirada ou inexistente")
            }
            authRepository.getCurrentSession()
        } catch (e: Exception) {
            Result.error("Falha ao recuperar sessão: ${e.localizedMessage}", e)
        }
    }
}
