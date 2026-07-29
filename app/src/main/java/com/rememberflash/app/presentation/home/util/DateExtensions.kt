package com.rememberflash.app.presentation.home.util

import java.util.Calendar

/**
 * Retorna o timestamp correspondente à meia-noite do mesmo dia.
 * Usado para agrupar/comparar datas ignorando a hora.
 *
 * Antes duplicado em HomeScreen.kt e HomeViewModel.kt como `toMidnight()`.
 */
fun Long.startOfDayMillis(): Long =
    Calendar.getInstance().apply {
        timeInMillis = this@startOfDayMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
