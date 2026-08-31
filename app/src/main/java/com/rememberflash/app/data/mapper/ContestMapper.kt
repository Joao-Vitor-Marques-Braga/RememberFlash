package com.rememberflash.app.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rememberflash.app.data.local.database.entity.ContestEntity
import com.rememberflash.app.domain.model.Contest

private val gson = Gson()

fun ContestEntity.toDomain(): Contest {
    val listType = object : TypeToken<List<String>>() {}.type
    val allowedList: List<String> = try {
        gson.fromJson(allowedItems, listType) ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
    val prohibitedList: List<String> = try {
        gson.fromJson(prohibitedItems, listType) ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    return Contest(
        id = id,
        userId = userId,
        title = title,
        description = description,
        organizerName = organizerName,
        questionType = questionType,
        syllabusPdfUri = syllabusPdfUri,
        examDate = examDate,
        examDateStr = examDateStr,
        examLocation = examLocation,
        allowedPen = allowedPen,
        allowedItems = allowedList,
        prohibitedItems = prohibitedList,
        aiDifficulty = aiDifficulty,
        aiRigor = aiRigor,
        aiTone = aiTone,
        isActive = isActive,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Contest.toEntity(): ContestEntity {
    val allowedJson = try { gson.toJson(allowedItems) } catch (_: Exception) { "[]" }
    val prohibitedJson = try { gson.toJson(prohibitedItems) } catch (_: Exception) { "[]" }

    return ContestEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        organizerName = organizerName,
        questionType = questionType,
        syllabusPdfUri = syllabusPdfUri,
        examDate = examDate,
        examDateStr = examDateStr,
        examLocation = examLocation,
        allowedPen = allowedPen,
        allowedItems = allowedJson,
        prohibitedItems = prohibitedJson,
        aiDifficulty = aiDifficulty,
        aiRigor = aiRigor,
        aiTone = aiTone,
        isActive = isActive,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
