package com.rememberflash.app.presentation.question.resolve

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rememberflash.app.presentation.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionResolveContestScreen(
    viewModel: QuestionResolveContestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTutorChat: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(uiState.proposalSaveResult) {
        uiState.proposalSaveResult?.let { result ->
            when (result) {
                is com.rememberflash.app.domain.common.Result.Success -> {
                    android.widget.Toast.makeText(context, "Cronograma adaptado com sucesso!", android.widget.Toast.LENGTH_LONG).show()
                    onNavigateBack()
                }
                is com.rememberflash.app.domain.common.Result.Error -> {
                    android.widget.Toast.makeText(context, result.message, android.widget.Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.contestTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Erro desconhecido.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (uiState.questions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhuma questão encontrada para este simulado.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (uiState.isFinished) {
                // TELA DE RESULTADO DO SIMULADO COMPLETO
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Simulado Finalizado!",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "${uiState.score}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            Text(
                                text = "Acertos de ${uiState.questions.size} questões",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            val pct = (uiState.score.toFloat() / uiState.questions.size.toFloat() * 100).toInt()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Aproveitamento: $pct%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (pct >= 70) SuccessGreen else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // [DUPLICAÇÃO REMOVIDA + BUG #1/#2 CORRIGIDOS]
                    // Substituído pelo componente centralizado QuestionTimeSummaryCard.
                    // O rótulo inclui o nome da disciplina, que é específico desta variante
                    // de simulado (concurso completo vs. simulado por disciplina).
                    val questionLabels = uiState.questions.mapIndexed { idx, qWithDisp ->
                        qWithDisp.question.id to "Questão ${idx + 1} (${qWithDisp.disciplineName})"
                    }
                    QuestionTimeSummaryCard(
                        questionTimes = uiState.questionTimes,
                        questionLabels = questionLabels
                    )

                    // PONTOS DE MAIOR DIFICULDADE
                    if (uiState.difficulties.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Pontos de Maior Dificuldade:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                        )
                        uiState.difficulties.forEach { diff ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = diff.disciplineName,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "${diff.correctAnswers} acertos de ${diff.totalQuestions} questões",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        text = "${diff.accuracy.toInt()}%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    // PROPOSTA DE ADAPTAÇÃO DE CRONOGRAMA
                    if (uiState.showRecalculationProposal && uiState.comparisonList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(
                                    text = "💡 Proposta de Ajuste no Cronograma",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Detectamos dificuldades! Deseja adaptar seu tempo diário de estudos focado na melhoria do seu desempenho?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                uiState.comparisonList.forEach { comp ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = comp.disciplineName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${comp.currentMinutesPerDay}m",
                                                style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp).padding(horizontal = 4.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "${comp.proposedMinutesPerDay}m",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (comp.proposedMinutesPerDay > comp.currentMinutesPerDay) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.rejectProposedSchedule() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Rejeitar")
                                    }

                                    Button(
                                        onClick = { viewModel.acceptProposedSchedule() },
                                        modifier = Modifier.weight(1.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (uiState.isSavingProposal) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text("Aceitar Alterações")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Voltar para Concurso", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // RESOLVENDO QUESTÕES DO SIMULADO
                val currentIndex = uiState.currentIndex
                val item = uiState.questions[currentIndex]
                val question = item.question
                val selectedIndex = uiState.selectedAnswers[currentIndex]
                val hasSubmitted = uiState.submittedAnswers.contains(currentIndex)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                ) {
                    // Indicador de Progresso
                    LinearProgressIndicator(
                        progress = { (currentIndex.toFloat() + 1) / uiState.questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Questão ${currentIndex + 1} de ${uiState.questions.size}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TimerBadge(
                            questionId = question.id,
                            hasSubmitted = hasSubmitted,
                            questionTimes = uiState.questionTimes,
                            currentIndex = currentIndex
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text(
                                text = item.disciplineName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Enunciado
                    Text(
                        text = question.statement,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Alternativas
                    question.options.forEachIndexed { optionIndex, optionText ->
                        val isSelected = selectedIndex == optionIndex
                        
                        val containerColor = when {
                            hasSubmitted && optionIndex == question.correctIndex -> SuccessGreen.copy(alpha = 0.15f)
                            hasSubmitted && isSelected && selectedIndex != question.correctIndex -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val borderColor = when {
                            hasSubmitted && optionIndex == question.correctIndex -> SuccessGreen
                            hasSubmitted && isSelected && selectedIndex != question.correctIndex -> MaterialTheme.colorScheme.error
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        }

                        val borderWidth = if (isSelected || (hasSubmitted && optionIndex == question.correctIndex)) 2.dp else 1.dp

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .border(
                                    BorderStroke(borderWidth, borderColor),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(containerColor, shape = RoundedCornerShape(12.dp))
                                .clickable(enabled = !hasSubmitted) {
                                    viewModel.selectOption(optionIndex)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { if (!hasSubmitted) viewModel.selectOption(optionIndex) },
                                enabled = !hasSubmitted,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Justificativa e Feedback do Tutor (após submeter)
                    if (hasSubmitted) {
                        val chosenIsCorrect = selectedIndex == question.correctIndex
                        val feedbackTitle = if (chosenIsCorrect) "Você acertou!" else "Você errou."
                        val feedbackColor = if (chosenIsCorrect) SuccessGreen else MaterialTheme.colorScheme.error

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = feedbackColor.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, feedbackColor.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (chosenIsCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = feedbackColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = feedbackTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = feedbackColor
                                    )
                                }
                                
                                if (!chosenIsCorrect) {
                                    val correctLetter = ('A' + question.correctIndex)
                                    Text(
                                        text = "Gabarito: Alternativa $correctLetter",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                if (!question.explanation.isNullOrBlank()) {
                                    Text(
                                        text = "Justificativa Didática:",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                    Text(
                                        text = question.explanation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onNavigateToTutorChat(question.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.QuestionAnswer, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Dúvidas? Pergunte ao Tutor")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botões de Navegação Inferiores
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = viewModel::previousQuestion,
                            enabled = currentIndex > 0
                        ) {
                            Text("Anterior", fontWeight = FontWeight.Bold)
                        }

                        if (!hasSubmitted) {
                            Button(
                                onClick = viewModel::submitAnswer,
                                enabled = selectedIndex != null,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Responder", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = viewModel::nextQuestion,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                val btnText = if (currentIndex == uiState.questions.size - 1) "Finalizar" else "Próxima"
                                Text(btnText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

