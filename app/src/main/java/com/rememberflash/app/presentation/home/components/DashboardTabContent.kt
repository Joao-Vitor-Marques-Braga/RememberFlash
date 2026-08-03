package com.rememberflash.app.presentation.home.components

import com.rememberflash.app.presentation.home.HomeUiState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.presentation.theme.AccentOrange
import com.rememberflash.app.presentation.theme.SuccessGreen

@Composable
fun DashboardTabContent(
    uiState: HomeUiState,
    onNavigateToCreateContest: () -> Unit,
    onNavigateToContestDetail: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredContests = remember(uiState.activeContests, searchQuery) {
        uiState.activeContests.filter { contest ->
            contest.title.contains(searchQuery, ignoreCase = true) ||
            contest.organizerName.contains(searchQuery, ignoreCase = true) ||
            contest.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Olá, ${uiState.user?.name?.substringBefore(" ") ?: "Estudante"}",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Pronto para os estudos de hoje?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        DailyGoalProgressCard(
            hasTodayGoals = uiState.todayGoals.isNotEmpty(),
            progress = uiState.dailyGoalProgress
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Meus Concursos",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.activeContests.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar concursos...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpar busca",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        ContestsList(
            isLoading = uiState.isLoading,
            contests = filteredContests,
            isSearchActive = searchQuery.isNotEmpty(),
            onNavigateToCreateContest = onNavigateToCreateContest,
            onNavigateToContestDetail = onNavigateToContestDetail
        )
    }
}

@Composable
private fun DailyGoalProgressCard(hasTodayGoals: Boolean, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!hasTodayGoals) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Ainda não existem metas diárias a serem batidas",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${(progress * 100).toInt()}% da meta diária concluída",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ContestsList(
    isLoading: Boolean,
    contests: List<Contest>,
    isSearchActive: Boolean,
    onNavigateToCreateContest: () -> Unit,
    onNavigateToContestDetail: (Long) -> Unit
) {
    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        contests.isEmpty() -> {
            if (isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum concurso encontrado para a busca",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToCreateContest() }
                        .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Toque no + para adicionar seu primeiro edital",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(contests) { contest ->
                    ContestCard(
                        contest = contest,
                        onClick = { onNavigateToContestDetail(contest.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ContestCard(
    contest: Contest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contest.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (contest.isSynced) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Sincronizado",
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Pendente de sincronização",
                        tint = AccentOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (contest.organizerName.isNotBlank()) {
                Text(
                    text = "Banca: ${contest.organizerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // TODO: valor fixo (0.3f) até existir cálculo real de progresso do concurso.
            LinearProgressIndicator(
                progress = { 0.3f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
