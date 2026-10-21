package com.rememberflash.app.presentation.home.components

import com.rememberflash.app.presentation.home.HomeUiState
import com.rememberflash.app.presentation.home.util.PerformancePeriod
import com.rememberflash.app.presentation.home.util.PerformanceFilterSelection
import com.rememberflash.app.presentation.home.util.filterAnsweredByPeriod
import com.rememberflash.app.presentation.home.util.filterByPerformanceSelection
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.model.Topic
import com.rememberflash.app.presentation.home.util.percentOf

@Composable
fun PerformanceTabContent(
    uiState: HomeUiState,
    onGoToDashboard: () -> Unit
) {
    var periodFilter by remember { mutableStateOf(PerformancePeriod.ALL_TIME) }
    var activeFilter by remember { mutableStateOf<PerformanceFilterSelection>(PerformanceFilterSelection.All) }
    var contestForModal by remember { mutableStateOf<Contest?>(null) }

    val now = System.currentTimeMillis()
    val timeFilteredQuestions = uiState.allQuestions.filterAnsweredByPeriod(periodFilter, now)
    val filteredQuestions = timeFilteredQuestions.filterByPerformanceSelection(activeFilter, uiState.allDisciplines)

    val hasAnyAnsweredQuestion = uiState.allQuestions.any { it.chosenOption != null }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Desempenho de Estudos",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        PeriodFilterChips(selected = periodFilter, onSelect = { periodFilter = it })

        Spacer(modifier = Modifier.height(12.dp))

        PerformanceFilterSelector(
            activeFilter = activeFilter,
            activeContests = uiState.activeContests,
            onSelectAll = { activeFilter = PerformanceFilterSelection.All },
            onSelectContestForModal = { contestForModal = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!hasAnyAnsweredQuestion) {
            NoQuestionsAnsweredState(onGoToDashboard = onGoToDashboard)
        } else {
            PerformanceSummaryCard(questions = filteredQuestions)

            ResponseTimeSummaryCard(
                attempts = uiState.mockExamAttempts,
                allQuestions = uiState.allQuestions,
                activeFilter = activeFilter,
                allDisciplines = uiState.allDisciplines
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (activeFilter) {
                    is PerformanceFilterSelection.All -> "Desempenho por Matéria"
                    is PerformanceFilterSelection.ByContest -> "Matérias de ${(activeFilter as PerformanceFilterSelection.ByContest).contest.title}"
                    is PerformanceFilterSelection.ByDiscipline -> "Matéria: ${(activeFilter as PerformanceFilterSelection.ByDiscipline).discipline.name}"
                    is PerformanceFilterSelection.ByTopic -> "Matéria: ${(activeFilter as PerformanceFilterSelection.ByTopic).discipline.name} • ${(activeFilter as PerformanceFilterSelection.ByTopic).topic.name}"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val disciplinesToShow = when (val filter = activeFilter) {
                is PerformanceFilterSelection.All -> uiState.allDisciplines
                is PerformanceFilterSelection.ByContest -> uiState.allDisciplines.filter { it.contestId == filter.contest.id }
                is PerformanceFilterSelection.ByDiscipline -> listOf(filter.discipline)
                is PerformanceFilterSelection.ByTopic -> listOf(filter.discipline)
            }

            disciplinesToShow.forEach { discipline ->
                val discQuestions = if (activeFilter is PerformanceFilterSelection.ByTopic) {
                    filteredQuestions.filter { it.disciplineId == discipline.id }
                } else {
                    timeFilteredQuestions.filter { it.disciplineId == discipline.id }
                }
                if (discQuestions.isNotEmpty()) {
                    DisciplinePerformanceCard(discipline = discipline, questions = discQuestions)
                }
            }
        }
    }

    // Modal para selecionar Disciplina e Tópico do Concurso selecionado
    contestForModal?.let { contest ->
        ContestFilterModal(
            contest = contest,
            allDisciplines = uiState.allDisciplines,
            allTopics = uiState.allTopics,
            onSelectFilter = { activeFilter = it },
            onDismiss = { contestForModal = null }
        )
    }
}

@Composable
private fun ResponseTimeSummaryCard(
    attempts: List<com.rememberflash.app.domain.model.MockExamAttempt>,
    allQuestions: List<Question>,
    activeFilter: PerformanceFilterSelection,
    allDisciplines: List<Discipline>
) {
    val gson = remember { com.google.gson.Gson() }
    val typeToken = remember { object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type }

    var totalSeconds = 0
    var questionsCount = 0

    val filteredQuestionIds = remember(allQuestions, activeFilter, allDisciplines) {
        allQuestions.filterByPerformanceSelection(activeFilter, allDisciplines).map { it.id }.toSet()
    }

    attempts.forEach { attempt ->
        if (!attempt.timesJson.isNullOrBlank()) {
            val timesMap = try {
                gson.fromJson<Map<String, Int>>(attempt.timesJson, typeToken)
            } catch (e: Exception) {
                null
            }
            timesMap?.forEach { (qIdStr, timeSecs) ->
                val qId = qIdStr.toLongOrNull() ?: return@forEach
                if (filteredQuestionIds.contains(qId)) {
                    totalSeconds += timeSecs
                    questionsCount++
                }
            }
        }
    }

    val avgSeconds = if (questionsCount > 0) totalSeconds / questionsCount else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Tempo de Resposta",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = when (activeFilter) {
                            is PerformanceFilterSelection.All -> "Tempo médio global por questão"
                            is PerformanceFilterSelection.ByContest -> "Tempo médio neste concurso"
                            is PerformanceFilterSelection.ByDiscipline -> "Tempo médio nesta matéria"
                            is PerformanceFilterSelection.ByTopic -> "Tempo médio neste tópico"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (questionsCount > 0) "${avgSeconds}s" else "N/A",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$questionsCount questões",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PeriodFilterChips(
    selected: PerformancePeriod,
    onSelect: (PerformancePeriod) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PerformancePeriod.entries.forEach { period ->
            val isSelected = selected == period
            Surface(
                onClick = { onSelect(period) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = period.label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun PerformanceFilterSelector(
    activeFilter: PerformanceFilterSelection,
    activeContests: List<Contest>,
    onSelectAll: () -> Unit,
    onSelectContestForModal: (Contest) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { isExpanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filtro: ${activeFilter.displayLabel}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // Opção 1: Todas para puxar um geral
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = if (activeFilter is PerformanceFilterSelection.All) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Todas (Geral)",
                                fontWeight = if (activeFilter is PerformanceFilterSelection.All) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "Desempenho consolidado de todos os concursos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                onClick = {
                    onSelectAll()
                    isExpanded = false
                }
            )

            if (activeContests.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "SELECIONE UM CONCURSO:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                activeContests.forEach { contest ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = contest.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        onClick = {
                            isExpanded = false
                            onSelectContestForModal(contest)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContestFilterModal(
    contest: Contest,
    allDisciplines: List<Discipline>,
    allTopics: List<Topic>,
    onSelectFilter: (PerformanceFilterSelection) -> Unit,
    onDismiss: () -> Unit
) {
    val contestDisciplines = remember(allDisciplines, contest) {
        allDisciplines.filter { it.contestId == contest.id }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header com botão fechar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contest.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Selecione o concurso, matéria ou tópico",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Opção 1: Todo o Concurso
                    Surface(
                        onClick = {
                            onSelectFilter(PerformanceFilterSelection.ByContest(contest))
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Todo o Concurso",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Consolidado de todas as matérias",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (contestDisciplines.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Disciplinas e Tópicos",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        contestDisciplines.forEach { discipline ->
                            val disciplineTopics = remember(allTopics, discipline) {
                                allTopics.filter { it.disciplineId == discipline.id }
                            }
                            var isTopicsExpanded by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = discipline.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (disciplineTopics.isNotEmpty()) {
                                            IconButton(
                                                onClick = { isTopicsExpanded = !isTopicsExpanded },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isTopicsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = if (isTopicsExpanded) "Recolher tópicos" else "Expandir tópicos",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Botão Selecionar Matéria Completa
                                    OutlinedButton(
                                        onClick = {
                                            onSelectFilter(PerformanceFilterSelection.ByDiscipline(contest, discipline))
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Filtrar por esta matéria",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    }

                                    // Tópicos da Disciplina
                                    if (disciplineTopics.isNotEmpty()) {
                                        if (isTopicsExpanded) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                disciplineTopics.forEach { topic ->
                                                    Surface(
                                                        onClick = {
                                                            onSelectFilter(PerformanceFilterSelection.ByTopic(contest, discipline, topic))
                                                            onDismiss()
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.surface,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.BookmarkBorder,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = topic.name,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "${disciplineTopics.size} subtópico(s) disponível(is) • toque na seta para ver",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .clickable { isTopicsExpanded = true }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Nenhuma disciplina cadastrada neste concurso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun NoQuestionsAnsweredState(onGoToDashboard: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
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
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGoToDashboard, shape = RoundedCornerShape(12.dp)) {
            Text("Ir para Concursos")
        }
    }
}

@Composable
private fun PerformanceSummaryCard(questions: List<Question>) {
    val total = questions.size
    val correct = questions.count { it.isCorrect == true }
    val wrong = questions.count { it.isCorrect == false }
    val correctPct = percentOf(correct, total)
    val wrongPct = percentOf(wrong, total)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rendimento Global",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                CircularProgressIndicator(
                    progress = { correctPct / 100f },
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                PerformanceStat(value = "$total", label = "Respondidas", color = MaterialTheme.colorScheme.primary)
                PerformanceStat(value = "$correct", label = "Acertos ($correctPct%)", color = MaterialTheme.colorScheme.primary)
                PerformanceStat(value = "$wrong", label = "Erros ($wrongPct%)", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PerformanceStat(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DisciplinePerformanceCard(discipline: Discipline, questions: List<Question>) {
    val total = questions.size
    val correct = questions.count { it.isCorrect == true }
    val correctPct = percentOf(correct, total)

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = discipline.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    text = "$correct/$total ($correctPct%)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { correctPct / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
