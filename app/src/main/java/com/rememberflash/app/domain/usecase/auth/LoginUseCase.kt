package com.rememberflash.app.domain.usecase.auth

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Persiste a sessão local com token JWT criptografado via EncryptedSharedPreferences.
     * Nesta fase inicial (MVP), o token é recebido de um backend externo futuro.
     * Para desenvolvimento, aceita qualquer token não-vazio e cria sessão local.
     */
    suspend operator fun invoke(email: String, passwordKey: String): Result<Unit> {
        if (email.isBlank() || passwordKey.isBlank()) {
            return Result.error("E-mail e senha são obrigatórios")
        }
        return try {
            val authResult = authRepository.authenticateUser(email, passwordKey)
            when (authResult) {
                is Result.Success -> {
                    authRepository.saveSession(token = "mock_token_login", user = authResult.data)
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
