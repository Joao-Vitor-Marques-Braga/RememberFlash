package com.rememberflash.app.data.remote.gemini

/**
 * Templates de prompt rígidos para mitigar alucinações da IA Generativa (RN04).
 * Cada template é imutável e estruturado para forçar respostas em JSON válido
 * com campos predefinidos, reduzindo a margem de invenção do modelo.
 */
object PromptTemplates {

    fun buildEssayTranscribePrompt(): String = """
        |Você é um especialista em OCR e transcrição paleográfica de redações manuscritas em português brasileiro.
        |Transcreva fielmente todo o texto manuscrito presente na imagem da folha de redação.
        |
        |DIRETRIZES RÍGIDAS:
        |1. Transcreva exatamente as palavras, pontuações, acentos e quebras de linha/parágrafo escritas pelo autor.
        |2. Mantenha os parágrafos separados por uma linha em branco.
        |3. Corrija apenas erros óbvios de legibilidade da caligrafia cursiva, mantendo o conteúdo, estrutura e vocabulário originais.
        |4. NÃO adicione introdução, saudações, explicações, aspas extras ou notas adicionais.
        |5. Retorne APENAS o texto puro transcrito da redação.
    """.trimMargin()

    fun buildEssayEvaluationPrompt(
        essayText: String,
        theme: String,
        banca: String,
        rigor: String,
        tone: String
    ): String {
        val bancaDirective = when {
            banca.contains("CEBRASPE", ignoreCase = true) || banca.contains("CESPE", ignoreCase = true) ->
                "BANCA EXAMINADORA: CEBRASPE / CESPE. Aplique rigorosamente a metodologia do CEBRASPE: avaliação de Apresentação e Estrutura Textual (legibilidade, respeito às margens e paragrafação), Desenvolvimento do Tema (progressão lógica e profundidade dos tópicos) e Domínio da Modalidade Escrita / Norma Culta (descontos objetivos de microestrutura: grafia, acentuação, pontuação, morfossintaxe e regência)."
            banca.contains("FGV", ignoreCase = true) ->
                "BANCA EXAMINADORA: FGV (Fundação Getulio Vargas). Aplique o padrão FGV: foco extremo na consistência argumentativa, pertinência vocabular precisa, estruturação de parágrafos sem clichês, e domínio impecável da norma culta."
            banca.contains("FCC", ignoreCase = true) ->
                "BANCA EXAMINADORA: FCC (Fundação Carlos Chagas). Aplique o padrão FCC: avaliação dividida em Conteúdo (perspectiva adotada e capacidade de reflexão crítica), Estrutura (coesão, encadeamento e articulação das frases) e Expressão (adequação vocabular, concordância, regência e pontuação)."
            banca.contains("VUNESP", ignoreCase = true) ->
                "BANCA EXAMINADORA: VUNESP. Aplique o padrão VUNESP: Critério A (Tema e abordagem crítica), Critério B (Gênero dissertativo, estrutura e progressão), Critério C (Coesão, coerência e modalidade escrita)."
            banca.contains("UNIRV", ignoreCase = true) ->
                "BANCA EXAMINADORA: UniRV (Universidade de Rio Verde). Avalie pelas 5 competências (Adequação ao Tema, Norma Culta, Argumentação, Coesão/Coerência e Conclusão), com nota total de 0.0 a 10.0."
            else ->
                "BANCA EXAMINADORA: $banca. Avalie adotando os critérios formais e a matriz de correção típica desta banca examinadora em concursos públicos de alto nível."
        }

        return """
        |Você é um examinador e avaliador especialista em redações de vestibulares e concursos públicos.
        |$bancaDirective
        |Avalie minuciosamente o texto abaixo, sendo justo, criterioso e realista na atribuição de notas.
        |
        |DIRETRIZES DE AVALIAÇÃO:
        |A nota final varia de 0.0 a 10.0 e deve corresponder EXATAMENTE à soma das 5 competências avaliadas (cada uma valendo de 0.0 a 2.0):
        |
        |1. Adequação ao Tema e Gênero Textual (0.0 a 2.0):
        |   - 2.0: Aborda o tema integralmente com clareza e perfeito domínio da estrutura dissertativo-argumentativa.
        |   - 1.5: Aborda o tema com pequenos desvios secundários ou tangenciamento leve.
        |   - 1.0: Abordagem superficial do tema ou estrutura textual confusa.
        |   - 0.5 ou 0.0: Fuga parcial/total ao tema ou cópia de textos motivadores.
        |
        |2. Domínio da Norma Culta e Correção Gramatical (0.0 a 2.0):
        |   - 2.0: Excelente, no máximo 1 a 2 desvios gramaticais/ortográficos leves.
        |   - 1.5: Bom domínio, de 3 a 5 desvios de concordância, pontuação, acentuação ou regência.
        |   - 1.0: Razoável, presença frequente de erros gramaticais que prejudicam a fluidez.
        |   - 0.5 ou menor: Muitos erros graves de ortografia, concordância e sintaxe.
        |
        |3. Argumentação, Consistência e Repertório (0.0 a 2.0):
        |   - 2.0: Argumentos sólidos, fundamentados com repertório consistente e tese bem defendida.
        |   - 1.5: Argumentação previsível (senso comum), mas coerente.
        |   - 1.0: Argumentos frágeis, circulares ou contradições no posicionamento.
        |   - 0.5 ou menor: Ausência de fundamentação ou exposição sem defesa de ponto de vista.
        |
        |4. Coesão e Coerência Textual (0.0 a 2.0):
        |   - 2.0: Uso diversificado e correto de conectivos interparágrafos e intraparágrafos, progressão lógica fluida.
        |   - 1.5: Pouca variedade de operadores argumentativos ou repetição frequente de termos.
        |   - 1.0: Falhas de articulação que dificultam a leitura entre as ideias.
        |   - 0.5 ou menor: Quebra total de progressão textual ou frases desconexas.
        |
        |5. Estrutura Conclusiva e Fechamento das Ideias (0.0 a 2.0):
        |   - 2.0: Conclusão consistente que retoma a tese e sintetiza a discussão de forma clara e completa.
        |   - 1.5: Conclusão presente, porém vaga ou com solução/fechamento genérico.
        |   - 1.0: Conclusão abrupta ou que insere novos argumentos sem fechamento.
        |   - 0.5 ou menor: Ausência de conclusão ou texto inacabado.
        |
        |CONFIGURAÇÕES DO CONCURSO ATIVO:
        |- Banca Examinadora: $banca
        |- Rigor na Correção: $rigor. (Se for 'Rigoroso' ou 'Rígido', seja implacável com desvios gramaticais e argumentação fraca. Se for 'Flexível', valorize mais o esforço e a clareza geral. Se for 'Padrão', siga estritamente o padrão da banca examinadora).
        |- Tom do Feedback: $tone. (Se for 'Direto', seja sucinto e aponte os erros e acertos diretamente. Se for 'Explicativo', explique a regra gramatical/argumentativa e dê exemplos de como melhorar. Se for 'Descontraído', use uma linguagem encorajadora e amigável).
        |
        |TEMA DA PROPOSTA: $theme
        |
        |TEXTO DA REDAÇÃO:
        |$essayText
        |
        |IMPORTANTE:
        |- Seja crítico e evite notas infladas ou padronizadas. A maioria dos textos reais apresenta erros gramaticais ou argumentativos e não deve receber notas próximas de 10 automaticamente.
        |- O campo "nota" DEVE ser a soma exata das notas de cada uma das 5 competências (ex: se as notas forem 1.5, 1.0, 1.5, 1.5, 1.0, a "nota" total deve ser 6.5).
        |
        |Responda EXCLUSIVAMENTE em JSON válido no formato abaixo, sem nenhum texto adicional ou markdown fora do bloco JSON:
        |{
        |  "nota": <soma_exata_das_competencias_entre_0.0_e_10.0>,
        |  "competencias": [
        |    {
        |      "nome": "Adequação ao tema e gênero textual",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback analítico e apontamento de falhas ou acertos>"
        |    },
        |    {
        |      "nome": "Domínio da norma culta e correção gramatical",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback analítico e apontamento de erros encontrados>"
        |    },
        |    {
        |      "nome": "Argumentação e consistência",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback analítico sobre a tese e repertório>"
        |    },
        |    {
        |      "nome": "Coesão e coerência",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback analítico sobre o encadeamento e conectivos>"
        |    },
        |    {
        |      "nome": "Estrutura conclusiva e fechamento",
        |      "nota": <0.0 a 2.0>,
        |      "comentario": "<feedback analítico sobre a conclusão>"
        |    }
        |  ],
        |  "pontos_fortes": ["<ponto 1>", "<ponto 2>"],
        |  "sugestoes_melhoria": ["<sugestão 1>", "<sugestão 2>"]
        |}
        """.trimMargin()
    }

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
        
        val escopoRegra = if (contextType == "edital") {
            """
            |   - Você deve responder APENAS dúvidas relacionadas às regras, datas, disciplinas, requisitos e detalhes do Edital do Concurso fornecido abaixo.
            |   - Se o estudante fizer perguntas gerais de outro assunto fora do escopo deste edital (ex: pedir receitas, piadas, papo furado), você deve recusar de forma simpática, respondendo exatamente:
            |     "Como seu tutor de estudos, meu foco é ajudar você com as dúvidas deste edital. Vamos voltar às regras ou conteúdo da prova."
            """.trimMargin()
        } else {
            """
            |   - Você deve responder APENAS dúvidas relacionadas diretamente ao assunto da questão/redação fornecida abaixo.
            |   - Se o estudante fizer perguntas gerais de outro assunto, fora do escopo educacional da questão/redação ou tentar desviar o assunto (ex: pedir receitas, programar código não relacionado, piadas, papo furado), você deve recusar de forma simpática, respondendo exatamente:
            |     "Como seu tutor de estudos, meu foco é ajudar você com o concurso. Vamos voltar à dúvida sobre o conteúdo."
            """.trimMargin()
        }
        
        return """
            |Você é um Tutor Particular de Estudos 1:1, especialista em concursos públicos brasileiros.
            |Seu objetivo é sanar dúvidas específicas sobre uma questão recém-respondida, sobre o feedback de uma redação do estudante, ou sobre o edital do concurso.
            |
            |REGRAS DO TUTOR:
            |1. Tom do Tutor Interativo: $tone.
            |   - Se for 'Direto/Objetivo', seja curto, claro e direto ao ponto na explicação.
            |   - Se for 'Explicativo/Detalhado', explique didaticamente, se aprofundando nos conceitos e trazendo exemplos práticos.
            |2. LIMITE DE ESCOPO (Fuga de Escopo - A1):
            |$escopoRegra
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
