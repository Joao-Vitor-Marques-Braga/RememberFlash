# Skill: Auditoria de Texto Acadêmico e Cobertura DERS

## Objetivo
Analisar criticamente os arquivos do TFC em Markdown (`docs/JoaoVitorMarquesBraga_relatorio_DERS.md`), validando a conformidade científica, a coesão de requisitos e a resolução integral dos apontamentos realizados no documento revisado (`docs/JoaoVitorMarquesBraga_RelatorioDERS (analisado).md`).

## Instruções de Execução
1. **Auditoria de Cobertura (Revisado vs. Oficial):**
   - Cruzar os comentários da versão analisada (ex: unicidade de perfil, modelo BYOK, exclusão lógica, verificação de prazos e fluxo de telas) com a versão oficial.
   - Apontar qualquer discrepância pendente, ausência de fluxo alternativo ou inconsistência em relação às anotações da banca examinadora.

2. **Verificação Estrutural e de Numeração:**
   - Conferir se todas as Figuras, Quadros e Subseções possuem numeração sequencial estrita, eliminando itens duplicados (ex: duplicidade de Figuras 22 e 62, duplicidade do Quadro 29 e título da subseção 4.1.2).
   - Validar se os nomes das seções e títulos no Sumário batem rigorosamente com os títulos nas páginas de conteúdo.

3. **Consistência de Requisitos e Modelagem:**
   - Garantir que cada Requisito Funcional (RF001 a RF014) possua: Breve Descrição, Atores, Fluxo Básico, Quadro de Campos da Tela, Fluxos Alternativos (um para cada ação/exceção), Pré-condições, Pós-condições e Protótipo.
   - Validar a coerência entre as entidades descritas no DERS e as tabelas/relações dos arquivos de modelagem (`docs/entidade-relacionamento.mmd`).

4. **Padrão de Saída (.agents/relatorio_texto.md):**
   - **[SEÇÃO / ARTEFATO]**: Nome da Seção, Quadro ou Figura
   - **[STATUS DE COBERTURA]**: [COBERTO | PARCIAL | PENDENTE]
   - **[PROBLEMA DETECTADO]**: Descrição detalhada do erro ou inconsistência
   - **[CORREÇÃO SUGERIDA]**: Texto ou instrução de refatoração exata