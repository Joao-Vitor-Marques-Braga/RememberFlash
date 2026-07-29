package com.rememberflash.app.data.local.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import com.rememberflash.app.domain.model.User

/**
 * Responsável apenas pelos dados do usuário logado no momento.
 *
 * Mantém o mesmo formato de armazenamento (campos separados) da versão
 * original, para não exigir migração de dados já persistidos.
 */
internal class UserPreferences(
    private val prefs: SharedPreferences
) {
    fun save(user: User) {
        prefs.edit {
            putString(PreferenceKeys.USER_ID, user.id)
            putString(PreferenceKeys.USER_NAME, user.name)
            putString(PreferenceKeys.USER_EMAIL, user.email)
            putString(PreferenceKeys.USER_CPF, user.cpf)
        }
    }

    fun get(): User? {
        val id = prefs.getString(PreferenceKeys.USER_ID, null) ?: return null
        return User(
            id = id,
            name = prefs.getString(PreferenceKeys.USER_NAME, "").orEmpty(),
            email = prefs.getString(PreferenceKeys.USER_EMAIL, "").orEmpty(),
            cpf = prefs.getString(PreferenceKeys.USER_CPF, "").orEmpty()
        )
    }

    fun clear() {
        prefs.edit {
            remove(PreferenceKeys.USER_ID)
            remove(PreferenceKeys.USER_NAME)
            remove(PreferenceKeys.USER_EMAIL)
            remove(PreferenceKeys.USER_CPF)
        }
    }
}
