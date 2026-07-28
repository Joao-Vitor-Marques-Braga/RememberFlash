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
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.presentation.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionResolveScreen(
    viewModel: QuestionResolveViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTutorChat: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulado: ${uiState.disciplineName}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.questions.isEmpty()) {
                Text(
                    text = "Nenhuma questão encontrada para este simulado.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            } else if (uiState.isFinished) {
                // TELA DE RESULTADO FINAL
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Voltar para Matéria", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // RESOLVENDO QUESTÕES
                val currentIndex = uiState.currentIndex
                val question = uiState.questions[currentIndex]
                val selectedIndex = uiState.selectedAnswers[currentIndex]
                val hasSubmitted = uiState.submittedAnswers.contains(currentIndex)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Header progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Questão ${currentIndex + 1} de ${uiState.questions.size}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (question.tokensSpent > 0) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${question.tokensSpent} tks",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = (currentIndex + 1).toFloat() / uiState.questions.size.toFloat(),
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(6.dp),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Enunciado
                    Text(
                        text = question.statement,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = MaterialTheme.typography.titleMedium.lineHeight * 1.25f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Alternativas
                    question.options.forEachIndexed { index, option ->
                        val letter = ('A' + index)
                        val isSelected = selectedIndex == index
                        
                        // Decide cores do card com base no estado de submissão
                        val borderStroke = if (hasSubmitted) {
                            if (index == question.correctIndex) {
                                BorderStroke(2.dp, SuccessGreen)
                            } else if (isSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            }
                        } else if (isSelected) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        }

                        val containerColor = if (hasSubmitted) {
                            if (index == question.correctIndex) {
                                SuccessGreen.copy(alpha = 0.1f)
                            } else if (isSelected) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        } else if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable(enabled = !hasSubmitted) { viewModel.selectOption(index) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            border = borderStroke
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bloco de Justificativa após responder
                    if (hasSubmitted) {
                        val chosenIsCorrect = selectedIndex == question.correctIndex
                        val feedbackColor = if (chosenIsCorrect) SuccessGreen else MaterialTheme.colorScheme.error
                        val feedbackIcon = if (chosenIsCorrect) Icons.Default.CheckCircle else Icons.Default.Error
                        val feedbackTitle = if (chosenIsCorrect) "Você acertou!" else "Você errou!"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = feedbackColor.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, feedbackColor.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = feedbackIcon,
                                        contentDescription = null,
                                        tint = feedbackColor,
                                        modifier = Modifier.size(24.dp)
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

                                if (hasSubmitted) {
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
                    }

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

                        if (hasSubmitted) {
                            Button(
                                onClick = viewModel::nextQuestion,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (currentIndex == uiState.questions.size - 1) "Ver Resultado" else "Próxima",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = viewModel::submitAnswer,
                                enabled = selectedIndex != null,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Responder", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
