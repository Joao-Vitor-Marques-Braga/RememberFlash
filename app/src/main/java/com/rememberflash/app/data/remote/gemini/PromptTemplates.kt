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

    fun buildFullEditalParsingPrompt(
        editalText: String,
        jobPosition: String
    ): String = """
        |Você é um assistente especialista em concursos públicos brasileiros.
        |Analise o texto completo do edital (e anexos) fornecido abaixo focado especificamente no cargo: $jobPosition.
        |Retorne APENAS um objeto JSON estruturado.
        |
        |### CARGO DE INTERESSE:
        |$jobPosition
        |
        |### CONTEXTO DO EDITAL (TEXTO COMPLETO EXTRAÍDO):
        |$editalText
        |
        |### INSTRUÇÕES DE RETORNO:
        |Responda EXCLUSIVAMENTE em JSON válido no formato abaixo, sem tags de marcação (como ```json) ou qualquer outro texto adicional.
        |
        |Identifique no texto:
        |1. Nome simplificado do concurso (Ex: INSS, Banco do Brasil, Polícia Federal).
        |2. Nome da banca organizadora (Ex: CEBRASPE, FCC, FGV).
        |3. Formato da prova (Certo/Errado ou Múltipla Escolha).
        |4. Data da prova escrita em formato DD/MM/AAAA (ou null se não encontrada).
        |5. Locais de aplicação da prova.
        |6. Especificação da caneta permitida (cor da tinta e material do tubo).
        |7. Lista de itens permitidos e proibidos de portar/levar.
        |8. O conjunto completo de disciplinas exigidas no conteúdo programático EXCLUSIVAMENTE para o cargo de "$jobPosition". Tente identificar o número de questões ou peso de cada disciplina para este cargo. Se não estiver explícito, atribua um valor proporcional adequado (ex: 10 para as mais importantes, 5 para as básicas).
        |
        |{
        |  "title": "Nome simplificado do concurso (Ex: INSS, Banco do Brasil, Polícia Federal)",
        |  "organizer": "Nome da banca organizadora (Ex: CESPE, FCC, FGV)",
        |  "examFormat": "Certo/Errado ou Múltipla Escolha",
        |  "examDate": "Data da prova escrita em formato DD/MM/AAAA ou nulo se não achar",
        |  "examLocation": "Cidades ou regiões de aplicação da prova",
        |  "allowedPen": "Especificação da caneta permitida (cor e tipo de tubo)",
        |  "allowedItems": ["item 1", "item 2"],
        |  "prohibitedItems": ["item 1", "item 2"],
        |  "disciplines": [
        |    {
        |      "name": "Nome da disciplina (Ex: Língua Portuguesa)",
        |      "weight": <número de questões ou peso do edital (Ex: 10.0 ou 15.0)>
        |    }
        |  ]
        |}
    """.trimMargin()

    fun buildTutorChatPrompt(
        contextType: String,
        contextDetails: String,
        tone: String,
        history: List<Pair<String, String>>,
        latestMessage: String
    ): String {
        val historyBlock = history.joinToString("\n") { (user, tutor) ->
            "Estudante: $user\nTutor: $tutor"
        }
        
        return """
            |Você é um Tutor Particular de Estudos 1:1, especialista em concursos públicos brasileiros.
            |Seu objetivo é sanar dúvidas específicas sobre uma questão recém-respondida ou sobre o feedback de uma redação do estudante.
            |
            |REGRAS DO TUTOR:
            |1. Tom do Tutor Interativo: $tone.
            |   - Se for 'Direto/Objetivo', seja curto, claro e direto ao ponto na explicação.
            |   - Se for 'Explicativo/Detalhado', explique didaticamente, se aprofundando nos conceitos e trazendo exemplos práticos.
            |2. LIMITE DE ESCOPO (Fuga de Escopo - A1):
            |   - Você deve responder APENAS dúvidas relacionadas diretamente ao assunto da questão/redação fornecida abaixo.
            |   - Se o estudante fizer perguntas gerais de outro assunto, fora do escopo educacional da questão/redação ou tentar desviar o assunto (ex: pedir receitas, programar código não relacionado, piadas, papo furado), você deve recusar de forma simpática, respondendo exatamente:
            |     "Como seu tutor de estudos, meu foco é ajudar você com o concurso. Vamos voltar à dúvida sobre o conteúdo."
            |
            |CONTEXTO ATIVO DE ESTUDO (TIPO: $contextType):
            |$contextDetails
            |
            |HISTÓRICO DA CONVERSA:
            |$historyBlock
            |
            |Estudante: $latestMessage
            |Tutor:
        """.trimMargin()
    }

    fun buildStudySchedulePrompt(
        contestTitle: String,
        disciplines: List<Pair<String, Double>>,
        startDateStr: String,
        endDateStr: String,
        minutesPerDay: Int,
        maxSubjectsPerDay: Int,
        availableDaysOfWeek: List<String>
    ): String = """
        |Você é um assistente especialista em planejar rotinas de estudo de alta performance.
        |Gere um cronograma de estudos personalizado para o concurso: "$contestTitle".
        |
        |### PARÂMETROS DE ENTRADA:
        |- Data de Início: $startDateStr
        |- Data da Prova: $endDateStr
        |- Minutos disponíveis por dia: $minutesPerDay minutos
        |- Máximo de matérias por dia: $maxSubjectsPerDay matérias
        |- Dias da semana disponíveis para estudo: ${availableDaysOfWeek.joinToString(", ")}
        |
        |### DISCIPLINAS E PESOS DO EDITAL:
        |${disciplines.joinToString("\n") { (name, weight) -> "- $name (Peso/Importância: $weight)" }}
        |
        |### REGRAS DE DISTRIBUIÇÃO:
        |1. Distribua as matérias proporcionalmente entre a data de início e a data da prova. Matérias com maior peso/importância devem receber mais tempo de estudo ou sessões mais frequentes.
        |2. Programe sessões APENAS nos dias da semana especificados como disponíveis. Nos outros dias, não agende nenhuma sessão (deixe como descanso).
        |3. O tempo total alocado em um único dia não pode ultrapassar $minutesPerDay minutos.
        |4. Não agende mais de $maxSubjectsPerDay matérias diferentes no mesmo dia.
        |5. Caso o tempo total até a prova seja insuficiente para cobrir todo o conteúdo programático (conforme peso das disciplinas e tempo disponível), preencha o campo "warning" com a mensagem: "O tempo selecionado é insuficiente para cobrir todas as matérias até a data da prova. O cronograma foi montado focando apenas nos tópicos de maior peso." E faça a distribuição resumida focando nas disciplinas de maior importância.
        |
        |### FORMATO DE RETORNO:
        |Responda EXCLUSIVAMENTE em JSON válido no formato abaixo, sem tags de marcação (como ```json) ou qualquer outro texto adicional.
        |
        |{
        |  "warning": "Mensagem de alerta caso o tempo seja insuficiente, ou null",
        |  "sessions": [
        |    {
        |      "date": "YYYY-MM-DD",
        |      "disciplineName": "Nome exato da disciplina da lista fornecida",
        |      "minutes": <minutos de estudo alocados para este dia (Ex: 60)>
        |    }
        |  ]
        |}
    """.trimMargin()
}
