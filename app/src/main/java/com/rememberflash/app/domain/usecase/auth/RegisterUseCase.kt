package com.rememberflash.app.domain.usecase.auth

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.exception.AuthException
import com.rememberflash.app.domain.repository.AuthRepository
import com.rememberflash.app.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    /**
     * Valida as regras de negócio de cadastro:
     * 1) Validação canônica dos dígitos verificadores do CPF (Módulo 11) e rejeição de repetidos.
     * 2) Verificação de unicidade do CPF no banco Room via [userRepository.isCpfRegistered] (RN01).
     * 3) Criação do usuário e geração de token criptográfico UUID para a sessão.
     */
    suspend operator fun invoke(name: String, cpf: String, email: String, password: String): Result<Unit> {
        val cleanCpf = cpf.filter { it.isDigit() }

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.error("Todos os campos são obrigatórios")
        }

        if (password.length < 6) {
            return Result.error("A senha deve ter no mínimo 6 caracteres")
        }

        return try {
            // 1) Algoritmo canônico de validação dos dígitos verificadores do CPF (módulo 11)
            validateCpf(cleanCpf)

            // 2) RN01 - Consulta no Room para garantir CPF Único
            if (userRepository.isCpfRegistered(cleanCpf)) {
                throw AuthException("CPF já cadastrado no sistema")
            }

            // 3) Registro do usuário e persistência de token criptográfico
            val regResult = authRepository.registerUser(
                name = name.trim(),
                cpf = cleanCpf,
                email = email.trim().lowercase(),
                passwordKey = password
            )

            when (regResult) {
                is Result.Success -> {
                    val cryptoToken = UUID.randomUUID().toString()
                    authRepository.saveSession(token = cryptoToken, user = regResult.data)
                    userRepository.saveUser(regResult.data)
                    Result.success(Unit)
                }
                is Result.Error -> Result.error(regResult.message)
                else -> Result.error("Erro desconhecido")
            }
        } catch (e: AuthException) {
            Result.error(e.message ?: "Erro de autenticação", e)
        } catch (e: Exception) {
            Result.error("Erro ao salvar perfil: ${e.localizedMessage}", e)
        }
    }

    /**
     * Algoritmo canônico de validação de CPF (Módulo 11).
     * Rejeita CPFs com tamanho diferente de 11, sequências de números repetidos
     * ou dígitos verificadores matematicamente inválidos com exceções descritivas.
     */
    fun validateCpf(cleanCpf: String) {
        if (cleanCpf.length != 11) {
            throw AuthException("CPF deve conter exatamente 11 dígitos numéricos")
        }

        if (cleanCpf.all { it == cleanCpf[0] }) {
            throw AuthException("CPF inválido: número com todos os dígitos repetidos")
        }

        // 1º Dígito Verificador (D1)
        val sum1 = (0..8).sumOf { i -> cleanCpf[i].digitToInt() * (10 - i) }
        val rem1 = (sum1 * 10) % 11
        val expectedD1 = if (rem1 == 10 || rem1 == 11) 0 else rem1

        if (cleanCpf[9].digitToInt() != expectedD1) {
            throw AuthException("CPF inválido: primeiro dígito verificador incorreto")
        }

        // 2º Dígito Verificador (D2)
        val sum2 = (0..9).sumOf { i -> cleanCpf[i].digitToInt() * (11 - i) }
        val rem2 = (sum2 * 10) % 11
        val expectedD2 = if (rem2 == 10 || rem2 == 11) 0 else rem2

        if (cleanCpf[10].digitToInt() != expectedD2) {
            throw AuthException("CPF inválido: segundo dígito verificador incorreto")
        }
    }
}
