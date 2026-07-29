package com.rememberflash.app.data.local.preferences

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Responsável apenas pela API Key do Gemini fornecida pelo usuário (BYOK).
 */
internal class GeminiApiKeyPreferences(
    private val prefs: SharedPreferences
) {
    fun save(apiKey: String) {
        prefs.edit { putString(PreferenceKeys.GEMINI_API_KEY, apiKey) }
    }

    fun get(): String? = prefs.getString(PreferenceKeys.GEMINI_API_KEY, null)

    fun exists(): Boolean = !get().isNullOrBlank()

    fun clear() {
        prefs.edit { remove(PreferenceKeys.GEMINI_API_KEY) }
    }
}
