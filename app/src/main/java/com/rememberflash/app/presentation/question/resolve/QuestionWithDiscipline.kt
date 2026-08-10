package com.rememberflash.app.presentation.question.resolve

import com.rememberflash.app.domain.model.Question

/**
 * Associa uma [Question] ao nome da disciplina à qual pertence.
 *
 * [ITEM 1.6 — LOCALIZAÇÃO CORRIGIDA]
 * Esta classe estava declarada dentro de [QuestionResolveContestViewModel], misturando
 * modelo de dados com lógica de apresentação no mesmo arquivo. Agora tem arquivo próprio
 * dentro da feature, em paridade com [QuestionResolveUiState] e
 * [QuestionResolveContestUiState].
 *
 * É um modelo de apresentação (não de domínio puro), pois combina [Question] com um
 * metadado de exibição ([disciplineName]) necessário somente nesta camada.
 */
data class QuestionWithDiscipline(
    val question: Question,
    val disciplineName: String
)
