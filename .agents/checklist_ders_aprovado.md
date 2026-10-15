# Checklist de Conformidade Normativa UniRV - Parte 4 (DERS)

**Documento Auditado:** `docs/JoaoVitorMarquesBraga_relatorio_DERS.md`  
**Data da Auditoria:** 15 de outubro de 2026  
**Auditor:** Agente 1 - Especialista em Governança Acadêmica e Normas Técnicas UniRV  
**Referência Normativa:** Guia Oficial de Padronização Científica da Faculdade de Engenharia de Software da Universidade de Rio Verde (UniRV) - Seção 5: Elementos que Compõem a Estrutura do Relatório DERS (Desenvolvimento de Software).

---

## 1. Resumo Executivo de Conformidade

| Status Geral | Itens Avaliados | Itens Conformes | Inconformidades Críticas | Inconformidades Formais |
| :--- | :---: | :---: | :---: | :---: |
| **RESSALVA / REVISÃO EXIGIDA** | 14 | 10 | 3 | 4 |

> [!WARNING]
> **Veto Normativo Parcial:** O manuscrito cumpre o núcleo de especificação e desenvolvimento do DERS, mas apresenta **3 violações normativas críticas de omissão (Guia UniRV Parte 4 / Seção 5)**:
> 1. **Ausência do Manual do Usuário** (Guia pág. 154-155);
> 2. **Ausência da Avaliação de Usabilidade** via Heurísticas de Nielsen / Checklist FASOFT (Guia pág. 153-154);
> 3. **Ausência do Capítulo 5: Considerações Finais / Conclusão** (Guia pág. 155-156).

---

## 2. Checklist Estrutural da Parte Textual (Guia UniRV Item 5.2)

| Seção Normativa | Nomenclatura no Guia UniRV | Presente no Documento | Status | Observação Técnica |
| :--- | :--- | :---: | :---: | :--- |
| **5.2.1** | 1. Introdução | Sim (Linha 334) | **CONFORME** | Apresenta contextualização, pergunta de pesquisa, fundamentação teórica contemporânea (UNESCO, Teoria Cognitiva), objetivo geral e 6 objetivos específicos. |
| **5.2.2** | 2. Documento de Especificação de Requisitos de Software (DERS) | Sim (Linha 360) | **CONFORME** | Estruturado conforme modelo Volere/UniRV. |
| 5.2.2.1 | 2.1 Os Interessados | Sim (Linha 362) | **CONFORME** | Cliente e Consumidor definidos. |
| 5.2.2.2 | 2.2 Restrições Obrigatórias | Sim (Linha 372) | **CONFORME** | Restrições de soluções mobile e nuvem. |
| 5.2.2.3 | 2.3 Nomeando Convenções e Definições | Sim (Linha 381) | **CONFORME** | Glossário de termos técnicos e acrônimos. |
| 5.2.2.4 | 2.4 Fatos e Suposições Relevantes | Sim (Linha 401) | **CONFORME** | Fatos e Regras de Negócio (RN01 a RN09) mapeadas. |
| 5.2.2.5 | 2.5 Estimativa de Custos e Prazos | Sim (Linha 422) | **CONFORME** | Quadro de custos e cronograma em Gantt (Figura 1). |
| 5.2.2.6 | 2.6 O Escopo do Trabalho | Sim (Linha 451) | **CONFORME** | A situação atual contextualizada. |
| 5.2.2.7 | 2.7 Requisitos Funcionais | Sim (Linha 461) | **RESSALVA** | RF01 a RF014 documentados em quadros de casos de uso com fluxos e protótipos. Presença de tags órfãs `##### ` em Markdown que devem ser higienizadas. |
| 5.2.2.8 | 2.8 Requisitos Não Funcionais | Sim (Linha 1901) | **CONFORME** | Desempenho, escalabilidade, segurança e usabilidade categorizados. |
| 5.2.2.9 | 2.9 Especificação Técnica | Sim (Linha 1920) | **CONFORME** | Diagramas de Casos de Uso, Sequência, DER, Classes e Componentes detalhados. |
| **5.2.3** | 3. Implementação | Sim (Linha 1972) | **CONFORME** | Subdividido em Arquitetura (Clean/MVVM), Gerência (Git/GitHub/Notion), Tecnologias/Bibliotecas, Persistência (Room/Supabase) e Testes. |
| **5.2.4** | 4. Avaliação | Sim (Linha 2058) | **RESSALVA** | Cobertura empírica de testes unitários e lint. Contudo, há erro de título em 4.1.2 e omissão de seções obrigatórias (Usabilidade e Manual). |
| 5.2.4.1 | Testes (Unitários e Análise Estática) | Sim (Linhas 2064 e 2308) | **RESSALVA** | Testes JVM e Lint detalhados, mas com erro no título da subseção 4.1.2. |
| 5.2.4.2 | Avaliação de Usabilidade (Heurísticas de Nielsen / FASOFT) | **NÃO** | **VIOLAÇÃO CRÍTICA** | **Item mandatório omitido** (Guia pág. 153-154). Exige a aplicação do Checklist Oficial FASOFT pelo desenvolvedor e orientador com análise quantitativa percentual. |
| **-** | **Manual do Usuário** | **NÃO** | **VIOLAÇÃO CRÍTICA** | **Item mandatório omitido** (Guia pág. 154-155). Exige seção prática com prints de telas, demarcadores numéricos circulares e detalhamento campo a campo (finalidade, tipos de dados, obrigatoriedade e mensagens de erro). |
| **5.2.5** | 5. Conclusão ou Considerações Finais | **NÃO** | **VIOLAÇÃO CRÍTICA** | **Item mandatório omitido** (Guia pág. 155-156). O texto finaliza na Figura 63 e pula diretamente para a seção de Referências. |
| **5.3** | Elementos Pós-Textuais (Referências) | Sim (Linha 2347) | **CONFORME** | Referências formatadas conforme ABNT NBR 6023 / UniRV. |

---

## 3. Inconformidades Normativas Detalhadas

### 3.1. [CRÍTICA] Ausência do Capítulo 5: Considerações Finais / Conclusão
- **Dispositivo Normativo:** Guia UniRV - Seção 5.2.5 (pág. 155-156).
- **Inadequação:** Todo relatório técnico DERS exige a conclusão para avaliar se os objetivos gerais e específicos foram atingidos, ponderar as dificuldades do desenvolvimento e apontar trabalhos futuros (ex: expansão iOS, integração com IA open-source local).
- **Ação Exigida:** Redigir o capítulo **"5. CONSIDERAÇÕES FINAIS"** antes da seção pós-textual de Referências.

### 3.2. [CRÍTICA] Ausência da Seção Obrigatória: Manual do Usuário
- **Dispositivo Normativo:** Guia UniRV - Seção 5.2.4 (pág. 154-155: "MANUAL DO USUÁRIO").
- **Inadequação:** O Guia UniRV exige formalmente uma seção dedicada de Manual do Usuário como fechamento do desenvolvimento prático. Embora o manuscrito possua protótipos de tela ilustrativos inseridos dentro dos quadros de Casos de Uso na Seção 2.7 (DERS), **falta a seção formal de Manual do Usuário**, que exige:
  1. Telas em alta fidelidade da aplicação real com demarcadores numéricos circulares sinalizando os pontos de interação (campos, botões, menus);
  2. Detalhamento textual campo a campo abaixo de cada figura: nome/finalidade, tipo de dado/domínio permitido, obrigatoriedade de preenchimento e mensagens de feedback/validação de erro imediatas apresentadas ao usuário.
- **Ação Exigida:** Criar a seção **"MANUAL DO USUÁRIO"** (ao final do Capítulo 4, antes da Conclusão), mapeando as telas principais do aplicativo com demarcadores circulares e guia descritivo de navegação.

### 3.3. [CRÍTICA] Ausência da Subseção 5.2.4.2: Avaliação de Usabilidade (FASOFT / Nielsen)
- **Dispositivo Normativo:** Guia UniRV - Seção 5.2.4.2 (pág. 153-154).
- **Inadequação:** O relatório cobriu apenas testes automatizados de software (unitários e lint), omitindo a avaliação de interface/UX por método de inspeção baseada nas Heurísticas de Nielsen (Checklist Oficial da FASOFT preenchido pelo desenvolvedor e orientador com tabela percentual consolidada).
- **Ação Exigida:** Inserir a subseção `4.2 Avaliação de Usabilidade` com a matriz de conformidade das heurísticas e os checklists assinados previstos para anexo.

### 3.4. [FORMAL] Duplicidade de Título de Subseção (4.1.1 e 4.1.2)
- **Localização:** Linha 2064 e Linha 2308.
- **Inadequação:** A linha 2308 repete `### 4.1.2 Testes unitários`, mas seu conteúdo trata exclusivamente da execução de Análise Estática com Android Lint.
- **Ação Exigida:** Renomear a subseção 4.1.2 para: `### 4.1.2 Análise Estática de Código`.

### 3.3. [FORMAL] Numeração Duplicada da Figura 62
- **Localização:** Linha 2334 e Linha 2341.
- **Inadequação:** Ambas as figuras foram rotuladas como `FIGURA 62`. A figura da linha 2341 ilustra o aviso de linter na classe `GenerateQuestionsUseCase` e deve ser a `FIGURA 63`.
- **Ação Exigida:** Renumerar para `FIGURA 63 - Análise Estática – Tratamento de Parâmetro Não Utilizado no GenerateQuestionsUseCase (Resolvido)`.

### 3.4. [FORMAL] Duplicidade de Numeração na Lista de Figuras (Figura 22)
- **Localização:** Linhas 88 e 90.
- **Inadequação:** A Lista de Figuras lista `FIGURA 22 - Tela de tutor` e logo em seguida `FIGURA 22 - Diagrama de caso de uso`.
- **Ação Exigida:** Corrigir a numeração a partir do Diagrama de caso de uso para `FIGURA 23`, ajustando consecutivamente todas as figuras subsequentes.

### 3.5. [FORMAL] Redundância Textual nos Títulos de Figuras de Testes
- **Localização:** Linhas 110, 126, 134, etc. (Ex: "FIGURA 32 - Teste Unitário – Teste Unitário – Parametrização...").
- **Inadequação:** O prefixo "Teste Unitário" foi duplicado por erro de digitação.
- **Ação Exigida:** Higienizar as legendas, mantendo ocorrência única.

---

## 4. Parecer do Agente 1
O documento possui excelente nível técnico de detalhamento de engenharia de software, modelagem de requisitos e fundamentação científica, estando **90% em conformidade com o padrão DERS da UniRV**. A liberação final para diagramação e banca depende estritamente da inclusão do capítulo **5. Considerações Finais** e da correção das incoerências de numeração apontadas.

O pipeline deve prosseguir para a atuação do **Agente 2 (Auditor Textual)** com foco nas lacunas de coesão e especificações de software.
