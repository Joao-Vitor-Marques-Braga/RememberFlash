package com.rememberflash.app.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rememberflash.app.data.local.database.entity.QuestionEntity
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.model.QuestionSource

private val gson = Gson()

fun QuestionEntity.toDomain(): Question {
    val listType = object : TypeToken<List<String>>() {}.type
    val optionsList: List<String> = try {
        gson.fromJson(optionsJson, listType) ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    return Question(
        id = id,
        disciplineId = disciplineId,
        statement = statement,
        options = optionsList,
        correctIndex = correctIndex,
        explanation = explanation,
        source = try { QuestionSource.valueOf(source) } catch (_: Exception) { QuestionSource.MANUAL },
        chosenOption = chosenOption,
        isCorrect = isCorrect,
        answeredAt = answeredAt,
        tokensSpent = tokensSpent,
        createdAt = createdAt
    )
}

fun Question.toEntity(): QuestionEntity {
    val optionsJsonString = try {
        gson.toJson(options)
    } catch (_: Exception) {
        "[]"
    }

    return QuestionEntity(
        id = id,
        disciplineId = disciplineId,
        statement = statement,
        optionsJson = optionsJsonString,
        correctIndex = correctIndex,
        explanation = explanation,
        source = source.name,
        chosenOption = chosenOption,
        isCorrect = isCorrect,
        answeredAt = answeredAt,
        tokensSpent = tokensSpent,
        createdAt = createdAt
    )
}
