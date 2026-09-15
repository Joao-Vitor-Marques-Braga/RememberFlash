Atualizar RF006, RF007 e RF008: Explicar que a geração de Flashcards e Questões pode ser atrelada tanto à Disciplina global quanto a um Tópico específico (topic_id).
2. "Tutor do Edital" (Chat de Dúvidas sobre o Certame)
Por que no documento: Formaliza a capacidade da IAG de ler anexos do edital e tirar dúvidas sobre regras da banca (critérios de correção da redação, recursos, pontuação mínima, etc.).
O que alterar nas Regras de Negócio e Requisitos:
Atualizar a RN07 em 

docs/01_regras_de_negocio.md
:
Texto atual: "A funcionalidade de Tutoria Interativa só poderá ser acionada a partir de uma questão já respondida ou de uma redação já avaliada..."
Novo texto:
RN07 – Gatilhos da Tutoria Interativa: A funcionalidade de "Tutoria Interativa" pode ser acionada a partir de três contextos específicos: (1) de uma questão já resolvida (para entender erros/acertos); (2) de uma redação avaliada (para esclarecer feedbacks gramaticais e de competências); ou (3) da pasta de um concurso com edital anexado ("Tutor do Edital"), utilizando o conteúdo minerado do PDF para esclarecer dúvidas sobre normas, critérios de pontuação da banca e logística do certame.

Atualizar o RF014 (Interagir Tutoria Interativa):
Adicionar no fluxo de eventos o Contexto 3: Dúvidas sobre o Edital, especificando que o sistema injeta o texto extraído do edital no prompt da IAG para responder dúvidas regimentais da prova.
3. Métricas de Tempo de Resposta por Questão
Por que no documento: O concurseiro vive sob a restrição do tempo de prova (ex: 3 a 4 minutos por questão). O cronômetro individual e o cálculo do tempo médio agregam valor pedagógico real.
O que alterar nos Requisitos:
Atualizar o RF010 (Visualizar Desempenho e Progresso):
Incluir no Quadro 27 e no texto do requisito o card de Tempo Médio de Resolução por Questão (geral e discriminado por disciplina).
Atualizar o RF008 / Ambiente de Resolução:
Registrar a presença do cronômetro progressivo durante a execução do simulado e a gravação do tempo gasto por item na tentativa (MockExamAttemptEntity).
4. Campos Adicionais de Logística de Prova
Por que no documento: Reduz a ansiedade do candidato no dia do certame, reunindo em um único card as orientações práticas exigidas no edital.
O que alterar nos Requisitos:
Atualizar o RF003 (Manter Concurso) – Quadro 20:
Incluir na tabela de campos da tela de criação/edição:
Cargo Pretendido (Texto)
Local da Prova (Texto - opcional)
Caneta Permitida (Ex: Preta com tubo transparente)
Itens Permitidos (Ex: Documento oficial, água em garrafa transparente)
Itens Proibidos (Ex: Relógio digital, aparelhos eletrônicos)
5. Algoritmo Autônomo de Recálculo de Cronograma Pós-Simulado
Por que no documento: Este é um dos pontos mais fortes para defender em banca de TCC: o aplicativo não é um repositório estático, mas um sistema adaptativo que detecta fraquezas do estudante e propõe correções de rota.
O que alterar nas Regras de Negócio e Requisitos:
Nova Regra de Negócio (RN09) em 

docs/01_regras_de_negocio.md
:
RN09 – Adaptação Heurística do Cronograma: Ao concluir um simulado, caso o rendimento do estudante em qualquer disciplina seja inferior a 70% de acertos, o sistema acionará um mecanismo inteligente que recalcula a ponderação de tempo diário, aplicando um multiplicador de reforço (1.3x a 2.0x) para as matérias deficitárias e gerando uma proposta comparativa para aprovação do usuário.

Atualizar o RF013 (Manter Cronograma Dinâmico):
Adicionar o fluxo alternativo A4 – Proposta Autônoma de Recalibragem por Rendimento Insuficiente, documentando a integração entre o encerramento do simulado (QuestionResolveContestViewModel) e o caso de uso ProposeScheduleRecalculationUseCase.