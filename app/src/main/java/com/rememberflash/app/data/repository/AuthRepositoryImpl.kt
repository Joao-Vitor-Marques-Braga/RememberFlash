package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.preferences.RegisteredUser
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val preferencesManager: SecurePreferencesManager
) : AuthRepository {

    override suspend fun saveSession(token: String, user: User) {
        preferencesManager.saveToken(token)
        preferencesManager.saveUser(user)
    }

    override suspend fun getCurrentSession(): Result<User> {
        val user = preferencesManager.getUser()
            ?: return Result.error("Nenhuma sessão ativa encontrada")
        return Result.success(user)
    }

    override suspend fun clearSession() {
        preferencesManager.clear()
    }

    override suspend fun isSessionValid(): Boolean {
        return preferencesManager.hasValidSession()
    }

    override suspend fun saveGeminiApiKey(apiKey: String) {
        preferencesManager.saveGeminiApiKey(apiKey)
    }

    override suspend fun getGeminiApiKey(): String? {
        return preferencesManager.getGeminiApiKey()
    }

    override suspend fun hasGeminiApiKey(): Boolean {
        return preferencesManager.hasGeminiApiKey()
    }

    override suspend fun registerUser(name: String, cpf: String, email: String, passwordKey: String): Result<User> {
        return try {
            val user = User(
                id = UUID.randomUUID().toString(),
                name = name,
                email = email,
                cpf = cpf
            )
            val success = preferencesManager.saveRegisteredUser(RegisteredUser(user, passwordKey))
            if (success) {
                Result.success(user)
            } else {
                Result.error("E-mail ou CPF já cadastrado.")
            }
        } catch (e: Exception) {
            Result.error("Erro ao registrar: ${e.localizedMessage}", e)
        }
    }

    override suspend fun authenticateUser(email: String, passwordKey: String): Result<User> {
        return try {
            val registered = preferencesManager.findRegisteredUser(email)
                ?: return Result.error("Usuário não cadastrado. Por favor, registre-se primeiro.")
            
            if (registered.passwordKey != passwordKey) {
                return Result.error("Senha incorreta.")
            }
            Result.success(registered.user)
        } catch (e: Exception) {
            Result.error("Erro ao autenticar: ${e.localizedMessage}", e)
        }
    }

    override suspend fun changePassword(currentPasswordKey: String, newPasswordKey: String): Result<Unit> {
        return try {
            val userSession = getCurrentSession().getOrNull()
                ?: return Result.error("Sessão ativa não encontrada.")
            
            val registered = preferencesManager.findRegisteredUser(userSession.email)
                ?: return Result.error("Usuário não encontrado no cadastro.")
            
            if (registered.passwordKey != currentPasswordKey) {
                return Result.error("Senha atual incorreta.")
            }
            
            val success = preferencesManager.updateRegisteredUserPassword(userSession.email, newPasswordKey)
            if (success) {
                Result.success(Unit)
            } else {
                Result.error("Erro ao atualizar a senha no armazenamento local.")
            }
        } catch (e: Exception) {
            Result.error("Erro ao alterar senha: ${e.localizedMessage}", e)
        }
    }

    override suspend fun changeEmail(newEmail: String, passwordKey: String): Result<Unit> {
        return try {
            val userSession = getCurrentSession().getOrNull()
                ?: return Result.error("Sessão ativa não encontrada.")
            
            val registered = preferencesManager.findRegisteredUser(userSession.email)
                ?: return Result.error("Usuário não encontrado no cadastro.")
            
            if (registered.passwordKey != passwordKey) {
                return Result.error("Senha incorreta.")
            }
            
            val emailExists = preferencesManager.getRegisteredUsers().any {
                it.user.email.equals(newEmail, ignoreCase = true) && !it.user.email.equals(userSession.email, ignoreCase = true)
            }
            if (emailExists) {
                return Result.error("E-mail já cadastrado por outro usuário.")
            }
            
            val success = preferencesManager.updateRegisteredUserEmail(userSession.email, newEmail)
            if (success) {
                val updatedUser = userSession.copy(email = newEmail)
                preferencesManager.saveUser(updatedUser)
                Result.success(Unit)
            } else {
                Result.error("Erro ao atualizar o e-mail no armazenamento local.")
            }
        } catch (e: Exception) {
            Result.error("Erro ao alterar e-mail: ${e.localizedMessage}", e)
        }
    }
}
