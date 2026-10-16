# Equipe de Refinamento do TFC - Configuração Atualizada

## Configurações Globais
- **Contexto do Projeto:** TFC Mobile Android nativo em Kotlin (Clean Architecture, MVVM, Jetpack Compose).
- **Manuscrito Oficial (DERS e Implementação):** `docs/JoaoVitorMarquesBraga_relatorio_DERS.md`
- **Manuscrito de Referência com Apontamentos:** `docs/JoaoVitorMarquesBraga_RelatorioDERS (analisado).md`
- **Guia de Regras da Instituição:** `docs/guia_unirv.pdf` (Foco estrito na Parte 4 | Desenvolvimento DERS)
- **Modelos Conceituais e Visuais:** `docs/entidade-relacionamento.mmd`, diagramas UML e capturas de tela dos testes

## Agentes e Pipeline de Execução

### Agente 1: Auditor de Normas UniRV
- **Role:** Especialista em Governança Acadêmica e Normas Técnicas
- **Goal:** Validar se a estrutura de `docs/JoaoVitorMarquesBraga_relatorio_DERS.md` atende às normas da Faculdade de Engenharia de Software da UniRV (Parte 4 do guia oficial).
- **Skills:** `.agents/skills/normas_unirv.md`.
- **Output:** Salva o checklist de conformidade em `.agents/checklist_ders_aprovado.md`.

### Agente 2: Auditor de Cobertura e Texto Acadêmico
- **Role:** Revisor Crítico de Requisitos e Conteúdo
- **Goal:** Confrontar `docs/JoaoVitorMarquesBraga_RelatorioDERS (analisado).md` contra o oficial `docs/JoaoVitorMarquesBraga_relatorio_DERS.md`, auditando a resolução das notas de revisão, sequenciamento de quadros/figuras e coerência metodológica.
- **Skills:** `.agents/skills/auditoria_academica.md`.
- **Output:** Salva o parecer detalhado e matriz de pendências em `.agents/relatorio_texto.md`.

### Agente 3: Redator Textual
- **Role:** Escritor Científico Especialista
- **Goal:** Ajustar `docs/JoaoVitorMarquesBraga_relatorio_DERS.md` aplicando as correções aprovadas no relatório de texto (corrigir numerações duplicadas, cabeçalhos do sumário e padronização dos fluxos).

### Agente 4: Auditor de Código Kotlin e Testes
- **Role:** Engenheiro de Software QA Sênior
- **Goal:** Cruzar os RFs (RF001 a RF014) e RNs (RN01 a RN09) do DERS oficial diretamente com os UseCases, Repositórios, DAOs Room e a suíte de 85 testes unitários na JVM demonstrada no capítulo 4.
- **Skills:** `.agents/skills/analise_arquitetura_kotlin.md`.
- **Output:** Salva as inconformidades técnicas e de cobertura em `.agents/bugs_codigo.md`.

### Agente 5: Desenvolvedor Kotlin
- **Role:** Engenheiro de Software Mobile Android
- **Goal:** Refatorar o código-fonte Kotlin e atualizar cenários de teste unitário baseando-se no relatório de bugs e divergências apontadas pelo Auditor de Código.