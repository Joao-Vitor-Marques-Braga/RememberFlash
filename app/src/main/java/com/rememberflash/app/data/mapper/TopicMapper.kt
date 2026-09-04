package com.rememberflash.app.data.mapper

import com.rememberflash.app.data.local.database.entity.TopicEntity
import com.rememberflash.app.domain.model.Topic

fun TopicEntity.toDomain(flashcardsCount: Int = 0, questionsCount: Int = 0): Topic = Topic(
    id = id,
    disciplineId = disciplineId,
    contestId = contestId,
    name = name,
    description = description,
    isCompleted = isCompleted,
    orderIndex = orderIndex,
    flashcardsCount = flashcardsCount,
    questionsCount = questionsCount,
    createdAt = createdAt
)

fun Topic.toEntity(): TopicEntity = TopicEntity(
    id = id,
    disciplineId = disciplineId,
    contestId = contestId,
    name = name,
    description = description,
    isCompleted = isCompleted,
    orderIndex = orderIndex,
    createdAt = createdAt
)
