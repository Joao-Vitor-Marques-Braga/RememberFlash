package com.rememberflash.app.domain.model

/**
 * Redação capturada via câmera/galeria para avaliação via OCR + IA.
 * [contestId] é nullable para permitir simulações livres sem vinculação
 * a um concurso específico, conforme especificado no DER.
 */
data class Essay(
    val id: Long = 0L,
    val userId: String,
    val contestId: Long? = null,
    val title: String,
    val theme: String = "",
    val imageUri: String,
    val extractedText: String? = null,
    val aiFeedbackJson: String? = null,
    val score: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
