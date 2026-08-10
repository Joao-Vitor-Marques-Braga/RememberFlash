package com.rememberflash.app.presentation.question.resolve

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Exibe o tempo decorrido na questão atual como um badge compacto.
 *
 * BUGS CORRIGIDOS:
 * - [BUG #2] `remember(currentIndex)` agora inicializa `elapsedSeconds` a partir de
 *   `questionTimes[questionId]` em vez de 0. Com isso, ao navegar de volta para uma
 *   questão já visitada, o timer exibe o tempo real acumulado, e não recomeça do zero.
 *
 * DUPLICAÇÃO REMOVIDA:
 * - `TimerBadge` existia identicamente em `QuestionResolveScreen` e
 *   `QuestionResolveContestScreen`. Esta versão centralizada é a única fonte de
 *   verdade para a feature.
 *
 * @param questionId  ID da questão atual (Long), usado como chave em [questionTimes].
 * @param hasSubmitted Se `true`, o timer para e exibe o tempo final travado pelo ViewModel.
 * @param questionTimes Mapa `questionId → segundos` vindo do UiState.
 * @param currentIndex  Índice atual da questão; mudança de valor reinicia o estado local.
 */
@Composable
internal fun TimerBadge(
    questionId: Long,
    hasSubmitted: Boolean,
    questionTimes: Map<Long, Int>,
    currentIndex: Int
) {
    // [BUG #2 — FIX] Inicializa com o tempo já acumulado pelo ViewModel para esta questão.
    // Antes, a inicialização era sempre 0, fazendo o timer visual reiniciar do zero ao
    // navegar de volta para uma questão já visitada.
    var elapsedSeconds by remember(currentIndex) {
        mutableStateOf(questionTimes[questionId] ?: 0)
    }

    LaunchedEffect(currentIndex, hasSubmitted) {
        if (!hasSubmitted) {
            // Contagem local enquanto a questão não for submetida.
            while (true) {
                kotlinx.coroutines.delay(1000L)
                elapsedSeconds++
            }
        } else {
            // Quando o usuário submete, sincroniza com o valor travado pelo ViewModel
            // (gravado em submitAnswer()), garantindo que o visual reflita o valor real.
            elapsedSeconds = questionTimes[questionId] ?: 0
        }
    }

    val mins = elapsedSeconds / 60
    val secs = elapsedSeconds % 60
    val timerText = String.format("%02d:%02d", mins, secs)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = timerText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Card de resumo de tempo exibido na tela de resultado final.
 *
 * BUGS CORRIGIDOS:
 * - [BUG #1] O tempo médio agora é calculado dividindo pelo número de questões que
 *   têm tempo registrado (`questionTimes.size`), e não pelo total de questões do
 *   simulado. Isso evita médias deflacionadas quando o usuário encerra sem responder
 *   todas as questões.
 *
 * DUPLICAÇÃO REMOVIDA:
 * - O bloco de "Tempo de Resolução" era idêntico nas duas screens. A única diferença
 *   era o rótulo de cada linha (ex: "Questão 1" vs "Questão 1 (Matemática)"), que
 *   agora é parametrizado via [questionLabels].
 *
 * @param questionTimes  Mapa `questionId → segundos` vindo do UiState.
 * @param questionLabels Lista ordenada de pares `(questionId, rótulo)` a exibir por linha.
 *   Crie o rótulo no call-site de acordo com a variante (disciplina ou concurso).
 */
@Composable
internal fun QuestionTimeSummaryCard(
    questionTimes: Map<Long, Int>,
    questionLabels: List<Pair<Long, String>>
) {
    val totalSeconds = questionTimes.values.sum()

    // [BUG #1 — FIX] Usa o número de questões com tempo registrado como denominador.
    // Antes, dividia por questions.size (total do simulado), o que gerava médias
    // artificialmente baixas quando questões ficavam sem tempo registrado.
    val questionsWithTime = questionTimes.size
    val avgSeconds = if (questionsWithTime > 0) totalSeconds / questionsWithTime else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tempo de Resolução",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Tempo Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${avgSeconds}s",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Tempo Médio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tempo por Questão",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )

            questionLabels.forEach { (questionId, label) ->
                val timeSpent = questionTimes[questionId] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${timeSpent}s",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
