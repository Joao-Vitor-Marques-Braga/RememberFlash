# Relatório de Auditoria Textual, Diferencial e Coesão Acadêmica

**Documentos Auditados:**
- Oficial: `docs/JoaoVitorMarquesBraga_relatorio_DERS.md`
- Analisado (com notas de revisão da banca/orientador): `docs/JoaoVitorMarquesBraga_RelatorioDERS (analisado).md`

**Data da Auditoria:** 16 de outubro de 2026  
**Auditor:** Agente 2 - Auditor de Cobertura e Texto Acadêmico  
**Skill Aplicada:** `.agents/skills/auditoria_academica.md`  

---

## 1. Matriz Diferencial Estrita: Apontamentos de Revisão (Analisado vs. Oficial)

| ID / Item | Apontamento Original no Documento Analisado | Seção no Oficial | Status de Cobertura | Parecer do Agente 2 e Diagnóstico |
| :--- | :--- | :--- | :---: | :--- |
| **REV-01** | *Transferência de "Conhecimento do Esforço" e "Metas do Projeto" (Linha 206: "esses dois itens, deve ser transferidos para a INTRODUÇÃO")* | Seção 1 (Linhas 346–349) | **[COBERTO]** | O texto de contextualização do problema e metas foi devidamente integrado aos parágrafos finais da Introdução, alinhando a Seção 2 diretamente ao modelo Volere/UniRV. |
| **REV-02** | *Unicidade de CPF e perfil único (Comentário sobre duplicidade de contas e integridade das métricas)* | Seção 2.4.2 (RN01) e RF001 (Linha 409) | **[COBERTO]** | A regra RN01 foi formalizada: cada usuário vincula-se a um único CPF com validação matemática dos dígitos verificadores (Módulo 11) e checagem de unicidade. |
| **REV-03** | *Regras de Negócio Globais (RN01 a RN09)* | Seção 2.4.2 (Linhas 408–421) | **[COBERTO]** | Todas as 9 regras de negócio mandatórias foram descritas textualmente no item 2.4.2: RN01 (CPF único), RN02 (Autenticação obrigatória), RN03 (Isolamento de dados), RN04 (Prompt travado por perfis), RN05 (Data de prova futura), RN06 (Exclusão lógica de pastas), RN07 (Tutoria com contexto restrito e stateless), RN08 (Modelo BYOK), RN09 (Recalibragem para matérias < 70%). |
| **REV-04** | *Modelo BYOK - "Bring Your Own Key" (Eliminação de chave global hardcoded e descentralização de custos)* | Seção 2.2.1, 2.3.1, 2.4.2 (RN08), 2.5.1 | **[COBERTO]** | Documentado como restrição mandatória de solução, convenção terminológica, RN08 e justificativa na estimativa de custos operacionais (Quadro 1). |
| **REV-05** | *Exclusão Lógica e Arquivamento (Preservação de histórico educacional)* | Seção 2.4.2 (RN06) e RF003 | **[COBERTO]** | Formalizado no DERS que a exclusão de pasta realiza soft delete (`isActive = false`), preservando o histórico de resolução de questões para consulta e reativação. |
| **REV-06** | *Validação de Prazos e Datas (Data da prova no cronograma)* | Seção 2.4.2 (RN05) e RF013 | **[COBERTO]** | Restrição de data estritamente futura incorporada como RN05, com bloqueio no fluxo básico e alternativo de validação de calendário. |
| **REV-07** | *Fluxos de Telas: Listagem Inicial Obrigatória (Linha 1033 e 2129: "obrigatoriamente, deve iniciar com a tela de listagem...")* | RF003 (Concursos) e RF013 (Cronograma) | **[COBERTO]** | Ajustado nos fluxos de caso de uso para iniciar na visualização de listagem das entidades antes do acionamento de formulários. |
| **REV-08** | *Pureza dos Requisitos (Linha 1527: "Documentos de especificação de Requisitos, na sua pureza, não devem e nem podem, trazer soluções tecnológicas")* | Seção 2.7 (RF001 a RF014) | **[PARCIAL]** | Foram removidas menções a bibliotecas específicas nos títulos dos RFs, mas ainda persistem termos de solução (como chamadas diretas a formatos de JSON e nomes de SDKs) dispersos dentro das descrições dos fluxos operacionais. |
| **REV-09** | *Fluxos Alternativos para Toda Ação/Exceção (Linha 777: "toda a ação, deve ter um fluxo alternativo")* | Seção 2.7 (Quadros 1 a 14) | **[COBERTO]** | Todos os RFs contam com fluxos alternativos mapeando cancelamento, campos vazios, falhas de conectividade e validação de formatos. |
| **REV-10** | *Status do Projeto ("Já está pronto? / O projeto já está pronto?")* | Breve Descrição e Introdução | **[PARCIAL]** | O texto foi ajustado para tempo presente e pretérito conforme o estágio de desenvolvimento concluído, mas requer harmonização com o Capítulo 5 (Considerações Finais). |

---

## 2. Incoerências Internas de Numeração e Estrutura Identificadas

### 2.1. Duplicidade da Figura 22
- **Localização:** Lista de Figuras (Linhas 88 e 90) e corpo do texto (Linhas 1836 e 1923).
- **Inconsistência:**
  - Linha 88: `FIGURA 22 - Tela de tutor`
  - Linha 90: `FIGURA 22 - Diagrama de caso de uso`
- **Diagnóstico:** Status **[PENDENTE]**. A numeração da segunda Figura 22 (Diagrama de caso de uso) deve ser alterada para `FIGURA 23`, com renumeração progressiva em cascata de todas as 41 figuras subsequentes até a Figura 64.

### 2.2. Duplicidade da Figura 62
- **Localização:** Linhas 2334 e 2341.
- **Inconsistência:**
  - Linha 2334: `FIGURA 62 - Análise Estática – Conversão de URI de PDF...`
  - Linha 2341: `FIGURA 62 - Análise Estática – Conversão de URI...` (porém a imagem exibe a resolução de parâmetro não utilizado na classe `GenerateQuestionsUseCase`).
- **Diagnóstico:** Status **[PENDENTE]**. A legenda da linha 2341 deve ser corrigida para `FIGURA 63 - Análise Estática – Tratamento de Parâmetro Não Utilizado no GenerateQuestionsUseCase (Resolvido)`.

### 2.3. Duplicidade do Quadro 29
- **Localização:** Lista de Quadros (Linhas 232 e 236) e corpo do texto (Linhas 1669 e 1845).
- **Inconsistência:**
  - Linha 232 / 1669: `Quadro 29 - Detalhamento de Campos (Tela de Histórico e Detalhes)` (pertencente ao RF012/RF013).
  - Linha 236 / 1845: `Quadro 29 - Detalhamento de Campos (Interface de Tutoria Interativa)` (pertencente ao RF014).
- **Diagnóstico:** Status **[PENDENTE]**. O quadro da Interface de Tutoria Interativa deve ser renumerado para `Quadro 31`, ajustando o sumário de quadros.

### 2.4. Duplicidade de Título da Subseção 4.1.2
- **Localização:** Linhas 330 (Sumário), 2064 e 2308 (Corpo).
- **Inconsistência:** O item 4.1.1 intitula-se `4.1.1 Testes unitários` e a linha 2308 repete exatamente `### 4.1.2 Testes unitários`, mas seu conteúdo trata exclusivamente da execução de Análise Estática via Android Lint.
- **Diagnóstico:** Status **[PENDENTE]**. A subseção 4.1.2 deve ser retificada para:  
  `### 4.1.2 Análise Estática de Código (Android Lint)`.

### 2.5. Redundância em Legendas de Figuras de Testes
- **Localização:** Linhas 110, 126, 134 e correspondentes no corpo.
- **Inconsistência:** Repetição inadvertida do termo: `FIGURA 32 - Teste Unitário – Teste Unitário – Parametrização...` e `FIGURA 40 - Teste Unitário – Teste Unitário – Criação Manual...`.
- **Diagnóstico:** Status **[PENDENTE]**. Higienização textual mantendo ocorrência simples.

### 2.6. Presença de Tags Markdown Órfãs
- **Localização:** Linhas 511, 533, 535, 547, 549 e ao longo de todo o item 2.7.
- **Inconsistência:** Linhas isoladas contendo apenas `##### ` sem nenhum conteúdo textual.
- **Diagnóstico:** Status **[PENDENTE]**. Higienização por exclusão para evitar quebra de hierarquia em renderizadores PDF/HTML.

---

## 3. Coesão Textual: Objetivos Específicos vs. Resultados

| # | Objetivo Específico | Seção de Resultados no TFC | Parecer de Coesão | Ação Textual Necessária |
| :-: | :--- | :--- | :---: | :--- |
| **OE1** | Interface gráfica responsiva e minimalista, redução da carga cognitiva. | Capítulo 4 (Avaliação) | **[PARCIAL]** | O manuscrito apresenta testes de casos de uso na JVM, mas não possui avaliação empírica de usabilidade/UX (Nielsen/FASOFT) no Capítulo 4. |
| **OE2** | Extração de PDF e mineração de flashcards via LLM / SM-2 offline. | Seção 4.1.1 (Figuras 40 a 43) | **[COESO]** | Comprovado por testes unitários da lógica SM-2 e geração sintética de baralhos. |
| **OE3** | Criação balanceada de simulados por banca e medição de desempenho. | Seção 4.1.1 (Figuras 35 a 39) | **[COESO]** | Comprovado pelos testes de montagem de cadernos e cronômetro de prova. |
| **OE4** | Motor de IAG com templates rígidos e salvaguardas anti-alucinação. | Seção 4.1.1 (Figuras 32 e 48) | **[COESO]** | Comprovado pelo bloqueio de prompt livre e injeção contextual de diretrizes pedagógicas. |
| **OE5** | OCR on-device com ML Kit a custo zero de nuvem para manuscritos. | Seção 4.1.1 (Figuras 46 a 49) | **[CONTRADITÓRIO]** | A Introdução promete OCR on-device a custo zero para manuscritos, mas os Resultados provam que texto cursivo depende de IA multimodal em nuvem (Gemini). O OE5 na Introdução deve ser retificado para arquitetura híbrida. |
| **OE6** | Cronograma dinâmico e recalibragem para disciplinas < 70%. | Seção 4.1.1 (Figuras 44, 45, 50, 51) | **[COESO]** | Comprovado pela aplicação do multiplicador adaptativo (1.3x a 2.0x) e validação de data futura. |

---

## 4. Requisitos Consolidados para Auditoria Técnica no Código (Convocação do Agente 4)

Com base nas especificações aprovadas no DERS oficial, os seguintes requisitos funcionais e regras mandatórias devem ser imediatamente auditados no código-fonte Kotlin e testes pelo **Agente 4**:

1. **RF001 / RF002 e RN01 (Identidade e Unicidade):**
   - Validar se `RegisterUseCase` e os testes unitários aplicam o algoritmo Módulo 11 oficial para CPF e realizam a verificação de unicidade no banco de dados Room.
   - Validar custódia da sessão e token de autenticação.

2. **RF003 e RN06 (Ciclo de Vida de Concursos e Exclusão Lógica):**
   - Auditar se `SoftDeleteContestUseCase` e as chamadas no ViewModel respeitam estritamente a exclusão lógica (`isActive = false`) e se não há invocação acidental de exclusão física (`DELETE`) na camada de apresentação.
   - Verificar limite de tamanho de edital (15MB).

3. **RF005 e RN04 (Parametrização e Guardrails da IAG):**
   - Conferir se os perfis de IA (dificuldade, rigor, tom) são fechados e custodiados em `SecurePreferencesManager`.

4. **RF008 / RF009 / RF010 (Simulados, Questões e Métricas):**
   - Checar montagem de cadernos proporcionais por disciplina e cálculo de assertividade.

5. **RF011 / RF012 e OE5 (Esteira Híbrida de OCR e Avaliação):**
   - Auditar o fallback: transcrição multimodal com Gemini para caligrafia manuscrita e ML Kit local on-device para texto tipografado.

6. **RF013 e RN05 / RN09 (Cronograma Dinâmico e Recalibragem Adaptativa):**
   - Validar restrição de data de prova futura no `GenerateStudyScheduleUseCase`.
   - Validar no `ProposeScheduleRecalculationUseCase` o gatilho de desempenho inferior a 70% com aplicação do fator multiplicador adaptativo.

7. **RF014 e RN07 / RN08 (Tutoria Contextual e Modelo BYOK):**
   - Auditar se a tutoria é estritamente vinculada à entidade ativa (questão, redação ou edital) em memória volátil (*stateless*, RNF06).
   - Validar custódia de chave pessoal da API Gemini (BYOK) em `EncryptedSharedPreferences` (RNF05).
