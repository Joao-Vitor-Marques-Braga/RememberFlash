package com.rememberflash.app.data.local.preferences

/**
 * Substitui as strings soltas ("Médio", "Padrão", "Explicativo") por tipos
 * fechados: evita erro de digitação e deixa os valores válidos explícitos
 * em um único lugar.
 */
enum class AiDifficulty(val storageValue: String) {
    EASY("Fácil"),
    MEDIUM("Médio"),
    HARD("Difícil");

    companion object {
        val DEFAULT = MEDIUM
        fun fromStorageValue(value: String?): AiDifficulty =
            entries.firstOrNull { it.storageValue == value } ?: DEFAULT
    }
}

enum class AiRigor(val storageValue: String) {
    FLEXIBLE("Flexível"),
    STANDARD("Padrão"),
    STRICT("Rigoroso");

    companion object {
        val DEFAULT = STANDARD
        fun fromStorageValue(value: String?): AiRigor =
            entries.firstOrNull { it.storageValue == value } ?: DEFAULT
    }
}

enum class AiTone(val storageValue: String) {
    EXPLANATORY("Explicativo"),
    DIRECT("Direto"),
    FRIENDLY("Descontraído");

    companion object {
        val DEFAULT = EXPLANATORY
        fun fromStorageValue(value: String?): AiTone =
            entries.firstOrNull { it.storageValue == value } ?: DEFAULT
    }
}
