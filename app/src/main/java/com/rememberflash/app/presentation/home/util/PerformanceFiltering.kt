package com.rememberflash.app.presentation.home.util

import com.rememberflash.app.domain.model.Question

enum class PerformancePeriod(val label: String, val windowDays: Int?) {
    LAST_7_DAYS("7 dias", 7),
    LAST_30_DAYS("30 dias", 30),
    ALL_TIME("Geral", null)
}

private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000L

/**
 * Mantém apenas questões já respondidas dentro da janela de tempo do
 * período escolhido. Usado tanto para o card de rendimento global quanto
 * para o breakdown por disciplina (que não aplica o filtro de disciplina
 * selecionada — mesmo comportamento do HomeScreen original).
 */
fun List<Question>.filterAnsweredByPeriod(
    period: PerformancePeriod,
    now: Long = System.currentTimeMillis()
): List<Question> {
    val answered = filter { it.chosenOption != null }

    return period.windowDays?.let { days ->
        val cutoff = now - days * MILLIS_PER_DAY
        answered.filter { it.answeredAt != null && it.answeredAt >= cutoff }
    } ?: answered
}

/** Restringe a uma disciplina específica; `null` mantém todas. */
fun List<Question>.filterByDiscipline(disciplineId: Long?): List<Question> =
    filter { disciplineId == null || it.disciplineId == disciplineId }
