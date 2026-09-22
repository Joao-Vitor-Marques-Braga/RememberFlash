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

No início das avaliações do Módulo de Questões e Simulados, conforme apresentado na Figura 35, realizou-se um teste unitário para validar o fluxo principal de geração automatizada de itens avaliativos via Inteligência Artificial Generativa, operacionalizado pelo caso de uso `GenerateQuestionsUseCase`. Para a execução do teste, os repositórios de disciplinas (`DisciplineRepository`), concursos (`ContestRepository`) e questões (`QuestionRepository`) foram instanciados com implementações simuladas (*fakes*), e o cliente de integração com a API Gemini (`GeminiClient`) foi mockado para retornar um *payload* JSON estruturado, contendo enunciado, alternativas, gabarito e fundamentação pedagógica. Visto que o requisito **RF008** estabelece que as questões devem ser formuladas sob medida segundo as diretrizes da banca examinadora (ex.: FGV), formato pretendido (múltipla escolha com cinco alternativas) e nível de complexidade configurado (ex.: difícil), o teste comprovou que o caso de uso invocou o motor de IAG com os parâmetros exatos de calibração, desserializou o contrato JSON para a entidade `Question` com a marcação de proveniência `AI_GENERATED`, limpou os registros obsoletos da matéria e persistiu com sucesso o novo lote de questões no banco local.

FIGURA 35 – Teste Unitário – Geração Procedimental de Questões via IAG (RF008)
*(Espaço reservado para inserção do print do teste unitário TU-QUEST-01 no Android Studio)*
Fonte: O autor (2026).

Em seguida, conforme apresentado na Figura 36, foi realizado um teste unitário que valida a orquestração e montagem do simulado geral do certame com base na distribuição de questões estipulada pela banca examinadora, executado pelo caso de uso `GenerateContestMockExamUseCase`. Os repositórios de concursos (`ContestRepository`), disciplinas (`DisciplineRepository`) e questões (`QuestionRepository`) foram configurados com implementações simuladas (*fakes*) na JVM, e o caso de uso de geração granular (`GenerateQuestionsUseCase`) foi mockado para responder com êxito a cada requisição de matéria. Visto que o requisito **RF009** preconiza que o simulado geral não deve gerar um número arbitrário de questões, mas sim consolidar a prova completa respeitando os quantitativos de itens definidos no edital do certame para cada disciplina (neste cenário, 10 questões para Direito Constitucional e 10 questões para Direito Administrativo, totalizando a prova da banca FGV), o teste assegurou que o caso de uso identificou as disciplinas ativas do concurso, obteve a quantidade de questões estipulada para cada uma, disparou a geração das respectivas baterias, emitiu as notificações de progresso esperadas a cada etapa (`onProgress`) e concluiu com sucesso a orquestração do simulado completo.

FIGURA 36 - Teste Unitário – Montagem de Simulado Geral com Disciplinas da Banca (RF009)
*(Espaço reservado para inserção do print do teste unitário TU-QUEST-04 no Android Studio)*
Fonte: O autor (2026).

Por fim, conforme apresentado na Figura 37, foi realizado um teste unitário que valida a persistência estruturada do histórico de resolução de simulados e o registro individual do tempo despendido por questão, executado pelo método `recordAttempt` do caso de uso `GenerateContestMockExamUseCase`. O repositório de questões (`QuestionRepository`) foi instanciado com uma implementação simulada na JVM para recepcionar os registros de tentativa. Visto que o requisito **RF010** estabelece o controle de ritmo de prova por meio da cronometragem progressiva de cada questão respondida e a consolidação dos acertos para cálculo estatístico de desempenho, o teste assegurou que o caso de uso converteu adequadamente os mapas de respostas (`answersMap`) e de tempos gastos em milissegundos (`timesMap`) para formato JSON serializado (`answersJson` e `timesJson`), gravando com sucesso a entidade `MockExamAttempt` com a pontuação final (2 de 3 acertos) e mantendo a integridade temporal do simulado.

FIGURA 37 - Teste Unitário – Histórico de Resolução e Cronometragem por Questão (RF010)
*(Espaço reservado para inserção do print do teste unitário TU-QUEST-05 no Android Studio)*
Fonte: O autor (2026).

Complementando a validação do módulo na camada de apresentação (MVVM), conforme demonstrado na Figura 38, realizou-se o teste unitário da classe `QuestionResolveViewModel`, responsável por gerenciar a interação do usuário durante a resolução do simulado em tempo real. O teste utilizou um despachante de corrotinas controlado (`StandardTestDispatcher`), juntamente com implementações simuladas dos casos de uso de recuperação de disciplinas e questões, além de um `SavedStateHandle` injetado. Visto que o requisito **RF010** exige resposta imediata na experiência de resolução, o teste assegurou que, ao selecionar uma alternativa e submeter a resposta, o ViewModel avalia instantaneamente o gabarito, reflete o acerto ou erro no estado reativo da tela (`uiState.score`), avança progressivamente entre as questões e, ao atingir o último item da lista, comuta o estado da prova para finalizado (`isFinished = true`), disparando a persistência automática da tentativa no repositório.

FIGURA 38 – Teste Unitário – Resolução Interativa e Cômputo Reativo de Pontuação no ViewModel (RF010)
*(Espaço reservado para inserção do print do teste unitário TU-QUEST-09 no Android Studio)*
Fonte: O autor (2026).

A Figura 39 ilustra a execução com êxito da suíte de testes unitários do Módulo de Questões e Simulados no ambiente de automação de testes do Android Studio via Gradle.

FIGURA 39 – Testes Unitários Aprovados do Módulo de Questões e Simulados (RF008, RF009, RF010)
*(Espaço reservado para inserção do print da execução dos 10 testes aprovados das classes de questões no index.html ou Android Studio)*
Fonte: O autor (2026).

A execução das classes de testes que compõem o módulo (`GenerateQuestionsUseCaseTest`, `GenerateContestMockExamUseCaseTest` e `QuestionResolveViewModelTest`) obteve 100% de sucesso nos 10 cenários desenvolvidos, distribuídos em três baterias de testes automatizados com tempo total de execução na JVM de aproximadamente 7 segundos. Os resultados atestaram a capacidade do aplicativo de orquestrar chamadas de IAG de maneira controlada com filtros de segurança (RF008), salvaguardar a consistência na distribuição de questões estipuladas pela banca examinadora (RF009) e mensurar com acurácia a velocidade de resolução e a pontuação dos concurseiros na interface (RF010), constituindo a base empírica necessária para os módulos subsequentes de revisão ativa.

#### 4.1.1.3 Módulo de Geração e Gestão de Flashcards (`flashcard`)

O terceiro bloco prioritário de testes unitários teve como propósito avaliar o mecanismo de memorização ativa do aplicativo, responsável pela criação manual e geração autônoma de cartões mnemônicos (*flashcards*) ancorados no algoritmo de repetição espaçada SM-2, compreendendo os requisitos funcionais **RF006** e **RF007**. Foram avaliados os casos de uso `CreateFlashcardUseCase` e `GenerateFlashcardsFromTextUseCase`.

As validações contemplaram:
* **Criação Manual e Calibração Mnemônica (RF006):** Verificação das restrições de integridade no preenchimento de frentes e versos, vinculação obrigatória a uma disciplina válida e inicialização determinística dos parâmetros canônicos do algoritmo de repetição espaçada SM-2 (`easeFactor = 2.5`, `interval = 0`, `repetitions = 0`), atribuindo a proveniência `FlashcardSource.MANUAL`.
* **Mineração Automatizada de Documentos via IA (RF007):** Validação da extração e sintetização de conceitos-chave a partir de materiais textuais através da API Google Gemini, interceptando arquivos escaneados sem camada legível de texto (< 50 caracteres), respeitando o teto de quantidade solicitada e garantindo a persistência estrita com marcação `FlashcardSource.PDF_EXTRACT` vinculada à disciplina e ao respectivo tópico temático.

A Tabela XX sintetiza a matriz de testes unitários do Módulo de Geração e Gestão de Flashcards.

TABELA XX – Matriz de Testes Unitários do Módulo de Flashcards
| ID | Classe / Componente Alvo | Método / Cenário Testado | Tipo de Cenário | Regra / Requisito | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| TU-FLASH-01 | `CreateFlashcardUseCase` | `createFlashcard should fail when front is blank` | Edge Case | RF006 | Rejeição de criação com frente vazia retornando mensagem informativa. | Aprovado |
| TU-FLASH-02 | `CreateFlashcardUseCase` | `createFlashcard should fail when back is blank` | Edge Case | RF006 | Rejeição de criação com verso vazio impedindo cartão inconsistente. | Aprovado |
| TU-FLASH-03 | `CreateFlashcardUseCase` | `createFlashcard should fail when disciplineId is invalid` | Edge Case | RF006 | Bloqueio de inserção sem vínculo estrito a uma disciplina válida. | Aprovado |
| TU-FLASH-04 | `CreateFlashcardUseCase` | `createFlashcard should succeed with valid data and initialize default SM-2 metrics` | Happy Path | RF006 | Gravação manual com sucesso e inicialização canônica do algoritmo SM-2. | Aprovado |
| TU-FLASH-05 | `GenerateFlashcardsFromTextUseCase` | `useCase should return error for text shorter than 50 chars` | Edge Case | RF007 | Interceptação de documento sem camada de texto (escaneado) com alerta amigável. | Aprovado |
| TU-FLASH-06 | `GenerateFlashcardsFromTextUseCase` | `useCase should generate flashcards and persist with correct discipline and topic` | Happy Path | RF007 | Geração procedimental via Gemini, desserialização JSON e vinculação temática. | Aprovado |
| TU-FLASH-07 | `GenerateFlashcardsFromTextUseCase` | `useCase should respect quantity limit` | Happy Path | RF007 | Limitação exata da quantidade de cartões persistidos conforme solicitado pelo usuário. | Aprovado |
Fonte: O autor (2026).

No início das avaliações do Módulo de Flashcards, conforme demonstrado na Figura 40, realizou-se um teste unitário para validar as regras de integridade e a inicialização de parâmetros mnemônicos na criação manual de cartões de estudo, executado pelo caso de uso `CreateFlashcardUseCase`. O repositório de flashcards (`FlashcardRepository`) foi instanciado com uma implementação simulada (*fake*) em memória na JVM. Visto que o requisito **RF006** exige a validação estrita dos dados para impedir cartões vazios ou órfãos e estabelece a adoção do algoritmo de repetição espaçada SM-2 para orientar as futuras revisões ativas, o teste assegurou que o caso de uso barrou tentativas com campos vazios ou identificador de disciplina nulo e, diante de dados válidos, persistiu o cartão com a marcação `FlashcardSource.MANUAL`, inicializando o fator de facilidade padrão em 2.5 (`easeFactor`), o intervalo inicial em zero dias e a contagem de repetições zerada.

FIGURA 40 – Teste Unitário – Criação Manual e Inicialização SM-2 de Flashcard (RF006)
*(Espaço reservado para inserção do print do teste unitário TU-FLASH-04 no Android Studio)*
Fonte: O autor (2026).

Em seguida, conforme apresentado na Figura 41, foi executado um teste unitário para validar o motor de mineração e síntese de flashcards a partir de documentos de estudo via Inteligência Artificial Generativa, operacionalizado pelo caso de uso `GenerateFlashcardsFromTextUseCase`. Para o teste, o cliente de integração com a API Gemini (`GeminiClient`) foi mockado para responder com uma estrutura JSON contendo perguntas e respostas formuladas a partir do texto de entrada. Visto que o requisito **RF007** determina que o aplicativo deve ser capaz de minerar resumos e materiais em PDF transformando-os em cartões estruturados vinculados à disciplina e tópico de estudo, o teste comprovou que o caso de uso rejeitou textos insuficientes ou sem camada legível de OCR (< 50 caracteres), invocou a IA com o texto fornecido, processou o retorno JSON estruturado, limitou a quantidade ao teto requisitado e persistiu os cartões vinculados à disciplina e ao tópico com a marcação `FlashcardSource.PDF_EXTRACT`.

FIGURA 41 – Teste Unitário – Mineração Automatizada de Flashcards via IA (RF007)
*(Espaço reservado para inserção do print do teste unitário TU-FLASH-06 no Android Studio)*
Fonte: O autor (2026).

A Figura 42 ilustra a execução com êxito da bateria de testes unitários voltada à criação manual de cartões e inicialização das métricas mnemônicas no caso de uso `CreateFlashcardUseCase`.

FIGURA 42 – Testes Unitários Aprovados – Criação Manual e Inicialização SM-2 de Flashcards (RF006)
*(Print da classe CreateFlashcardUseCaseTest no index.html ou Android Studio)*
Fonte: O autor (2026).

Por sua vez, a Figura 43 demonstra a aprovação integral dos testes automatizados de mineração e síntese de flashcards a partir de documentos via Inteligência Artificial no caso de uso `GenerateFlashcardsFromTextUseCase`.

FIGURA 43 – Testes Unitários Aprovados – Mineração e Geração de Flashcards via IA (RF007)
*(Print da classe GenerateFlashcardsFromTextUseCaseTest no index.html ou Android Studio)*
Fonte: O autor (2026).

A execução isolada das duas baterias de testes que compõem o módulo obteve 100% de sucesso nos 7 cenários desenvolvidos (4 testes para criação manual e 3 testes para geração automatizada via IA), com tempo total acumulado na JVM de aproximadamente 17 segundos. Os resultados atestaram a robustez do aplicativo na aplicação consistente do algoritmo de repetição espaçada SM-2 (RF006) e na sintetização inteligente de documentos didáticos em cartões de memorização (RF007), garantindo a persistência estrita e o isolamento contextual dos materiais por disciplina e tópico.

#### 4.1.1.4 Módulo de Cronograma Dinâmico de Estudos (`schedule`)

O quarto bloco de testes unitários foi projetado para assegurar a consistência temporal e pedagógica do planejador de estudos adaptativo, compreendendo os requisitos **RF013** e a regra de negócio **RN05**. Foram submetidos a avaliação automatizada o caso de uso orquestrador `GenerateStudyScheduleUseCase` e o componente de apresentação `ScheduleViewModel`.

As validações contemplaram:
* **Restrição Temporal de Data da Prova Futura (RN05 / RF013):** Comprovação de que o sistema rejeita de forma determinística qualquer tentativa de agendamento de cronograma cuja data do certame seja no passado ou no dia corrente, exigindo obrigatoriamente um horizonte temporal futuro válido.
* **Distribuição Proporcional da Carga Horária e Metas Diárias (RF013):** Validação do cálculo de sessões de estudo com base no tempo diário disponível (`minutesPerDay`), na quantidade máxima de matérias por dia (`maxSubjectsPerDay`) e nos dias da semana habilitados pelo estudante (ex.: segundas, quartas e sextas), gerando as metas pontuais (`DailyGoal`) de tempo e revisão mnemônica no banco de dados.
* **Limpeza e Substituição de Metas Anteriores (RF013):** Verificação de que a geração de um novo cronograma limpa adequadamente as metas preexistentes do concurso, prevenindo a duplicação ou sobreposição desordenada de tarefas no calendário.
* **Planejamento Semanal Recorrente de Longo Prazo (RF013):** Teste de projeção de cronogramas estendidos com desdobramento de metas por dezenas de semanas até a data da prova.

A Tabela XX sintetiza a matriz de testes unitários do Módulo de Cronograma Dinâmico.

TABELA XX – Matriz de Testes Unitários do Módulo de Cronograma Dinâmico
| ID | Classe / Componente Alvo | Método / Cenário Testado | Tipo de Cenário | Regra / Requisito | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| TU-SCHED-01 | `ScheduleViewModel` | `testViewModelValidatesFutureExamDate` | Edge Case | RN05 / RF013 | Bloqueio imediato de datas passadas com mensagem de erro na UI. | Aprovado |
| TU-SCHED-02 | `GenerateStudyScheduleUseCase` | `testGenerateScheduleSuccessfully` | Happy Path | RF013 | Geração do cronograma, rateio de minutos diários e persistência das metas. | Aprovado |
| TU-SCHED-03 | `GenerateStudyScheduleUseCase` | `testGenerateScheduleClearsOldGoals` | Happy Path | RF013 | Purga de metas anteriores antes de inserir o novo cronograma no repositório. | Aprovado |
| TU-SCHED-04 | `GenerateStudyScheduleUseCase` | `testGenerateScheduleHandlesInsufficientTimeWarning` | Edge Case | RF013 | Emissão e persistência de aviso pedagógico quando o tempo até a prova for escasso. | Aprovado |
| TU-SCHED-05 | `GenerateStudyScheduleUseCase` | `testGenerateScheduleWeeklyPlanLongTermUntil2027` | Happy Path | RF013 | Projeção em escala de metas semanais para cronogramas de longo prazo. | Aprovado |
Fonte: O autor (2026).

No início das avaliações do cronograma, conforme demonstrado na Figura 44, realizou-se um teste unitário focado na regra de negócio **RN05**, executado pela classe `ScheduleViewModel`. O teste utilizou implementações simuladas de repositórios e um despachante de teste de corrotinas (`StandardTestDispatcher`). Ao simular a entrada de uma data retroativa no formulário (ontem), o teste comprovou que o ViewModel interceptou a solicitação antes de qualquer envio de dados, comutou o estado de erro da tela para a mensagem `"A data da prova deve ser uma data futura válida."` e bloqueou a criação do plano, garantindo a restrição temporal estipulada pela **RN05** e **RF013**.

FIGURA 44 – Teste Unitário – Validação de Data Futura no Cronograma de Estudos (RN05, RF013)
*(Espaço reservado para inserção do print do teste TU-SCHED-01 no Android Studio)*
Fonte: O autor (2026).

Em seguida, conforme apresentado na Figura 45, foi executado um teste unitário no caso de uso `GenerateStudyScheduleUseCase` para validar a distribuição proporcional de horas e a criação das metas diárias de estudo (`DailyGoal`). Para a execução, o cliente de integração com a IA generativa (`GeminiScheduleClient`) foi mockado para responder com a grade semanal particionada entre as disciplinas cadastradas (Língua Portuguesa e Direito Constitucional). O teste assegurou que o caso de uso distribuiu rigorosamente a carga horária de 120 minutos diários em sessões balanceadas de 60 minutos para cada matéria, calculou as datas com base nos dias da semana liberados pelo aluno e persistiu com sucesso as metas diárias associadas ao cronograma, atendendo plenamente ao requisito **RF013**.

FIGURA 45 – Teste Unitário – Distribuição de Carga Horária e Metas Diárias (RF013)
*(Espaço reservado para inserção do print do teste TU-SCHED-02 no Android Studio)*
Fonte: O autor (2026).

A Figura 46 ilustra a execução com êxito da suíte de testes unitários do Módulo de Cronograma Dinâmico no ambiente de automação de testes do Android Studio via Gradle.

FIGURA 46 – Testes Unitários Aprovados do Módulo de Cronograma Dinâmico de Estudos (RF013, RN05)
*(Espaço reservado para inserção do print da classe ScheduleTest no index.html ou Android Studio)*
Fonte: O autor (2026).

A execução da classe `ScheduleTest` resultou em 100% de aproveitamento em todos os 5 cenários avaliados, com tempo total de execução na JVM de aproximadamente 5 segundos. Os resultados atestaram que o módulo impede anomalias temporais de agendamento retroativo (RN05) e calcula com exatidão a distribuição horária e as metas diárias personalizadas de estudo (RF013), assegurando a flexibilidade e a consistência requeridas na preparação de concurseiros.

#### 4.1.1.5 Módulo de Avaliação de Redações via OCR e IA (`essay`)

O quinto bloco de testes unitários foi formulado para validar a esteira de processamento e correção dissertativa, contemplando a digitalização de manuscritos (**RF011**), a avaliação analítica por Inteligência Artificial Generativa (**RF012**) e o encapsulamento estrito das diretrizes pedagógicas de rigor e tom do concurso (**RN04**). Foram avaliados os casos de uso `ExtractTextFromImageUseCase` e `EvaluateEssayUseCase`.

As validações contemplaram:
* **Extração Híbrida de Texto Manuscrito e Fallback OCR (RF011):** Verificação da arquitetura de reconhecimento visual, assegurando que o sistema prioriza a transcrição multimodal inteligente via Google Gemini para caligrafias cursivas quando a chave de API estiver ativa, aciona de forma resiliente o motor local do Google ML Kit em caso de instabilidade de conexão e emite mensagens claras de orientação caso a imagem capturada não possua texto legível.
* **Injeção de Rigor, Tom e Banca Examinadora (RF012 / RN04):** Comprovação de que o caso de uso `EvaluateEssayUseCase` resgata o concurso pai associado à redação e injeta estritamente o nome da banca (ex.: FGV), o nível de rigor (ex.: Rigoroso) e o tom avaliativo (ex.: Analítico e Crítico) no contrato de prompt com a IA, blindando o sistema contra comandos livres e persistindo o *payload* JSON estruturado com os critérios de correção e a nota final no banco de dados local.
* **Bloqueio de Redações sem OCR Prévio (RF012):** Validação de guarda que impede o envio de requisições de avaliação caso o texto extraído da imagem seja nulo ou vazio.

A Tabela XX sintetiza a matriz de testes unitários do Módulo de Avaliação de Redações.

TABELA XX – Matriz de Testes Unitários do Módulo de Redações
| ID | Classe / Componente Alvo | Método / Cenário Testado | Tipo de Cenário | Regra / Requisito | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| TU-RED-01 | `ExtractTextFromImageUseCase` | `extractText should prioritize AI handwriting transcription when Gemini API key is configured` | Happy Path | RF011 | Transcrição precisa de texto cursivo via IA multimodal com chave configurada. | Aprovado |
| TU-RED-02 | `ExtractTextFromImageUseCase` | `extractText should fallback to local OCR when AI transcription fails or throws exception` | Edge Case | RF011 | Comutação transparente para o Google ML Kit local na ocorrência de falhas na nuvem. | Aprovado |
| TU-RED-03 | `ExtractTextFromImageUseCase` | `extractText should return friendly error when no text is found in image` | Edge Case | RF011 | Interceptação de fotos desfocadas/sem texto emitindo alerta amigável de enquadramento. | Aprovado |
| TU-RED-04 | `EvaluateEssayUseCase` | `evaluateEssay should inject parent contest banca, rigor and tone into AI evaluator and persist feedback with score` | Happy Path | RF012 / RN04 | Injeção de banca, rigor e tom no avaliador de IA e gravação do JSON com nota 8.5. | Aprovado |
| TU-RED-05 | `EvaluateEssayUseCase` | `evaluateEssay should fail when essay extractedText is blank` | Edge Case | RF012 | Bloqueio de avaliação sem texto de OCR prévio impedindo chamadas inválidas à IA. | Aprovado |
Fonte: O autor (2026).

No início das validações de redação, conforme apresentado na Figura 47, realizou-se um teste unitário no caso de uso `ExtractTextFromImageUseCase` para validar a resiliência da esteira de OCR e transcrição caligráfica. O teste utilizou implementações mockadas do transcrevedor multimodal (`HandwrittenTranscriber`), do extrator óptico local (`TextExtractor`) e do repositório de autenticação (`AuthRepository`). Visto que o requisito **RF011** demanda alta fidelidade na leitura de redações manuscritas de concursos, o teste comprovou que o caso de uso priorizou o motor de IAG multimodal do Gemini quando a chave de API estava presente e, ao simular uma falha de conexão na nuvem, acionou imediatamente o fallback do OCR local pelo ML Kit, garantindo a extração do texto com sucesso mesmo diante de oscilações de conectividade.

FIGURA 47 – Teste Unitário – Transcrição Multimodal e Fallback de OCR na Redação (RF011)
*(Espaço reservado para inserção do print do teste TU-RED-01 / TU-RED-02 no Android Studio)*
Fonte: O autor (2026).

Em seguida, conforme ilustrado na Figura 48, foi executado um teste unitário no caso de uso `EvaluateEssayUseCase` para comprovar a injeção contextual de diretrizes pedagógicas e a persistência do feedback estruturado. Para o teste, o repositório do concurso pai (`ContestRepository`) foi configurado com a banca FGV, perfil de rigor `"Rigoroso"` e tom `"Analítico e Crítico"`, enquanto o avaliador de IA (`EssayEvaluator`) foi mockado para responder com um *payload* estruturado de correção contendo notas por competência, pontos fortes, oportunidades de melhoria e nota geral de 8.5. Visto que as regras **RF012** e **RN04** estabelecem que a IA deve avaliar a redação balizada estritamente pelo perfil configurado no certame, o teste assegurou que o caso de uso injetou com exatidão a banca e o tom predefinidos, realizou o *parsing* seguro do JSON retornado e persistiu com êxito a avaliação completa e a nota calculada na entidade `Essay`.

FIGURA 48 – Teste Unitário – Correção de Redação com Injeção de Rigor e Tom da Banca (RF012, RN04)
*(Espaço reservado para inserção do print do teste TU-RED-04 no Android Studio)*
Fonte: O autor (2026).

A Figura 49 ilustra a execução com êxito da suíte de testes unitários do Módulo de Redações no ambiente de automação de testes do Android Studio via Gradle.

FIGURA 49 – Testes Unitários Aprovados do Módulo de Avaliação de Redações (RF011, RF012, RN04)
*(Espaço reservado para inserção do print da classe EssayTest no index.html ou Android Studio)*
Fonte: O autor (2026).

A execução da classe `EssayTest` obteve 100% de aprovação nos 5 cenários avaliados, com tempo de execução na JVM de aproximadamente 35 segundos. Os resultados atestaram empiricamente a robustez da arquitetura híbrida de digitalização de textos manuscritos (RF011) e a eficácia da correção orientada por perfis calibrados de IA sem concessão de prompt livre (RF012, RN04), proporcionando ao estudante um diagnóstico pedagógico aprofundado e consistente com os critérios reais de bancas de concursos públicos.

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
