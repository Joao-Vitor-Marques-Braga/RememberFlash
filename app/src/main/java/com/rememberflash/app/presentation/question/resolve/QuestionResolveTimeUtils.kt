package com.rememberflash.app.presentation.question.resolve

/**
 * Utilitários de cálculo de tempo para a feature de resolução de questões.
 *
 * DUPLICAÇÕES REMOVIDAS:
 * - [ITEM 1.1] O bloco de cálculo de tempo de `updateTimeSpentForCurrentQuestion()`
 *   era idêntico em [QuestionResolveViewModel] e [QuestionResolveContestViewModel].
 * - [ITEM 1.2] O bloco de travamento de tempo de `submitAnswer()` era idêntico
 *   nos dois ViewModels (5 linhas, mesma semântica).
 *
 * Limitação conhecida: `selectOption()`, `nextQuestion()` e `previousQuestion()` ainda
 * existem duplicados nos dois ViewModels. Não foram unificados pois dependem de
 * `_uiState.value.copy(...)` com tipos distintos (`QuestionResolveUiState` vs
 * `QuestionResolveContestUiState`), e a introdução de uma classe base ou interface
 * comum foi descartada para manter a arquitetura existente.
 */

/**
 * Calcula o tempo decorrido em segundos desde [startTime] (epoch millis).
 *
 * @return Segundos decorridos, **mínimo 1** para [startTime] válido (> 0).
 *   Retorna **0** se [startTime] for inválido (≤ 0), sem criar entradas falsas.
 */
internal fun elapsedSecondsFrom(startTime: Long): Int {
    if (startTime <= 0L) return 0
    val elapsedMillis = System.currentTimeMillis() - startTime
    return (elapsedMillis / 1000).toInt().coerceAtLeast(1)
}

/**
 * Acumula [elapsedSeconds] no mapa de tempos para [questionId].
 *
 * @return Novo mapa imutável com o tempo acumulado. Se [elapsedSeconds] ≤ 0,
 *   retorna [questionTimes] inalterado (sem criar entradas com valor zero).
 */
internal fun accumulateQuestionTime(
    questionTimes: Map<Long, Int>,
    questionId: Long,
    elapsedSeconds: Int
): Map<Long, Int> {
    if (elapsedSeconds <= 0) return questionTimes
    val updated = questionTimes.toMutableMap()
    updated[questionId] = (updated[questionId] ?: 0) + elapsedSeconds
    return updated
}
