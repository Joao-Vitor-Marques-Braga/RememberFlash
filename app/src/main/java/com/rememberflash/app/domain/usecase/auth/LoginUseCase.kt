package com.rememberflash.app.domain.usecase.auth

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User
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
    suspend operator fun invoke(token: String, user: User): Result<Unit> {
        if (token.isBlank()) {
            return Result.error("Token de autenticação não pode ser vazio")
        }
        if (user.email.isBlank()) {
            return Result.error("E-mail do usuário é obrigatório")
        }
        return try {
            authRepository.saveSession(token, user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Falha ao salvar sessão: ${e.localizedMessage}", e)
        }
    }
}
