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
    suspend fun registerUser(name: String, cpf: String, email: String, passwordKey: String): Result<User>
    suspend fun authenticateUser(email: String, passwordKey: String): Result<User>
    suspend fun changePassword(currentPasswordKey: String, newPasswordKey: String): Result<Unit>
    suspend fun changeEmail(newEmail: String, passwordKey: String): Result<Unit>
}
