package com.rememberflash.app.domain.usecase.auth

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.exception.AuthException
import com.rememberflash.app.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.random.Random

class RequestPasswordResetUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Solicita redefinição de senha para o e-mail informado (RF002):
     * 1) Valida se o e-mail existe na base local (Room / UserRepository).
     * 2) Gera um OTP numérico aleatório de 6 dígitos.
     * 3) Define timestamp de expiração para 15 minutos a partir do momento atual.
     * 4) Salva o OTP e timestamp no repositório vinculado ao e-mail.
     */
    suspend operator fun invoke(email: String): Result<String> {
        val cleanEmail = email.trim().lowercase()

        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.error("Informe um e-mail válido")
        }

        return try {
            // 1) Validação da existência do e-mail no Room
            val user = userRepository.findByEmail(cleanEmail)
                ?: throw AuthException("E-mail não encontrado no sistema")

            // 2) Geração de OTP numérico de 6 dígitos
            val otp = Random.nextInt(100000, 999999).toString()

            // 3) Timestamp de expiração para 15 minutos (15 * 60 * 1000 ms)
            val expiresAt = System.currentTimeMillis() + (15 * 60 * 1000L)

            // 4) Persistência temporária do OTP vinculado ao e-mail
            userRepository.savePasswordResetOtp(cleanEmail, otp, expiresAt)

            Result.success(otp)
        } catch (e: AuthException) {
            Result.error(e.message ?: "Falha ao solicitar recuperação", e)
        } catch (e: Exception) {
            Result.error("Erro ao solicitar código de recuperação: ${e.localizedMessage}", e)
        }
    }
}
