package com.rememberflash.app.data.repository

import com.rememberflash.app.data.local.database.dao.UserDao
import com.rememberflash.app.data.local.database.entity.UserEntity
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.domain.model.User
import com.rememberflash.app.domain.repository.UserRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val securePreferencesManager: SecurePreferencesManager
) : UserRepository {

    // Cache em memória thread-safe para tokens OTP de recuperação de senha
    private val resetOtpCache = ConcurrentHashMap<String, Pair<String, Long>>()

    override suspend fun isCpfRegistered(cpf: String): Boolean {
        val cleanCpf = cpf.filter { it.isDigit() }
        val count = userDao.countByCpf(cleanCpf)
        if (count > 0) return true
        return securePreferencesManager.getRegisteredUsers().any { it.user.cpf == cleanCpf }
    }

    override suspend fun findByCpf(cpf: String): User? {
        val cleanCpf = cpf.filter { it.isDigit() }
        val entity = userDao.findByCpf(cleanCpf)
        if (entity != null) {
            return User(
                id = entity.id,
                name = entity.name,
                email = entity.email,
                cpf = entity.cpf,
                createdAt = entity.createdAt
            )
        }
        return securePreferencesManager.getRegisteredUsers().firstOrNull { it.user.cpf == cleanCpf }?.user
    }

    override suspend fun findByEmail(email: String): User? {
        val cleanEmail = email.trim().lowercase()
        val entity = userDao.findByEmail(cleanEmail)
        if (entity != null) {
            return User(
                id = entity.id,
                name = entity.name,
                email = entity.email,
                cpf = entity.cpf,
                createdAt = entity.createdAt
            )
        }
        return securePreferencesManager.findRegisteredUser(cleanEmail)?.user
    }

    override suspend fun saveUser(user: User, passwordHash: String?) {
        userDao.insert(
            UserEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                cpf = user.cpf,
                passwordHash = passwordHash,
                createdAt = user.createdAt
            )
        )
    }

    override suspend fun updatePassword(email: String, newPasswordHash: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        // 1. Atualiza no Room
        val rowsAffected = userDao.updatePassword(cleanEmail, newPasswordHash)
        
        // 2. Atualiza no SecurePreferences (cache local)
        val prefUpdated = securePreferencesManager.updateRegisteredUserPassword(cleanEmail, newPasswordHash)

        return rowsAffected > 0 || prefUpdated
    }

    override suspend fun savePasswordResetOtp(email: String, otp: String, expiresAt: Long) {
        val cleanEmail = email.trim().lowercase()
        resetOtpCache[cleanEmail] = Pair(otp, expiresAt)
    }

    override suspend fun getPasswordResetOtp(email: String): Pair<String, Long>? {
        val cleanEmail = email.trim().lowercase()
        return resetOtpCache[cleanEmail]
    }

    override suspend fun clearPasswordResetOtp(email: String) {
        val cleanEmail = email.trim().lowercase()
        resetOtpCache.remove(cleanEmail)
    }
}
