package com.rememberflash.app.presentation.home.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Substitui os `Int` mágicos (`currentTab == 0`, `1`, `2`, `3`...) usados
 * no HomeScreen original. Cada aba carrega seu próprio ícone e rótulo,
 * então a barra de navegação inferior deixa de repetir 4 blocos quase
 * idênticos de NavigationBarItem (ver HomeScreen.kt).
 */
enum class HomeTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Início", Icons.Default.Home),
    ESSAYS("Redações", Icons.Default.Assignment),
    PERFORMANCE("Desempenho", Icons.Default.BarChart),
    SCHEDULE("Calendário", Icons.Default.CalendarToday)
}
