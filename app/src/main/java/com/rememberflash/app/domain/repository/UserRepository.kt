package com.rememberflash.app.domain.repository

import com.rememberflash.app.domain.model.User

interface UserRepository {
    suspend fun isCpfRegistered(cpf: String): Boolean
    suspend fun findByCpf(cpf: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun saveUser(user: User, passwordHash: String? = null)
    suspend fun updatePassword(email: String, newPasswordHash: String): Boolean
    suspend fun savePasswordResetOtp(email: String, otp: String, expiresAt: Long)
    suspend fun getPasswordResetOtp(email: String): Pair<String, Long>?
    suspend fun clearPasswordResetOtp(email: String)
}
