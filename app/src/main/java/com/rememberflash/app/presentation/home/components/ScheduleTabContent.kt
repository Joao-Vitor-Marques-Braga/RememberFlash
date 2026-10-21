package com.rememberflash.app.presentation.home.components

import com.rememberflash.app.presentation.home.HomeUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.model.Topic
import com.rememberflash.app.presentation.home.util.percentOf
import com.rememberflash.app.presentation.home.util.ratioOf
import com.rememberflash.app.presentation.home.util.startOfDayMillis
import com.rememberflash.app.presentation.schedule.MonthCalendar
import com.rememberflash.app.presentation.theme.SuccessGreen

enum class TopicRecommendationLevel {
    HIGH_PRIORITY,
    UNPRACTICED,
    MEDIUM,
    MASTERED
}

data class TopicRecommendation(
    val topic: Topic,
    val totalAnswered: Int,
    val correctCount: Int,
    val accuracy: Int,
    val level: TopicRecommendationLevel,
    val badgeLabel: String,
    val reason: String
)

private fun getRecommendedTopicsForDiscipline(
    disciplineId: Long,
    topics: List<Topic>,
    questions: List<Question>
): List<TopicRecommendation> {
    val disciplineTopics = topics.filter { it.disciplineId == disciplineId }
    if (disciplineTopics.isEmpty()) return emptyList()

    val disciplineQuestions = questions.filter { it.disciplineId == disciplineId }

    val recommendations = disciplineTopics.map { topic ->
        val topicQuestions = disciplineQuestions.filter { it.topicId == topic.id }
        val answeredQuestions = topicQuestions.filter { it.chosenOption != null || it.isCorrect != null }
        val correctQuestions = answeredQuestions.filter {
            it.isCorrect == true || (it.chosenOption != null && it.chosenOption == it.correctIndex)
        }
        val answeredCount = answeredQuestions.size
        val correctCount = correctQuestions.size
        val accuracy = if (answeredCount > 0) (correctCount * 100) / answeredCount else 0

        val (level, badge, reason) = when {
            answeredCount == 0 -> Triple(
                TopicRecommendationLevel.UNPRACTICED,
                "Não praticado",
                "Sem questões resolvidas ainda"
            )
            accuracy < 60 -> Triple(
                TopicRecommendationLevel.HIGH_PRIORITY,
                "$accuracy% acertos",
                "Foco crítico • Rendimento baixo"
            )
            accuracy < 75 -> Triple(
                TopicRecommendationLevel.HIGH_PRIORITY,
                "$accuracy% acertos",
                "Atenção • Abaixo de 75%"
            )
            accuracy <= 85 -> Triple(
                TopicRecommendationLevel.MEDIUM,
                "$accuracy% acertos",
                "Bom ritmo • Praticar mais"
            )
            else -> Triple(
                TopicRecommendationLevel.MASTERED,
                "$accuracy% acertos",
                "Dominado • Fixação sólida"
            )
        }

        TopicRecommendation(
            topic = topic,
            totalAnswered = answeredCount,
            correctCount = correctCount,
            accuracy = accuracy,
            level = level,
            badgeLabel = badge,
            reason = reason
        )
    }

    return recommendations.sortedWith(
        compareBy(
            { rec ->
                when (rec.level) {
                    TopicRecommendationLevel.HIGH_PRIORITY -> 0
                    TopicRecommendationLevel.UNPRACTICED -> 1
                    TopicRecommendationLevel.MEDIUM -> 2
                    TopicRecommendationLevel.MASTERED -> 3
                }
            },
            { rec -> rec.accuracy }
        )
    ).take(3)
}

@Composable
fun ScheduleTabContent(
    uiState: HomeUiState,
    onUpdateGoalProgress: (DailyGoal, Int, Int) -> Unit,
    onNavigateToDiscipline: (Long) -> Unit = {}
) {
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val selectedDayStart = selectedDate.startOfDayMillis()
    val selectedDateGoals = uiState.allDailyGoals.filter { it.date.startOfDayMillis() == selectedDayStart }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Cronograma Geral",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    MonthCalendar(
                        selectedDate = selectedDate,
                        dailyGoals = uiState.allDailyGoals,
                        onDateSelected = { date -> selectedDate = date }
                    )
                }
            }
        }

        item {
            Text(
                text = "Atribuições do Dia:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (selectedDateGoals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma meta de estudo programada para este dia.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(selectedDateGoals) { goal ->
                val discipline = uiState.allDisciplines.firstOrNull { it.id == goal.disciplineId }
                val contest = uiState.activeContests.firstOrNull { it.id == discipline?.contestId }
                DailyGoalCard(
                    goal = goal,
                    discipline = discipline,
                    contest = contest,
                    attempts = uiState.mockExamAttempts,
                    topics = uiState.allTopics,
                    questions = uiState.allQuestions,
                    selectedDate = selectedDate,
                    onUpdate = onUpdateGoalProgress,
                    onNavigateToDiscipline = onNavigateToDiscipline
                )
            }
        }
    }
}

@Composable
private fun DailyGoalCard(
    goal: DailyGoal,
    discipline: Discipline?,
    contest: Contest?,
    attempts: List<com.rememberflash.app.domain.model.MockExamAttempt>,
    topics: List<Topic>,
    questions: List<Question>,
    selectedDate: Long,
    onUpdate: (DailyGoal, Int, Int) -> Unit,
    onNavigateToDiscipline: (Long) -> Unit
) {
    val targetQuestions = maxOf(15, goal.targetMinutes / 2)
    val targetCards = if (goal.flashcardsTarget > 15) 10 else goal.flashcardsTarget.coerceAtLeast(5)
    val selectedDayStart = selectedDate.startOfDayMillis()
    val nextDayStart = selectedDayStart + 24L * 60 * 60 * 1000
    val autoSolvedQuestions = attempts
        .filter { it.disciplineId == goal.disciplineId && it.createdAt in selectedDayStart until nextDayStart }
        .sumOf { it.totalQuestions }
    val effectiveQuestions = maxOf(goal.completedMinutes, autoSolvedQuestions)

    val totalItems = targetQuestions + targetCards
    val completedItems = effectiveQuestions.coerceAtMost(targetQuestions) + goal.flashcardsCompleted.coerceAtMost(targetCards)
    val completionPct = percentOf(completedItems, totalItems)

    val recommendedTopics = remember(goal.disciplineId, topics, questions) {
        getRecommendedTopicsForDiscipline(goal.disciplineId, topics, questions)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToDiscipline(goal.disciplineId) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = contest?.title ?: "Concurso Geral",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "$completionPct%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (completionPct == 100) SuccessGreen else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = discipline?.name ?: "Sem disciplina",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Toque no card para abrir a disciplina →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Acessar disciplina",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Bloco de Tópicos Recomendados por Métricas
            if (recommendedTopics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tópicos Recomendados (Métricas):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    recommendedTopics.forEach { rec ->
                        val (badgeBg, badgeText) = when (rec.level) {
                            TopicRecommendationLevel.HIGH_PRIORITY -> Color(0xFFFDE8E8) to Color(0xFFC81E1E)
                            TopicRecommendationLevel.UNPRACTICED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                            TopicRecommendationLevel.MEDIUM -> Color(0xFFFEF08A) to Color(0xFF854D0E)
                            TopicRecommendationLevel.MASTERED -> Color(0xFFDEF7EC) to Color(0xFF03543F)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = rec.topic.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = rec.reason,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(badgeBg, shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = rec.badgeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = badgeText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progresso de Questões Resolvidas
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Questões Resolvidas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$effectiveQuestions / $targetQuestions",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                LinearProgressIndicator(
                    progress = { ratioOf(effectiveQuestions, targetQuestions) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = { onUpdate(goal, maxOf(0, effectiveQuestions - 5), goal.flashcardsCompleted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("-5 questões", style = MaterialTheme.typography.bodySmall)
                    }
                    androidx.compose.material3.Button(
                        onClick = { onUpdate(goal, effectiveQuestions + 5, goal.flashcardsCompleted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+5 questões", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progresso de Flashcards (Revisar Cards)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Revisar Cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${goal.flashcardsCompleted} / $targetCards",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                LinearProgressIndicator(
                    progress = { ratioOf(goal.flashcardsCompleted, targetCards) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = { onUpdate(goal, effectiveQuestions, maxOf(0, goal.flashcardsCompleted - 5)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("-5 cards", style = MaterialTheme.typography.bodySmall)
                    }
                    androidx.compose.material3.Button(
                        onClick = { onUpdate(goal, effectiveQuestions, goal.flashcardsCompleted + 5) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("+5 cards", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
