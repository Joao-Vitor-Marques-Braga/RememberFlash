package com.rememberflash.app.data.local.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class RegisteredUser(
    val user: com.rememberflash.app.domain.model.User,
    val passwordKey: String
)

/**
 * Responsável apenas pelo cadastro local de usuários (lista serializada em JSON).
 */
internal class RegisteredUsersPreferences(
    private val prefs: SharedPreferences,
    private val gson: Gson = Gson()
) {
    private val listType = object : TypeToken<List<RegisteredUser>>() {}.type

    fun getAll(): List<RegisteredUser> {
        val json = prefs.getString(PreferenceKeys.REGISTERED_USERS, null) ?: return emptyList()
        return runCatching { gson.fromJson<List<RegisteredUser>>(json, listType) }
            .getOrNull()
            .orEmpty()
    }

    /**
     * Adiciona um novo usuário cadastrado.
     * @return false se já existir um cadastro com o mesmo e-mail ou CPF.
     */
    fun add(registeredUser: RegisteredUser): Boolean {
        val users = getAll()
        if (users.any { it.isSamePerson(registeredUser) }) return false

        val updated = users + registeredUser
        prefs.edit { putString(PreferenceKeys.REGISTERED_USERS, gson.toJson(updated)) }
        return true
    }

    fun findByEmail(email: String): RegisteredUser? =
        getAll().firstOrNull { it.user.email.equals(email, ignoreCase = true) }

    fun updatePassword(email: String, newPasswordKey: String): Boolean {
        val users = getAll().toMutableList()
        val index = users.indexOfFirst { it.user.email.equals(email, ignoreCase = true) }
        if (index == -1) return false
        val currentUser = users[index]
        users[index] = currentUser.copy(passwordKey = newPasswordKey)
        prefs.edit { putString(PreferenceKeys.REGISTERED_USERS, gson.toJson(users)) }
        return true
    }

    private fun RegisteredUser.isSamePerson(other: RegisteredUser): Boolean =
        user.email.equals(other.user.email, ignoreCase = true) || user.cpf == other.user.cpf
}
