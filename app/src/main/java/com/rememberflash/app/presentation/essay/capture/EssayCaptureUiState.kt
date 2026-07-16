package com.rememberflash.app.presentation.essay.capture

import android.net.Uri

enum class SubmissionMethod {
    NONE, CAMERA, TYPING
}

data class EssayCaptureUiState(
    val method: SubmissionMethod = SubmissionMethod.NONE,
    val theme: String = "",
    val text: String = "",
    val imageUri: Uri? = null,
    val pdfUri: Uri? = null,
    val isOcrLoading: Boolean = false,
    val isEvaluating: Boolean = false,
    val error: String? = null,
    val successEssayId: Long? = null
)
