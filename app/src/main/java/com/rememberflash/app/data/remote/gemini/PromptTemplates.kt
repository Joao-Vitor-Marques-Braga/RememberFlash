package com.rememberflash.app.data.remote.gemini

/**
 * Templates de prompt rígidos para mitigar alucinações da IA Generativa (RN04).
 * Cada template é imutável e estruturado para forçar respostas em JSON válido
 * com campos predefinidos, reduzindo a margem de invenção do modelo.
 */
object PromptTemplates {

    fun buildEssayEvaluationPrompt(
        essayText: String,
        theme: String,
        rigor: String,
        tone: String
    ): String = """
        |Você é um avaliador especialista em redações de concursos públicos brasileiros.
        |Avalie a redação abaixo seguindo rigorosamente os critérios da banca CESPE/CEBRASPE.
        |
        |CRITÉRIOS DE PERSONALIZAÇÃO:
        |- Rigor na Correção: $rigor. (Se for 'Rígido', seja extremamente exigente com a norma culta, coesão e adequação temática. Se for 'Flexível', valorize mais o conteúdo e a estrutura geral da argumentação, atenuando pequenas falhas gramaticais. Se for 'Padrão', siga estritamente as diretrizes tradicionais da banca).
        |- Tom do Tutor Interativo: $tone. (Se for 'Direto/Objetivo', forneça feedbacks curtos, diretos ao ponto e objetivos. Se for 'Explicativo/Detalhado', explique didaticamente a origem de cada erro e como corrigi-lo com exemplos).
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

    fun buildFlashcardExtractionPrompt(
        rawText: String,
        difficulty: String,
        tone: String
    ): String = """
        |Você é um especialista em concursos públicos brasileiros.
        |Extraia os conceitos-chave do texto abaixo e gere flashcards de estudo.
        |
        |CRITÉRIOS DE PERSONALIZAÇÃO:
        |- Nível de Dificuldade das Questões: $difficulty. (Se for 'Fácil', foque em conceitos básicos e definições literais simples. Se for 'Médio', traga questionamentos de nível intermediário. Se for 'Difícil', elabore perguntas complexas contendo pegadinhas, jurisprudências e exceções detalhadas da lei).
        |- Tom do Tutor Interativo: $tone. (Se for 'Direto/Objetivo', escreva frentes e versos muito concisos e objetivos. Se for 'Explicativo/Detalhado', adicione breves explicações didáticas no verso do cartão).
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

    fun buildQuestionGenerationPrompt(
        disciplineName: String,
        banca: String,
        format: String,
        difficulty: String,
        quantity: Int,
        theme: String?
    ): String = """
        |Você é um elaborador especialista de questões para concursos públicos brasileiros.
        |Gere um lote de questões de concurso com as seguintes diretrizes:
        |
        |DISCIPLINA: $disciplineName
        |BANCA EXAMINADORA: $banca
        |FORMATO: $format
        |DIFICULDADE: $difficulty
        |QUANTIDADE: $quantity
        |${if (!theme.isNullOrBlank()) "TEMA ESPECÍFICO A FOCAR: $theme" else ""}
        |
        |Responda EXCLUSIVAMENTE em JSON válido no formato abaixo, sem comentários adicionais fora do JSON:
        |{
        |  "questoes": [
        |    {
        |      "statement": "<enunciado elaborado em alto nível no estilo da banca>",
        |      "options": ["<alternativa A>", "<alternativa B>", "<alternativa C>", "<alternativa D>", "<alternativa E>"],
        |      "correctIndex": <índice de 0 a 4 da alternativa correta, ou de 0 a 1 se Certo/Errado>,
        |      "explanation": "<justificativa didática detalhada citando lei, doutrina ou jurisprudência>"
        |    }
        |  ]
        |}
        |
        |Atenção: Se o formato for Certo/Errado, a lista "options" deve conter exatamente duas strings: ["Certo", "Errado"]. O "correctIndex" será 0 para Certo e 1 para Errado.
    """.trimMargin()

    fun buildSyllabusAndRulesParsingPrompt(
        header: String,
        rules: String,
        syllabus: String
    ): String = """
        |Analise os recortes de texto do edital abaixo e retorne APENAS um objeto JSON estruturado.
        |
        |### TEXTO DE CABEÇALHO DO EDITAL:
        |$header
        |
        |### TEXTO DE REGRAS E PROIBIÇÕES:
        |$rules
        |
        |### CONTEÚDO PROGRAMÁTICO DO CARGO:
        |$syllabus
        |
        |### INSTRUÇÕES DE RETORNO:
        |Responda EXCLUSIVAMENTE em JSON válido no formato abaixo, sem tags de marcação (como ```json) ou texto adicional:
        |{
        |  "title": "Nome simplificado do concurso (Ex: INSS, Banco do Brasil, Polícia Federal)",
        |  "organizer": "Nome da banca organizadora (Ex: CESPE, FCC, FGV)",
        |  "examFormat": "Certo/Errado ou Múltipla Escolha",
        |  "examDate": "Data da prova escrita em formato DD/MM/AAAA ou nulo se não achar",
        |  "examLocation": "Cidades ou regiões de aplicação da prova",
        |  "allowedPen": "Especificação da caneta permitida (cor e tipo de tubo)",
        |  "allowedItems": ["item 1", "item 2"],
        |  "prohibitedItems": ["item 1", "item 2"],
        |  "disciplines": ["Disciplina A", "Disciplina B"]
        |}
    """.trimMargin()
}
