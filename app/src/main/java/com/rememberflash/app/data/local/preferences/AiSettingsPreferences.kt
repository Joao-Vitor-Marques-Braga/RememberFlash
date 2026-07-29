package com.rememberflash.app.data.local.preferences

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Responsável apenas pelas preferências de comportamento da IA.
 */
internal class AiSettingsPreferences(
    private val prefs: SharedPreferences
) {
    fun saveDifficulty(difficulty: AiDifficulty) {
        prefs.edit { putString(PreferenceKeys.AI_DIFFICULTY, difficulty.storageValue) }
    }

    fun getDifficulty(): AiDifficulty =
        AiDifficulty.fromStorageValue(prefs.getString(PreferenceKeys.AI_DIFFICULTY, null))

    fun saveRigor(rigor: AiRigor) {
        prefs.edit { putString(PreferenceKeys.AI_RIGOR, rigor.storageValue) }
    }

    fun getRigor(): AiRigor =
        AiRigor.fromStorageValue(prefs.getString(PreferenceKeys.AI_RIGOR, null))

    fun saveTone(tone: AiTone) {
        prefs.edit { putString(PreferenceKeys.AI_TONE, tone.storageValue) }
    }

    fun getTone(): AiTone =
        AiTone.fromStorageValue(prefs.getString(PreferenceKeys.AI_TONE, null))
}
