package com.rememberflash.app.presentation.home.components

import com.rememberflash.app.presentation.home.HomeUiState
import com.rememberflash.app.presentation.home.util.PerformancePeriod
import com.rememberflash.app.presentation.home.util.filterAnsweredByPeriod
import com.rememberflash.app.presentation.home.util.filterByDiscipline
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.presentation.home.util.percentOf

@Composable
fun PerformanceTabContent(
    uiState: HomeUiState,
    onGoToDashboard: () -> Unit
) {
    var periodFilter by remember { mutableStateOf(PerformancePeriod.ALL_TIME) }
    var disciplineFilterId by remember { mutableStateOf<Long?>(null) }

    val now = System.currentTimeMillis()
    val timeFilteredQuestions = uiState.allQuestions.filterAnsweredByPeriod(periodFilter, now)
    val filteredQuestions = timeFilteredQuestions.filterByDiscipline(disciplineFilterId)

    val hasAnyAnsweredQuestion = uiState.allQuestions.any { it.chosenOption != null }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        OfflineBanner()

        Text(
            text = "Desempenho de Estudos",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        PeriodFilterChips(selected = periodFilter, onSelect = { periodFilter = it })

        Spacer(modifier = Modifier.height(12.dp))

        DisciplineFilterDropdown(
            disciplines = uiState.allDisciplines,
            selectedDisciplineId = disciplineFilterId,
            onSelect = { disciplineFilterId = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!hasAnyAnsweredQuestion) {
            NoQuestionsAnsweredState(onGoToDashboard = onGoToDashboard)
        } else {
            PerformanceSummaryCard(questions = filteredQuestions)

            ResponseTimeSummaryCard(
                attempts = uiState.mockExamAttempts,
                allQuestions = uiState.allQuestions,
                filteredDisciplineId = disciplineFilterId
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Desempenho por Matéria",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            uiState.allDisciplines.forEach { discipline ->
                val discQuestions = timeFilteredQuestions.filter { it.disciplineId == discipline.id }
                if (discQuestions.isNotEmpty()) {
                    DisciplinePerformanceCard(discipline = discipline, questions = discQuestions)
                }
            }
        }
    }
}

@Composable
private fun ResponseTimeSummaryCard(
    attempts: List<com.rememberflash.app.domain.model.MockExamAttempt>,
    allQuestions: List<Question>,
    filteredDisciplineId: Long?
) {
    val gson = remember { com.google.gson.Gson() }
    val typeToken = remember { object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type }

    var totalSeconds = 0
    var questionsCount = 0

    attempts.forEach { attempt ->
        if (!attempt.timesJson.isNullOrBlank()) {
            val timesMap = try {
                gson.fromJson<Map<String, Int>>(attempt.timesJson, typeToken)
            } catch (e: Exception) {
                null
            }
            timesMap?.forEach { (qIdStr, timeSecs) ->
                val qId = qIdStr.toLongOrNull() ?: return@forEach
                val question = allQuestions.firstOrNull { it.id == qId }
                
                if (filteredDisciplineId == null || question?.disciplineId == filteredDisciplineId) {
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
                        text = if (filteredDisciplineId != null) "Tempo médio nesta matéria" else "Tempo médio global por questão",
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
private fun OfflineBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
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
private fun DisciplineFilterDropdown(
    disciplines: List<Discipline>,
    selectedDisciplineId: Long?,
    onSelect: (Long?) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedName = disciplines.firstOrNull { it.id == selectedDisciplineId }?.name ?: "Todas"

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { isExpanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Filtro Disciplina: $selectedName")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onSelect(null)
                    isExpanded = false
                }
            )
            disciplines.forEach { discipline ->
                DropdownMenuItem(
                    text = { Text(discipline.name) },
                    onClick = {
                        onSelect(discipline.id)
                        isExpanded = false
                    }
                )
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
