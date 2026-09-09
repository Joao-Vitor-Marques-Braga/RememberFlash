package com.rememberflash.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.rememberflash.app.BuildConfig
import com.rememberflash.app.data.local.preferences.RegisteredUser
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.data.remote.supabase.dto.UserSupabaseDto
import com.rememberflash.app.data.sync.SyncManager
import com.rememberflash.app.data.util.AesEncryptionUtil
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.Postgrest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: SecurePreferencesManager,
    private val postgrest: Postgrest,
    private val syncManager: SyncManager
) : AuthRepository {

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isSupabaseConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL.isNotBlank() &&
                !BuildConfig.SUPABASE_URL.contains("placeholder.supabase.co") &&
                BuildConfig.SUPABASE_ANON_KEY.isNotBlank() &&
                BuildConfig.SUPABASE_ANON_KEY != "placeholder-anon-key"
    }

    override suspend fun saveSession(token: String, user: User) {
        preferencesManager.saveToken(token)
        preferencesManager.saveUser(user)
        syncManager.triggerSync(user.id)
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
        if (isSupabaseConfigured() && isOnline()) {
            try {
                val currentUser = preferencesManager.getUser()
                if (currentUser != null) {
                    val encryptedKey = AesEncryptionUtil.encrypt(apiKey)
                    postgrest.from("users").update({
                        set("gemini_api_key", encryptedKey)
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

    private fun hashSha256(input: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.trim().toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    override suspend fun registerUser(name: String, cpf: String, email: String, passwordKey: String): Result<User> {
        return try {
            val cleanCpf = cpf.filter { it.isDigit() }
            val hashedCpf = hashSha256(cleanCpf)
            val hashedPassword = hashSha256(passwordKey)
            val online = isOnline() && isSupabaseConfigured()

            if (online) {
                // 1. ONLINE: Verifica direto no Supabase
                val existingEmail = try {
                    postgrest.from("users").select {
                        filter { eq("email", email.trim().lowercase()) }
                    }.decodeSingleOrNull<UserSupabaseDto>()
                } catch (e: Exception) {
                    Log.w("AuthRepositoryImpl", "Erro ao verificar e-mail online: ${e.localizedMessage}")
                    null
                }

                if (existingEmail != null) {
                    return Result.error("Este e-mail já está cadastrado. Por favor, faça login.")
                }

                val existingCpf = try {
                    postgrest.from("users").select {
                        filter { eq("cpf", hashedCpf) }
                    }.decodeSingleOrNull<UserSupabaseDto>()
                } catch (e: Exception) {
                    Log.w("AuthRepositoryImpl", "Erro ao verificar CPF online: ${e.localizedMessage}")
                    null
                }

                if (existingCpf != null) {
                    return Result.error("Este CPF já está cadastrado em outra conta.")
                }

                val user = User(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    email = email.trim().lowercase(),
                    cpf = cleanCpf
                )

                // Salva no Supabase com password_hash, cpf (SHA-256) e gemini_api_key (AES-256)
                val encryptedGeminiKey = AesEncryptionUtil.encrypt(preferencesManager.getGeminiApiKey())
                val dto = UserSupabaseDto(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    cpf = hashedCpf,
                    passwordHash = hashedPassword,
                    geminiApiKey = encryptedGeminiKey
                )
                postgrest.from("users").upsert(dto)
                Log.d("AuthRepositoryImpl", "Usuário criado com sucesso no Supabase: ${user.email}")

                // Salva no cache local seguro
                preferencesManager.saveRegisteredUser(RegisteredUser(user, hashedPassword))
                Result.success(user)
            } else {
                // 2. OFFLINE: Verifica no armazenamento local
                val localUser = preferencesManager.findRegisteredUser(email)
                if (localUser != null) {
                    return Result.error("E-mail já cadastrado localmente.")
                }

                val user = User(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    email = email.trim().lowercase(),
                    cpf = cleanCpf
                )
                val success = preferencesManager.saveRegisteredUser(RegisteredUser(user, hashedPassword))
                if (!success) {
                    return Result.error("E-mail ou CPF já cadastrado localmente.")
                }
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.error("Erro ao registrar: ${e.localizedMessage}", e)
        }
    }

    override suspend fun authenticateUser(email: String, passwordKey: String): Result<User> {
        return try {
            val hashedPassword = hashSha256(passwordKey)
            val online = isOnline() && isSupabaseConfigured()

            if (online) {
                // 1. ONLINE: Consulta direto no Supabase
                Log.d("AuthRepositoryImpl", "Consultando usuário diretamente na nuvem (Supabase)...")
                val remoteUser = try {
                    postgrest.from("users").select {
                        filter { eq("email", email.trim().lowercase()) }
                    }.decodeSingleOrNull<UserSupabaseDto>()
                } catch (e: Exception) {
                    Log.e("AuthRepositoryImpl", "Falha na requisição ao Supabase: ${e.localizedMessage}", e)
                    null
                }

                if (remoteUser != null) {
                    // Aceita o hash SHA-256 ou texto legado caso exista
                    if (remoteUser.passwordHash != hashedPassword && remoteUser.passwordHash != passwordKey) {
                        return Result.error("Senha incorreta.")
                    }
                    val user = User(
                        id = remoteUser.id,
                        name = remoteUser.name,
                        email = remoteUser.email,
                        cpf = remoteUser.cpf
                    )
                    // Atualiza cache local para acesso offline futuro
                    preferencesManager.saveRegisteredUser(RegisteredUser(user, hashedPassword))
                    
                    // Descriptografa a chave do Gemini caso venha do Supabase
                    val decryptedGeminiKey = AesEncryptionUtil.decrypt(remoteUser.geminiApiKey)
                    decryptedGeminiKey?.let { preferencesManager.saveGeminiApiKey(it) }

                    return Result.success(user)
                }

                // Se consultou na nuvem e não encontrou, e não há no local
                val localRegistered = preferencesManager.findRegisteredUser(email)
                if (localRegistered == null) {
                    return Result.error("Usuário não cadastrado. Por favor, registre-se primeiro.")
                }
            }

            // 2. OFFLINE (ou fallback caso não encontre rede): Consulta local
            Log.d("AuthRepositoryImpl", "Consultando usuário no armazenamento local (offline)...")
            val localRegistered = preferencesManager.findRegisteredUser(email)
            if (localRegistered != null) {
                if (localRegistered.passwordKey != hashedPassword && localRegistered.passwordKey != passwordKey) {
                    return Result.error("Senha incorreta.")
                }
                return Result.success(localRegistered.user)
            }

            if (!isOnline()) {
                Result.error("Você está offline e este usuário não foi encontrado neste dispositivo.")
            } else {
                Result.error("Usuário não cadastrado. Por favor, registre-se primeiro.")
            }
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

            val currentHash = hashSha256(currentPasswordKey)
            val newHash = hashSha256(newPasswordKey)

            if (registered.passwordKey != currentHash && registered.passwordKey != currentPasswordKey) {
                return Result.error("Senha atual incorreta.")
            }

            val success = preferencesManager.updateRegisteredUserPassword(userSession.email, newHash)
            if (success) {
                if (isSupabaseConfigured() && isOnline()) {
                    try {
                        postgrest.from("users").update({
                            set("password_hash", newHash)
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
