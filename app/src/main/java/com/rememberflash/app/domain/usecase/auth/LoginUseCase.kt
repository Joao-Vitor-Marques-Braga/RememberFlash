package com.rememberflash.app.domain.usecase.auth

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.AuthRepository
import java.util.UUID
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Autentica as credenciais do usuário e persiste a sessão local com token criptográfico UUID via SecurePreferencesManager.
     */
    suspend operator fun invoke(email: String, passwordKey: String): Result<Unit> {
        if (email.isBlank() || passwordKey.isBlank()) {
            return Result.error("E-mail e senha são obrigatórios")
        }
        return try {
            val authResult = authRepository.authenticateUser(email, passwordKey)
            when (authResult) {
                is Result.Success -> {
                    val cryptoToken = UUID.randomUUID().toString()
                    authRepository.saveSession(token = cryptoToken, user = authResult.data)
                    Result.success(Unit)
                }
                is Result.Error -> Result.error(authResult.message)
                else -> Result.error("Erro desconhecido")
            }
        } catch (e: Exception) {
            Result.error("Falha ao autenticar: ${e.localizedMessage}", e)
        }
    }
}
