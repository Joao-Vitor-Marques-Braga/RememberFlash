package com.rememberflash.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.hilt.navigation.compose.hiltViewModel
import com.rememberflash.app.presentation.home.model.HomeTab
import com.rememberflash.app.presentation.home.components.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCreateContest: () -> Unit,
    onNavigateToContestDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEssayCapture: () -> Unit,
    onNavigateToEssayResult: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val logoutTriggered by viewModel.logoutEvent.collectAsState()
    var currentTab by remember { mutableStateOf(HomeTab.DASHBOARD) }
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(logoutTriggered) {
        if (logoutTriggered) {
            onLogout()
        }
    }

    Scaffold(
        bottomBar = {
            HomeBottomBar(currentTab = currentTab, onTabSelected = { currentTab = it })
        },
        floatingActionButton = {
            HomeFab(
                currentTab = currentTab,
                onNavigateToCreateContest = onNavigateToCreateContest,
                onNavigateToEssayCapture = onNavigateToEssayCapture
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            HomeTopBar(
                userName = uiState.user?.name,
                onNavigateToSettings = onNavigateToSettings,
                onAvatarClick = { showProfileDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (currentTab) {
                HomeTab.DASHBOARD -> DashboardTabContent(
                    uiState = uiState,
                    onNavigateToCreateContest = onNavigateToCreateContest,
                    onNavigateToContestDetail = onNavigateToContestDetail
                )
                HomeTab.ESSAYS -> EssaysTabContent(
                    essays = uiState.essays,
                    onNavigateToEssayResult = onNavigateToEssayResult
                )
                HomeTab.PERFORMANCE -> PerformanceTabContent(
                    uiState = uiState,
                    onGoToDashboard = { currentTab = HomeTab.DASHBOARD }
                )
                HomeTab.SCHEDULE -> ScheduleTabContent(
                    uiState = uiState,
                    onUpdateGoalProgress = viewModel::updateGoalProgress
                )
            }
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = uiState.user?.name
                            ?.split(" ")
                            ?.take(2)
                            ?.joinToString("") { it.take(1) }
                            ?: "US"
                        Text(
                            text = initials.uppercase(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Minha Conta",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileField(label = "Nome", value = uiState.user?.name ?: "N/A")
                    ProfileField(label = "E-mail", value = uiState.user?.email ?: "N/A")
                    ProfileField(label = "CPF", value = uiState.user?.cpf ?: "N/A")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showProfileDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sair da Conta", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Fechar")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HomeBottomBar(currentTab: HomeTab, onTabSelected: (HomeTab) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        HomeTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun HomeFab(
    currentTab: HomeTab,
    onNavigateToCreateContest: () -> Unit,
    onNavigateToEssayCapture: () -> Unit
) {
    when (currentTab) {
        HomeTab.DASHBOARD -> ExtendedFloatingActionButton(
            onClick = onNavigateToCreateContest,
            icon = { Icon(Icons.Default.Add, contentDescription = "Novo Concurso") },
            text = { Text("Novo Concurso") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
        HomeTab.ESSAYS -> ExtendedFloatingActionButton(
            onClick = onNavigateToEssayCapture,
            icon = { Icon(Icons.Default.Add, contentDescription = "Nova Redação") },
            text = { Text("Nova Redação") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
        else -> {
            // Sem botão para Desempenho e Calendário
        }
    }
}
