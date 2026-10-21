package com.rememberflash.app.presentation.home.util

import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.model.Topic

enum class PerformancePeriod(val label: String, val windowDays: Int?) {
    LAST_7_DAYS("7 dias", 7),
    LAST_30_DAYS("30 dias", 30),
    ALL_TIME("Geral", null)
}

sealed interface PerformanceFilterSelection {
    val displayLabel: String
    val summaryTitle: String

    data object All : PerformanceFilterSelection {
        override val displayLabel: String = "Todas"
        override val summaryTitle: String = "Todas as Matérias"
    }

    data class ByContest(val contest: Contest) : PerformanceFilterSelection {
        override val displayLabel: String = "Concurso: ${contest.title}"
        override val summaryTitle: String = contest.title
    }

    data class ByDiscipline(val contest: Contest, val discipline: Discipline) : PerformanceFilterSelection {
        override val displayLabel: String = "${discipline.name} (${contest.title})"
        override val summaryTitle: String = discipline.name
    }

    data class ByTopic(val contest: Contest, val discipline: Discipline, val topic: Topic) : PerformanceFilterSelection {
        override val displayLabel: String = "${topic.name} • ${discipline.name}"
        override val summaryTitle: String = topic.name
    }
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

/** Filtra questões com base na seleção hierárquica (Geral, Concurso, Disciplina ou Tópico). */
fun List<Question>.filterByPerformanceSelection(
    selection: PerformanceFilterSelection,
    allDisciplines: List<Discipline>
): List<Question> {
    return when (selection) {
        is PerformanceFilterSelection.All -> this
        is PerformanceFilterSelection.ByContest -> {
            val discIds = allDisciplines
                .filter { it.contestId == selection.contest.id }
                .map { it.id }
                .toSet()
            filter { it.disciplineId in discIds }
        }
        is PerformanceFilterSelection.ByDiscipline -> {
            filter { it.disciplineId == selection.discipline.id }
        }
        is PerformanceFilterSelection.ByTopic -> {
            filter { it.topicId == selection.topic.id }
        }
    }
}
