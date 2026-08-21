package com.rememberflash.app.data.local.preferences

import android.content.Context
import androidx.core.content.edit
import com.rememberflash.app.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fachada para o armazenamento seguro do app (RNF05).
 *
 * Cada responsabilidade (sessão, usuário, API key, cadastro local,
 * preferências de IA) vive em uma classe própria; esta fachada apenas
 * as compõe para manter uma única porta de entrada simples de injetar
 * via Hilt, sem expor os detalhes de SharedPreferences para o resto do app.
 */
@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = EncryptedPreferencesFactory.create(context, PreferenceKeys.PREFS_FILE_NAME)

    private val authSession = AuthSessionPreferences(prefs)
    private val userPreferences = UserPreferences(prefs)
    private val geminiApiKeyPreferences = GeminiApiKeyPreferences(prefs)
    private val registeredUsersPreferences = RegisteredUsersPreferences(prefs)
    private val aiSettingsPreferences = AiSettingsPreferences(prefs)

    // --- Sessão / token ---
    fun saveToken(token: String) = authSession.saveToken(token)
    fun getToken(): String? = authSession.getToken()
    fun hasValidSession(): Boolean = authSession.isSessionValid() && userPreferences.get() != null

    // --- Usuário logado ---
    fun saveUser(user: User) = userPreferences.save(user)
    fun getUser(): User? = userPreferences.get()

    // --- API key do Gemini (BYOK) ---
    fun saveGeminiApiKey(apiKey: String) = geminiApiKeyPreferences.save(apiKey)
    fun getGeminiApiKey(): String? = geminiApiKeyPreferences.get()
    fun hasGeminiApiKey(): Boolean = geminiApiKeyPreferences.exists()

    // --- Cadastro local de usuários ---
    fun getRegisteredUsers(): List<RegisteredUser> = registeredUsersPreferences.getAll()
    fun saveRegisteredUser(registeredUser: RegisteredUser): Boolean =
        registeredUsersPreferences.add(registeredUser)
    fun findRegisteredUser(email: String): RegisteredUser? =
        registeredUsersPreferences.findByEmail(email)
    fun updateRegisteredUserPassword(email: String, newPasswordKey: String): Boolean =
        registeredUsersPreferences.updatePassword(email, newPasswordKey)
    fun updateRegisteredUserEmail(oldEmail: String, newEmail: String): Boolean =
        registeredUsersPreferences.updateEmail(oldEmail, newEmail)

    // --- Preferências de comportamento da IA ---
    fun saveDifficulty(difficulty: String) =
        aiSettingsPreferences.saveDifficulty(AiDifficulty.fromStorageValue(difficulty))

    fun getDifficulty(): String =
        aiSettingsPreferences.getDifficulty().storageValue

    fun saveRigor(rigor: String) =
        aiSettingsPreferences.saveRigor(AiRigor.fromStorageValue(rigor))

    fun getRigor(): String =
        aiSettingsPreferences.getRigor().storageValue

    fun saveTone(tone: String) =
        aiSettingsPreferences.saveTone(AiTone.fromStorageValue(tone))

    fun getTone(): String =
        aiSettingsPreferences.getTone().storageValue

    /** Limpa toda a sessão (logout completo). */
    fun clear() {
        authSession.clearToken()
        userPreferences.clear()
    }
}
