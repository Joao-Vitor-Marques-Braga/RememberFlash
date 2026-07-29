package com.rememberflash.app.presentation.home.util

/**
 * Substitui o padrão repetido `if (total > 0) (parte * 100) / total else 0`
 * que aparecia em vários pontos de HomeScreen.kt (progresso de meta,
 * acertos/erros, desempenho por disciplina).
 */
fun percentOf(part: Int, total: Int): Int =
    if (total > 0) (part * 100) / total else 0

/**
 * Mesma ideia, mas como fração 0f..1f — usado nas barras de progresso
 * (LinearProgressIndicator / CircularProgressIndicator).
 */
fun ratioOf(part: Int, total: Int): Float =
    if (total > 0) part.toFloat() / total.toFloat() else 0f
