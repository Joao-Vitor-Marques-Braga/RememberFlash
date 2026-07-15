package com.rememberflash.app.data.mapper

import com.rememberflash.app.data.local.database.entity.EssayEntity
import com.rememberflash.app.domain.model.Essay

fun EssayEntity.toDomain(): Essay = Essay(
    id = id,
    userId = userId,
    contestId = contestId,
    title = title,
    theme = theme,
    imageUri = imageUri,
    extractedText = extractedText,
    aiFeedbackJson = aiFeedbackJson,
    score = score,
    createdAt = createdAt
)

fun Essay.toEntity(): EssayEntity = EssayEntity(
    id = id,
    userId = userId,
    contestId = contestId,
    title = title,
    theme = theme,
    imageUri = imageUri,
    extractedText = extractedText,
    aiFeedbackJson = aiFeedbackJson,
    score = score,
    createdAt = createdAt
)
