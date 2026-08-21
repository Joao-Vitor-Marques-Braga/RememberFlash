package com.rememberflash.app.presentation.schedule

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rememberflash.app.domain.model.DailyGoal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Modal de erro
    var showErrorDialog by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            showErrorDialog = true
        }
    }

    if (showErrorDialog && uiState.error != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Atenção", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.error ?: "") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog de carregamento na geração do cronograma
    if (uiState.isGenerating) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Planejando estudos...", fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("O Gemini está organizando o peso das matérias e gerando seu cronograma...")
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cronograma de Estudos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (!uiState.isEditingForm && uiState.schedule != null) {
                        IconButton(onClick = viewModel::onRecalculateClicked) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recalcular Cronograma")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.isEditingForm) {
                // Modo Formulário de Parametrização
                ScheduleFormView(
                    uiState = uiState,
                    onContestSelected = viewModel::onContestSelected,
                    onExamDateChanged = viewModel::onExamDateChanged,
                    onMinutesChanged = viewModel::onMinutesPerDayChanged,
                    onMaxSubjectsChanged = viewModel::onMaxSubjectsPerDayChanged,
                    onToggleDay = viewModel::toggleAvailableDay,
                    onGenerateClicked = viewModel::onGenerateScheduleClicked,
                    onCancelClicked = viewModel::onCancelEditFormClicked
                )
            } else {
                // Modo Calendário Interativo
                ScheduleCalendarView(
                    uiState = uiState,
                    onDateSelected = viewModel::onSelectDate,
                    onUpdateProgress = viewModel::updateGoalProgress,
                    onRecalculate = viewModel::onRecalculateClicked
                )
            }
        }
    }
}

@Composable
fun ScheduleFormView(
    uiState: ScheduleUiState,
    onContestSelected: (Long) -> Unit,
    onExamDateChanged: (Long) -> Unit,
    onMinutesChanged: (Int) -> Unit,
    onMaxSubjectsChanged: (Int) -> Unit,
    onToggleDay: (String) -> Unit,
    onGenerateClicked: () -> Unit,
    onCancelClicked: () -> Unit
) {
    val context = LocalContext.current
    var expandedContestDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Parâmetros do Planejamento",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Seletor de Concurso
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.allContests.firstOrNull { it.id == uiState.contestId }?.title ?: "Selecione um concurso...",
                onValueChange = {},
                readOnly = true,
                label = { Text("Pasta de Concurso") },
                trailingIcon = {
                    IconButton(onClick = { expandedContestDropdown = !expandedContestDropdown }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            DropdownMenu(
                expanded = expandedContestDropdown,
                onDismissRequest = { expandedContestDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                uiState.allContests.forEach { contest ->
                    DropdownMenuItem(
                        text = { Text(contest.title) },
                        onClick = {
                            onContestSelected(contest.id)
                            expandedContestDropdown = false
                        }
                    )
                }
            }
        }

        // Seletor de Data da Prova (DatePickerDialog)
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                onExamDateChanged(selectedCal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val examDateFormatted = if (uiState.examDate != null) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(uiState.examDate))
        } else {
            ""
        }

        OutlinedTextField(
            value = examDateFormatted,
            onValueChange = {},
            readOnly = true,
            label = { Text("Data da Prova") },
            placeholder = { Text("Selecione...") },
            trailingIcon = {
                IconButton(onClick = { datePicker.show() }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Tempo Diário e Máximo de Matérias lado a lado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.minutesPerDay.toString(),
                onValueChange = { input ->
                    val value = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                    onMinutesChanged(value.coerceIn(30, 720))
                },
                label = { Text("Tempo Diário (min)") },
                placeholder = { Text("Ex: 120") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.maxSubjectsPerDay.toString(),
                onValueChange = { input ->
                    val value = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                    onMaxSubjectsChanged(value.coerceIn(1, 5))
                },
                label = { Text("Máx. Matérias/dia") },
                placeholder = { Text("Ex: 2") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Dias Disponíveis da Semana (Pílulas Horizontais)
        Text(
            text = "Dias disponíveis para estudo",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        val daysMap = listOf(
            "Segunda" to "Seg",
            "Terça" to "Ter",
            "Quarta" to "Qua",
            "Quinta" to "Qui",
            "Sexta" to "Sex",
            "Sábado" to "Sáb",
            "Domingo" to "Dom"
        )

        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daysMap.forEach { (fullDay, shortDay) ->
                val isSelected = uiState.availableDays.contains(fullDay)
                Surface(
                    onClick = { onToggleDay(fullDay) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) else null,
                    modifier = Modifier.height(38.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = shortDay,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de Submit
        Button(
            onClick = onGenerateClicked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Gerar Cronograma Dinâmico", fontWeight = FontWeight.Bold)
        }

        if (uiState.schedule != null) {
            OutlinedButton(
                onClick = onCancelClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScheduleCalendarView(
    uiState: ScheduleUiState,
    onDateSelected: (Long) -> Unit,
    onUpdateProgress: (DailyGoal, Int, Int) -> Unit,
    onRecalculate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner de Alerta para tempo insuficiente
        if (!uiState.cronogramaWarning.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Aviso",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.cronogramaWarning,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Tokens Gastos
        if (uiState.schedule != null && uiState.schedule.tokensSpent > 0) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Tokens IA: ${uiState.schedule.tokensSpent}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Calendário Interativo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            MonthCalendar(
                selectedDate = uiState.selectedDate,
                dailyGoals = uiState.dailyGoals,
                onDateSelected = onDateSelected,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Agenda de Metas do dia selecionado
        val selectedDateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(uiState.selectedDate))
        
        Text(
            text = "Metas de $selectedDateFormatted",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        val goals = uiState.selectedDateGoals
        if (goals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Nenhuma meta de estudos para hoje! Aproveite para descansar ou revisar flashcards pendentes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        } else {
            goals.forEach { goal ->
                GoalProgressCard(
                    goal = goal,
                    disciplineName = uiState.disciplinesMap[goal.disciplineId] ?: "Disciplina",
                    onUpdate = onUpdateProgress
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GoalProgressCard(
    goal: DailyGoal,
    disciplineName: String,
    onUpdate: (DailyGoal, Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.isCompleted) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título e Status de Conclusão
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = disciplineName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (goal.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Concluído",
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    IconButton(
                        onClick = {
                            onUpdate(goal, goal.targetMinutes, goal.flashcardsTarget)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Marcar como Concluído",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Progresso de Tempo (Minutos)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tempo de Estudo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${goal.completedMinutes} / ${goal.targetMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                LinearProgressIndicator(
                    progress = { 
                        if (goal.targetMinutes > 0) {
                            (goal.completedMinutes.toFloat() / goal.targetMinutes).coerceIn(0f, 1f)
                        } else 0f 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onUpdate(goal, goal.completedMinutes - 15, goal.flashcardsCompleted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("-15m")
                    }
                    Button(
                        onClick = { onUpdate(goal, goal.completedMinutes + 15, goal.flashcardsCompleted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+15m")
                    }
                }
            }

            // Progresso de Flashcards
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Revisar Flashcards",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${goal.flashcardsCompleted} / ${goal.flashcardsTarget} flashcards",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                LinearProgressIndicator(
                    progress = { 
                        if (goal.flashcardsTarget > 0) {
                            (goal.flashcardsCompleted.toFloat() / goal.flashcardsTarget).coerceIn(0f, 1f)
                        } else 0f 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onUpdate(goal, goal.completedMinutes, goal.flashcardsCompleted - 5) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("-5 cards")
                    }
                    Button(
                        onClick = { onUpdate(goal, goal.completedMinutes, goal.flashcardsCompleted + 5) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                    ) {
                        Text("+5 cards")
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCalendar(
    selectedDate: Long,
    dailyGoals: List<DailyGoal>,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = selectedDate }) }

    val daysInMonth = calendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = Calendar.getInstance().apply {
        timeInMillis = calendarMonth.timeInMillis
        set(Calendar.DAY_OF_MONTH, 1)
    }.get(Calendar.DAY_OF_WEEK)

    val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
    val monthYearText = monthYearFormat.format(calendarMonth.time).replaceFirstChar { it.uppercase() }

    Column(modifier = modifier) {
        // Cabeçalho do Calendário
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = calendarMonth.timeInMillis
                    add(Calendar.MONTH, -1)
                }
                calendarMonth = newCal
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mês Anterior")
            }

            Text(
                text = monthYearText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = calendarMonth.timeInMillis
                    add(Calendar.MONTH, 1)
                }
                calendarMonth = newCal
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Próximo Mês")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cabeçalho dos dias da semana
        val weekDays = listOf("S", "T", "Q", "Q", "S", "S", "D")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grade dos dias do mês
        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (c in 0 until 7) {
                    val index = r * 7 + c
                    if (index < startOffset || index >= totalCells) {
                        Box(modifier = Modifier.size(40.dp))
                    } else {
                        val dayNum = index - startOffset + 1
                        val cellCal = Calendar.getInstance().apply {
                            timeInMillis = calendarMonth.timeInMillis
                            set(Calendar.DAY_OF_MONTH, dayNum)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val cellTime = cellCal.timeInMillis
                        val isSelected = cellTime.toMidnight() == selectedDate.toMidnight()

                        // Metas do dia específico
                        val dayGoals = dailyGoals.filter { it.date.toMidnight() == cellTime.toMidnight() }
                        val hasGoals = dayGoals.isNotEmpty()
                        val allCompleted = hasGoals && dayGoals.all { it.isCompleted }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(cellTime) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (hasGoals) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (allCompleted) Color.Green else MaterialTheme.colorScheme.error
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

private fun Long.toMidnight(): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
