package com.rememberflash.app.data.mapper

import com.rememberflash.app.data.local.database.entity.ContestEntity
import com.rememberflash.app.domain.model.Contest

fun ContestEntity.toDomain(): Contest = Contest(
    id = id,
    userId = userId,
    title = title,
    description = description,
    organizerName = organizerName,
    questionType = questionType,
    syllabusPdfUri = syllabusPdfUri,
    examDate = examDate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Contest.toEntity(): ContestEntity = ContestEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    organizerName = organizerName,
    questionType = questionType,
    syllabusPdfUri = syllabusPdfUri,
    examDate = examDate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)
