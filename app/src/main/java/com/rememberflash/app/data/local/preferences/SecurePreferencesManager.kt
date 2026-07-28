package com.rememberflash.app.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.rememberflash.app.domain.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de preferências seguras via EncryptedSharedPreferences (RNF05).
 * Armazena token JWT, dados do usuário e API Key do Gemini (BYOK)
 * com criptografia AES-256 no keystore do Android.
 */
@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        prefs.edit()
            .putString(KEY_JWT_TOKEN, token)
            .putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_JWT_TOKEN, null)

    fun saveUser(user: User) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_CPF, user.cpf)
            .apply()
    }

    fun getUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val cpf = prefs.getString(KEY_USER_CPF, "") ?: ""
        return User(id = id, name = name, email = email, cpf = cpf)
    }

    fun saveGeminiApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, apiKey).apply()
    }

    fun getGeminiApiKey(): String? = prefs.getString(KEY_GEMINI_API_KEY, null)

    fun hasGeminiApiKey(): Boolean = !getGeminiApiKey().isNullOrBlank()

    fun hasValidSession(): Boolean {
        val token = getToken()
        val user = getUser()
        val timestamp = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0L)
        if (token.isNullOrBlank() || user == null) return false
        
        val diff = System.currentTimeMillis() - timestamp
        val twentyFourHoursMs = 24L * 60 * 60 * 1000L
        return diff in 0L..twentyFourHoursMs
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private val gson = Gson()

    fun getRegisteredUsers(): List<RegisteredUser> {
        val json = prefs.getString(KEY_REGISTERED_USERS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<RegisteredUser>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveRegisteredUser(registeredUser: RegisteredUser): Boolean {
        val users = getRegisteredUsers().toMutableList()
        if (users.any { it.user.email.equals(registeredUser.user.email, ignoreCase = true) || it.user.cpf == registeredUser.user.cpf }) {
            return false
        }
        users.add(registeredUser)
        val json = gson.toJson(users)
        prefs.edit().putString(KEY_REGISTERED_USERS, json).apply()
        return true
    }

    fun findRegisteredUser(email: String): RegisteredUser? {
        return getRegisteredUsers().firstOrNull { it.user.email.equals(email, ignoreCase = true) }
    }

    fun saveDifficulty(difficulty: String) {
        prefs.edit().putString(KEY_DIFFICULTY, difficulty).apply()
    }

    fun getDifficulty(): String = prefs.getString(KEY_DIFFICULTY, "Médio") ?: "Médio"

    fun saveRigor(rigor: String) {
        prefs.edit().putString(KEY_RIGOR, rigor).apply()
    }

    fun getRigor(): String = prefs.getString(KEY_RIGOR, "Padrão") ?: "Padrão"

    fun saveTone(tone: String) {
        prefs.edit().putString(KEY_TONE, tone).apply()
    }

    fun getTone(): String = prefs.getString(KEY_TONE, "Explicativo") ?: "Explicativo"

    companion object {
        private const val PREFS_FILE_NAME = "remember_flash_secure_prefs"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_CPF = "user_cpf"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_REGISTERED_USERS = "registered_users"
        private const val KEY_DIFFICULTY = "ai_difficulty"
        private const val KEY_RIGOR = "ai_rigor"
        private const val KEY_TONE = "ai_tone"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
    }
}

data class RegisteredUser(
    val user: User,
    val passwordKey: String
)
