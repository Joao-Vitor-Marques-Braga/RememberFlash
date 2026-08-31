package com.rememberflash.app.presentation.essay.capture

import android.net.Uri

import com.rememberflash.app.domain.model.Contest

enum class SubmissionMethod {
    NONE, CAMERA, TYPING
}

data class EssayCaptureUiState(
    val method: SubmissionMethod = SubmissionMethod.NONE,
    val availableContests: List<Contest> = emptyList(),
    val selectedContestId: Long? = null,
    val theme: String = "",
    val text: String = "",
    val imageUri: Uri? = null,
    val pdfUri: Uri? = null,
    val isOcrLoading: Boolean = false,
    val isEvaluating: Boolean = false,
    val error: String? = null,
    val successEssayId: Long? = null
)
