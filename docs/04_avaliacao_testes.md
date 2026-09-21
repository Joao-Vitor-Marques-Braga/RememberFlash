# 4. AVALIAÇÃO

## 4.1 VERIFICAÇÃO E AVALIAÇÃO

A etapa de verificação e avaliação constitui um pilar fundamental da engenharia de software, tendo por finalidade constatar empiricamente se o produto desenvolvido atende com precisão aos requisitos funcionais (RF001 a RF014) e respeita as regras de negócio previamente estabelecidas (RN01 a RN09). No projeto **RememberFlash**, a estratégia de avaliação combinou a execução de uma suíte automatizada de testes unitários isolados na JVM e inspeções rigorosas por meio de análise estática de código.

### 4.1.1 Testes unitários

Para assegurar o correto funcionamento e a estabilidade dos módulos da aplicação móvel, foram desenvolvidos e executados testes unitários utilizando o framework **JUnit 4** integrado às bibliotecas **MockK**, **Turbine** e **Kotlinx Coroutines Test**. A priorização dos testes foi estruturada para blindar rigorosamente o pipeline de valor central do concurseiro no aplicativo:

1. **Módulo de Cadastro e Gestão de Concursos (`contest`):** Validação da persistência de diretrizes do certame (Banca, Cargo, Tipo de Questão - RF003), validação estrita de tamanho e formato de editais em PDF (até 15MB), parametrização dos perfis de IA sem inserção de prompt livre (RN04) e execução de exclusão lógica (*Soft Delete* - RN06) com alternância segura do status `isActive` sem perda de histórico.
2. **Módulo de Geração de Questões e Simulados (`question`):** Validação da geração procedimental de questões via IAG calibradas por nível de dificuldade e banca examinadora (RF008), montagem de simulados balanceados proporcionais ao edital (RF009) e contagem progressiva do tempo de resolução por questão para controle de ritmo de prova (RF010).
3. **Módulo de Geração e Gestão de Flashcards (`flashcard`):** Validação da criação manual e geração automatizada de cartões de estudo mnemônicos a partir da mineração de documentos via IA (RF006 / RF007), garantindo o correto isolamento e vinculação por disciplina e tópico temático.
4. **Módulo de Geração do Cronograma Dinâmico (`schedule`):** Validação da restrição estrita de "Data da Prova" futura (RN05 / RF013), distribuição proporcional da carga horária semanal e cálculo de metas diárias de horas e revisão baseadas nos dias da semana disponíveis.
5. **Módulo de Avaliação de Redações via OCR e IA (`essay`):** Validação do fluxo híbrido de captura textual: extração local on-device via Google ML Kit para imagens contendo textos impressos e tipografados, combinada à transcrição de caligrafia cursiva e manuscrita executada diretamente pela própria IAG multimodal (Google Gemini); validação da avaliação analítica com rigor pedagógico parametrizado (flexível, padrão ou rigoroso - RN04) e persistência estruturada do histórico de notas e feedbacks com critérios formativos de correção (RF011 / RF012).
6. **Módulo de Recalibragem Pedagógica Autônoma do Cronograma (`schedule`):** Validação do algoritmo inteligente que analisa o aproveitamento pós-simulado (RN09 / RF013), identificando matérias com taxa de acertos inferior a 70% e aplicando um multiplicador adaptativo de reforço (1.3x a 2.0x) para redistribuir a rotina de estudos em prol das disciplinas deficitárias.
7. **Restante do Sistema (Ampla Cobertura):** Validação dos demais módulos fundamentais da aplicação:
   * *Autenticação e Usuário (`auth` / `user`):* Validação matemática rigorosa de CPF (algoritmo dos dígitos verificadores e rejeição de 11 dígitos repetidos - RN01 / RF001), integridade de tokens e controle de sessão local offline-first (RN02 / RF002).
   * *Tutoria Interativa (`tutor`):* Validação das salvaguardas pedagógicas que travam conversação livre desconexa, restringindo o prompt da IA ao contexto de erro na questão resolvida, feedback da redação ou mineração do edital (RN07 / RF014).
   * *Estados de Interface (ViewModels):* Auditoria reativa de fluxos `StateFlow` via **Turbine**, validando a alternância consistente de estados (*Idle -> Loading -> Success / Error*).

A abordagem adotada permitiu isolar as regras de negócio de dependências de infraestrutura, simulando comportamentos de sucesso (*Happy Path*) e de tratamento de exceções (*Edge Cases / Negative Paths*). A seguir, detalham-se os cenários de testes executados e aprovados para os módulos prioritários do sistema:

#### 4.1.1.1 Módulo de Cadastro e Gestão de Concursos (`contest`)

O primeiro bloco prioritário de testes unitários foi consolidado na classe `ContestTest`, cobrindo exaustivamente o caso de uso `CreateContestUseCase`, o ViewModel `ContestFormViewModel` e os casos de uso de exclusão lógica e reativação (`SoftDeleteContestUseCase`, `ReactivateContestUseCase`, `GetActiveContestsUseCase`, `GetArchivedContestsUseCase`). 

As validações contemplaram:
* **Persistência de Diretrizes (RF003):** Comprovação de que as diretrizes do certame (Banca Organizadora, Cargo Pretendido, Tipo de Questão e parametrizações de IA) são salvas com integridade no banco de dados. No cenário de cadastro sem edital em PDF, o caso de uso `CreateContestUseCase` insere o registro com sucesso e vincula automaticamente as disciplinas básicas padrão ("Conhecimentos Gerais" e "Conhecimentos Específicos").
* **Validação Estrita de Editais em PDF (RF003):** No `ContestFormViewModel`, assegurou-se que extensões inválidas (como `.docx` ou arquivos de imagem) e documentos que ultrapassem o teto de 15MB sejam interceptados imediatamente na camada de apresentação, atribuindo mensagens de erro descritivas ao `uiState` e bloqueando o envio, enquanto PDFs íntegros são admitidos com limpeza de quaisquer falhas preexistentes.
* **Parametrização Segura de IA sem Prompt Livre (RN04):** Conforme preconizado pela regra **RN04**, o formulário de concurso impede categoricamente que o usuário injete prompts arbitrários ou instruções abertas na IA. Os testes validaram que apenas opções estritamente enumeradas de dificuldade (*Fácil, Médio, Difícil*), rigor pedagógico (*Flexível, Padrão, Rigoroso*) e tom (*Direto, Explicativo, Socrático*) são aceitas e propagadas para os modelos de domínio.
* **Exclusão Lógica e Preservação de Histórico (RN06):** Validou-se a regra **RN06**, que determina que a exclusão de um concurso não deve expurgar dados físicos do banco de dados local Room nem do Supabase. O teste comprovou que a ação de arquivamento aciona `softDeleteContestUseCase`, alternando o atributo `isActive` para `false` e preservando intactos o histórico de disciplinas, simulados e questões, enquanto os seletores reativos (`GetActiveContestsUseCase` e `GetArchivedContestsUseCase`) segregam com precisão os certames ativos dos arquivados, permitindo também a sua restauração determinística com `reactivateContestUseCase`.

A Tabela XX sintetiza a matriz de cenários executados para o Módulo de Cadastro e Gestão de Concursos.

TABELA XX – Matriz de Testes Unitários do Módulo de Concursos
| ID | Classe / Componente Alvo | Método / Cenário Testado | Tipo de Cenário | Regra / Requisito | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| TU-CONC-01 | `CreateContestUseCase` | `createContestUseCase should persist contest guidelines and create default disciplines when no PDF is attached` | Happy Path | RF003 | Concurso persistido com banca, cargo e tipo de questão; 2 disciplinas padrão criadas. | Aprovado |
| TU-CONC-02 | `ContestFormViewModel` | `ContestFormViewModel should validate mandatory guidelines before saving without edital` | Edge Case | RF003 | Emissão de erro de validação para campos obrigatórios vazios (Título e Banca). | Aprovado |
| TU-CONC-03 | `ContestFormViewModel` | `ContestFormViewModel should require Organizer and Job Position when PDF edital is attached` | Edge Case | RF003 | Bloqueio de submissão se edital estiver anexado sem definição de Banca e Cargo. | Aprovado |
| TU-CONC-04 | `ContestFormViewModel` | `ContestFormViewModel onPdfError should reject invalid formats like docx or images` | Edge Case | RF003 | Notificação imediata de formato não suportado; estado de erro no formulário. | Aprovado |
| TU-CONC-05 | `ContestFormViewModel` | `ContestFormViewModel onPdfError should reject files exceeding the 15MB limit` | Edge Case | RF003 | Rejeição de arquivo com alerta de limite máximo de 15MB excedido. | Aprovado |
| TU-CONC-06 | `ContestFormViewModel` | `ContestFormViewModel addPdfAttachment should accept valid PDF and clear errors` | Happy Path | RF003 | PDF admitido, URI adicionada à lista e mensagens de erro limpas do estado. | Aprovado |
| TU-CONC-07 | `ContestFormViewModel` | `ContestFormViewModel should strictly configure predefined AI profiles without free prompt input` | Happy Path | RN04 | Atualização restrita aos perfis canônicos (Médio, Rigoroso, Socrático) sem input livre. | Aprovado |
| TU-CONC-08 | `SoftDeleteContestUseCase` | `softDeleteContestUseCase should deactivate contest setting isActive to false without deleting data` | Happy Path | RN06 | Atributo `isActive` comutado para `false`; registro mantido no repositório. | Aprovado |
| TU-CONC-09 | `SoftDeleteContestUseCase` | `softDeleteContestUseCase should fail when invalid contest ID is provided` | Edge Case | RN06 | Retorno de `Result.Error` caso o ID informado não exista no banco. | Aprovado |
| TU-CONC-10 | `GetActiveContestsUseCase` / `GetArchivedContestsUseCase` | `getActiveContestsUseCase and getArchivedContestsUseCase should filter correctly based on isActive flag` | Happy Path | RN06 | Segregação determinística em fluxos reativos (`Flow`) entre concursos ativos e arquivados. | Aprovado |
| TU-CONC-11 | `ReactivateContestUseCase` | `reactivateContestUseCase should restore archived contest setting isActive to true` | Happy Path | RN06 | Reativação com sucesso restaurando `isActive = true` no certame. | Aprovado |
Fonte: O autor (2026).

Conforme apresentado na Figura XX, foi realizado um teste unitário que valida o cenário positivo da persistência das diretrizes de um novo concurso sem edital anexado, executado pelo caso de uso `CreateContestUseCase`. Os repositórios de concursos (`ContestRepository`) e de disciplinas (`DisciplineRepository`) foram configurados com implementações simuladas (*fake/mock*), permitindo verificar o armazenamento dos dados de forma isolada na JVM. Visto que o certame requer a definição de diretrizes mandatórias (Banca Organizadora, Cargo Pretendido e Formato de Questão - RF003) e que o cadastro sem anexo de edital deve provisionar uma base preliminar de estudos para o usuário, o teste assegurou que o método `insert` do repositório de concursos foi executado persistindo com exatidão as informações enviadas e definindo o certame como ativo (`isActive = true`), além de confirmar que o método `insert` do repositório de disciplinas foi acionado para criar automaticamente as duas disciplinas padrão de "Conhecimentos Gerais" e "Conhecimentos Específicos", e que o resultado corresponde ao identificador numérico gerado esperado para o concurso.

FIGURA XX – Teste Unitário – Cadastro de Concurso e Criação de Disciplinas Básicas (RF003)
*(Espaço reservado para inserção do print do teste unitário TU-CONC-01 no Android Studio)*
Fonte: O autor (2026).

Em seguida, conforme apresentado na Figura XX, foi realizado um teste unitário que valida a restrição estrita de formato e tamanho de arquivos de edital anexados ao formulário de concurso, executado pelo ViewModel `ContestFormViewModel`. O componente de apresentação foi instanciado com os casos de uso mockados e o gerenciador de sincronização injetado. Visto que o requisito **RF003** determina a aceitação restrita de editais em formato PDF e impõe um teto dimensional de até 15MB por arquivo para mitigar riscos de estouro de memória nativa e consumo desnecessário da cota multimodal de IAG, o teste assegurou que o método `onPdfError` intercepta com sucesso arquivos que excedam o limite estabelecido, rejeitando a inclusão do documento na lista de anexos (`pdfAttachments`) e emitindo a notificação de erro correspondente no estado da interface (`uiState.error`), assegurando igualmente que a inserção de um arquivo válido remova alertas prévios e anexe o documento com sucesso.

FIGURA XX – Teste Unitário – Validação de Anexo de Edital em PDF até 15MB (RF003)
*(Espaço reservado para inserção do print do teste unitário TU-CONC-05 / TU-CONC-06 no Android Studio)*
Fonte: O autor (2026).

Adicionalmente, conforme apresentado na Figura XX, foi realizado um teste unitário que valida a parametrização dos perfis de Inteligência Artificial sem inserção de prompt livre, executado pelo ViewModel `ContestFormViewModel`. O componente de apresentação foi instanciado com os casos de uso mockados e repositórios simulados. Visto que a regra de negócio **RN04** e o requisito **RF005** determinam que o comportamento cognitivo da IAG generativa deve ser calibrado unicamente por meio de perfis predefinidos fechados (Dificuldade: *Fácil, Médio, Difícil*; Rigor Pedagógico: *Flexível, Padrão, Rigoroso*; Tom de Resposta: *Direto, Explicativo, Socrático*), mitigando riscos de alucinação, evasão e comandos livres desconexos, o teste assegurou que as alterações de perfil através dos métodos `onAiDifficultyChanged`, `onAiRigorChanged` e `onAiToneChanged` atualizam de forma determinística o estado da tela (`uiState`), e que, ao submeter o formulário via `onSaveClicked`, esses parâmetros canônicos são persistidos com exatidão na entidade do concurso no repositório (`aiDifficulty`, `aiRigor` e `aiTone`), sem qualquer exposição de campos de digitação aberta de prompts.

FIGURA XX – Teste Unitário – Parametrização dos Perfis de IA sem Prompt Livre (RN04)
*(Espaço reservado para inserção do print do teste unitário TU-CONC-07 no Android Studio)*
Fonte: O autor (2026).

Por fim, conforme apresentado na Figura XX, foi realizado um teste unitário que valida a execução de exclusão lógica (*Soft Delete*) e a preservação integral do histórico acadêmico do certame, executado pelo caso de uso `SoftDeleteContestUseCase`. O repositório de concursos (`ContestRepository`) e o de disciplinas (`DisciplineRepository`) foram configurados de forma isolada na JVM, simulando a existência prévia de um concurso ativo associado a matérias cadastradas. Visto que a regra de negócio **RN06** determina categoricamente que o arquivamento ou exclusão de um certame não deve expurgar dados físicos do banco de dados, resguardando o histórico de simulados, questões e métricas de desempenho do estudante, o teste assegurou que o método `softDelete` comutou o status `isActive` para `false` sem deletar o registro nem suas disciplinas vinculadas, comprovando ainda que o caso de uso `ReactivateContestUseCase` é capaz de restaurar o certame ao estado ativo (`isActive = true`) sem qualquer perda de consistência.

FIGURA XX – Teste Unitário – Exclusão Lógica e Preservação de Histórico (RN06)
*(Espaço reservado para inserção do print do teste unitário TU-CONC-08 / TU-CONC-11 no Android Studio)*
Fonte: O autor (2026).

A Figura XX ilustra a execução com êxito da suíte de testes unitários do Módulo de Concursos no ambiente de automação de testes do Android Studio via Gradle.

FIGURA XX – Testes Unitários Aprovados do Módulo de Cadastro e Gestão de Concursos (RF003, RN04, RN06)
*(Espaço reservado para inserção do print da execução dos 11 testes aprovados da classe ContestTest)*
Fonte: O autor (2026).

A execução da suíte `ContestTest` resultou na aprovação integral dos 11 cenários de testes projetados (taxa de sucesso de 100%), atestando a robustez tanto da camada de domínio quanto da camada de apresentação. O tempo total de compilação e execução isolada na JVM via Gradle foi de 42 segundos, dispensando completamente o consumo de recursos de emuladores ou dispositivos físicos conectados. Os resultados comprovaram empiricamente que a persistência das diretrizes do certame (RF003), o bloqueio de arquivos corrompidos ou superiores a 15MB (RF003), o encapsulamento estrito dos perfis cognitivos da IA sem concessão de prompt livre (RN04) e a alternância segura do status `isActive` sem expurgo físico de registros (RN06) comportam-se de forma determinística e resiliente, garantindo a estabilidade estrutural necessária para o prosseguimento das etapas acadêmicas do concurseiro.

#### 4.1.1.2 Módulo de Geração de Questões e Simulados (`question`)

O segundo bloco prioritário de testes unitários foi concebido para validar o motor de geração de itens avaliativos por Inteligência Artificial Generativa e a esteira de resolução interativa de simulados, abrangendo os requisitos **RF008**, **RF009** e **RF010**. Foram avaliados os casos de uso `GenerateQuestionsUseCase`, `GenerateContestMockExamUseCase` e o ViewModel de resolução `QuestionResolveViewModel`.

As validações contemplaram:
* **Geração Procedimental via IAG Calibrada (RF008):** Comprovação de que o caso de uso `GenerateQuestionsUseCase` injeta rigorosamente os parâmetros de disciplina, banca examinadora (ex.: FGV, Cebraspe), formato de questão (Múltipla Escolha com 5 alternativas ou Certo/Errado) e nível de dificuldade cognitiva ao requisitar o modelo Gemini, validando a desserialização do contrato JSON retornado e bloqueando palavras-chave inseguras através de filtro local.
* **Montagem de Simulados Gerais do Certame (RF009):** No caso de uso `GenerateContestMockExamUseCase`, assegurou-se a orquestração do simulado completo, gerando o volume exato de itens estabelecido para cada matéria conforme os quantitativos fixados pela banca examinadora no edital do concurso.
* **Cronometragem e Histórico de Tentativas (RF010):** Validação da persistência estruturada do histórico de resolução em `MockExamAttempt`, armazenando as respostas assinaladas e o vetor JSON contendo o tempo individual despendido em cada questão (em milissegundos) para controle de ritmo de prova, além do cômputo reativo de pontuação no `QuestionResolveViewModel`.

A Tabela XX sintetiza a matriz de testes unitários executados para o Módulo de Questões e Simulados.

TABELA XX – Matriz de Testes Unitários do Módulo de Questões e Simulados
| ID | Classe / Componente Alvo | Método / Cenário Testado | Tipo de Cenário | Regra / Requisito | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| TU-QUEST-01 | `GenerateQuestionsUseCase` | `generateQuestions should call Gemini with correct parameters and save parsed questions (RF008)` | Happy Path | RF008 | Geração de questões via IA com banca e dificuldade estritas; persistência como `AI_GENERATED`. | Aprovado |
| TU-QUEST-02 | `GenerateQuestionsUseCase` | `generateQuestions should block unsafe themes according to security filter keywords` | Edge Case | RF008 | Bloqueio de temas sensíveis antes do envio à API com mensagem de segurança. | Aprovado |
| TU-QUEST-03 | `GenerateQuestionsUseCase` | `generateQuestions should handle invalid JSON response returning error` | Edge Case | RF008 | Tratamento de resposta malformada da IA retornando `Result.Error` amigável. | Aprovado |
| TU-QUEST-04 | `GenerateContestMockExamUseCase` | `generateContestMockExam should orchestrate full mock exam with questions per discipline defined by exam board` | Happy Path | RF009 | Orquestração do simulado geral gerando as questões de cada disciplina conforme estipulado pela banca examinadora. | Aprovado |
| TU-QUEST-05 | `GenerateContestMockExamUseCase` | `recordAttempt should persist attempt in repository with answers history and per-question times` | Happy Path | RF010 | Gravação de pontuação, total de questões e mapas JSON de respostas e tempos por questão. | Aprovado |
| TU-QUEST-06 | `QuestionResolveViewModel` | `init should load discipline and questions correctly` | Happy Path | RF010 | Carregamento reativo das questões da disciplina e inicialização do índice em zero. | Aprovado |
| TU-QUEST-07 | `QuestionResolveViewModel` | `submitAnswer should verify correct answer and increment score` | Happy Path | RF010 | Validação da opção correta com incremento imediato da pontuação. | Aprovado |
| TU-QUEST-08 | `QuestionResolveViewModel` | `submitAnswer should handle incorrect answer without incrementing score` | Edge Case | RF010 | Interceptação de alternativa incorreta mantendo a pontuação estável. | Aprovado |
| TU-QUEST-09 | `QuestionResolveViewModel` | `nextQuestion should advance to next question and complete on last question` | Happy Path | RF010 | Avanço entre questões e finalização do simulado com gravação automática da tentativa. | Aprovado |
Fonte: O autor (2026).

Conforme apresentado na Figura XX, foi realizado um teste unitário que valida o cenário positivo da geração automatizada de questões via Inteligência Artificial Generativa, executado pelo caso de uso `GenerateQuestionsUseCase`. Os repositórios de disciplinas (`DisciplineRepository`), concursos (`ContestRepository`) e questões (`QuestionRepository`) foram configurados com implementações simuladas (*fakes*), e o cliente de integração com o Google Gemini (`GeminiClient`) foi mockado para retornar um payload JSON estruturado contendo enunciado, alternativas, índice da resposta correta e fundamentação pedagógica. Visto que o requisito **RF008** estabelece que as questões devem ser geradas sob medida respeitando a banca examinadora do concurso (ex.: FGV), o formato pretendido (Múltipla Escolha com 5 alternativas) e o nível cognitivo configurado (ex.: Difícil), o teste assegurou que o caso de uso invocou o motor de IAG com os parâmetros canônicos exatos, converteu o JSON recebido para a entidade de domínio `Question` com a marcação de origem `AI_GENERATED`, limpou o lote anterior da disciplina no repositório e persistiu com sucesso a nova bateria de questões.

FIGURA XX – Teste Unitário – Geração Procedimental de Questões via IAG (RF008)
*(Espaço reservado para inserção do print do teste unitário TU-QUEST-01 no Android Studio)*
Fonte: O autor (2026).

Em seguida, conforme apresentado na Figura 36, foi realizado um teste unitário que valida a orquestração e montagem do simulado geral do certame com base na distribuição de questões estipulada pela banca examinadora, executado pelo caso de uso `GenerateContestMockExamUseCase`. Os repositórios de concursos (`ContestRepository`), disciplinas (`DisciplineRepository`) e questões (`QuestionRepository`) foram configurados com implementações simuladas (*fakes*) na JVM, e o caso de uso de geração granular (`GenerateQuestionsUseCase`) foi mockado para responder com êxito a cada requisição de matéria. Visto que o requisito **RF009** preconiza que o simulado geral não deve gerar um número arbitrário de questões, mas sim consolidar a prova completa respeitando os quantitativos de itens definidos no edital do certame para cada disciplina (neste cenário, 10 questões para Direito Constitucional e 10 questões para Direito Administrativo, totalizando a prova da banca FGV), o teste assegurou que o caso de uso identificou as disciplinas ativas do concurso, obteve a quantidade de questões estipulada para cada uma, disparou a geração das respectivas baterias, emitiu as notificações de progresso esperadas a cada etapa (`onProgress`) e concluiu com sucesso a orquestração do simulado completo.

FIGURA 36 - Teste Unitário – Montagem de Simulado Geral com Disciplinas da Banca (RF009)
*(Espaço reservado para inserção do print do teste unitário TU-QUEST-04 no Android Studio)*
Fonte: O autor (2026).

Por fim, conforme apresentado na Figura XX, foi realizado um teste unitário que valida a persistência estruturada do histórico de resolução de simulados e o registro individual do tempo despendido por questão, executado pelo método `recordAttempt` do caso de uso `GenerateContestMockExamUseCase`. O repositório de questões (`QuestionRepository`) foi instanciado com uma implementação simulada na JVM para recepcionar os registros de tentativa. Visto que o requisito **RF010** estabelece o controle de ritmo de prova por meio da cronometragem progressiva de cada questão respondida e a consolidação dos acertos para cálculo estatístico de desempenho, o teste assegurou que o caso de uso converteu adequadamente os mapas de respostas (`answersMap`) e de tempos gastos em milissegundos (`timesMap`) para formato JSON serializado (`answersJson` e `timesJson`), gravando com sucesso a entidade `MockExamAttempt` com a pontuação final (2 de 3 acertos) e mantendo a integridade temporal do simulado.

FIGURA XX – Teste Unitário – Histórico de Resolução e Cronometragem por Questão (RF010)
*(Espaço reservado para inserção do print do teste unitário TU-QUEST-05 no Android Studio)*
Fonte: O autor (2026).

A Figura XX ilustra a execução com êxito da suíte de testes unitários do Módulo de Questões e Simulados no ambiente de automação de testes do Android Studio via Gradle.

FIGURA XX – Testes Unitários Aprovados do Módulo de Questões e Simulados (RF008, RF009, RF010)
*(Espaço reservado para inserção do print da execução dos 10 testes aprovados das classes de questões no index.html ou Android Studio)*
Fonte: O autor (2026).

A execução das classes de testes que compõem o módulo (`GenerateQuestionsUseCaseTest`, `GenerateContestMockExamUseCaseTest` e `QuestionResolveViewModelTest`) obteve 100% de sucesso nos 10 cenários desenvolvidos, com tempo total de execução na JVM de aproximadamente 12 segundos. Os resultados atestaram a capacidade do aplicativo de orquestrar chamadas de IAG de maneira controlada (RF008), salvaguardar a consistência na distribuição de questões proporcionais às matérias (RF009) e mensurar com acurácia a velocidade de resolução dos concurseiros (RF010), constituindo a base empírica necessária para os módulos subsequentes de revisão ativa.

Em seguida, no teste unitário apresentado na Figura XX, validou-se o caso de uso `GenerateStudyScheduleUseCase`, comprovando que qualquer tentativa de cadastrar um cronograma com data de prova no passado ou no mesmo dia é interceptada e rejeitada, garantindo a restrição de data futura estipulada pela **RN05**.

FIGURA XX – Teste Unitário – Validação de Data Futura no Cronograma de Estudos (RN05)
*(Espaço reservado para inserção do print do teste de data futura)*
Fonte: O autor (2026).

No módulo de redações, conforme ilustrado na Figura XX, foi estruturado um teste unitário no caso de uso de extração e avaliação textual (`ExtractTextFromImageUseCase` / `EvaluateEssayUseCase`), comprovando o funcionamento da arquitetura híbrida de reconhecimento: transcrição de caligrafia manuscrita realizada diretamente pela própria IAG multimodal (Google Gemini) quando configurada a chave de API, com suporte a textos impressos/digitados e fallback por OCR local (Google ML Kit), seguido pela injeção do perfil de rigor selecionado (RN04) e pela emissão estruturada de notas e apontamentos gramaticais sem risco de comandos livres ou alucinações.

FIGURA XX – Teste Unitário – Avaliação Analítica de Redação com OCR e IA (RF011)
*(Espaço reservado para inserção do print do teste de avaliação de redação)*
Fonte: O autor (2026).

Outro teste unitário de destaque, apresentado na Figura XX, foi implementado no caso de uso `ProposeScheduleRecalculationUseCase`. O cenário simula a conclusão de um simulado no qual o estudante obteve taxa de aproveitamento de 50% em uma determinada matéria. O teste comprovou que o algoritmo autônomo identifica o déficit inferior a 70% (conforme preconizado pela **RN09**), gera um item de dificuldade, aplica o multiplicador adaptativo de foco e eleva a meta de minutos diários dedicada à matéria no cronograma sugerido.

FIGURA XX – Teste Unitário – Algoritmo Adaptativo de Recalibragem de Cronograma (RN09)
*(Espaço reservado para inserção do print do teste do algoritmo de recálculo)*
Fonte: O autor (2026).

Na Figura XX, evidencia-se também a validação no caso de uso `RegisterUseCase`, comprovando o bloqueio de cadastros com CPF duplicado e a verificação aritmética dos dígitos verificadores, impedindo inconsistências documentais conforme a **RN01**.

FIGURA XX – Teste Unitário – Validação Matemática e Unicidade de CPF (RN01)
*(Espaço reservado para inserção do print do teste unitário de CPF no Android Studio)*
Fonte: O autor (2026).

Conforme evidenciado na Figura XX, a suíte completa de testes unitários foi executada na JVM via Gradle, obtendo 100% de aprovação e atestando a robustez dos componentes e das regras de negócio do aplicativo móvel.

FIGURA XX – Painel Geral de Execução dos Testes Unitários Aprovados
*(Espaço reservado para inserção do print do painel de testes do Android Studio - barra verde / Passed)*
Fonte: O autor (2026).

---

### 4.1.2 Teste de análise estática

A análise estática de código foi conduzida utilizando a ferramenta oficial **Android Lint** em conjunto com as inspeções automáticas de código da IDE **Android Studio**. Esse procedimento permitiu identificar falhas potenciais, riscos de vazamento de memória e divergências em relação às diretrizes oficiais de desenvolvimento para Android antes da compilação final da aplicação.

Durante esse processo, foram identificadas e resolvidas inconsistências técnicas relevantes:

1. **Caso 1: Gerenciamento Seguro de Fluxos no PDFBox Android (Resource Leak):**
   * *Problema Identificado:* Na rotina inicial de mineração de editais em PDF, a leitura do arquivo estava sendo executada sem o fechamento determinístico da instância `PDDocument` em blocos de exceção, gerando alerta de risco de esgotamento de descritores de arquivo e memória (*Resource Leak*).
   * *Solução Adotada:* Refatorou-se o método encapsulando o objeto em uma estrutura segura `use { document -> ... }`, garantindo o fechamento e a liberação imediata da memória nativa ao término da leitura, mesmo na ocorrência de falhas.

FIGURA XX – Análise Estática – Caso Leitura de PDF (Alerta de Vazamento de Recursos)
*(Espaço reservado para inserção do print do código antes da correção)*
Fonte: O autor (2026).

FIGURA XX – Análise Estática – Caso Leitura de PDF (Resolvido com bloco `use`)
*(Espaço reservado para inserção do print do código corrigido)*
Fonte: O autor (2026).

2. **Caso 2: Recomposição Desnecessária em Listas do Jetpack Compose (Performance):**
   * *Problema Identificado:* Na tela de listagem de flashcards e disciplinas, a passagem direta de listas instáveis para componentes `LazyColumn` disparava recomposições excessivas de itens que não haviam sofrido mutação de estado (*Unnecessary Recompositions*).
   * *Solução Adotada:* Aplicou-se o modificador `key` explícito nos itens das listas com base no identificador primário (`item.id`) e declarou-se o estado com `remember` e coleções imutáveis, garantindo que o Jetpack Compose atualize visualmente apenas os elementos modificados.

FIGURA XX – Análise Estática – Caso de Recomposição no Jetpack Compose
*(Espaço reservado para inserção do print do alerta do Compose Lint)*
Fonte: O autor (2026).

FIGURA XX – Análise Estática – Caso de Recomposição no Jetpack Compose (Otimizado com `key`)
*(Espaço reservado para inserção do print do código otimizado)*
Fonte: O autor (2026).

3. **Caso 3: Criptografia e Armazenamento Seguro de Credenciais:**
   * *Problema Identificado:* O linter apontou risco de segurança na tentativa de armazenamento de parâmetros de configuração em SharedPreferences convencionais.
   * *Solução Adotada:* Migrou-se a persistência das chaves de API e tokens JWT para o `EncryptedSharedPreferences` com a chave mestre `MasterKey.DEFAULT_MASTER_KEY_ALIAS`, atendendo às recomendações do Guia de Segurança do Android.

FIGURA XX – Análise Estática – Armazenamento Seguro de Credenciais (Resolvido)
*(Espaço reservado para inserção do print da configuração do EncryptedSharedPreferences)*
Fonte: O autor (2026).
