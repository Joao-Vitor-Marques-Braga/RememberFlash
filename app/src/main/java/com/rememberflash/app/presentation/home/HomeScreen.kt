package com.rememberflash.app.presentation.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rememberflash.app.R
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.presentation.theme.AccentOrange
import com.rememberflash.app.presentation.theme.SuccessGreen

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCreateContest: () -> Unit,
    onNavigateToContestDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEssayCapture: () -> Unit,
    onNavigateToEssayResult: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") },
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Redações") },
                    label = { Text("Redações") },
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Desempenho") },
                    label = { Text("Desempenho") },
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateContest,
                icon = { Icon(Icons.Default.Add, contentDescription = "Novo Concurso") },
                text = { Text("Novo Concurso") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
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
            
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initials avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = uiState.user?.name?.split(" ")
                        ?.take(2)?.joinToString("") { it.take(1) } ?: "US"
                    Text(
                        text = initials.uppercase(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "RememberFlash",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { /* Notificações */ }) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notificações",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (currentTab) {
                0 -> {
                    // Greeting
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

                    // Progress Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                progress = { uiState.dailyGoalProgress },
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 6.dp,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "${(uiState.dailyGoalProgress * 100).toInt()}% da meta diária concluída",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ação Rápida: Redações via OCR
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEssayCapture() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Avaliar Redação (OCR + IA)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Envie uma foto da folha ou digite diretamente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Meus Concursos",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.activeContests.isEmpty()) {
                        // Empty State Dashed
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
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.activeContests) { contest ->
                                ContestCard(
                                    contest = contest,
                                    onClick = { onNavigateToContestDetail(contest.id) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Redações Tab!
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Minhas Redações",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Button(
                                onClick = onNavigateToEssayCapture,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nova")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.essays.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhuma redação enviada ainda.\nClique em 'Nova' para começar!",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.essays) { essay ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateToEssayResult(essay.id) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = essay.theme,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "Criada em: " + java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(essay.createdAt)),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            if (essay.score != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "${essay.score} / 10.0",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = "Corrigindo...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Aba Desempenho
                    var periodFilter by remember { mutableStateOf("Geral") }
                    var disciplineFilterId by remember { mutableStateOf<Long?>(null) }
                    var isDisciplineDropdownExpanded by remember { mutableStateOf(false) }

                    val now = System.currentTimeMillis()
                    val timeFilteredQuestions = uiState.allQuestions.filter { question ->
                        if (question.chosenOption == null) return@filter false
                        when (periodFilter) {
                            "7 dias" -> question.answeredAt != null && question.answeredAt >= now - 7 * 24 * 3600 * 1000L
                            "30 dias" -> question.answeredAt != null && question.answeredAt >= now - 30 * 24 * 3600 * 1000L
                            else -> true
                        }
                    }

                    val filteredQuestions = timeFilteredQuestions.filter { question ->
                        disciplineFilterId == null || question.disciplineId == disciplineFilterId
                    }

                    val totalAnswered = filteredQuestions.size
                    val totalCorrect = filteredQuestions.count { it.isCorrect == true }
                    val totalWrong = filteredQuestions.count { it.isCorrect == false }
                    val correctPct = if (totalAnswered > 0) (totalCorrect * 100) / totalAnswered else 0
                    val wrongPct = if (totalAnswered > 0) (totalWrong * 100) / totalAnswered else 0

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Exibindo dados offline. Conecte-se à internet para sincronizar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Text(
                            text = "Desempenho de Estudos",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("7 dias", "30 dias", "Geral").forEach { p ->
                                val isSelected = periodFilter == p
                                Surface(
                                    onClick = { periodFilter = p },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = p,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            val selectedDisciplineName = uiState.allDisciplines.firstOrNull { it.id == disciplineFilterId }?.name ?: "Todas"
                            
                            OutlinedButton(
                                onClick = { isDisciplineDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "Filtro Disciplina: $selectedDisciplineName")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = isDisciplineDropdownExpanded,
                                onDismissRequest = { isDisciplineDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas") },
                                    onClick = {
                                        disciplineFilterId = null
                                        isDisciplineDropdownExpanded = false
                                    }
                                )
                                uiState.allDisciplines.forEach { disc ->
                                    DropdownMenuItem(
                                        text = { Text(disc.name) },
                                        onClick = {
                                            disciplineFilterId = disc.id
                                            isDisciplineDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (uiState.allQuestions.none { it.chosenOption != null }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Você ainda não respondeu nenhuma questão.",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { currentTab = 0 },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Ir para Concursos")
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Rendimento Global",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(140.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { correctPct.toFloat() / 100f },
                                            modifier = Modifier.size(120.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 12.dp,
                                            trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                            strokeCap = StrokeCap.Round
                                        )
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$correctPct%",
                                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Acertos",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$totalAnswered",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(text = "Respondidas", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$totalCorrect",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(text = "Acertos ($correctPct%)", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$totalWrong",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Text(text = "Erros ($wrongPct%)", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Desempenho por Matéria",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            uiState.allDisciplines.forEach { disc ->
                                val discQuestions = timeFilteredQuestions.filter { it.disciplineId == disc.id }
                                if (discQuestions.isNotEmpty()) {
                                    val discTotal = discQuestions.size
                                    val discCorrect = discQuestions.count { it.isCorrect == true }
                                    val discCorrectPct = (discCorrect * 100) / discTotal

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = disc.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "$discCorrect/$discTotal ($discCorrectPct%)",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))

                                            LinearProgressIndicator(
                                                progress = { discCorrectPct.toFloat() / 100f },
                                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                strokeCap = StrokeCap.Round
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Perfil do Concurseiro", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
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

            // Mocked progress bar for the contest
            LinearProgressIndicator(
                progress = { 0.3f }, // Dummy value for prototype
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
