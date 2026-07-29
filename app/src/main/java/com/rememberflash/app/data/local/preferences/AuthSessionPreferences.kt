package com.rememberflash.app.data.local.preferences

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Responsável apenas pela sessão: token JWT e sua janela de validade.
 */
internal class AuthSessionPreferences(
    private val prefs: SharedPreferences
) {
    fun saveToken(token: String) {
        prefs.edit {
            putString(PreferenceKeys.JWT_TOKEN, token)
            putLong(PreferenceKeys.LOGIN_TIMESTAMP, System.currentTimeMillis())
        }
    }

    fun getToken(): String? = prefs.getString(PreferenceKeys.JWT_TOKEN, null)

    fun isSessionValid(): Boolean {
        val token = getToken()
        if (token.isNullOrBlank()) return false

        val loginTimestamp = prefs.getLong(PreferenceKeys.LOGIN_TIMESTAMP, 0L)
        val elapsed = System.currentTimeMillis() - loginTimestamp
        return elapsed in 0L..SESSION_DURATION_MS
    }

    fun clearToken() {
        prefs.edit {
            remove(PreferenceKeys.JWT_TOKEN)
            remove(PreferenceKeys.LOGIN_TIMESTAMP)
        }
    }

    private companion object {
        const val SESSION_DURATION_MS = 24L * 60 * 60 * 1000L // 24 horas
    }
}
