package com.rememberflash.app.data.mapper

import com.rememberflash.app.data.local.database.entity.DisciplineEntity
import com.rememberflash.app.domain.model.Discipline

fun DisciplineEntity.toDomain(): Discipline = Discipline(
    id = id,
    contestId = contestId,
    name = name,
    weight = weight,
    totalTopics = totalTopics,
    isActive = isActive,
    isSynced = isSynced,
    createdAt = createdAt
)

fun Discipline.toEntity(): DisciplineEntity = DisciplineEntity(
    id = id,
    contestId = contestId,
    name = name,
    weight = weight,
    totalTopics = totalTopics,
    isActive = isActive,
    isSynced = isSynced,
    createdAt = createdAt
)
