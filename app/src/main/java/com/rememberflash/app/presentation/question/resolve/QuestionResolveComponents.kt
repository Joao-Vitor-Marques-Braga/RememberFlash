package com.rememberflash.app.presentation.question.resolve

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rememberflash.app.presentation.theme.SuccessGreen

/**
 * Card de feedback exibido após o usuário submeter uma resposta a uma questão.
 *
 * [CODE SMELL 2.2 — DUPLICAÇÃO REMOVIDA]
 * Este bloco de composição existia inline tanto em [QuestionResolveScreen] quanto em
 * [QuestionResolveContestScreen], com diferenças visuais mínimas (alpha 0.08f vs 0.05f,
 * ponto final no título "errou!"). Estas pequenas variações foram unificadas nesta
 * versão canônica, que é a única fonte de verdade para o card de feedback.
 *
 * @param isCorrect            Se o usuário acertou a questão.
 * @param correctIndex         Índice da alternativa correta (0-based, para derivar a letra).
 * @param explanation          Texto da justificativa didática gerada pela IA, ou null.
 * @param questionId           ID da questão para navegação ao tutor.
 * @param onNavigateToTutorChat Lambda de navegação ao chat do tutor.
 * @param modifier             Modifier externo para controle de espaçamento no call-site.
 */
@Composable
internal fun QuestionFeedbackCard(
    isCorrect: Boolean,
    correctIndex: Int,
    explanation: String?,
    questionId: Long,
    onNavigateToTutorChat: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedbackColor = if (isCorrect) SuccessGreen else MaterialTheme.colorScheme.error
    val feedbackIcon = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Error
    val feedbackTitle = if (isCorrect) "Você acertou!" else "Você errou!"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = feedbackColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, feedbackColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // — Cabeçalho: ícone + título —
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

            // — Gabarito (apenas quando errou) —
            if (!isCorrect) {
                val correctLetter = ('A' + correctIndex)
                Text(
                    text = "Gabarito: Alternativa $correctLetter",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // — Justificativa didática (quando disponível) —
            if (!explanation.isNullOrBlank()) {
                Text(
                    text = "Justificativa Didática:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // — Botão do Tutor —
            OutlinedButton(
                onClick = { onNavigateToTutorChat(questionId) },
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
