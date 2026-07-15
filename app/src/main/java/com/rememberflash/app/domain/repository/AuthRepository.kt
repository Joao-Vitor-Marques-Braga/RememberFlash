package com.rememberflash.app.domain.repository

import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.User

interface AuthRepository {
    suspend fun saveSession(token: String, user: User)
    suspend fun getCurrentSession(): Result<User>
    suspend fun clearSession()
    suspend fun isSessionValid(): Boolean
    suspend fun saveGeminiApiKey(apiKey: String)
    suspend fun getGeminiApiKey(): String?
    suspend fun hasGeminiApiKey(): Boolean
}
