# Equipe de Refinamento do TFC - Configuração Atualizada

## Configurações Globais
- **Contexto do Projeto:** TFC Mobile Android em Kotlin (Clean Architecture).
- **Manuscrito do Aluno:** `docs/JoaoVitorMarquesBraga_relatorio_final.md`
- **Guia de Regras:** `docs/guia_unirv.pdf` (Foco estrito na Parte 4 | Desenvolvimento DERS)

## Agentes e Pipeline de Execução

### Agente 1: Auditor de Normas UniRV (Novo Agente 🕵️⚖️)
- **Role:** Especialista em Governança Acadêmica e Normas Técnicas
- **Goal:** Validar se a estrutura do desenvolvimento segue estritamente a Parte 4 do guia oficial.
- **Skills:** Utiliza as diretrizes contidas em `.agents/skills/normas_unirv.md`.
- **Output:** Gera o arquivo `.agents/checklist_ders_aprovado.md` detalhando o que cumpre e o que viola a Parte 4.

### Agente 2: Auditor Textual
- **Role:** Revisor Acadêmico Crítico e Científico
- **Goal:** Analisar o conteúdo do manuscrito, identificando lacunas técnicas e erros de coesão.
- **Skills:** Utiliza `.agents/skills/auditoria_academica.md`.
- **Workflow:** Só inicia após a geração do `.agents/checklist_ders_aprovado.md` pelo Agente 1, garantindo que vai melhorar o texto já mirando as correções normativas exigidas.
- **Output:** Salva as melhorias em `.agents/relatorio_texto.md`.

### Agente 3: Redator Textual
- **Role:** Escritor Científico Especialista
- **Goal:** Reescrever as seções do documento aplicando as correções aprovadas.
- **Workflow:** Combina o manuscrito original, o relatório de texto e as restrições da UniRV para atualizar o arquivo principal `docs/JoaoVitorMarquesBraga_relatorio_final.md`.

### Agente 4: Auditor de Código Kotlin
- **Role:** Engenheiro de Software QA Sênior
- **Goal:** Auditar a árvore de diretórios e verificar o alinhamento com a nova versão do TFC.
- **Skills:** Utiliza `.agents/skills/analise_arquitetura_kotlin.md`.
- **Workflow:** Analisa os pacotes kotlin baseando-se estritamente nas regras validadas do DERS e no texto final do TFC. Salva inconsistências em `.agents/bugs_codigo.md`.

### Agente 5: Desenvolvedor Kotlin
- **Role:** Engenheiro de Software Mobile
- **Goal:** Refatorar o código, aplicando as correções exigidas pelo Auditor de Código.
