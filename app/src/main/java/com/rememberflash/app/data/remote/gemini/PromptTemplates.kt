package com.rememberflash.app.data.remote.gemini

/**
 * Templates de prompt rígidos para mitigar alucinações da IA Generativa (RN04).
 * Cada template é imutável e estruturado para forçar respostas em JSON válido
 * com campos predefinidos, reduzindo a margem de invenção do modelo.
 */
object PromptTemplates {

    fun buildEssayEvaluationPrompt(essayText: String, theme: String): String = """
        |Você é um avaliador especialista em redações de concursos públicos brasileiros.
        |Avalie a redação abaixo seguindo rigorosamente os critérios da banca CESPE/CEBRASPE.
        |
        |TEMA: $theme
        |
        |REDAÇÃO:
        |$essayText
        |
        |Responda EXCLUSIVAMENTE em JSON válido no formato abaixo, sem texto adicional:
        |{
        |  "nota": <número de 0.0 a 10.0>,
        |  "competencias": [
        |    {
        |      "nome": "Adequação ao tema e à proposta",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback específico>"
        |    },
        |    {
        |      "nome": "Domínio da norma culta",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback específico>"
        |    },
        |    {
        |      "nome": "Argumentação e organização",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback específico>"
        |    },
        |    {
        |      "nome": "Coesão e coerência",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback específico>"
        |    },
        |    {
        |      "nome": "Proposta de intervenção",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback específico>"
        |    }
        |  ],
        |  "pontos_fortes": ["<ponto 1>", "<ponto 2>"],
        |  "sugestoes_melhoria": ["<sugestão 1>", "<sugestão 2>"]
        |}
    """.trimMargin()

    fun buildFlashcardExtractionPrompt(rawText: String): String = """
        |Você é um especialista em concursos públicos brasileiros.
        |Extraia os conceitos-chave do texto abaixo e gere flashcards de estudo.
        |
        |TEXTO:
        |$rawText
        |
        |Responda EXCLUSIVAMENTE em JSON válido no formato abaixo, sem texto adicional:
        |{
        |  "flashcards": [
        |    {
        |      "frente": "<pergunta objetiva sobre o conceito>",
        |      "verso": "<resposta concisa e precisa>"
        |    }
        |  ]
        |}
        |
        |Gere entre 5 e 20 flashcards. Priorize conceitos que são frequentemente
        |cobrados em provas. Cada flashcard deve ser auto-contido.
    """.trimMargin()

    fun buildScheduleOptimizationPrompt(
        disciplines: String,
        availableHours: Double,
        daysUntilExam: Int,
        currentProgress: String
    ): String = """
        |Você é um coach de estudos para concursos públicos.
        |Com base nos dados abaixo, sugira ajustes no cronograma de estudos.
        |
        |DISCIPLINAS E PESOS:
        |$disciplines
        |
        |PROGRESSO ATUAL:
        |$currentProgress
        |
        |HORAS DISPONÍVEIS POR DIA: $availableHours
        |DIAS ATÉ A PROVA: $daysUntilExam
        |
        |Responda EXCLUSIVAMENTE em JSON válido no formato abaixo:
        |{
        |  "recomendacoes": [
        |    {
        |      "disciplina": "<nome>",
        |      "percentual_tempo": <porcentagem do tempo total>,
        |      "justificativa": "<motivo da priorização>"
        |    }
        |  ],
        |  "dica_geral": "<conselho estratégico para o aluno>"
        |}
    """.trimMargin()
}
