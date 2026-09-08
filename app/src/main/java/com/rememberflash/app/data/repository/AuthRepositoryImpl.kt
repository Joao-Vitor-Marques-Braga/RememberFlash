package com.rememberflash.app.data.repository

import android.util.Log
import com.rememberflash.app.BuildConfig
import com.rememberflash.app.data.local.preferences.RegisteredUser
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.data.remote.supabase.dto.UserSupabaseDto
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import io.github.jan.supabase.postgrest.Postgrest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val preferencesManager: SecurePreferencesManager,
    private val postgrest: Postgrest
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
        if (BuildConfig.SUPABASE_URL.isNotBlank()) {
            try {
                val currentUser = preferencesManager.getUser()
                if (currentUser != null) {
                    postgrest.from("users").update({
                        set("gemini_api_key", apiKey)
                    }) {
                        filter { eq("id", currentUser.id) }
                    }
                }
            } catch (e: Exception) {
                Log.w("AuthRepositoryImpl", "Could not sync geminiApiKey to Supabase: ${e.localizedMessage}")
            }
        }
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
            if (!success) {
                return Result.error("E-mail ou CPF já cadastrado localmente.")
            }

            // Sync to Supabase
            if (BuildConfig.SUPABASE_URL.isNotBlank()) {
                try {
                    val dto = UserSupabaseDto(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        cpf = user.cpf,
                        passwordHash = passwordKey,
                        geminiApiKey = preferencesManager.getGeminiApiKey()
                    )
                    postgrest.from("users").upsert(dto)
                    Log.d("AuthRepositoryImpl", "User successfully registered in Supabase: ${user.email}")
                } catch (e: Exception) {
                    Log.e("AuthRepositoryImpl", "Failed to sync user to Supabase: ${e.localizedMessage}", e)
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.error("Erro ao registrar: ${e.localizedMessage}", e)
        }
    }

    override suspend fun authenticateUser(email: String, passwordKey: String): Result<User> {
        return try {
            val localRegistered = preferencesManager.findRegisteredUser(email)
            if (localRegistered != null) {
                if (localRegistered.passwordKey != passwordKey) {
                    return Result.error("Senha incorreta.")
                }
                return Result.success(localRegistered.user)
            }

            // Fallback: check in Supabase if user registered from another device
            if (BuildConfig.SUPABASE_URL.isNotBlank()) {
                try {
                    val remoteUser = postgrest.from("users").select {
                        filter { eq("email", email) }
                    }.decodeSingleOrNull<UserSupabaseDto>()

                    if (remoteUser != null) {
                        if (remoteUser.passwordHash != passwordKey) {
                            return Result.error("Senha incorreta.")
                        }
                        val user = User(
                            id = remoteUser.id,
                            name = remoteUser.name,
                            email = remoteUser.email,
                            cpf = remoteUser.cpf
                        )
                        // Cache locally for offline access
                        preferencesManager.saveRegisteredUser(RegisteredUser(user, passwordKey))
                        remoteUser.geminiApiKey?.let { preferencesManager.saveGeminiApiKey(it) }
                        return Result.success(user)
                    }
                } catch (e: Exception) {
                    Log.w("AuthRepositoryImpl", "Supabase authentication fallback error: ${e.localizedMessage}")
                }
            }

            Result.error("Usuário não cadastrado. Por favor, registre-se primeiro.")
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
                if (BuildConfig.SUPABASE_URL.isNotBlank()) {
                    try {
                        postgrest.from("users").update({
                            set("password_hash", newPasswordKey)
                        }) {
                            filter { eq("email", userSession.email) }
                        }
                    } catch (e: Exception) {
                        Log.w("AuthRepositoryImpl", "Failed to update password in Supabase: ${e.localizedMessage}")
                    }
                }
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

                if (BuildConfig.SUPABASE_URL.isNotBlank()) {
                    try {
                        postgrest.from("users").update({
                            set("email", newEmail)
                        }) {
                            filter { eq("id", updatedUser.id) }
                        }
                    } catch (e: Exception) {
                        Log.w("AuthRepositoryImpl", "Failed to update email in Supabase: ${e.localizedMessage}")
                    }
                }

                Result.success(Unit)
            } else {
                Result.error("Erro ao atualizar o e-mail no armazenamento local.")
            }
        } catch (e: Exception) {
            Result.error("Erro ao alterar e-mail: ${e.localizedMessage}", e)
        }
    }
}
