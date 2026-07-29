package com.rememberflash.app.data.local.preferences

/**
 * Chaves usadas no armazenamento seguro. Centralizar aqui evita strings
 * mágicas espalhadas pelas classes que leem/escrevem preferências.
 */
internal object PreferenceKeys {
    const val PREFS_FILE_NAME = "remember_flash_secure_prefs"

    const val JWT_TOKEN = "jwt_token"
    const val LOGIN_TIMESTAMP = "login_timestamp"

    const val USER_ID = "user_id"
    const val USER_NAME = "user_name"
    const val USER_EMAIL = "user_email"
    const val USER_CPF = "user_cpf"

    const val GEMINI_API_KEY = "gemini_api_key"
    const val REGISTERED_USERS = "registered_users"

    const val AI_DIFFICULTY = "ai_difficulty"
    const val AI_RIGOR = "ai_rigor"
    const val AI_TONE = "ai_tone"
}
