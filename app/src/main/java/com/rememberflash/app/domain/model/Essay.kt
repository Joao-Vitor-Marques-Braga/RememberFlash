package com.rememberflash.app.domain.model

/**
 * Redação capturada via câmera/galeria para avaliação via OCR + IA.
 * Possui vínculo obrigatório com um concurso pai através de [contestId] (RF011 / RF003).
 */
data class Essay(
    val id: Long = 0L,
    val userId: String,
    val contestId: Long,
    val title: String,
    val theme: String = "",
    val imageUri: String,
    val extractedText: String? = null,
    val aiFeedbackJson: String? = null,
    val score: Double? = null,
    val tokensSpent: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
