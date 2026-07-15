package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.AuthRepository
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
}
