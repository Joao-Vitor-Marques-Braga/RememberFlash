package com.rememberflash.app.domain.usecase.auth

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.exception.AuthException
import com.rememberflash.app.domain.repository.UserRepository
import java.security.MessageDigest
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Redefine a senha do usuário após validação de segurança (RF002):
     * 1) Validação do código de segurança (OTP) de 6 dígitos.
     * 2) Validação da janela temporal de expiração (máximo 15 minutos).
     * 3) Validação das regras de complexidade da nova senha (mínimo 8 caracteres, alfanumérico e símbolo).
     * 4) Atualização do hash SHA-256 no Room e limpeza do OTP.
     */
    suspend operator fun invoke(
        email: String,
        otp: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        val cleanOtp = otp.trim()

        if (cleanEmail.isBlank()) {
            return Result.error("E-mail é obrigatório")
        }

        if (cleanOtp.isBlank() || cleanOtp.length != 6) {
            return Result.error("O código de segurança deve conter 6 dígitos")
        }

        if (newPassword != confirmPassword) {
            return Result.error("As senhas não coincidem")
        }

        return try {
            // 1) Validação do OTP e tempo de expiração (15 minutos)
            val resetData = userRepository.getPasswordResetOtp(cleanEmail)
                ?: throw AuthException("Nenhum código de recuperação solicitado para este e-mail")

            val (savedOtp, expiresAt) = resetData

            if (savedOtp != cleanOtp) {
                throw AuthException("Código de segurança inválido")
            }

            if (System.currentTimeMillis() > expiresAt) {
                userRepository.clearPasswordResetOtp(cleanEmail)
                throw AuthException("Código de segurança expirado. Solicite um novo código.")
            }

            // 2) Validação de complexidade de senha
            validatePasswordComplexity(newPassword)

            // 3) Geração do hash SHA-256 da nova senha
            val passwordHash = hashSha256(newPassword)

            // 4) Atualização no Room através do UserRepository / UserDao
            val updated = userRepository.updatePassword(cleanEmail, passwordHash)
            if (!updated) {
                throw AuthException("Usuário não encontrado para atualização de senha")
            }

            // 5) Limpeza do OTP utilizado
            userRepository.clearPasswordResetOtp(cleanEmail)

            Result.success(Unit)
        } catch (e: AuthException) {
            Result.error(e.message ?: "Falha ao redefinir senha", e)
        } catch (e: Exception) {
            Result.error("Erro ao redefinir senha: ${e.localizedMessage}", e)
        }
    }

    /**
     * Valida a complexidade da senha exigida:
     * - Mínimo de 8 caracteres
     * - Contém letras e números (alfanumérico)
     * - Contém pelo menos um símbolo / caractere especial
     */
    fun validatePasswordComplexity(password: String) {
        if (password.length < 8) {
            throw AuthException("A nova senha deve ter no mínimo 8 caracteres")
        }

        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            throw AuthException("A nova senha deve conter caracteres alfanuméricos (letras e números)")
        }

        val hasSymbol = password.any { !it.isLetterOrDigit() }
        if (!hasSymbol) {
            throw AuthException("A nova senha deve conter pelo menos um símbolo especial (ex: @, #, $, !)")
        }
    }

    private fun hashSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.trim().toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
