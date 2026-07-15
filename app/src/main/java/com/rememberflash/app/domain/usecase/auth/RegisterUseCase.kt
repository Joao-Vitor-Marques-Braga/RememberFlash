package com.rememberflash.app.domain.usecase.auth

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import java.util.UUID
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Valida as regras de negócio de cadastro, incluindo a RN01 (Perfil Único por CPF).
     * Como estamos em MVP local (sem backend), simulamos que CPFs iniciados com "999"
     * já existem no sistema, retornando erro.
     */
    suspend operator fun invoke(name: String, cpf: String, email: String, password: String): Result<Unit> {
        val cleanCpf = cpf.filter { it.isDigit() }
        
        if (cleanCpf.length != 11) {
            return Result.error("CPF deve conter 11 dígitos")
        }
        
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.error("Todos os campos são obrigatórios")
        }

        // RN01 - Simulação de verificação de unicidade no banco de dados
        if (cleanCpf.startsWith("999")) {
            return Result.error("RN01: Este CPF já está cadastrado no sistema. Permitido apenas um perfil por pessoa.")
        }

        // Sucesso: cria o usuário simulado e salva a sessão
        return try {
            val regResult = authRepository.registerUser(name = name, cpf = cleanCpf, email = email, passwordKey = password)
            when (regResult) {
                is Result.Success -> {
                    authRepository.saveSession(token = "mock_token_register", user = regResult.data)
                    Result.success(Unit)
                }
                is Result.Error -> Result.error(regResult.message)
                else -> Result.error("Erro desconhecido")
            }
        } catch (e: Exception) {
            Result.error("Erro ao salvar perfil: ${e.localizedMessage}")
        }
    }
}
