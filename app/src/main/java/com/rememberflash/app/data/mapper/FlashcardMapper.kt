package com.rememberflash.app.data.mapper

import com.rememberflash.app.data.local.database.entity.FlashcardEntity
import com.rememberflash.app.domain.model.Flashcard
import com.rememberflash.app.domain.model.FlashcardSource

fun FlashcardEntity.toDomain(): Flashcard = Flashcard(
    id = id,
    disciplineId = disciplineId,
    front = front,
    back = back,
    source = try { FlashcardSource.valueOf(source) } catch (_: Exception) { FlashcardSource.MANUAL },
    nextReviewAt = nextReviewAt,
    easeFactor = easeFactor,
    interval = interval,
    repetitions = repetitions,
    createdAt = createdAt
)

fun Flashcard.toEntity(): FlashcardEntity = FlashcardEntity(
    id = id,
    disciplineId = disciplineId,
    front = front,
    back = back,
    source = source.name,
    nextReviewAt = nextReviewAt,
    easeFactor = easeFactor,
    interval = interval,
    repetitions = repetitions,
    createdAt = createdAt
)
