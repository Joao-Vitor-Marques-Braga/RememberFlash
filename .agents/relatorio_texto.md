# Relatório de Auditoria Textual e Coesão Acadêmica

**Documento Auditado:** `docs/JoaoVitorMarquesBraga_relatorio_DERS.md`  
**Data da Auditoria:** 15 de outubro de 2026  
**Skill Aplicada:** `.agents/skills/auditoria_academica.md`  
**Pipeline:** Agente 1 / Agente 2 - Auditoria de Texto Acadêmico e Científico

---

## 1. Verificação de Coesão: Objetivos Específicos vs. Entregas (Metodologia e Resultados)

| # | Objetivo Específico (Seção 1 - Linhas 351–356) | Entrega no DERS / Metodologia (Seção 2 e 3) | Validação nos Resultados (Seção 4) | Diagnóstico de Coesão |
| :-: | :--- | :--- | :--- | :--- |
| **OE1** | *Projetar uma interface gráfica responsiva e minimalista focada na redução da carga cognitiva visual e na otimização da experiência do usuário móvel.* | Protótipos de tela nos quadros de RF01 a RF14 (Seção 2.7); Material Design 3 e Jetpack Compose (Seção 3.3.2). | Menciona Compose UI Test na Seção 3.3.5, mas **não apresenta testes visuais nem avaliação empírica de usabilidade/UX** no Capítulo 4. | **LACUNA DE RESULTADOS:** Falta demonstrar a validação empírica da interface no Capítulo 4 (ex: métricas de usabilidade ou execução de Compose UI Test). |
| **OE2** | *Implementar a geração automatizada de lotes de flashcards via LLM a partir da extração de dados de editais em PDF, estruturando a persistência e o fluxo de revisão ativa em modo offline.* | RF06 e RF07; PdfBox-Android para extração sintática de PDF; Room Database com conversão de tipos (Gson); algoritmo SM-2 (Seção 3.1 e 3.3.3). | Validado na Seção 4.1.1 (Figuras 40, 41, 42 e 43) cobrindo criação manual, mineração IA e cálculo de intervalos SM-2. | **COESO E CUMPRIDO.** |
| **OE3** | *Desenvolver o módulo para a criação automatizada de simulados e questões de múltipla escolha focadas nas bancas examinadoras, adaptando o nível de complexidade ao progresso do usuário.* | RF08, RF09 e RF10; parametrização por banca, cargo e matéria; gerador de prova balanceado (Seções 2.7, 3.1 e 3.3.3). | Validado na Seção 4.1.1 (Figuras 35 a 39) com teste de montagem balanceada, cronometragem e resolução de simulado. | **COESO E CUMPRIDO.** |
| **OE4** | *Integrar o motor de IAG utilizando templates de prompt rígidos embutidos no código-fonte, estabelecendo salvaguardas que mitiguem riscos de alucinação.* | RF05, RN04 e RN07; travas contra envio de prompt livre; injeção contextual estrita na tutoria (RF14) e correção (RF12). | Validado na Seção 4.1.1 (Figuras 32 e 48) testando a injeção determinística de tom e rigor pedagógico sem desvios. | **COESO E CUMPRIDO.** |
| **OE5** | *Implementar um módulo de Reconhecimento Óptico de Caracteres (OCR) local (on-device) via Google ML Kit para digitalização e posterior correção de redações manuscritas a custo computacional zero de nuvem.* | RF11 e RF12; Google ML Kit Text Recognition on-device (Seção 3.3.3); fallback multimodal no Google Gemini para caligrafia cursiva (Seção 4.1.1). | Validado na Seção 4.1.1 (Figuras 46, 47, 48 e 49) abordando transcrição multimodal e fallback de OCR. | **INCONSISTÊNCIA TÉCNICA:** O OE5 afirma que o OCR é 100% on-device com custo zero de nuvem para manuscritos, mas os Resultados comprovam que texto manuscrito requer IA em nuvem (Gemini Multimodal). |
| **OE6** | *Desenvolver a lógica algorítmica reativa responsável pelo cálculo, criação e redistribuição automática de metas do cronograma diário baseada no desempenho do estudante.* | RF13, RN05 e RN09; restrição estrita de data de prova futura; multiplicador adaptativo (1.3x a 2.0x) para disciplinas com índice de acerto < 70%. | Validado na Seção 4.1.1 (Figuras 44, 45, 50 e 51) testando o algoritmo adaptativo de recalibragem de rotina. | **COESO E CUMPRIDO.** |

---

## 2. Requisitos de Software (Mapeamento de Tecnologias)

Abaixo estão consolidadas todas as menções tecnológicas extraídas ao longo do texto do TFC (`JoaoVitorMarquesBraga_relatorio_DERS.md`), estruturadas por camada arquitetural:

### 2.1. Camada de Apresentação e Mobile (Frontend)
- **Linguagem Principal:** Kotlin (tipagem estática, *null-safety*, concorrência com Coroutines/Flow).
- **Kit Declarativo de UI:** Jetpack Compose (Compose BOM, Compose UI, Compose Graphics).
- **Idiomaticidade Android:** AndroidX Core KTX, AndroidX Activity Compose.
- **Design System & Ergonomia:** Material Design 3 (*Material You*), Material Icons Extended.
- **Navegação e Ciclo de Vida:** Navigation Compose, Lifecycle Compose, ViewModel Compose, Hilt Navigation Compose.
- **Gerenciamento de Estado Reativo:** `StateFlow`, `SharedFlow`, `UiState`.

### 2.2. Camada de Negócio e Domínio (Clean Architecture)
- **Regras de Negócio e Casos de Uso:** Implementação em Kotlin Puro (independente de dependências de banco de dados ou Android framework).
- **Algoritmos Heurísticos Próprios:**
  - *Algoritmo de Repetição Espaçada:* Variação do SuperMemo 2 (SM-2) para agendamento mnemônico de flashcards.
  - *Algoritmo Heurístico de Recalibragem de Cronograma (RN09):* Cálculo adaptativo com fator ponderado (1.3x a 2.0x) para disciplinas deficitárias (< 70% acertos).
  - *Validação de CPF (RN01):* Algoritmo matemático oficial com verificação de módulo 11 e rejeição de sequências homogêneas.

### 2.3. Inteligência Artificial e Reconhecimento Óptico (IAG / OCR)
- **Motor de IAG:** Google Generative AI SDK (modelos Gemini 1.5 Flash / Pro).
- **Arquitetura de Prompts e Guardrails:** Templates rígidos embutidos no código-fonte, bloqueio de chat aberto desconexo (RN04 / RN07).
- **Reconhecimento Óptico Local (On-Device):** Google ML Kit Text Recognition para textos tipografados/impressos.
- **Processamento de Caligrafia Manuscrita:** Gemini Multimodal Vision API (processamento via nuvem para texto cursivo).
- **Ingestão Documental:** PdfBox-Android para interpretação, segmentação e mineração de editais em PDF (limite de 15MB).

### 2.4. Persistência de Dados e Nuvem (Estratégia Offline-First)
- **Banco de Dados Relacional Local:** Room Database (SQLite), módulos Runtime e KTX.
- **Conversores de Tipos Relacionais:** Gson (TypeConverters) para coleções de tags e alternativas estruturadas.
- **Segurança e Criptografia Local:** AndroidX Security Crypto (`EncryptedSharedPreferences`), criptografia simétrica AES-256 GCM integrada ao hardware (Android Keystore) para chave BYOK e sessão.
- **Backend e Nuvem:** Supabase SDK (PostgreSQL gerenciado, Supabase Auth, Postgrest RESTful, Supabase Storage para PDFs).
- **Sincronização:** `SyncManager` offline-first com persistência bidirecional.
- **Comunicação de Rede:** Ktor Client com motor assíncrono OkHttp.
- **Serialização de Dados:** Kotlinx Serialization JSON.

### 2.5. Injeção de Dependências e Build Tools
- **Injeção de Dependência:** Dagger Hilt (módulos `@Module`, `@InstallIn`, `@Provides`, `@Binds`).
- **Processamento de Anotações:** Kotlin Symbol Processing (KSP).
- **Automação de Compilação:** Gradle com Kotlin DSL (`build.gradle.kts`).
- **Sistemas de Controle de Versão e Gestão:** Git, GitHub, Notion (Kanban), GanttProject (Gráfico de Gantt).

### 2.6. Infraestrutura de Testes e Qualidade
- **Orquestração de Testes JVM:** JUnit 4.
- **Simulação e Isolamento:** MockK / Mockito-Kotlin.
- **Auditoria de Concorrência e Fluxos:** Kotlinx Coroutines Test (`runTest`, `StandardTestDispatcher`), Turbine para `StateFlow`.
- **Sincronização de Tarefas Arquiteturais:** AndroidX Arch Core Testing (`InstantTaskExecutorRule`).
- **Banco de Dados em Memória:** AndroidX Room Testing.
- **Testes de Instrumentação e UI:** AndroidX Test JUnit (AndroidJUnit4), Espresso, Compose UI Test.
- **Análise Estática de Código:** Android Lint, Inspeções da IDE Android Studio e Antigravity IDE.

---

## 3. Apontamentos Críticos e Sugestões de Correção

Abaixo estão listadas todas as falhas, lacunas de coesão e inconsistências identificadas, formatadas no padrão estrito da skill:

---

### [SEÇÃO]: 1. Introdução (Linha 355) vs. 4.1.1 Testes unitários (Linha 2072)
- **[PROBLEMA]:** Contradição técnica sobre a viabilidade e o custo do OCR de redações manuscritas. A Introdução estabelece no Objetivo Específico 5 que a digitalização de redações manuscritas é executada "localmente via Google ML Kit a custo computacional zero de nuvem". Contudo, a Seção 4.1.1 (item 5) esclarece com rigor que o ML Kit on-device só processa textos impressos/tipografados, sendo mandatória a chamada à IAG multimodal (Google Gemini) em nuvem para transcrever manuscritos cursivos.
- **[CORREÇÃO SUGERIDA]:** Reformular o 5º objetivo específico na Introdução (linha 355) para:  
  *"Implementar uma arquitetura híbrida de Reconhecimento Óptico de Caracteres (OCR) e transcrição, combinando a extração local on-device via Google ML Kit para textos tipografados com a interpretação multimodal em nuvem via Google Gemini para redações manuscritas e caligrafia cursiva, assegurando acurácia pedagógica com otimização de requisições."*

---

### [SEÇÃO]: 1. Introdução (Linha 351) vs. 4. Avaliação (Linhas 2058–2344)
- **[PROBLEMA]:** O Objetivo Específico 1 propõe "Projetar uma interface gráfica responsiva e minimalista focada na redução da carga cognitiva visual e na otimização da experiência do usuário móvel". Entretanto, no Capítulo 4 (Avaliação), os testes restringiram-se a testes unitários de casos de uso (JVM) e análise estática (Lint). Não há dados empíricos, testes de layout (Compose UI Test) ou avaliação de usabilidade/UX (como o System Usability Scale - SUS) que comprovem a validação deste objetivo.
- **[CORREÇÃO SUGERIDA]:** Adicionar uma subseção `4.1.3 Testes de Interface e Avaliação de Usabilidade`, descrevendo os testes automatizados com `ComposeTestRule` para aferição semântica dos nós de tela e a validação ergonômica sob as diretrizes do Material Design 3.

---

### [SEÇÃO]: 3.3.5 Ferramentas e bibliotecas de teste (Linha 2020) vs. 4.1.1 Testes unitários (Linha 2066)
- **[PROBLEMA]:** Divergência entre os frameworks de mock citados. Na Seção 3.3.5 afirma-se que o isolamento de dependências foi realizado com "Mockito Core e Mockito-Kotlin". Já na Seção 4.1.1 afirma-se que os testes utilizaram a biblioteca "MockK".
- **[CORREÇÃO SUGERIDA]:** Padronizar a menção tecnológica. Se o projeto utiliza MockK (o que é recomendado em projetos Kotlin puros com Coroutines), atualizar a Seção 3.3.5 para:  
  *"Para isolar o comportamento de dependências externas e repositórios sem acoplamento a serviços reais, empregou-se a biblioteca MockK, permitindo a criação de dublês de teste (mocks e fakes), espionagem de comportamento e a verificação estrita de chamadas e contratos de retorno compatíveis com corrotinas."*

---

### [SEÇÃO]: 4. Avaliação - Subseção 4.1.2 (Linha 2308)
- **[PROBLEMA]:** Erro de titulação e hierarquia. A subseção foi intitulada como `### 4.1.2 Testes unitários`, repetindo o título da subseção 4.1.1. O conteúdo aborda estritamente a execução do Android Lint.
- **[CORREÇÃO SUGERIDA]:** Alterar o título para:  
  `### 4.1.2 Análise Estática de Código (Android Lint)`

---

### [SEÇÃO]: 4. Avaliação - Legenda das Figuras 62 e 63 (Linha 2341)
- **[PROBLEMA]:** Duplicação do rótulo da Figura 62. A linha 2334 traz a Figura 62 correta (Conversão de URI) e a linha 2341 repete o cabeçalho `###### FIGURA 62 \- Análise Estática – Conversão de URI...` para o problema da classe `GenerateQuestionsUseCase` (tratamento da exceção `e`).
- **[CORREÇÃO SUGERIDA]:** Atualizar a legenda da linha 2341 para:  
  `###### FIGURA 63 - Análise Estática – Tratamento de Parâmetro Não Utilizado no GenerateQuestionsUseCase (Resolvido) {#figura-63---analise-estatica-parametro-nao-utilizado}`

---

### [SEÇÃO]: Lista de Figuras (Linhas 88 e 90)
- **[PROBLEMA]:** Duplicação na lista pré-textual de ilustrações, onde a "Tela de tutor" e o "Diagrama de caso de uso" compartilham o mesmo número (`FIGURA 22`).
- **[CORREÇÃO SUGERIDA]:** Renumerar a partir do Diagrama de caso de uso para `FIGURA 23`, corrigindo os números das figuras subsequentes até a Figura 63.

---

### [SEÇÃO]: 2.7 Requisitos Funcionais - Legendas de Figuras 32 e 40 (Linhas 110, 126, 134)
- **[PROBLEMA]:** Erro de digitação com duplicação de palavras nas legendas: "FIGURA 32 - Teste Unitário – Teste Unitário – Parametrização..." e "FIGURA 40 - Teste Unitário – Teste Unitário – Criação Manual...".
- **[CORREÇÃO SUGERIDA]:** Remover a duplicação em todas as ocorrências:  
  `FIGURA 32 - Teste Unitário – Parametrização de Perfis de IA com padrões pré-definidos (RN04, RF005)`  
  `FIGURA 40 - Teste Unitário – Criação Manual e Inicialização SM-2 de Flashcard (RF006)`

---

### [SEÇÃO]: 2.7 Requisitos Funcionais - Marcadores Markdown Órfãos (Linhas 511, 533, 535, 547, 549...)
- **[PROBLEMA]:** Presença de linhas contendo apenas `##### ` vazias sem texto, poluindo a estrutura de cabeçalhos do documento gerado.
- **[CORREÇÃO SUGERIDA]:** Remover todas as linhas órfãs de subtítulo vazias entre os Quadros de Requisitos.

---

### [SEÇÃO]: 4. Avaliação - Ausência do Manual do Usuário (Guia UniRV pág. 154-155)
- **[PROBLEMA]:** O manuscrito não contém a seção prática obrigatória de **Manual do Usuário**. As figuras de telas foram dispersas como protótipos dentro dos quadros de casos de uso na Seção 2.7 (DERS), mas as normas da FASOFT/UniRV exigem uma seção independente ao final do Capítulo 4 contendo:
  1. Prints das telas da aplicação em alta fidelidade com **demarcadores circulares numerados** sobre os botões, campos e menus;
  2. Guia de navegação descritivo abaixo de cada figura listando a finalidade de cada componente, formato dos dados, obrigatoriedade de preenchimento e mensagens de validação e feedback de erro geradas pelo sistema.
- **[CORREÇÃO SUGERIDA]:** Adicionar a seção `4.3 MANUAL DO USUÁRIO` estruturada com o mapeamento visual guiado das principais rotas do aplicativo (Login/Cadastro, Parametrização de IA, Mineração de Edital/Flashcards, Resolução de Simulados, Avaliação OCR de Redações, Cronograma Dinâmico e Chat do Tutor).

---

### [SEÇÃO]: 4. Avaliação - Subseção 5.2.4.2: Avaliação de Usabilidade (Guia UniRV pág. 153-154)
- **[PROBLEMA]:** Falta a avaliação de usabilidade heurística baseada nas Heurísticas de Nielsen (Checklist Oficial FASOFT). O Capítulo 4 avaliou apenas código (testes unitários e lint), omitindo a tabela quantitativa percentual de conformidade preenchida pelo acadêmico e orientador.
- **[CORREÇÃO SUGERIDA]:** Inserir a subseção `4.2 AVALIAÇÃO DE USABILIDADE`, contendo o Quadro consolidado de respostas (Sim, Parcial, Não, Não se aplica) e a análise percentual qualitativa/quantitativa de UX.

---

### [SEÇÃO]: Estrutura Textual - Encerramento do Documento (Linha 2346)
- **[PROBLEMA]:** O documento não possui o capítulo de conclusão (item 5.2.5 das normas UniRV), transitando diretamente da análise estática para as referências bibliográficas.
- **[CORREÇÃO SUGERIDA]:** Incluir o capítulo `5. CONSIDERAÇÕES FINAIS` estruturado em três tópicos:
  1. *Cumprimento dos Objetivos:* Síntese de como o aplicativo solucionou a sobrecarga cognitiva do concurseiro por meio da IA generativa e repetição espaçada.
  2. *Dificuldades Encontradas:* Ponderação sobre a dependência de conectividade para caligrafia cursiva e custos da API Gemini.
  3. *Trabalhos Futuros:* Migração para Kotlin Multiplatform (KMP/iOS), adoção de modelos de linguagem locais on-device (Gemini Nano) e gamificação avançada com ranking de desempenho.
