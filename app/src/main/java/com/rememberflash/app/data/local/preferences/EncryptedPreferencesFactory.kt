package com.rememberflash.app.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Responsável apenas por criar um [SharedPreferences] criptografado.
 *
 * Isola a lógica de recuperação (arquivo corrompido -> apaga e recria ->
 * se falhar de novo, cai para SharedPreferences comum) que antes estava
 * duplicada dentro do construtor de SecurePreferencesManager.
 */
internal object EncryptedPreferencesFactory {

    private const val TAG = "EncryptedPreferencesFactory"

    fun create(context: Context, fileName: String): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return runCatching { buildEncryptedPrefs(context, fileName, masterKey) }
            .getOrElse { firstError ->
                Log.e(TAG, "Falha ao inicializar EncryptedSharedPreferences, tentando apagar arquivo corrompido", firstError)
                recreateAfterCorruption(context, fileName, masterKey)
            }
    }

    private fun recreateAfterCorruption(
        context: Context,
        fileName: String,
        masterKey: MasterKey
    ): SharedPreferences {
        context.deleteSharedPreferences(fileName)
        return runCatching { buildEncryptedPrefs(context, fileName, masterKey) }
            .getOrElse { secondError ->
                Log.e(TAG, "Falha catastrófica ao reinicializar EncryptedSharedPreferences, usando fallback padrão", secondError)
                context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            }
    }

    private fun buildEncryptedPrefs(
        context: Context,
        fileName: String,
        masterKey: MasterKey
    ): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        fileName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
