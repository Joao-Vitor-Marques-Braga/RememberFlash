# **UNIVERSIDADE DE RIO VERDE (UniRV) FACULDADE DE ENGENHARIA DE SOFTWARE** 

**JOÃO VITOR MARQUES BRAGA ORIENTADOR: PROF. ESP. GABRIEL GARCIA PAVAN** 

# **REMEMBERFLASH: DESENVOLVIMENTO DE APLICATIVO MÓVEL ANDROID PARA AUXÍLIO EDUCACIONAL NO CONTEXTO DE CONCURSOS VIA INTELIGÊNCIA ARTIFICIAL GENERATIVA** 

**RIO VERDE - GO** 

**2026** 

**Ficha Catalográfica** 



**Bibliotecária responsável:** 

# **JOÃO VITOR MARQUES BRAGA** 

**REMEMBERFLASH: DESENVOLVIMENTO DE APLICATIVO MÓVEL ANDROID PARA AUXÍLIO EDUCACIONAL NO CONTEXTO DE CONCURSOS VIA INTELIGÊNCIA ARTIFICIAL GENERATIVA** 

Relatório de desenvolvimento apresentado à Banca Examinadora do Curso de Engenharia de Software da Universidade de Rio Verde (UniRV) como exigência parcial para a obtenção do título de Bacharel. 

Rio Verde, GO, 26 de maio de 2026. 

# **BANCA EXAMINADORA** 

Prof. Esp. Gabriel Garcia Pavan Universidade de Rio Verde (UniRV) 

Prof. Me. Emanuel Victor de Moura Oliveira Barros 

Universidade de Rio Verde (UniRV) 

Prof. Esp. João Dionísio Paraíba 

Universidade de Rio Verde (UniRV) 

# negrito 

# AGRADECIMENTOS 

Agradeço à minha família por todo o apoio prestado durante a minha jornada acadêmica. Aos amigos mais próximos, que contribuíram indiretamente para este trabalho por meio de ricas trocas de ideias. Por fim, aos professores da Faculdade de Engenharia de Software, essenciais na transmissão do conhecimento que tornou possível o desenvolvimento deste projeto. 

# **BREVE DESCRIÇÃO DO SISTEMA** 

Já está pronto? 

O projeto traz o desenvolvimento de um aplicativo móvel nativo Android, fundamentado na personalização e otimização do aprendizado para candidatos a concursos públicos, utilizando a Inteligência Artificial Generativa. O sistema permite a geração automatizada de simulados e flashcards a partir da extração de dados de editais em PDF, a avaliação de redações manuscritas via Reconhecimento Óptico de Caracteres (OCR) com feedback formativo, além da criação de cronogramas dinâmicos e interação com um tutor virtual. A principal inspiração vem da sobrecarga cognitiva e da exaustão mental enfrentadas pelos estudantes ao utilizarem ferramentas preparatórias tradicionais, passivas e dispersas durante um longo período. Portanto, o objetivo deste sistema é mitigar essa fadiga atuando como um mediador inteligente e unificado, com a dinâmica de adaptar os estudos às reais necessidades e tempo do candidato. 

Palavras-chave: Aplicativo Android; Concursos públicos; Inteligência Artificial Generativa; Reconhecimento Óptico de Caracteres. 

espaçamento entre paragráfo 

# **LISTA DE FIGURAS** 

|FIGURA 1 - Cronograma de atividades planejadas..................................................................18|
|---|
|FIGURA 2 - Tela de Login....................................................................................................... 28|
|FIGURA 3 - Tela de Cadastro...................................................................................................29|
|FIGURA 4 - Tela de Recuperação de Senha (Email de recuperação)...................................... 33|
|FIGURA 5 - Tela de Recuperação de Senha (Código de Verificação)..................................... 34|
|FIGURA 6 -  Tela de menu.......................................................................................................38|
|FIGURA 7 - Tela de cadastro de concurso............................................................................... 39|
|FIGURA 8 - Tela do menu de concurso....................................................................................42|
|FIGURA 9 - Tela de cadastro de disciplina.............................................................................. 43|
|FIGURA 10 - Tela de perfil...................................................................................................... 48|
|FIGURA 11 - Tela de Configurações da IAG...........................................................................49|
|FIGURA 12 - Tela para criar flashcards via PDF.....................................................................53|
|FIGURA 13 - Tela para criar novo flashcard manual...............................................................54|
|FIGURA 14 - Tela de geração de questões...............................................................................58|
|FIGURA 15 - Tela de geração de prova via IAG......................................................................62|
|FIGURA 16 - Tela de resolução de prova.................................................................................63|
|FIGURA 17 - Tela de desempenho...........................................................................................67|
|FIGURA 18 - Tela de captura OCR..........................................................................................71|
|FIGURA 19 - Tela de avaliação de redação..............................................................................72|
|FIGURA 20 - Tela de histórico de redações.............................................................................75|
|FIGURA 21 - Tela de geração de cronograma..........................................................................79|
|FIGURA 22 - Tela de tutor....................................................................................................... 83|
|FIGURA 22 - Diagrama de caso de uso................................................................................... 85|
|FIGURA 23 - Diagrama de sequência (autenticação).............................................................. 86|
|FIGURA 24 - Diagrama de sequência (geração de simulados)................................................86|
|FIGURA 25 - Diagrama de sequência (avaliação de redação via OCR)..................................87|
|FIGURA 26 - Diagrama de sequência (tutor interativo)...........................................................87|



FIGURA 27 - Diagrama de Entidade-Relacionamento............................................................ 88 FIGURA 28 - Diagrama de classes...........................................................................................89 FIGURA 29 - Diagrama de componentes.................................................................................90 

# **LISTA DE QUADROS** 

|Quadro 1 RF01 - Manter Usuário............................................................................................. 20|
|---|
|Quadro 2 RF02 - Alterar Senha................................................................................................ 20|
|Quadro 3 RF03 - Manter Concurso...........................................................................................20|
|Quadro 4 RF04 - Manter Disciplina......................................................................................... 21|
|Quadro 5 RF05 - Parametrizar Inteligência Artificial Generativa............................................21|
|Quadro 6 RF06 - Manter Flashcard.......................................................................................... 21|
|Quadro 7 RF07 - Manter Flashcard via Documento PDF........................................................ 21|
|Quadro 8 RF08 - Manter Questões via Inteligência Artificial Generativa............................... 22|
|Quadro 9 RF09 - Manter Prova via Edital (PDF)..................................................................... 22|
|Quadro 10 RF10 - Visualizar Desempenho e Progresso...........................................................23|
|Quadro 11 RF11 - Avaliar Redação via OCR........................................................................... 23|
|Quadro 12 RF12 - Consultar Histórico de Redações................................................................23|
|Quadro 13 RF13 - Manter Cronograma Dinâmico................................................................... 24|
|Quadro 14 RF14 - Interagir Tutoria Interativa..........................................................................24|
|Quadro 15 - Detalhamento de Campos (Tela de Login)........................................................... 25|
|Quadro 16 - Detalhamento de Campos (Tela de Cadastro).......................................................26|
|Quadro 17 - Detalhamento de Campos (Tela de Solicitação de Recuperação).........................30|
|Quadro 17 - Detalhamento de Campos (Tela de Redefinição de Senha)..................................31|
|Quadro 18 - Detalhamento de Campos (Tela de Criação de Concurso)................................... 35|
|Quadro 19 - Detalhamento de Campos (Modal de Criação/Edição de Disciplina).................. 40|
|Quadro 20 - Detalhamento de Campos (Tela de Parametrização da IA).................................. 44|
|Quadro 21 - Detalhamento de Campos (Tela de Criação/Edição de Flashcard).......................46|
|Quadro 22 - Detalhamento de Campos (Tela de Geração via IA)............................................ 50|
|Quadro 23 - Detalhamento de Campos (Tela de Geração de Questões)...................................55|
|Quadro 24 - Detalhamento de Campos (Tela de Geração de Prova)........................................ 59|
|Quadro 25 - Detalhamento de Filtros e Elementos Visuais (Painel de Desempenho)..............64|
|Quadro 26 - Detalhamento de Campos (Tela de Revisão e Submissão)...................................69|



Quadro 27 - Detalhamento de Campos (Tela de Histórico e Detalhes).................................... 73 Quadro 28 - Detalhamento de Campos (Tela de Parametrização do Cronograma).................. 76 Quadro 29 - Detalhamento de Campos (Interface de Tutoria Interativa)................................. 80 verificar a norma 

# **SUMÁRIO** 

|**1. INTRODUÇÃO...................................................................................................................12**|
|---|
|**2. DERS....................................................................................................................................13**|
|2.1. O PROPÓSITO DO PROJETO......................................................................................... 13|
|2.1.1 Conhecimento do Esforço do Projeto.............................................................................. 13|
|2.1.2 Metas do Projeto.............................................................................................................. 14|
|2.2. OS INTERESSADOS........................................................................................................14|
|2.2.1 O Cliente.......................................................................................................................... 14|
|2.2.2 O CONSUMIDOR...........................................................................................................14|
|2.3. RESTRIÇÕES OBRIGATÓRIAS..................................................................................... 14|
|2.3.1 Restrições de Soluções.....................................................................................................14|
|2.4. NOMEANDO CONVENÇÕES E DEFINIÇÕES.............................................................14|
|2.4.1 Definições de todos os termos, incluindo acrônimos, usados no projeto........................ 14|
|2.5. FATOS E SUPOSIÇÕES RELEVANTES......................................................................... 16|
|2.5.1 Fatos Relevantes.............................................................................................................. 16|
|2.4.2 Regras do Negócio...........................................................................................................16|
|2.6. ESTIMATIVA DE CUSTOS E PRAZOS PARA A ELABORAÇÃO.............................. 17|
|2.6.1 Estimativa de Custos para Elaboração.............................................................................17|
|2.6.2. Estimativa de prazo para elaboração...............................................................................17|
|2.6.3. Cronograma de atividades planejadas.............................................................................18|
|2.7. O ESCOPO DO TRABALHO...........................................................................................19|
|2.7.1 A situação atual................................................................................................................19|
|2.8. REQUISITOS FUNCIONAIS........................................................................................... 19|
|2.8.1 Prioridade de Requisito....................................................................................................19|
|2.8.2. Descrição dos Requisitos Funcionais..............................................................................20|
|2.8.3 Especificação dos Requisitos Funcionais........................................................................ 24|
|2.9. REQUISITOS NÃO FUNCIONAIS..................................................................................83|
|2.10. ESPECIFICAÇÃO TÉCNICA........................................................................................ 85|



|2.10.1 Caso de Uso................................................................................................................... 85|
|---|
|2.9.2 Sequência do Produto.......................................................................................................86|
|2.9.3 Diagrama de Entidade Relacionamento...........................................................................88|
|2.9.4 Diagrama de Classe..........................................................................................................89|
|2.9.5 Diagrama de Componentes.............................................................................................. 90|
|**REFERÊNCIAS......................................................................................................................91**|



12 

# **1. INTRODUÇÃO** 

O avanço recente da Inteligência Artificial Generativa, impulsionado pelo surgimento de Grandes Modelos de Linguagem (LLMs), tem transformado profundamente diversos setores, com destaque para o cenário educacional. Segundo a UNESCO (2024), a integração da inteligência artificial na educação tem o potencial de inovar as práticas de ensino, atuando como um verdadeiro "treinador 1:1" capaz de promover a aquisição de habilidades e a aprendizagem autodirigida. Nesse contexto, ferramentas tecnológicas deixam de ser apenas acessórios para se tornarem vetores essenciais de personalização e eficiência no aprendizado. 

Apesar desse potencial tecnológico, estudantes em preparação para exames e para todos? concursos públicos ainda enfrentam barreiras significativas. Devido à hiperconectividade atual, o acesso à informação deixou de ser o principal obstáculo, dando lugar a um severo desafio de curadoria. A vasta gama de conteúdos dispersos e desestruturados gera uma alta sobrecarga cognitiva, exigindo que o aluno invista horas preciosas apenas selecionando o que estudar e em qual ordem, tempo que poderia ser revertido em estudo efetivo (ALSHAIKH et al., 2024; KERN, 2024). Além disso, as soluções mercadológicas tradicionais frequentemente cobram valores que extrapolam a realidade financeira dos estudantes, oferecendo verificar citação metodologias estáticas e genéricas baseadas em modelos massificados (NIERO, 2022). final do paragráfo Diante desse cenário de excesso de informação irrelevante e da elitização dos materiais preparatórios, evidencia-se a urgência de soluções tecnológicas adaptativas. A aplicação de LLMs permite romper o modelo estático dos cursos tradicionais, filtrando o conteúdo para gerar trilhas de estudo personalizadas e _feedbacks_ imediatos, transformando o dispositivo móvel em um tutor inteligente focado exclusivamente nas reais necessidades do estudante (DAMAŠEVIČIUS, 2024; THORNE, 2024; XU et al., 2024). 

Como provar ?? 

temos uma incoerencia 

Este Trabalho Final de Curso tem como objetivo geral desenvolver um aplicativo móvel nativo Android, programado em linguagem Kotlin, voltado para a personalização e otimização do aprendizado de concurseiros. Integrado à API do Google Gemini, o sistema propõe uma solução prática onde a IA atua como facilitadora cognitiva, oferecendo a geração automatizada de simulados e flashcards a partir da extração de dados de editais em PDF, a avaliação de redações manuscritas via Reconhecimento Óptico de Caracteres (OCR) com _feedback_ formativo, e a criação de cronogramas dinâmicos de estudo. 

o projeto já está pronto? 

Para alcançar esse propósito, foram definidos os seguintes objetivos específicos: projetar uma interface de usuário responsiva e minimalista para reduzir a carga cognitiva; estruturar a persistência de dados local do aplicativo; integrar o motor cognitivo da IA 

não localizado a hipótese e justificativa não tem trabalhos relacionados 

13 

não determinou o problema 

Generativa utilizando templates de prompt rígidos para evitar alucinações e garantir a segurança do conteúdo; implementar o módulo de OCR para digitalização de textos; e desenvolver algoritmos para a criação de cronogramas adaptativos baseados na disponibilidade real do usuário. 

Nova Página 

# **2. DERS** 

# 2.1. O PROPÓSITO DO PROJETO 

# **2.1.1 Conhecimento do Esforço do Projeto** 

Este projeto consiste no desenvolvimento e na implementação de um ecossistema móvel voltado para a otimização e automação do processo de preparação educacional focado em concursos públicos. Métodos de estudo convencionais e materiais estáticos frequentemente sobrecarregam o estudante devido ao volume massivo de editais, além de falharem em fornecer um _feedback_ imediato e personalizado sobre erros cometidos em simulados e redações. Ferramentas que tentam mitigar essas barreiras de aprendizado costumam apresentar alta complexidade de uso ou custos proibitivos para a realidade da maioria dos candidatos. 

O esforço de desenvolvimento do RememberFlash parte do equilíbrio entre automação inteligente, acessibilidade e centralização de ferramentas essenciais em uma única plataforma nativa Android. A necessidade de uma arquitetura baseada em Inteligência Artificial Generativa (IAG) e processamento de imagem (OCR) fundamenta-se na dificuldade que os estudantes enfrentam para diagnosticar deficiências específicas de aprendizado de forma autônoma. Ao integrar a geração automática de cronogramas dinâmicos, simulados adaptativos e correções textuais em tempo real, o aplicativo atua como um mediador ativo do conhecimento, estimulando a aprendizagem ativa e a memorização de longo prazo. Vale ressaltar que o êxito do propósito da plataforma está atrelado à capacidade de processamento contextual das requisições via modelos de linguagem em grande escala (LLMs), servindo como um facilitador personalizado de estudos. funcionalidades 

O produto permite que o usuário gerencie seus editais, simule avaliações completas geradas sob medida por disciplina, converta textos manuscritos de redações em dados estruturados via OCR e interaja com um tutor interativo virtual síncrono, sem a rigidez ou os altos custos de mentorias tradicionais. A mitigação da sobrecarga de informações e o direcionamento estratégico do tempo disponível do concurseiro são as prioridades absolutas 

esses dois itens, deve ser transferidos para a INTRODUÇÃO. 

14 

no desenvolvimento deste projeto, blindando o usuário contra a dispersão cognitiva e potencializando seu desempenho. 

# **2.1.2 Metas do Projeto** 

O projeto estabelece como metas principais proporcionar um ambiente móvel unificado que viabilize o acompanhamento adaptativo e personalizado da rotina de estudos, elevar o índice de retenção de conteúdos por meio de ciclos de revisão automatizados e acelerar o tempo de resposta nos _feedbacks_ de redações e dúvidas conceituais. Busca-se, fundamentalmente, reduzir a exaustão mental provocada pela gestão manual de cronogramas e materiais, substituindo ferramentas tradicionais e dispersas por uma solução preditiva, inteligente e focada na eficiência do aprendizado. 

# 2.2. OS INTERESSADOS 

# **2.2.1 O Cliente** 

O cliente principal do sistema consiste na equipe idealizadora e de desenvolvimento do projeto RememberFlash. 

# **2.2.2 O CONSUMIDOR** 

recomendo: deixar o mesmo cliente e consumidor 

Os consumidores primários do sistema são estudantes em preparação para concursos públicos que buscam otimizar suas rotinas de estudo. 

# 2.3. RESTRIÇÕES OBRIGATÓRIAS 

# **2.3.1 Restrições de Soluções** 

O sistema será executado exclusivamente em dispositivos móveis compatíveis com o sistema operacional Android. 

O sistema requer exclusivamente conexão ativa com a internet para o processamento de recursos baseados em Inteligência Artificial Generativa. questionamento: o consumo de tokens, seria uma restrição de soluções? 

2.4. NOMEANDO CONVENÇÕES E DEFINIÇÕES 

# **2.4.1 Definições de todos os termos, incluindo acrônimos, usados no projeto** 

- API ( _Application Programming Interface_ ): Acrônimo para Interface de Programação de Aplicações. Trata-se de um intermediário de software que permite que duas aplicações distintas se comuniquem entre si. 

- _Back-end_ : Refere-se à camada da aplicação responsável pelo processamento de dados e pelas regras de negócio, operando no lado do servidor. 

15 

- Banco de Dados: Consiste em uma coleção organizada de informações estruturadas, geralmente armazenadas eletronicamente em um sistema de computador. 

- _Front-end_ : Corresponde à camada de apresentação da aplicação, ou seja, a interface gráfica com a qual os usuários interagem diretamente. Inclui todos os elementos visuais, como botões, caixas de seleção, gráficos, layouts e textos. 

- Engenharia de _Prompt_ : Técnica de estruturar, calibrar e refinar as instruções enviadas para um modelo de Inteligência Artificial Generativa. 

- IAG (Inteligência Artificial Generativa): Ramo da inteligência artificial focado na criação de novos conteúdos a partir de padrões aprendidos em grandes volumes de dados. 

- JSON ( _JavaScript Object Notation_ ): Formato leve e padronizado de intercâmbio de dados, fácil de ler e escrever para humanos e máquinas. 

- LLM ( _Large Language Model_ ): Modelo de Linguagem de Grande Escala. É um sistema de redes neurais treinado com vastas quantidades de texto para compreender e gerar linguagem natural humana. 

- OCR (Optical Character Recognition): Reconhecimento Óptico de Caracteres. Tecnologia de visão computacional utilizada pelo aplicativo para converter imagens contendo texto manuscrito. 

- OTP ( _One Time Password_ ): Senha de Uso Único. Código numérico temporário enviado ao usuário (via e-mail) para validar sua identidade durante fluxos de alta criticidade, como a recuperação de credenciais de acesso. 

- RAG ( _Retrieval-Augmented Generation_ ): Geração Aumentada por Recuperação. Técnica avançada de IA que aprimora a precisão das respostas ao orquestrar a extração de dados de documentos externos e injetá-los no contexto do modelo antes da geração do conteúdo. 

- _Token_ (Autenticação): Credencial digital segura gerada pelo servidor após um login bem-sucedido. 

- _Token_ (Inteligência Artificial): Unidade básica de processamento de texto lida e gerada pelos modelos LLM. Pode representar uma palavra inteira, uma sílaba ou um caractere. O consumo dos serviços em nuvem de IAG é precificado e limitado com base no volume de _tokens_ processados. 

- UI ( _User Interface_ ): Interface do Usuário. Representa a arquitetura visual do aplicativo móvel, contemplando os padrões estéticos, cores, tipografia e os componentes de interação com os quais o estudante tem contato direto. 

16 

# 2.5. FATOS E SUPOSIÇÕES RELEVANTES 

# **2.5.1 Fatos Relevantes** 

A preparação para processos seletivos concorridos, como o vestibular da Prefeitura de Rio Verde, envolve o domínio de múltiplas disciplinas e a gestão eficiente do tempo. O estudante se depara com a necessidade de conciliar a absorção de grandes volumes de conteúdo teórico com a prática constante de exercícios e a escrita de redações. Além disso, cada candidato possui um perfil único de aprendizado, com ritmos, dificuldades e disponibilidades de horário distintas, o que torna os métodos de estudo genéricos muitas vezes insuficientes para garantir um desempenho competitivo. 

# **2.4.2 Regras do Negócio** 

- **RN01** - Perfil Único: Cada estudante poderá manter apenas um perfil ativo no sistema, vinculado a um e-mail único, para garantir a integridade das métricas de desempenho e do histórico de avaliações do usuário. 

e se o aluno tiver 2 email, ele poderá ter dois perfis? 

- **RN02** - Autenticação Obrigatória: O usuário deve estar devidamente cadastrado e autenticado no sistema para conseguir acessar o ambiente de estudos, gerar conteúdos via IAG e salvar seu progresso. 

regra de negócio não é do sistema. 

- **RN03** - Isolamento de Dados: O sistema deve garantir que um usuário autenticado só tenha permissão para ler, alterar ou excluir os dados e pastas que pertencem ao seu próprio ID de identificação, impossibilitando o acesso a informações de terceiros. 

o sistema não faz... 

   - **RN04** - Restrição de _Prompt_ : A parametrização da IAG não permite a inserção de _prompts_ livres pelo usuário final. O estudante deve apenas selecionar perfis predefinidos (nível de dificuldade e foco). 

   - **RN05** - Validação de Data do Cronograma: A elaboração de um Cronograma Dinâmico exige que a "Data da Prova" informada pelo estudante seja estritamente superior (data futura) à data atual do sistema 

- **RN06** - Exclusão em Cascata: A exclusão de uma Pasta de Concurso pelo usuário 

- perde-se o histórico? qual a finalidade acarretará a exclusão em cascata de todas as disciplinas, _flashcards_ , questões e dessa exclusão? cronogramas atrelados a ela no banco de dados local, evitando acúmulo de dados órfãos. 

   - **RN07** - Contexto da Tutoria Interativa: A funcionalidade de "Tutoria Interativa" só poderá ser acionada a partir de uma questão já respondida ou de uma redação já 

17 

avaliada, utilizando estritamente o conteúdo do erro/acerto como contexto base para a explicação gerada pela IAG. 

ficou confuso. 

- **RN08** - Persistência Local: O sistema deve permitir o armazenamento local de informações para a revisão de conteúdos e/ou para a criação manual de novos flashcards 

# 2.6. ESTIMATIVA DE CUSTOS E PRAZOS PARA A ELABORAÇÃO 

# **2.6.1 Estimativa de Custos para Elaboração** 

será que este é foco neste momento? 

Estima-se um custo inicial de R$4.200,00 para o desenvolvimento de identidade visual, arquitetura de marca e prototipação das interfaces de alta fidelidade (UI/UX) das telas móveis do sistema. Para a infraestrutura operacional de homologação e produção, estima-se um custo mensal fixo de R$150,00 voltado para a manutenção dos serviços de _back-end_ em nuvem, hospedagem de APIs e persistência no banco de dados relacional distribuído (Cloud Computing). Adicionalmente, projeta-se um custo variável estimado em R$450,00 mensais 

Custos para o desenvolvimento. 

para as requisições de tokens de processamento junto à API de Inteligência Artificial Generativa (Google Gemini API) para a análise das redações e geração de simulados. recomenda-se uma tabela ao final do paragráfo para melhor ilustração. 

# **2.6.2. Estimativa de prazo para elaboração** 

O cronograma de desenvolvimento do aplicativo RememberFlash foi estruturado para ocorrer em um ciclo de aproximadamente 9 meses, com início no dia 04/02/2026 e a entrega final agendada para 09/11/2026. As atividades foram divididas em fases de engenharia, desenvolvimento de software e validação, permitindo em paralelo a confecção da redação em <u>aproximadamente (167 dias).</u> 

redação do que? 

18 

# **2.6.3. Cronograma de atividades planejadas** 

FIGURA 1 - Cronograma de atividades planejadas 

girar 180° 



<!-- Start of picture text -->
gl\z|? 2ig g\f|g alzl|zglg¢ zl\slgig slglei2ie 2<br>zie) /8lelsleleielsfel/e/sl2/2i2\e| |2/8\sle|s)e a4<br>z 2/E/8/* [2/3 e/8/2 8/8/82) glelelgisle |<br>3|2\2 a/3 £ (<br>S/2/)8/8/2/3/8PPP PU8/8)8/2)8/8/2)2)/8)EEEEEESHEELTEEEE2/218) 8) 8/8) 3/8) s Fae<br>=/B/S)s/s Ss) slalo oe /Sfoalwel|slo|~ 3) Bis)/ss = a7 * = “4 =<br>| | | Fl ) 8) dais 8<br>| | gi] e] gi a) ails 8 | =<br>22) 3| §] sess? 2 |<br>32 i) f) ayes & |<br>1. oe ! ! | ES<br><!-- End of picture text -->

Fonte: O autor (2026) 

19 

# 2.7. O ESCOPO DO TRABALHO 

# **2.7.1 A situação atual** 

A preparação para concursos e processos seletivos é atualmente mediada por métodos tradicionais e fragmentados, como cursos preparatórios em vídeo, apostilas estáticas e plataformas genéricas de banco de questões. Essas ferramentas operam sob um modelo de consumo passivo e descentralizado, onde a curadoria do material, a estruturação do cronograma e a confecção de resumos de revisão ativa exigem intervenção manual contínua do estudante, não havendo uma adaptação dinâmica do conteúdo ao nível de proficiência ou às restrições de tempo do candidato. 

cuidado com a de citação 

O uso prolongado dessa abordagem baseada na curadoria manual e na alternância 

entre múltiplas plataformas é associado ao fenômeno da sobrecarga cognitiva, que consome o tempo de estudo efetivo e impacta negativamente a produtividade e o engajamento do aluno. Além disso, a ausência de _feedbacks_ imediatos e formativos limita a capacidade de autoavaliação contínua, tornando o ciclo de aprendizagem lento, elitizado e excessivamente oneroso. recomenda-se que, nesse item, explique como funciona a situação atual, sem a necessidade de justificativa. 

# 2.8. REQUISITOS FUNCIONAIS 

# **2.8.1 Prioridade de Requisito** 

**Essencial:** requisitos sem os quais a aplicação não entrará em funcionamento. **Importante:** requisitos sem os quais a aplicação, no entanto, não alcançará o objetivo de funcionamento. 

**Desejável:** esse tipo de requisito não afeta diretamente o funcionamento do software, ou seja, o software pode funcionar de forma satisfatória sem tais requisitos. 

20 

# **2.8.2. Descrição dos Requisitos Funcionais** 

<u>Quadro 1 RF01 - Manter Usuário</u> 

|**Requisito n°**|**RF001**|Manter Usuário||||
|---|---|---|---|---|---|
|**Descrição**|Permite ao<br>aplicativo pa|usuário criar um<br>ra ter acesso ao se|a conta<br>u ambien|e autenticar-se de fo<br>te de estudos.|rma segura no|
|**Prioridade**|Essencial|**Dependências**|Nenhu|ma<br>**Conflito**|Nenhum|
|**Material de A**|**poio**|Diagrama<br>de<br>(autenticação).|caso|de<br>uso,<br>Diagrama|de sequência|



Fonte: O autor (2026). 

<u>Quadro 2 RF02 - Alterar Senha</u> 

|**Requisito n°**|**RF002**|Alterar Senha||||
|---|---|---|---|---|---|
|**Descrição**|Permite ao u<br>caso a tenha|suário solicitar a<br>esquecido.|redefinição de|sua senha de a|cesso via e-mail|
|**Prioridade**|Essencial|**Dependências**|RF001|**Conflito**|Nenhum|
|**Material de A**|**poio**|Diagrama de ca|so de uso.|||



Fonte: O autor (2026). 

<u>Quadro 3 RF03 - Manter Concurso</u> 

|**Requisito n°**|**RF003**|Manter Concurso|
|---|---|---|
|**Descrição**|Permite ao<br>parâmetros g<br>desejada (M<br>que o usuári<br>base de dado|usuário adicionar, editar e excluir "Pastas" principais e definir<br>lobais para elas, como a Banca Examinadora e o Tipo de Questão<br>últipla escolha ou Verdadeiro/Falso). Adicionalmente, possibilita<br>o anexe o arquivo PDF do edital do certame, que servirá como<br>s para a IAG estruturar o conteúdo.|
|**Prioridade**|Essencial|**Dependências**RF001<br>**Conflito**<br>Nenhum|
|**Material de A**|**poio**|Diagrama de caso de uso.|



Fonte: O autor (2026). 

21 

<u>Quadro 4 RF04 - Manter Disciplina</u> 

|**Requisito n°**|**RF004**|Manter Disciplina||
|---|---|---|---|
|**Descrição**|Permite ao|estudante criar e excluir subcate|gorias ou disciplinas específicas|
||dentro de u<br>textual) para|ma pasta (ex: Interpretação de t<br>organizar o material gerado.|extos, Processos de construção|
|**Prioridade**|Essencial|**Dependências**RF003|**Conflito**<br>Nenhum|
|**Material de A**|**poio**|Diagrama de caso de uso.||



Fonte: O autor (2026). 

<u>Quadro 5 RF05 - Parametrizar Inteligência Artificial Generativa</u> 

|**Requisito n°**|**RF005**|Parametrizar In|teligência Arti|ficial Generativ|a|
|---|---|---|---|---|---|
|**Descrição**|Permite ao u<br>para calibrar|suário selecionar<br>a geração de cont|perfis predefi<br>eúdos.|nidos de compo|rtamento da IAG|
|**Prioridade**|Importante|**Dependências**|RF001|**Conflito**|Nenhum|
|**Material de A**|**poio**|Diagrama de ca|so de uso.|||



Fonte: O autor (2026). 

<u>Quadro 6 RF06 - Manter</u> _<u>Flashcard</u>_ 

|**Requisito n°**|**RF006**|Manter_Flashcar_|_d_||
|---|---|---|---|---|
|**Descrição**|Permite a|inserção manual|de novos cartões de estudo|(flashcards)|
||preenchendo|um "Título" e uma|"Descrição".||
|**Prioridade**|Importante|**Dependências**|RF001<br>**Conflito**<br>|Nenhum|
|**Material de A**|**poio**|Diagrama de cas|o de uso.||



Fonte: O autor (2026). 

<u>Quadro 7 RF07 - Manter Flashcard via Documento PDF</u> 

|**Requisito n°**<br>**RF007**|Manter Flashcards via Documento PDF|
|---|---|



22 

|**Descrição**|Permite o u|pload de um arqu|ivo PDF para que o sistema e|xtraia o texto e|
|---|---|---|---|---|
||solicite à IA|G a criação auto|matizada de uma quantidade “x|” de_flashcards_|
||baseados no|documento.|||
|**Prioridade**|Essencial|**Dependências**|RF001<br>**Conflito**|Nenhum|
|**Material de A**|**poio**|Diagrama de ca|so de uso.||



Fonte: O autor (2026). 

<u>Quadro 8 RF08 - Manter Questões via Inteligência Artificial Generativa</u> 

|**Requisito n°**|**RF008**<br>Manter Questõe|s via Inteligênci|a Artificial Gene|rativa|
|---|---|---|---|---|
|**Descrição**|Permite<br>enviar<br>requisições<br>processualmente lotes com um<br>na matéria atual e na banca.|ao<br>modelo<br>a quantidade pr|de<br>linguagem<br>edefinida de que|<br>para<br>gerar<br>stões baseadas|
|**Prioridade**|Essencial<br>**Dependências**|RF001|**Conflito**|Nenhum|
|**Material de A**|**poio**<br>Diagrama de ca<br>simulados).|so de uso, Diag|rama de sequênc|ia (geração de|



Fonte: O autor (2026). 

<u>Quadro 9 RF09 - Manter Prova via Edital (PDF)</u> 

|**Requisito n°**|**RF009**|Manter Prova E|dital (PDF)|||
|---|---|---|---|---|---|
|**Descrição**|Permite ao<br>momento d<br>disciplinas e<br>simulado c<br>exigidos par|usuário realizar o<br>o cadastro do co<br>os pesos de cada<br>ompleto, estrutura<br>a a prova real.|_upload_do do<br>ncurso. O sist<br>matéria para<br>do com que|cumento do edi<br>ema extrai aut<br>solicitar à IAG<br>stões proporcio|tal (em PDF) no<br>omaticamente as<br>a criação de um<br>nais aos temas|
|**Prioridade**|Essencial|**Dependências**|RF003,<br>RF005|**Conflito**|Nenhum|
|**Material de A**|**poio**|Diagrama de ca|so de uso.|||



Fonte: O autor (2026). 

23 

## <u>Quadro 10 RF10 - Visualizar Desempenho e Progresso</u> 

|**Requisito n°**|**RF010**|Visualizar Dese|mpenho e Pro|gresso||
|---|---|---|---|---|---|
|**Descrição**|Exibe um pa<br>barras de pr<br>de Acertos (|inel analítico com<br>ogresso, quantidad<br>com porcentagem)|métricas deta<br>e total de ques<br>e Erros por d|lhadas de desem<br>tões respondidas<br>isciplina e simul|penho, incluindo<br>e a relação exata<br>ados.|
|**Prioridade**|Essencial|**Dependências**|RF008,|**Conflito**|Nenhum|
||||RF009|||
|**Material de A**|**poio**|Diagrama de ca|so de uso.|||



Fonte: O autor (2026). 

<u>Quadro 11 RF11 - Avaliar Redação via OCR</u> 

|**Requisito n°**|**RF011**|Avaliar Redação via OCR||
|---|---|---|---|
|**Descrição**|Permite ao u<br>ou enviando<br>análise e not|suário submeter sua redação digitando-a em cam<br>uma foto da folha manuscrita (usando OCR) p<br>a do modelo de IAG.|po de texto livre<br>ara receber uma|
|**Prioridade**|Essencial|**Dependências**RF005<br>**Conflito**|Nenhum|
|**Material de A**|**poio**|Diagrama de caso de uso, Diagrama de Sequ<br>de redação via OCR).|ência (avaliação|



Fonte: O autor (2026). 

<u>Quadro 12 RF12 - Consultar Histórico de Redações</u> 

|**Requisito n°**|**RF012**|Consultar Histó|rico de Redações||
|---|---|---|---|---|
|**Descrição**|Mantém um h|istórico das reda|ções submetidas pelo estudant|e, apresentando o|
||título, a data e<br>do_feedback_.|hora do envio, e|a nota obtida (ex: 7.5/10), pe|rmitindo a revisão|
|**Prioridade**|Importante|**Dependências**|RF011<br>**Conflito**|Nenhum|
|**Material de A**|**poio**|Diagrama de ca|so de uso.||



24 

Fonte: O autor (2026). 

<u>Quadro 13 RF13 - Manter Cronograma Dinâmico</u> 

|**Requisito n°**|**RF013**|Manter Cronograma Dinâmico|||
|---|---|---|---|---|
|**Descrição**|Calcula e g|era um cronograma de estudos|distribuído e|m um calendário|
||interativo. A|geração é parametrizada selecion|ando a Pasta d|e Conteúdo, Data|
||da Prova, M<br>disponíveis|inutos por dia, Máximo de matér<br>para estudo.|ias diárias e|os dias da semana|
|**Prioridade**|Essencial|**Dependências**RF003|**Conflito**|Nenhum|
|**Material de A**|**poio**|Diagrama de caso de uso.|||



Fonte: O autor (2026). 

<u>Quadro 14 RF14 - Interagir Tutoria Interativa</u> 

|**Requisito n°**|**RF014**|Interagir Tutori|a Interativa||
|---|---|---|---|---|
|**Descrição**|Permite ao<br>recém-respo<br>O sistema a<br>"tutor 1:1",<br>compreensão|estudante solicita<br>ndida no simulado<br>bre uma interface<br>sanando dúvidas<br>do erro e o apren|r explicações<br>ou sobre o_fee_<br>de_chat_conte<br>específicas<br>dizado ativo.|detalhadas sobre uma questão<br>_dback_recebido em uma redação.<br>xtual onde a IAG atua como um<br>sobre o tema para garantir a|
|**Prioridade**|Importante|**Dependências**|RF008,<br>RF009,<br>RF011|**Conflito**<br>Nenhum|
|**Material de A**|**poio**|Diagrama de<br>interativo).|caso de uso,|Diagrama de sequência (tutor|



Fonte: O autor (2026). 

# **2.8.3 Especificação dos Requisitos Funcionais** 

# **RF001 - Manter Usuário** 

1. BREVE DESCRIÇÃO 

25 

essas são as ações para gerar o fluxo alternativo 

Permite que o estudante (concurseiro) crie uma nova conta ou acesse o aplicativo móvel por meio de autenticação segura, estabelecendo uma sessão persistente para acessar o seu ambiente de estudos e histórico no banco de dados em nuvem. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

3. FLUXO DE EVENTOS 

- 3.1. FLUXO BÁSICO (Autenticação / Login) 

   1. O usuário abre o aplicativo móvel em seu dispositivo. 

   2. O sistema verifica localmente se já existe um _Token_ de sessão válido. 

   3. Não havendo sessão, o sistema exibe a tela inicial de Login. 

esse quadro deve vir aqui. 

4. O usuário informa os dados de acesso (E-mail e Senha). 

5. O usuário confirma a ação tocando no botão "Entrar". 

6. O sistema exibe um indicador de carregamento e envia as credenciais ao _back-end_ . 

7. O _back-end_ valida as credenciais no banco de dados. 

8. Em caso de sucesso, o sistema recebe o _Token_ de autorização, salva-o e redireciona o usuário para a tela inicial do aplicativo. 

Quadro 15 - Detalhamento de Campos (Tela de Login) 

|**Campo**|**Valor Default**|**Domínio**<br>**de**<br>**Valores**|**Tipo de Campo**<br>**na tela**|**Obrigatório**|
|---|---|---|---|---|
|E-mail|Branco|Formato<br>de|Campo de texto|Sim|
|||e-mail válido|editável||
|Senha|Branco|Alfanumérico|Campo de senha|Sim|
|||(mín.<br>6|(Mascarado)||
|||caracteres)|||
|Entrar|||Botão de submit|Sim|
|Cadastre-se|||Botão de Link|Sim|



26 

|Esqueci a senha|Botão de Link|Não|
|---|---|---|



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Credenciais Inválidas (Erro no Login)** 

1. O _back-end_ retorna que as credenciais não conferem ou o usuário não existe. 

2. O sistema oculta o indicador de carregamento. 

   - em qual passo do fluxo básico inicia este fluxo? 

3. O sistema exibe a mensagem de erro: "E-mail ou senha incorretos. Tente novamente." através de um aviso em tela. 

4. O sistema limpa o campo de email e senha e mantém o usuário na tela de login. 

# **A2 – Cadastro de Novo Usuário** 

1. No passo 3 do fluxo básico, o usuário toca no botão "Cadastre-se". 

2. O sistema navega para a tela de Criação de Conta. 

3. O usuário preenche os campos requeridos (Nome, E-mail, Senha e Confirmação de Senha) e toca em "Criar Conta". 

4. O sistema valida internamente se as senhas coincidem e se o formato do e-mail é válido. 

5. O sistema envia a requisição de cadastro para o _back-end_ . 

6. É verificado se o e-mail é único. Sendo único, realiza o hash da senha e salva o registro. 

7. O sistema exibe a mensagem "Conta criada com sucesso!", realiza o login automático e redireciona o usuário ao Dashboard. 

8. O fluxo alternativo é encerrado. 

Quadro 16 - Detalhamento de Campos (Tela de Cadastro) 

|**Campo**|**Valor Default**|**Domínio**|**de**|**Tipo de Campo**|**Obrigatório**|
|---|---|---|---|---|---|
|||**Valores**||**na tela**||
|Nome Completo|Branco|Formato|de|Campo de texto|Sim|
|||texto||editável||
|E-mail|Branco|Formato|de|Campo de texto|Sim|



27 

|||e-mail válido|||
|---|---|---|---|---|
|Senha|Branco|Alfanumérico|Campo de senha|Sim|
|||(mín.<br>6|(Ofuscado)||
|||caracteres)|||
|Confirmar|Branco|Alfanumérico|Campo de senha|Sim|
|Senha||(mín.<br>6|(Ofuscado)||
|||caracteres)|||
|Criar Conta|||Botão de submit|Sim|



Fonte: O autor(2026). no prototipo aparece mais uma ação: "ENTRAR" 

faltou RECUPERAR SENHA. 

# **A3 – Sessão Local já Existente (Auto-Login)** 

   1. O sistema detecta que já existe um Token de acesso válido armazenado no dispositivo. 

   2. O sistema pula a tela de Login e redireciona o usuário diretamente para a tela inicial. 

4. PRÉ-CONDIÇÕES 

   - O dispositivo móvel deve possuir conexão ativa com a internet para comunicação com o _back-end_ de validação ou ter acessado nas últimas 24 horas dias e validando o acesso por meio de _token_ . 

   - Para o login, o usuário deve estar previamente cadastrado no sistema. 

5. PÓS-CONDIÇÕES 

   - O estado de autenticação ( _Token_ JWT) é gravado no armazenamento local do dispositivo, garantindo o acesso seguro aos módulos restritos do aplicativo sem necessidade de novos logins a cada abertura. 

6. PONTOS DE EXTENSÃO 

recomenda-se informar no fluxo alternativo. 

   - Recuperação de senha (Redirecionamento para o fluxo do caso acione o botão "Esqueci a senha"). 

7. PROTÓTIPO 

28 

FIGURA 2 - Tela de Login 



<!-- Start of picture text -->
RememberFlash<br>| E-mail |<br>| Senha |<br>Esqueceu a senha?<br>Nao tem conta? Cadastre-se<br><!-- End of picture text -->

Fonte: O autor(2026). 

29 

FIGURA 3 - Tela de Cadastro 



<!-- Start of picture text -->
€ _ Crie sua Conta<br>| Senha<br>| Confirmar Senha<br>Ja tem conta? Entrar<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF002 - Alterar Senha** 

1. BREVE DESCRIÇÃO 

Permite que o usuário (Concurseiro) solicite a redefinição de sua senha de acesso caso a tenha esquecido. 

30 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

   - _Back-end_ (Sistema Externo / Serviço de E-mail) 

3. FLUXO DE EVENTOS 

- 3.1. FLUXO BÁSICO (Recuperação de Senha via Código OTP) 

   1. Na tela de Login (RF001), o usuário aciona a opção "Esqueci a senha". 

   2. O sistema navega para a tela de "Recuperação de Conta" solicitando o endereço de e-mail. 

back-end sistema 

recomendo usar sistema. 

3. O _Back-end_ valida a existência do e-mail no banco de dados. 

4. O _Back-end_ gera um código numérico temporário (6 dígitos, válido por 15 minutos) e o envia para o e-mail do usuário. 

5. O sistema avança para a tela de "Redefinição de Senha", exibindo a mensagem: "Enviamos um código de 6 dígitos para o seu e-mail". 

6. O usuário insere o código recebido, digita a nova senha e a confirmação da nova senha. 

7. O _Back-end_ valida se o código confere e se ainda está no prazo de validade. 

8. Em caso de sucesso,  é atualizado o registro no Banco de Dados. 

9. O sistema exibe a mensagem visual "Senha alterada com sucesso!" e redireciona o usuário de volta para a tela de Login. 

Quadro 17 - Detalhamento de Campos (Tela de Solicitação de Recuperação) 

|**Campo**|**Valor Default**|**Domínio**|**de**|**Tipo de Campo**|**Obrigatoriedad**|
|---|---|---|---|---|---|
|||**Valores**||**na Tela**|**e**|
|E-mail|Branco|Formato|de|Campo de texto|Sim|
|||e-mail||editável||
|Enviar Código||||Botão de submit|Sim|
|Voltar ao Login||||Botão de Link|Sim|



toda a ação, deve ter um fluxo alternativo. 

31 

Quadro 17 - Detalhamento de Campos (Tela de Redefinição de Senha) 

|**Campo**|**Valor Default**|**Domínio**<br>**Valores**|**de**|**Tipo de Campo**<br>**na Tela**|**Obrigatoriedad**<br>**e**|
|---|---|---|---|---|---|
|Código<br>de|Branco|Apenas||Campo|Sim|
|Segurança||Números<br>dígitos)|(6|numérico||
|Nova Senha|Branco|Alfanuméric<br>(mín.<br>caracteres)|o<br>6|Campo de senha<br>(Mascarado)|Sim|
|Confirmar Nova|Branco|Alfanuméric|o|Campo de senha|Sim|
|Senha||(mín.<br>caracteres)|6|(Mascarado)||
|Redefinir Senha||||Botão de submit|Sim|



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – E-mail não encontrado ou inválido** 

1. O _back-end_ não encontra o e-mail na base de dados. 

2. Por diretrizes de segurança (para evitar varredura de contas), retorna uma mensagem genérica, mas não envia o e-mail. 

3. O sistema avança para a tela de "Redefinição de Senha" exibindo a mensagem padrão: "Se o e-mail constar em nossa base, você receberá um código em instantes". 

4. O fluxo segue aguardando o usuário, protegendo a privacidade dos usuários reais. 

# **A2 – Código de Segurança (OTP) Inválido ou Expirado** 

1. O _back-end_ detecta que o código inserido pelo usuário está incorreto ou seu prazo de 15 minutos expirou. 

2. O sistema oculta o loading e exibe a mensagem de erro: "Código inválido ou expirado. Verifique seu e-mail ou solicite um novo código." 

3. O sistema destaca o campo "Código de Segurança" em vermelho e mantém o usuário na mesma tela. 

32 

# **A3 – Divergência na Confirmação da Nova Senha** 

   1. O usuário digita valores diferentes nos campos "Nova Senha" e "Confirmar Nova Senha". 

   2. O sistema, através da _ViewModel_ (validação local do aplicativo, sem ir à internet), detecta a divergência instantaneamente. 

   3. O sistema desabilita o botão "Redefinir Senha" e exibe um alerta abaixo do campo: "As senhas não coincidem". 

   4. O usuário corrige a digitação, o botão é habilitado, e o sistema retorna ao fluxo básico. 

4. PRÉ-CONDIÇÕES 

   - O dispositivo móvel deve possuir conexão ativa com a internet. 

   - O usuário não pode estar autenticado (logado) no aplicativo no momento da solicitação. 

5. PÓS-CONDIÇÕES 

   - A credencial de acesso do usuário é atualizada (hash) no banco de dados. Qualquer sessão anterior que estivesse ativa em outros dispositivos utilizando o _Token_ antigo será invalidada pelo _back-end_ . 

6. PONTOS DE EXTENSÃO 

   - Não se aplica. 

7. PROTÓTIPO 

33 

FIGURA 4 - Tela de Recuperação de Senha (Email de recuperação) 



<!-- Start of picture text -->
€ Recuperacgao<br>Esqueceu a senha?<br>Digite seu e-mail cadastrado. Enviaremos<br>um cédigo de 6 digitos para validacao<br>| E-mail<br>Voltar ao Login<br><!-- End of picture text -->

Fonte: O autor(2026). 

34 

FIGURA 5 - Tela de Recuperação de Senha (Código de Verificação) 



<!-- Start of picture text -->
€ Nova Senha<br>oe Cédigo enviado para o e-mail<br>j***@email.com<br>Cédigo de Seguranca<br>| 000000 |<br>| Nova Senha $0 |<br>| Confirmar Nova Senha Of |<br>Redefinir Senha<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF003 - Manter Concurso** 

# 1. BREVE DESCRIÇÃO 

Permite ao estudante criar, visualizar, editar e excluir pastas estruturais denominadas "Concursos". Estas pastas centralizam as disciplinas, simulados e parâmetros globais (Banca Examinadora e Tipo de Questão) que irão direcionar o comportamento da Inteligência 

35 

Artificial. Permite também o anexo do arquivo PDF do edital, que servirá de base contextual (RAG - Retrieval-Augmented Generation) para a API do Gemini gerar conteúdos precisos. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

3. FLUXO DE EVENTOS 

de acordo com o protótipo, a tela inicial não é essa. 

- 3.1. FLUXO BÁSICO (Criar Novo Concurso) 

   1. Na tela principal, o usuário aciona o Botão de submit "Novo Concurso". 

   2. O sistema apresenta a tela de formulário "Configurar Edital/Concurso". 

   3. O usuário preenche o Título da Pasta e seleciona a Banca Examinadora e o Tipo de Questão. 

   4. O usuário toca na opção "Anexar Edital (PDF)". 

   5. O sistema abre o seletor nativo de arquivos do Android. 

   6. O usuário confirma a criação tocando no botão "Salvar Concurso". 

   7. O sistema exibe um indicador de carregamento enquanto a _ViewModel_ empacota os dados e envia (junto com o arquivo binário) para o _back-end_ . 

   8. O _back-end_ salva as informações no banco de dados, armazena o arquivo em nuvem e retorna o ID do concurso gerado. 

   9. O sistema exibe a mensagem "Concurso cadastrado com sucesso!", atualiza e encerra o caso de uso. 

<u>Quadro 18 - Detalhamento de Campos (Tela de Criação de Concurso)</u> 

|**Campo**|**Valor default**|**Domínio**<br>**valores**|**de**|**Tipo de campo**<br>**na tela**|**Obrigatoriedad**<br>**e**|
|---|---|---|---|---|---|
|Título<br>do|Branco|Texto||Campo de texto|Sim|
|Concurso||||editável||
|Banca|Branco|Texto||Campo de texto|Sim|
|Examinadora||||editável||
|Tipo de Questão|Múltipla<br>escolha|Múltipla<br>Escolha,||Botões<br>de<br>Seleção (Radio|Sim|



36 

|||Certo/Errado|Buttons)|||
|---|---|---|---|---|---|
|Anexo do Edital|Nenhum|Formato .pdf|Botão|de|Não|
||Arquivo||Upload /|File||
||||Picker|||
|Salvar Concurso|||Botão de s|ubmit|Sim|



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Edição de Parâmetros do Concurso** 

1. No Dashboard, o usuário seleciona um Concurso já existente e aciona o ícone de "Editar" (lápis). 

2. O sistema recupera as informações do banco de dados e preenche a tela do formulário. 

3. O usuário modifica os dados desejados (ex: troca a banca examinadora ou faz o upload de um novo edital retificado). 

4. O usuário toca em "Salvar Alterações". 

5. O sistema envia a requisição de atualização para o _back-end_ . 

6. O sistema exibe a mensagem "Alterações salvas" e retorna à visualização da pasta. 

# **A2 – Arquivo Anexado Inválido (Erro de Validação Local)** 

1. O usuário seleciona um arquivo com extensão diferente de .pdf (ex: um .docx ou .jpg) ou um PDF que excede o limite estipulado (ex: 10MB). 

2. O sistema (através da validação da _ViewModel_ ) detecta a violação antes de consumir. 

3. O sistema recusa o anexo e exibe uma mensagem de erro em vermelho sob o botão: 

   - "Formato inválido ou arquivo muito grande. Selecione um PDF de até 10MB." 

4. O campo "Anexo do Edital" retorna ao estado "Nenhum arquivo". 

# **A3 – Exclusão da Pasta (Exclusão em Cascata)** 

1. Na tela inicial, o usuário acessa as opções secundárias de um Concurso e seleciona "Excluir". 

2. O sistema exibe um modal crítico de confirmação: "Tem certeza? Excluir este concurso apagará permanentemente todas as disciplinas, flashcards e desempenhos vinculados a ele." 

37 

3. O usuário confirma a exclusão. 

4. O sistema dispara a requisição de exclusão (DELETE) para o _back-end_ . 

5. O banco de dados aplica a regra ON DELETE CASCADE, varrendo e excluindo todas as entidades secundárias atreladas ao ID deste concurso. 

6. O sistema exibe "Concurso excluído" e atualiza a tela. 

# 4. PRÉ-CONDIÇÕES 

- O usuário deve estar autenticado e com uma sessão ativa no aplicativo. 

- Para a etapa de anexo, o usuário deve conceder ao aplicativo a permissão do Android para acessar o armazenamento do dispositivo. 

# 5. PÓS-CONDIÇÕES 

   - Os dados do Concurso são gravados no banco de dados e sincronizados. Os parâmetros globais definidos aqui passarão a ser injetados automaticamente no prompt (Engenharia de Prompt) toda vez que a IAG for acionada dentro desta pasta. 

6. PONTOS DE EXTENSÃO 

   - Após a criação bem-sucedida, o sistema pode sugerir o redirecionamento imediato para o caso de uso de Manter Disciplina para que o estudante comece a organizar os subtópicos da pasta recém-criada. 

7. PROTÓTIPO 

38 

FIGURA 6 -  Tela de menu 

carece de filtros para buscar pastas/concursos cadastrados 



<!-- Start of picture text -->
Ola, Joao Vitor<br>Pronto para os estudos de hoje?<br>) 65% da meta didria concluida<br>Meus Concursos<br>Policia Federal<br>Banca: CEBRASPE<br>——<br>Toque no + para adicionar seu primeiro edital<br>ft E} ica} 2<br>Inicio Simulados Calendario Perfil<br><!-- End of picture text -->

todas as ações que existem nesse protótipo, são fluxos alternativos. 

Fonte: O autor(2026). 

39 

FIGURA 7 - Tela de cadastro de concurso 



<!-- Start of picture text -->
€ Novo Concurso<br>| Titulo do Concurso |<br>| Banca Examinadora |<br>TIPO DE QUESTAO<br>© Miltipia Escolha<br>O Certo/Errado<br>H © Anexar Edital (PDF) }<br>f@ Salvar Concurso<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF004 - Manter Disciplina** 

1. BREVE DESCRIÇÃO 

Permite ao estudante criar, visualizar, editar e excluir subcategorias (Disciplinas) dentro de uma pasta estrutural (Concurso) previamente criada. Estas disciplinas funcionam 

40 

como gavetas organizacionais onde os flashcards, questões geradas por IA e o desempenho do aluno serão armazenados de forma isolada. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

3. FLUXO DE EVENTOS 

# 3.1. FLUXO BÁSICO (Criar Nova Disciplina) 

obrigatoriamente, deve iniciar com a tela de listagem de todos as pastas/concursos cadastrados. de acordo a Figura 6. 

1. Na tela inicial, o usuário acessa uma pasta de "Concurso" previamente criada. 

2. O sistema exibe a tela de detalhes do Concurso, listando as disciplinas já existentes (ou uma mensagem de estado vazio caso não haja nenhuma). 

3. O usuário aciona o botão "Adicionar Disciplina" (ícone de +). 

4. O sistema apresenta um componente sobreposto (Modal Bottom Sheet ou Dialog) contendo o campo de formulário. 

5. O usuário digita o Nome da Disciplina e toca em "Salvar". 

6. A _ViewModel_ valida o preenchimento e dispara a requisição (POST) para o _back-end_ , passando o ID do Concurso "Pai" e o nome da disciplina. 

7. O sistema fecha o modal e atualiza a lista exibindo a nova disciplina na tela. 

<u>Quadro 19 - Detalhamento de Campos (Modal de Criação/Edição de Disciplina)</u> 

|**Campo**|**Valor default**|**Domínio**|**de**|**Tipo de campo**|**Obrigatorieda**|
|---|---|---|---|---|---|
|||**valores**||**na tela**|**de**|
|Nome<br>da|Branco|Texto||Campo de texto|Sim|
|Disciplina||||editável||
|Salvar||||Botão de submit|Sim|
|Cancelar||||Botão de Link|Não|



Fonte: O autor(2026). 

Fluxos Alternativos 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Edição do Nome da Disciplina** 

1. Na tela do Concurso, o usuário aciona o botão de "Opções", selecionando "Editar". 

2. O sistema abre o modal já preenchido com o nome atual da disciplina. 

41 

3. O usuário altera o texto e toca em "Salvar". 

4. O sistema dispara a requisição (PUT/PATCH) para o _back-end_ . 

5. O sistema atualiza e reflete a mudança visualmente na lista. 

# **A2 – Nome em Branco ou Duplicado (Validação Local)** 

1. O usuário tenta salvar com o campo vazio ou com um nome que já existe dentro daquele mesmo Concurso. 

2. O sistema faz uma validação na _ViewModel_ e bloqueia a ação. 

3. O sistema exibe uma mensagem de erro sob o campo de texto: "O nome não pode estar vazio" ou "Já existe uma disciplina com este nome neste concurso." 

4. O usuário corrige a digitação e prossegue. 

# **A3 – Exclusão de Disciplina (Exclusão em Cascata Parcial)** 

1. Na tela do Concurso, o usuário aciona a opção "Excluir" em uma disciplina específica. 

2. O sistema exibe um alerta de segurança: "Tem certeza? Todos os _flashcards_ , questões e históricos atrelados EXCLUSIVAMENTE a esta disciplina serão apagados." 

3. O usuário confirma a exclusão. 

4. O sistema envia a requisição de exclusão (DELETE) para o _back-end_ . 

5. O banco de dados apaga a disciplina e os dados atrelados a ela, mas mantém o Concurso Pai intacto. 

# 4. PRÉ-CONDIÇÕES 

   - O usuário deve estar autenticado. 

   - O usuário precisa ter ao menos um Concurso cadastrado, pois a Disciplina é uma entidade "filha" e exige o ID do Concurso para existir no banco de dados relacional. 

5. PÓS-CONDIÇÕES 

   - A disciplina é persistida no banco de dados. A partir desse momento, ela passa a aparecer como uma opção válida nos filtros de geração de Questões, criação de Flashcards e estruturação do Cronograma Dinâmico. 

6. PONTOS DE EXTENSÃO 

   - Não se aplica. 

42 

# 7. PROTÓTIPO 

FIGURA 8 - Tela do menu de concurso 



<!-- Start of picture text -->
= Policia Federal 7<br>A\ Banca: CEBRASPE| Tipo: Miltipla Escolha<br>Disciplinas<br>®_Direito Administrativo —_12 Flashcards<br>= Lingua Portuguesa 8 Flashcards<br>Nenhuma outra disciplina encontrada<br>-<br>ft I} ca] :<br>Inicio Simulado _Cronograma Perfil<br><!-- End of picture text -->

Fonte: O autor(2026). 

43 

FIGURA 9 - Tela de cadastro de disciplina 

particularmente, não gosto de entidades com um único campo. 



<!-- Start of picture text -->
Nova Disciplina<br>Nome da Disciplina<br>Cancelar<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF005 - Parametrizar Inteligencia Artificial** 

1. BREVE DESCRIÇÃO 

Permite ao estudante (Concurseiro) configurar as preferências globais de comportamento do motor de IAG. Em vez de permitir o envio de prompts de texto livre (o 

44 

que geraria riscos de "alucinação" da IAG), o sistema disponibiliza perfis predefinidos que calibram o nível de dificuldade das questões geradas, o rigor da correção do módulo OCR de redações e o estilo de resposta do Tutor Interativo. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

3. FLUXO DE EVENTOS 

- 3.1. FLUXO BÁSICO (Configurar Preferências da IA) 

   1. No menu principal ou na tela de "Perfil", o usuário seleciona a opção "Configurações da Inteligência Artificial". 

   2. O sistema recupera a preferência do banco de dados vinculada ao usuário logado e protótipo não está ilustrado ao final. 

   exibe as configurações atuais. 

   3. O usuário altera os parâmetros desejados nos seletores da interface. 

   4. O usuário toca no botão "Salvar Configurações". 

   5. O sistema atualiza os valores na _ViewModel_ e dispara uma requisição (PUT/PATCH) para o _back-end_ . 

   6. O _back-end_ atualiza a preferência no banco de dados e retorna sucesso. 

   7. O sistema exibe a mensagem "Configurações da IA atualizadas com sucesso!" e retorna à tela anterior. 

Quadro 20 - Detalhamento de Campos (Tela de Parametrização da IA) 

|**Campo**||**Valor default**|**Domínio**<br>**de**<br>**valores**|**Tipo de campo**<br>**na tela**|**Obrigatoriedad**<br>**e**|
|---|---|---|---|---|---|
|Nível|de|Médio|Fácil,<br>Médio,|Campo de texto|Sim|
|Dificuldade<br>Questões|das||Difícil|editável||
|Rigor<br>Correção|na<br>da|Padrão|Flexível,<br>Padrão, Rígido|Campo de texto<br>editável|Sim|
|Redação||||||



detalhar, nas regras de negócios, a diferença de cada item. 

45 

|Tom do Tutor|Explicativo|Direto/Objetivo,|Botões|de|Sim|
|---|---|---|---|---|---|
|Interativo||Explicativo/Det|Seleção|(Radio||
|||alhado|Buttons)|||



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Falha de Comunicação ou Instabilidade de Rede** 

   1. A requisição para salvar as configurações no back-end falha devido à indisponibilidade de internet ou instabilidade no servidor em nuvem. 

   2. O sistema intercepta o erro através da _ViewModel_ . 

   3. O sistema exibe um alerta na tela: "Não foi possível salvar suas preferências na nuvem. Verifique sua conexão e tente novamente." 

   4. Os parâmetros selecionados pelo usuário continuam retidos visualmente na interface para que ele não precise reconfigurar tudo. 

4. PRÉ-CONDIÇÕES 

   - O usuário deve estar autenticado no sistema. 

5. PÓS-CONDIÇÕES 

   - As preferências são aplicadas globalmente para a conta do usuário. A partir deste momento, todas as chamadas feitas para gerar simulados e questões ou corrigir redações passarão a incluir a string de configuração pré definida no escopo da requisição ( _prompt_ ) enviada à API do Google Gemini. 

6. PONTOS DE EXTENSÃO 

SEM PROTÓTIPO ? 

- Não se aplica. 

# **RF006 - Manter Flashcard** 

1. BREVE DESCRIÇÃO 

Permite ao estudante criar, editar e excluir cartões de estudo (flashcards) de forma manual. O usuário preenche o Título e a Descrição do cartão, vinculando-o a uma Disciplina específica. 

46 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

3. FLUXO DE EVENTOS 

- 3.1. FLUXO BÁSICO (Criar Novo Flashcard) 

protótipo errado ! 

1. No aplicativo, o usuário acessa a tela de uma "Disciplina" previamente cadastrada. 

2. O usuário aciona o Botão de submit "Novo Flashcard". 

3. O sistema apresenta o modal de criação de cartão, contendo os campos de entrada para Título e Descrição. 

4. O usuário toca no botão "Salvar". 

5. O sistema (via _ViewModel_ ) valida se ambos os campos foram preenchidos corretamente. 

6. O sistema dispara em segundo plano uma requisição (POST) para sincronizar o novo cartão com o _back-end_ . 

7. O sistema exibe a mensagem "Flashcard salvo com sucesso!", fecha o formulário e atualiza a lista de cartões da disciplina. 

8. O caso de uso é encerrado. 

Quadro 21 - Detalhamento de Campos (Tela de Criação/Edição de Flashcard) 

|**Campo**|**Valor default**|**Domínio**|**de**|**Tipo de campo**|**Obrigatoriedad**|
|---|---|---|---|---|---|
|||**valores**||**na tela**|**e**|
|Título|Branco|Texto||Campo de texto<br>editável|Sim|
|Descrição|Branco|Texto||Campo de texto<br>editável|Sim|
|Salvar||||Botões de Ação|Sim|
|Cancelar||||Botão de Link|Sim|



Fonte: O autor(2026). 

47 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Edição do Flashcard** 

1. Na lista de _flashcards_ da disciplina, o usuário seleciona um cartão existente e aciona a opção "Editar". 

2. O sistema abre a interface de formulário preenchida com os dados atuais do Título e Descrição. 

3. O usuário modifica os textos desejados e toca em "Salvar". 

4. O sistema valida os campos e dispara a requisição de atualização (PUT/PATCH) para o _back-end_ . 

5. O sistema exibe "Flashcard atualizado" e reflete as mudanças na tela. 

# **A2 – Campos em Branco (Validação Local)** 

1. O usuário tenta salvar o cartão deixando Título ou Descrição em branco. 

2. O sistema bloqueia o salvamento imediatamente. 

3. O sistema destaca o campo vazio em vermelho e exibe a mensagem: "A frente e o verso do cartão são obrigatórios." 

4. O usuário preenche os campos e prossegue com o salvamento. 

# **A3 – Exclusão do Flashcard** 

1. Na lista de _flashcards_ , o usuário aciona a opção "Excluir" (via ícone de lixeira). 

2. O sistema solicita uma confirmação simples: "Deseja excluir este cartão?". 

3. O usuário confirma. 

4. O sistema remove o registro do banco de dados local, remove visualmente da lista e envia a instrução de exclusão (DELETE) para o _back-end_ . 

# 4. PRÉ-CONDIÇÕES 

   - O usuário deve estar autenticado. 

   - O usuário deve ter navegado até o interior de uma Disciplina para que o sistema saiba a qual categoria pertence o _flashcard_ . 

5. PÓS-CONDIÇÕES 

   - O _flashcard_ é armazenado localmente para permitir o estudo _offline_ e sincronizado com a nuvem. 

48 

# 6. PONTOS DE EXTENSÃO 

- Não se aplica. 

# 7. PROTÓTIPO 

FIGURA 10 - Tela de perfil 

protótipo no requisito errado 



<!-- Start of picture text -->
Meu Perfil<br>Joao Vitor<br>joao.vitor@email.com<br>4 Configuragdes da lA ><br>ft 3} ca 4<br>Inicio Simulado _Cronograma Perfil<br><!-- End of picture text -->

Essa ação está descrita em qual Requisito Funcional ? 

Fonte: O autor(2026). 

49 

FIGURA 11 - Tela de Configurações da IAG 

Protótipo no requisito errado. 



<!-- Start of picture text -->
€ _ Parametros da IA<br>|— NivelMédio de Dificuldade — ’ |<br>Padrao ’<br>|-— Rigor da Redago<br>TOM DO TUTOR INTERATIVO<br>© Direto/Objetivo<br>© xplicativo/Detalhado<br>@ Essas configuragdes afetam globalmente seus<br>simulados e corregdes.<br>Salvar Configuragées<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF007 - Manter Flashcards via Documento (PDF)** 

# 1. BREVE DESCRIÇÃO 

Permite que o estudante automatize a criação de materiais de estudo enviando um arquivo PDF (ex: apostilas, resumos, leis). O sistema extrai o conteúdo textual do arquivo, empacota-o em um prompt estruturado (junto com a quantidade desejada de cartões) e envia à 

50 

IAG para gerar um lote de flashcards focados nos pontos mais relevantes do documento. Os _flashcards_ gerados são salvos automaticamente na Disciplina selecionada. 

2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

   - API Google Gemini (Sistema Externo) 

3. FLUXO DE EVENTOS 

sempre iniciar na tela de listagem onde carrega através dos filtros, as disciplinas cadastradas. 

- 3.1. FLUXO BÁSICO (Gerar Flashcards via IA) 

   1. Na tela de uma "Disciplina", o usuário aciona a opção "Gerar Flashcard”. 

   2. O sistema exibe o formulário de "Geração Automatizada". 

   3. O usuário seleciona a "Quantidade de Flashcards" desejada 5, 10, 15, 20 ou x (sendo demonstra duas imagens contendo os protótipos. 

   este um campo numérico). entretanto a Figura 13, não há explicação no texto. 

   4. O usuário toca no botão "Selecionar Arquivo PDF". 

   5. O usuário seleciona o documento desejado. 

   6. O sistema valida o arquivo localmente (extensão e tamanho limite de 10MB) e exibe o nome do arquivo selecionado na tela. 

   7. O usuário toca em "Gerar Flashcards". 

   8. O sistema exibe uma tela de carregamento dinâmico e inicia as seguintes tarefas em _background_ : 

      - a. O Módulo Extrator analisa o arquivo PDF localmente e extrai todo o texto. 

      - b. A _ViewModel_ monta o payload, unindo o texto extraído, a quantidade solicitada e as regras de formatação. 

      - c. A requisição é enviada à API do Google Gemini. 

   9. O Gemini retorna um array JSON estruturado com os pares "Título" e "Descrição". 

   10. O sistema processa o retorno, salva os novos flashcards no banco de dados local vinculados à Disciplina atual e dispara a sincronização com o banco de dados. 

   11. O sistema oculta o carregamento, exibe a mensagem "X flashcards gerados com sucesso!" e atualiza a lista visual na tela da disciplina. 

Quadro 22 - Detalhamento de Campos (Tela de Geração via IA) 

|**Campo**|**Valor default**|**Domínio**|**de**|**Tipo de campo**|**Obrigatoriedad**|
|---|---|---|---|---|---|
|||**valores**||**na tela**|**e**|



51 

|Quantidade|10|5, 10, 15, 20, x|Lista<br>suspensa<br>ou Campo de<br>texto editável|Sim|
|---|---|---|---|---|
|Arquivo base|Branco|.pdf<br>(Max|Botão<br>de|Sim|
|||10MB)|Seleção<br>de||
||||Arquivo||
|Gerar|||Botão de submit|Sim|
|Flashcards|||||



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Arquivo PDF Inválido ou Muito Grande** 

1. A validação do sistema identifica que o arquivo excede o limite de 10MB ou não é um PDF. 

2. O sistema recusa o anexo e exibe uma mensagem de erro: "O arquivo deve ser um PDF de até 10MB. Tente dividir o documento ou escolher um arquivo menor." 

3. O botão "Gerar Flashcards" permanece desabilitado. 

# **A2 – Arquivo PDF sem Texto (PDF em Formato de Imagem)** 

1. O Módulo Extrator analisa o PDF, mas não encontra texto selecionável. 

2. A _ViewModel_ interrompe o processo antes de consumir a cota da API do Gemini. 

3. O sistema remove a tela de carregamento e exibe um Dialog de aviso: "Não foi possível ler o texto deste PDF. Certifique-se de que o documento não seja apenas uma imagem escaneada." 

# **A3 – Falha de Retorno ou Timeout da IA** 

1. A API do Google Gemini demora mais que o limite estabelecido ou retorna um erro de processamento/indisponibilidade de serviço. 

2. O sistema intercepta o erro, encerra o carregamento e exibe a mensagem: "A Criação automática demorou a responder ou o texto é muito complexo. Tente novamente em instantes." 

3. O sistema não grava nenhum dado fragmentado no banco. 

52 

4. PRÉ-CONDIÇÕES 

   - O usuário deve estar autenticado e com conexão estável à internet. 

   - O usuário deve ter concedido permissão de leitura de arquivos ao aplicativo. 

   - O arquivo PDF deve conter texto "copiável" (searchable text), não apenas imagens rasterizadas. 

5. PÓS-CONDIÇÕES 

   - Um lote de novos _flashcards_ estruturados com Título e Descrição é gerado e salvo na base de dados, estando imediatamente disponível para as sessões de revisão ativa do aluno. 

6. PONTOS DE EXTENSÃO 

   - Não se aplica. 

7. PROTÓTIPO 

53 

FIGURA 12 - Tela para criar flashcards via PDF 



<!-- Start of picture text -->
€  Geragao Inteligente<br>te) ArquivoEdital_PF_2024.pdf Base<br>Quantidade de cartées<br>[to<br>#° Iniciar Geragao<br>Os cartées gerados aparecerdo aqui para<br>sua revisdo antes de salvar.<br>Cancelar<br><!-- End of picture text -->

Fonte: O autor(2026). 

54 

FIGURA 13 - Tela para criar novo flashcard manual 



<!-- Start of picture text -->
€  Criar Flashcard<br>® Disciplina: Direito Administrativo<br>| Titulo (Frente) |<br>Cancelar<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF008 - Manter Questões via Inteligência Artificial** 

1. BREVE DESCRIÇÃO 

Permite ao estudante gerar processualmente lotes de questões para treino. O sistema atua como um orquestrador cognitivo: ele captura o contexto do usuário (Banca Examinadora 

55 

do Concurso, Nível de Dificuldade nas Preferências e a Disciplina atual), empacota essas diretrizes em um prompt estruturado no código-fonte (oculto ao usuário) e requisita à API do Google Gemini a criação de um JSON contendo o enunciado, as alternativas, a resposta correta e a justificativa instrucional para cada questão gerada. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

   - API Google Gemini (Sistema Externo) 

3. FLUXO DE EVENTOS 

obrigatório iniciar com a tela de listagem de disciplinas e após a escolha, carregar a tela da Disciplina. 

- 3.1. FLUXO BÁSICO (Gerar Lote de Questões) 

   1. Na tela de uma "Disciplina", o usuário aciona a opção "Gerar Questões". 

   2. O sistema exibe o formulário de parametrização do lote. 

   3. O usuário seleciona a quantidade de questões desejada e, opcionalmente, digita um tema específico. 

   4. A ViewModel executa a Engenharia de Prompt, concatenando: 

Documentos de especificação de Requisitos, na sua pureza, não devem e nem podem, trazer soluções tecnológicas. 

a. Regra da Banca e Tipo de Questão (Múltipla Escolha ou Certo/Errado). 

   - b. Nível de Dificuldade e Rigor. 

   - c. Tema solicitado (se houver) e o formato obrigatório de saída (JSON). 

5. A requisição HTTP é disparada para a API do Google Gemini. 

6. A API processa a solicitação e retorna uma matriz de objetos JSON com as questões. 

7. O sistema desserializa o JSON, valida a integridade dos dados e salva o lote de questões no banco de dados atrelado à Disciplina. 

8. O sistema encerra o carregamento, exibe a mensagem "Lote de [X] questões gerado com sucesso!" e direciona o usuário imediatamente para a tela de Resolução (Simulado). 

Quadro 23 - Detalhamento de Campos (Tela de Geração de Questões) 

|**Campo**|**Valor default**|**Domínio**|**de**|**Tipo de campo**|**Obrigatoriedad**|
|---|---|---|---|---|---|
|||**valores**||**na tela**|**e**|
|Tema Específico|Branco|Texto||Campo de texto<br>editável|Não|



56 

|Quantidade|5<br>5, 10, 15, 20|Lista Suspensa|Sim|
|---|---|---|---|
|Gerar Questões||Botão de submit|Sim|



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Quebra de Contrato JSON (Falha de Formatação da IA)** 

1. A LLM sofre uma "alucinação" e retorna a resposta em texto plano ou com a estrutura JSON corrompida. 

2. A _ViewModel_ intercepta o erro durante a desserialização nativa. 

3. O sistema aborta o salvamento para evitar dados corrompidos no banco. 

4. O sistema exibe a mensagem: "Não foi possível estruturar as questões corretamente. Por favor, tente novamente." 

5. O usuário retorna à tela para nova tentativa. 

# **A2 – Bloqueio por Filtro de Segurança (Safety Settings)** 

1. O tema solicitado pelo usuário envolve termos sensíveis que a LLM não possui acesso. 

2. O Google Gemini recusa a geração e retorna um código de bloqueio de segurança. 

3. O sistema intercepta a recusa da API. 

4. O sistema encerra o carregamento e exibe um _Dialog_ de aviso: "O tema solicitado foi bloqueado pelas políticas de segurança da IA do Google." 

# **A3 – Timeout da API** 

   1. A conexão com a API do Gemini demora mais do que o tempo estipulado. 

   2. O sistema cancela a requisição para não travar o dispositivo. 

4. PRÉ-CONDIÇÕES 

   - O usuário deve estar logado. 

   - O usuário deve possuir uma Pasta de Concurso configurada com a respectiva Banca Examinadora. 

5. PÓS-CONDIÇÕES 

57 

   - O banco de dados passa a contar com novas instâncias da entidade Questão, permitindo que o aluno inicie a resolução. 

6. PONTOS DE EXTENSÃO 

   - Após gerar o lote, o usuário resolve as questões, gerando métricas de acertos e erros que acionam a Visualização de Desempenho. 

   - Se, ao resolver uma questão, o aluno não entender a "justificativa" gerada, o sistema aciona o ponto de extensão para o Tutor Interativo. 

7. PROTÓTIPO 

58 

FIGURA 14 - Tela de geração de questões 



<!-- Start of picture text -->
€_ Gerar Questdes<br>Fe} ConfiguracgaoNivel: Médio Ativa: Banca CEBRASPE |<br>TEMA ESPECIFICO (OPCIONAL)<br>| Ex: Atos Administrativos, Licitagées |<br>QUANTIDADE DE QUESTOES<br>°<br>|<br>+Gerar Questées com IA<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF009 - Manter Prova Edital (PDF)** 

1. BREVE DESCRIÇÃO 

59 

Permite ao estudante gerar uma Prova completa, estruturada de forma proporcional e fidedigna ao exame real. O sistema utiliza o documento PDF do edital para extrair, via mineração de texto, a grade de disciplinas e o peso/quantidade de questões de cada matéria. Com esses dados, o sistema aciona a IAG para gerar um lote de questões que respeitam rigorosamente a distribuição exigida pela banca examinadora. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

   - API Google Gemini (Sistema Externo) 

3. FLUXO DE EVENTOS 

- 3.1. FLUXO BÁSICO (Gerar Prova Estruturada) 

   1. Na tela de um "Concurso", o usuário aciona a opção "Gerar Prova via Edital". 

   2. O sistema verifica que já existe um PDF do edital vinculado àquela pasta de Concurso. 

   3. O sistema exibe um indicador de carregamento dinâmico e executa as tarefas em _background_ : 

      - a. O Módulo Extrator lê o PDF e identifica a seção "Conteúdo Programático" e "Quadro de Provas/Pesos". 

      - b. A _ViewModel_ calcula a proporção matemática para formular questões. 

      - c. O sistema injeta as regras da banca, o nível de dificuldade e a proporção de matérias em um prompt estruturado e envia para o Google Gemini. 

   4. O _back-end_ retorna o JSON estruturado contendo toda a Prova. 

   5. O sistema salva no banco de dados. 

   6. O sistema exibe a mensagem "Prova gerada com sucesso! Estrutura baseada no edital." e libera o botão para iniciar a resolução. 

Quadro 24 - Detalhamento de Campos (Tela de Geração de Prova) 

|**Campo**|**Valor default**|**Domínio**<br>**valores**|**de**|**Tipo de campo**<br>**na tela**|**Obrigatoriedade**|
|---|---|---|---|---|---|
|Edital Base|[PDF|||Texto|Sim|
||vinculado<br>ao<br>Concurso]|||||



60 

|Gerar<br>Prova|Botão_Submit_|Sim|
|---|---|---|
|Completa|||



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Concurso sem Edital Vinculado** 

1. O sistema detecta que o usuário não fez o _upload_ do edital quando criou o Concurso. 

2. O sistema exibe o botão "Anexar Edital (PDF)" na tela de configuração da prova. 

3. O usuário aciona o botão, seleciona o arquivo no armazenamento do dispositivo, e o sistema vincula o PDF à pasta. 

4. O sistema atualiza a tela e o fluxo retorna ao fluxo básico. 

# **A2 – Edital sem Quadro de Pesos Claros (IA Flexível)** 

1. O módulo extrator e a IAG não conseguem localizar uma tabela exata de pesos e proporções no PDF. 

2. A IAG identifica apenas as disciplinas exigidas, mas não a proporção entre elas. 

3. O sistema aplica uma "Distribuição Igualitária". 

4. O sistema gera a prova e, ao finalizar, exibe um alerta informativo: "O quadro de pesos não foi identificado claramente no edital. As questões foram distribuídas igualmente entre as matérias do conteúdo programático." 

# **A3 – Documento Excede a Janela de Contexto (** **_Tokens_ )** 

   1. O sistema identifica que o edital é extremamente longo e excede o limite de _tokens_ suportado em uma única requisição do Gemini. 

   2. A _ViewModel_ intercepta essa limitação antes de gastar rede. 

   3. O sistema aplica uma regra de "Poda Heurística" (descarta as páginas iniciais de disposições gerais e anexos finais, enviando apenas as páginas que contêm as palavras-chave "Conteúdo Programático" e "Quadro de Provas"). 

   4. A requisição é feita com o texto otimizado e o fluxo retorna ao fluxo básico. 

4. PRÉ-CONDIÇÕES 

   - O usuário deve ter criado o concurso e as preferências da IAG devem estar parametrizadas. 

61 

   - Conexão ativa com a internet para requisição ao Gemini. 

5. PÓS-CONDIÇÕES 

   - Uma entidade de "Prova" é criada no banco de dados, contendo a lista completa de questões divididas por disciplinas, prontas para serem respondidas no ambiente de avaliação do aplicativo. 

6. PONTOS DE EXTENSÃO 

   - Após a finalização da resolução da prova, o sistema aciona o caso de uso de Visualizar Desempenho com o aproveitamento do aluno. 

7. PROTÓTIPO 

62 

FIGURA 15 - Tela de geração de prova via IAG 



<!-- Start of picture text -->
onde eu carrego um PDF, na<br>primeira vez???<br><!-- End of picture text -->



<!-- Start of picture text -->
€ Prova via Edital<br>‘a Edital_PF_2024.pdfv Analisado com sucesso<br>@ A Inteligéncia Artificial extraira o<br>contetido programatico e o quadro de<br>pesos deste documento para gerar uma<br>Prova proporcional e auténtica.<br>4 Gerar Prova Completa<br><!-- End of picture text -->

Fonte: O autor(2026). 

63 

FIGURA 16 - Tela de resolução de prova 



<!-- Start of picture text -->
X — Simulado - Policia Federal © 03:59:42<br>DIREITO PENAL Questo 1 de 120<br>De acordo com o Codigo Penal Brasileiro e a<br>jurisprudéncia dominante, no que tange aos<br>crimes contra a Administragao Publica,<br>assinale a alternativa correta:<br>A Opeculato culposo admite a<br>reparacado do dano como causa de<br>extingdo da punibilidade, caso ocorra<br>antes da sentenga irrecorrivel.<br>© Ocrime de concussao caracteriza-se<br>pela exigéncia, para si ou para outrem,<br>direta ou indiretamente, de vantagem<br>indevida, em razdo da fungao.<br># Nao entendeu? Explicar com Tutor IA<br>Confirmar e Avangar<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF010 - Visualizar Desempenho e Progresso** 

1. BREVE DESCRIÇÃO 

Permite ao estudante acompanhar a sua evolução de aprendizado através de um painel analítico interativo. O sistema compila o histórico de resoluções de questões avulsas e provas 

64 

(geradas previamente pela IAG) e exibe métricas detalhadas, incluindo barras de progresso, volume total de questões respondidas e a taxa exata de Acertos e Erros (em percentual e números absolutos), filtrados por disciplina ou por provas realizadas. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

3. FLUXO DE EVENTOS 

- 3.1. FLUXO BÁSICO (Visualizar Painel de Métricas) 

   1. No menu de navegação inferior do aplicativo, o usuário seleciona a aba "Desempenho". 

   2. O sistema roda em segundo plano uma rotina para sincronizar os dados locais com o _back-end_ , garantindo que o painel exiba as informações mais recentes. 

   3. A _ViewModel_ realiza o cálculo estatístico das métricas: 

      - a. Soma do total de questões respondidas. 

      - b. Cálculo percentual de Acertos vs. Erros geral. 

      - c. Agrupamento de acertos e erros segmentados por Disciplina e por Prova. 

   4. O usuário interage com os filtros de tempo (ex: "Últimos 7 dias", "Mês Atual", "Geral") ou seleciona uma disciplina específica para detalhar os dados. 

   5. O sistema atualiza os gráficos instantaneamente com base nos filtros selecionados. 

Quadro 25 - Detalhamento de Filtros e Elementos Visuais (Painel de Desempenho) 

|**Campo**|**Valor default**|**Domínio**<br>**de**<br>**valores**|**Tipo de campo**<br>**na tela**|**Obrigatoriedade**|
|---|---|---|---|---|
|Período<br>de<br>Avaliação|Visão Geral|7 dias, 30 dias,<br>Visão Geral|Filtro<br>em<br>formato<br>de<br>Botões|Sim|
|Filtro<br>por|Todas|[Lista<br>de|Lista Suspensa|Não|
|Disciplina||disciplinas<br>cadastradas]|||
|Rendimento<br>Global|0%|0 a 100%|Componente<br>Visual|Sim|



65 

|Desempenho|0/0|Barras|de|Lista de Barras|Sim|
|---|---|---|---|---|---|
|por Matéria||progresso<br>%|com|||



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Ausência de Dados (Estado Vazio / Empty State)** 

1. O sistema identifica que o usuário recém-criou a conta e não possui nenhum histórico de questões respondidas. 

2. O sistema não renderiza gráficos com valor "zero". 

3. Em vez dos gráficos, o sistema exibe uma ilustração (Empty State) e a mensagem: "Você ainda não respondeu nenhuma questão.". 

4. O sistema exibe um botão de atalho primário direcionando o usuário para a tela de concursos. 

# **A2 – Modo Offline (Sem conexão com o** **_Back-end_ )** 

   1. O dispositivo não consegue se conectar à internet para checar atualizações com o _back-end_ . 

   2. O sistema não interrompe a exibição da tela. Ele utiliza os dados cacheados na última sessão do banco de dados local. 

   3. O sistema exibe um pequeno aviso no topo da tela (ex: ícone de nuvem cortada): "Exibindo dados armazenados offline. Conecte-se à internet para sincronizar." 

   4. O usuário consegue visualizar os gráficos normalmente com base nos dados locais. 

4. PRÉ-CONDIÇÕES 

   - O usuário deve estar autenticado no sistema. 

   - É altamente recomendável que o usuário já tenha respondido a Questões ou finalizado uma Prova para que os gráficos tenham insumos para exibição. 

5. PÓS-CONDIÇÕES 

66 

   - O usuário adquire consciência de suas fraquezas e fortalezas acadêmicas. Os dados agregados nesta tela também servem de insumo algorítmico interno para a IAG, que poderá utilizá-los no futuro para criar cronogramas dinâmicos mais precisos focados nas matérias com menor rendimento. 

6. PONTOS DE EXTENSÃO 

   - Não se aplica. 

7. PROTÓTIPO 

67 

FIGURA 17 - Tela de desempenho 

São ações que geram fluxo alternativo. 



<!-- Start of picture text -->
Desempenho :<br>CAr| Vis&o Geral<br>(ee Spon<br>Todas as Disciplinas ’<br>q 75% )<br>300 Acertos | 100 Erros<br>Desempenho por Materia<br>Direito Administrativo 80%<br>—————<br>Direito Constitucional 70%<br>———eeee<br>Portugués 65%<br>————<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF011 - Avaliar Redação via OCR** 

1. BREVE DESCRIÇÃO 

68 

Permite ao estudante submeter redações para avaliação automatizada. O sistema oferece duas vias de entrada: a digitação em um campo de texto livre ou o envio de uma fotografia da folha manuscrita. No segundo caso, o sistema aciona um módulo interno de Reconhecimento Óptico de Caracteres (OCR) para extrair o texto da imagem. Após a confirmação do texto pelo usuário, os dados são processados pela IAG, que retorna uma nota estruturada e um _feedback_ formativo detalhado com base no rigor previamente parametrizado. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

   - API do Google Gemini (Sistema Externo) 

3. FLUXO DE EVENTOS 

- 3.1. FLUXO BÁSICO (Submissão de Redação Manuscrita via Foto) 

   1. No menu principal do aplicativo, o usuário acessa o módulo de "Redações" e aciona o botão "Nova Redação". 

   2. O sistema exibe as opções de submissão:"Escanear Folha Manuscrita" ou “Digitar Redação”. 

   3. O usuário seleciona "Escanear Folha Manuscrita". 

   4. O sistema abre a interface de câmera nativa do dispositivo móvel (ou permite selecionar da galeria). 

   5. O usuário captura a fotografia da folha de redação. 

   6. O sistema exibe uma tela de processamento e aciona o Módulo OCR, que mapeia os pixels da imagem e extrai os caracteres manuscritos em formato de texto. 

   7. O sistema apresenta o texto extraído em um campo de texto editável para que o usuário possa realizar a "Revisão Humana" (corrigir palavras que possa ter interpretado com pouca clareza devido à caligrafia). 

   8. O usuário confere o texto e toca no botão "Avaliar Redação". 

   9. A _ViewModel_ empacota o texto revisado juntamente com as regras de rigor de correção definidas nas preferências. 

   10. A requisição é disparada para a IAG, solicitando uma avaliação baseada em competências. 

   11. A IAG retorna um JSON contendo a Nota Final e os parágrafos de _feedback_ corretivo. 

   12. O sistema salva a avaliação no banco de dados local e sincroniza as informações com o _back-end_ em segundo plano. 

69 

13. O sistema exibe o "Painel de Resultados", detalhando a nota e as sugestões de melhoria. 

Quadro 26 - Detalhamento de Campos (Tela de Revisão e Submissão) 

|**Campo**|**Valor default**|**Domínio**<br>**de**<br>**valores**|**Tipo de campo**<br>**na tela**|**Obrigatoriedade**|
|---|---|---|---|---|
|Tema|Branco|Texto livre|Campo de texto<br>editável|Sim|
|Texto Extraído<br>(OCR)|[Texto<br>lido<br>pela câmera]|Texto livre|Campo de texto<br>expansível|Sim|
|Texto Digitado|Branco|Texto livre|Campo de texto<br>expansível<br>e<br>editável|Não|
|Avaliar<br>Redação|||Botão_Submit_|Sim|



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Submissão via Digitação Direta** 

1. O usuário seleciona a opção "Digitar Redação". 

2. O sistema pula as etapas de acionamento de câmera e OCR e exibe diretamente a tela com um campo de texto em branco. 

3. O usuário digita a sua redação manualmente pelo teclado do dispositivo móvel. 

4. O usuário toca em "Avaliar Redação". 

5. O fluxo retorna ao fluxo básico. 

# **A2 – Falha na Extração OCR (Imagem Ilegível ou Borrada)** 

70 

1. No passo 6 do fluxo básico, o Módulo OCR analisa a imagem, mas não atinge o nível mínimo de confiança para extrair sentenças lógicas. 

2. O sistema aborta o preenchimento automático. 

3. O sistema exibe um alerta de orientação: "Não foi possível ler o texto com clareza. Certifique-se de que a foto está bem iluminada e que a folha não está amassada." 

4. O fluxo retorna a solicitação de uma nova captura de imagem. 

# **A3 – Validação Local de Texto Insuficiente (Economia de Recursos)** 

1. O usuário tenta submeter o texto para avaliação. 

2. A _ViewModel_ realiza uma validação heurística local. 

3. O sistema bloqueia a chamada de rede para a IAG. 

4. O sistema exibe a mensagem de alerta: "Texto muito curto. Uma redação exige um mínimo de desenvolvimento para ser avaliada." 

5. O usuário complementa o texto. 

# 4. PRÉ-CONDIÇÕES 

- O usuário deve estar autenticado. 

- O aplicativo deve possuir as permissões de acesso à Câmera do dispositivo. 

- As "Configurações da Inteligência Artificial" devem estar parametrizadas para balizar o nível de rigor que a IAG utilizará na correção. 

- Conexão ativa com a internet. 

# 5. PÓS-CONDIÇÕES 

   - Uma nova entrada de Redação é salva no banco de dados, contendo o texto bruto submetido, a nota quantitativa final e o _feedback_ qualitativo, permitindo a futura consulta de histórico pelo aluno. 

6. PONTOS DE EXTENSÃO 

   - Caso o estudante não compreenda algum apontamento gramatical feito pela avaliação da IAG, ele poderá acionar a funcionalidade de Tutoria Interativa diretamente da tela de _feedback_ para tirar dúvidas sobre a correção. 

   - O registro da nota gera insumos que atualizam automaticamente o Painel de Desempenho. 

7. PROTÓTIPOS 

71 

FIGURA 18 - Tela de captura OCR 

demonstrar o protótipo antes da imagem carregada. 



<!-- Start of picture text -->
€ Avaliar Redagao<br>B Imagem capturada C Refazer<br>Os desafios da seguranga publica no Bras<br>|— Tema da Redag3o<br>Texto Extraido<br>A seguranga publica no Brasil enfrenta<br>disafios que perpassam desde a falta de<br>recurssos até a nessecidade de<br>integracgao entre as policias. Além disso,<br>a tecnologia surge como uma grande<br>aliada no combate a criminalidade, mas<br>seu uso ainda esbarra em questoes<br>orgamentarias e estruturais.<br>@ 0 texto acima foi lido via OCR. Por favor, revise<br>antes de enviar.<br>Avaliar Redacao via IA<br><!-- End of picture text -->

Fonte: O autor(2026). 

72 

FIGURA 19 - Tela de avaliação de redação 



<!-- Start of picture text -->
x Avaliagdo da lA<br>Nivel de Rigor: Padraéo<br>Competéncias<br>Gramatica e Ortografia 80%<br>——EEE<br>Estrutura e Coesdo 90%<br>—EEEEEEE<br>Argumentagao 85%<br>—— EEE<br>+, Feedback Detalhado<br>| ua introduc¢do esté muito clara, porém<br>i<br>( @ Diivida na correcao? Falar com Tutor IA )<br>Nn A<br><!-- End of picture text -->

ação que gera fluxo alternativo 

Fonte: O autor(2026). 

# **RF012 - Consultar Histórico de Redações** 

1. BREVE DESCRIÇÃO 

Permite ao estudante (Concurseiro) visualizar uma listagem cronológica de todas as redações submetidas e avaliadas pelo sistema. A interface apresenta um resumo de cada 

73 

submissão (tema, data e nota final) e permite que o usuário acesse os detalhes completos da correção, possibilitando a revisão contínua do _feedback_ gerado pela IAG. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

3. FLUXO DE EVENTOS 

- 3.1. FLUXO BÁSICO (Visualizar Lista e Detalhes da Redação) 

   1. No menu principal do aplicativo, o usuário acessa o módulo de "Redações". 

   2. O sistema consulta o banco de dados para recuperar a lista de redações já avaliadas. 

   3. O sistema exibe a "Lista de Redações", ordenada da mais recente para a mais antiga. 

   4. Cada item da lista (Card) apresenta as seguintes informações resumidas: Tema, Data e Hora da submissão, e a Nota Final obtida. fluxo básico finaliza aqui. qualquer ação, gera um fluxo alternativo 

   5. O usuário toca em um item específico da lista. 

   6. O sistema transita para a tela de "Detalhes da Avaliação", exibindo o texto original submetido e o _feedback_ completo gerado pela IAG (notas por competência, sugestões de melhoria e correção gramatical). 

   7. O usuário analisa o _feedback_ e pode retornar à lista. 

Quadro 27 - Detalhamento de Campos (Tela de Histórico e Detalhes) 

|**Campo**|**Valor default**|**Domínio**<br>**de**<br>**valores**|**Tipo de campo**<br>**na tela**|**Obrigatoriedade**|
|---|---|---|---|---|
|Lista<br>de|Vazia|Registros<br>do|Componente de|Sim|
|Redações||banco de dados|Lista||
|Card<br>de||Título,<br>Data|Componente|Sim|
|Resumo||(DD/MM/AAA<br>A) e Nota|Visual||
|Texto Original||Texto<br>submetido|Bloco de Texto|Sim|



74 

|_Feedback_<br>da|Análise|Bloco de Texto  Sim|
|---|---|---|
|IAG|estruturada||



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Histórico Vazio** 

1. O sistema identifica que não existem redações avaliadas vinculadas à conta do usuário. 

2. O sistema exibe um estado vazio com uma ilustração e a mensagem: "Você ainda não submeteu nenhuma redação.". 

3. O sistema exibe um Botão de _submit_ primária "Nova Redação" que, se acionado, direciona o usuário para submeter sua redação. 

# **A2 – Busca/Filtro no Histórico** 

   1. O usuário possui dezenas de redações e deseja encontrar uma específica. 

   2. O usuário utiliza a barra de busca localizada no topo da tela. 

   3. O usuário digita uma palavra-chave presente no Título ou no Tema. 

   4. O sistema filtra a lista em tempo real, exibindo apenas os resultados correspondentes. 

4. PRÉ-CONDIÇÕES 

   - O usuário deve estar autenticado no aplicativo. 

   - Para que o fluxo básico seja completo, o usuário deve ter submetido e avaliado pelo menos uma redação. 

5. PÓS-CONDIÇÕES 

   - O usuário consegue reavaliar seus erros passados, consolidando o aprendizado. Nenhuma alteração de dados ocorre neste fluxo, é uma operação de leitura/consulta. 

6. PONTOS DE EXTENSÃO 

   - Na tela de detalhes, o sistema pode oferecer um botão para acionar o Tutor Interativo, caso o aluno tenha dúvidas sobre algum ponto específico do _feedback_ exibido. 

7. PROTÓTIPO 

75 

FIGURA 20 - Tela de histórico de redações 

protótipo de "Detalhes da Avaliação" 



<!-- Start of picture text -->
€ Minhas Redagées<br>Q_ Pesquisar tema ou data<br>Os impactos da lA na<br>seguranca publica 880/1000<br>14/05/2024 - 14:20<br>A importancia da preservagéo<br>do meio ambiente 920/1000<br>10/05/2024 - 09:15<br>Desafios da educacao a<br>distancia no Brasil 750/1000<br>02/05/2024 - 18:45<br>Caminhos para combater a<br>intolerancia religiosa 960/1000<br>28/04/2024 - 11:30<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF013 - Manter Cronograma Dinâmico** 

1. BREVE DESCRIÇÃO 

Permite ao estudante gerar um planejamento de estudos automatizado e distribuído em um calendário interativo. A geração é impulsionada pela IAG, que recebe parâmetros de 

76 

disponibilidade do usuário (Data da prova, tempo de estudo diário, limite de matérias por dia e dias da semana disponíveis) e distribui o conteúdo programático do edital de forma otimizada, criando sessões de estudo equilibradas até o dia do exame. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

   - IAG (Sistema Externo) 

3. FLUXO DE EVENTOS 

obrigatoriamente, deve iniciar com a tela de listagem dos cronogramas cadastrados 

- 3.1. FLUXO BÁSICO (Gerar Novo Cronograma) 

   1. No menu de navegação, o usuário seleciona o módulo de "Cronograma". 

   2. O sistema exibe o formulário de parametrização. 

   3. O usuário seleciona a Pasta de Conteúdo (Concurso) base para o planejamento. 

   4. O usuário preenche os parâmetros de disponibilidade: Data da Prova, Minutos diários de estudo, Máximo de matérias por dia, e assinala quais dias da semana tem disponíveis para estudar. 

   5. O usuário aciona o botão "Gerar Cronograma Dinâmico". 

   6. A _ViewModel_ valida os dados inseridos. 

   7. O sistema exibe uma tela de carregamento e empacota as disciplinas da pasta e as restrições de tempo em um _prompt_ estruturado. 

   8. A requisição é enviada à IAG, que calcula a distribuição proporcional das matérias, criando sessões de estudo lógicas. 

   9. O sistema recebe a resposta, processa a matriz de sessões e grava no banco de dados. 

   10. O sistema sincroniza os dados recém-criados com o _back-end_ . 

   11. O sistema encerra o carregamento e renderiza um calendário interativo na tela, exibindo os blocos de estudo distribuídos dia a dia. 

Quadro 28 - Detalhamento de Campos (Tela de Parametrização do Cronograma) 

|**Campo**|**Valor default**|**Domínio**<br>**valores**|**de**|**Tipo**<br>**de**<br>**campo na tela**|**Obrigatoriedade**|
|---|---|---|---|---|---|
|Concurso|Selecione…|Pastas|de|Lista Suspensa|Sim|
|||concursos<br>criados||||



77 

|Data da prova|Branco|Formato<br>DD/MM/AAA<br>A|Seletor de|Data|Sim|
|---|---|---|---|---|---|
|Tempo Diário|120|30<br>a<br>720<br>(Minutos)|Campo<br>Numérico||Sim|
|Máx. Matérias|2|1 a 5|Campo||Sim|
|por dia|||Numérico|||
|Dias|Todos|Seg, Ter, Qua,|Caixas|de|Sim|
|disponíveis||Qui, Sex, Sáb,<br>Dom|Seleção|||
|Gerar|||Botão|de|Sim|
|Cronograma|||Submit|||



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Validação de Data Inválida** 

1. A _ViewModel_ identifica que a "Data da Prova" informada é igual à data atual do sistema ou encontra-se no passado. 

2. O sistema bloqueia a requisição antes de acionar a IAG, poupando processamento de rede. 

3. O sistema destaca o campo de data em vermelho e exibe a mensagem de erro: "A data da prova deve ser uma data futura válida." 

4. O usuário corrige a data e tenta novamente. 

# **A2 – Recalcular/Ajustar Cronograma Existente** 

1. Na tela do calendário interativo, o usuário percebe que atrasou as matérias da semana ou sua rotina mudou, e aciona o botão "Recalcular Cronograma". 

78 

2. O sistema abre o formulário de parâmetros (já preenchido com os valores anteriores). 

3. O usuário altera os dias disponíveis ou o tempo diário e aciona "Recalcular". 

4. O sistema limpa as sessões de estudo futuras no banco de dados. 

5. O sistema aciona a IAG para redistribuir o conteúdo faltante a partir da data atual até a data da prova. 

6. O calendário é atualizado. 

# **A3 – Tempo Restante Insuficiente** 

   1. No passo 8 do fluxo básico, a IAG analisa que o volume de disciplinas é muito alto para o tempo escasso (ex: faltam 2 dias para a prova e o usuário colocou apenas 30 minutos diários disponíveis). 

   2. O sistema recebe um alerta da inteligência artificial de impossibilidade lógica de cobertura total do edital. 

   3. O sistema exibe um aviso: "O tempo selecionado é insuficiente para cobrir todas as matérias até a data da prova. O cronograma foi montado focando apenas nos tópicos de maior peso." 

   4. O sistema renderiza o calendário com a versão resumida dos estudos. 

4. PRÉ-CONDIÇÕES 

   - O usuário deve estar autenticado. 

   - É obrigatória a existência de pelo menos uma pasta de Concurso (RF003) devidamente estruturada com suas respectivas disciplinas, para que a IAG saiba o que deve ser distribuído no tempo. 

5. PÓS-CONDIÇÕES 

   - A tabela de "Sessões de Cronograma" no banco de dados é populada. O usuário passa a receber indicativos visuais diários sobre o que deve estudar naquela data específica, mitigando a sobrecarga cognitiva da curadoria de material. 

6. PONTOS DE EXTENSÃO 

   - Não se aplica. 

7. PROTÓTIPO 

79 

FIGURA 21 - Tela de geração de cronograma 

protótipo para ver os cronogramas cadastrados. 



<!-- Start of picture text -->
€ Novo Cronograma<br>— Concurso Base ———<br>Ex: Policia Federal ° |<br>— Data da Prova ———<br>15/12/2026 a] |<br>Tempo Didrio (min) Max. Matérias/dia<br>120 | 2 |<br>Dias disponiveis para estudo<br>sab )( Dom )<br>+ Gerar Cronograma Dinamico<br><!-- End of picture text -->

Fonte: O autor(2026). 

# **RF014 - Interagir Tutoria Interativa** 

1. BREVE DESCRIÇÃO 

80 

Permite ao estudante iniciar uma sessão de chat contextual com a IAG atuando sob a persona de um tutor particular (1:1). Esta funcionalidade é acionada a partir da tela de correção de uma questão ou do _feedback_ de uma redação. O sistema injeta automaticamente o contexto do erro no prompt, permitindo que o aluno faça perguntas abertas (ex: "Por que a alternativa C está errada?") e receba explicações didáticas e focadas, garantindo a aprendizagem ativa e a mitigação de dúvidas pontuais. 

# 2. ATORES 

   - Estudante (Concurseiro) - Ator Principal 

   - IAG (Sistema Externo) 

3. FLUXO DE EVENTOS 

- 3.1. FLUXO BÁSICO (Esclarecer Dúvida Contextual) 

   1. O usuário visualiza a correção de uma questão recém-respondida ou o _feedback_ de uma redação. 

   2. O usuário aciona o Botão de submit "Dúvidas? Pergunte ao Tutor". 

   3. O sistema abre uma interface de chat interativo. 

   4. Em _background_ , a _ViewModel_ carrega o "Contexto Ativo" (o enunciado da questão, a alternativa que o aluno marcou, a resposta correta e a justificativa técnica) para injetar silenciosamente na próxima requisição. 

   5. O usuário digita a sua dúvida no campo de texto e toca no ícone de "Enviar". 

   6. O sistema exibe o balão de mensagem do usuário e um indicador de digitação para a IAG. 

   7. O sistema empacota o Contexto Ativo, a mensagem do aluno e o Tom do Tutor em um _prompt_ estruturado e dispara a requisição para a IAG. 

   8. A IAG processa a requisição e retorna uma explicação didática focada na dúvida do aluno. 

   9. O sistema renderiza o balão de resposta da IAG na interface de chat. 

Quadro 29 - Detalhamento de Campos (Interface de Tutoria Interativa) 

|**Campo**|**Valor default**|**Domínio**|**de**|**Tipo de campo**|**Obrigatoriedade**|
|---|---|---|---|---|---|
|||**valores**||**na tela**||



81 

|Mensagem|Branco|Texto livre|Campo de texto<br>editável|Sim|
|---|---|---|---|---|
|Enviar|Desabilitado||Botão<br>de<br>submit|Sim|
|Encerrar|||Botão de link|Não|
|Tutoria|||||



Fonte: O autor(2026). 

# 3.2. FLUXOS ALTERNATIVOS 

# **A1 – Fuga de Escopo (Context Boundary Violation)** 

1. O usuário digita uma pergunta totalmente desconexa do contexto educacional. 

2. O sistema envia a mensagem para a IAG. 

3. A IAG, instruída pelo prompt limitador configurado na arquitetura do aplicativo, recusa-se a mudar de assunto. 

4. A IAG retorna uma resposta padronizada de redirecionamento "Como seu tutor de estudos, meu foco é ajudar você com o concurso. Vamos voltar à dúvida sobre o conteúdo“. 

5. O sistema exibe a resposta na interface de chat, protegendo a ferramenta contra abusos de uso. 

# **A2 – Timeout ou Indisponibilidade da IAG** 

1. A requisição para a IAG sofre falha de conexão, instabilidade no servidor da nuvem ou estoura o tempo limite. 

2. O sistema intercepta o erro e remove o indicador de digitação. 

3. O sistema exibe um alerta não-intrusivo: "Seu tutor virtual está indisponível no momento devido a falhas na rede. Tente enviar a mensagem novamente em alguns segundos." 

4. O texto digitado pelo aluno permanece no campo de entrada para que ele não perca o que escreveu. 

82 

# 4. PRÉ-CONDIÇÕES 

   - O usuário deve possuir conexão ativa com a internet. 

   - A sessão de tutoria deve obrigatoriamente ser instanciada a partir de uma entidade educacional preexistente (uma Questão ou uma Redação) para que a IAG tenha um contexto base. 

5. PÓS-CONDIÇÕES 

   - A dúvida do estudante é sanada, reforçando a memorização do conteúdo. Por motivos de privacidade, a sessão de chat é puramente _stateless_ (sem estado), sendo o histórico da interação mantido apenas em memória temporária durante a execução da tela e completamente destruído após o encerramento da tutoria, sem sofrer qualquer tipo de persistência local ou em nuvem. 

6. PONTOS DE EXTENSÃO 

   - Não se aplica. 

7. PROTÓTIPO 

83 

FIGURA 22 - Tela de tutor 



<!-- Start of picture text -->
€ @ Tutor lA :<br>Contexto: Questdo 1 - Crimes contra a Adm. Publica. v<br>Old Jodo! Vi que vocé ficou com<br>duvida na justificativa da Ultima<br>questdo. Como posso te ajudar a<br>entender melhor esse conceito de<br>2 Peculato?<br>N&o entendi por que a apropriagdo<br>por erro de outrem entra nesse caso.<br>Otima pergunta! O Peculato<br>mediante erro de outrem (art. 313<br>do CP), também conhecido como<br>peculato-estelionato, ocorre quando<br>© funciondrio publico se apropria de<br>dinheiro ou qualquer utilidade que,<br>no exercicio do cargo, recebeu por<br>erro de outra pessoa.<br>O fundamental aaui é que o<br>Digite sua divida... ‘co<br><!-- End of picture text -->

Fonte: O autor(2026). 

# 2.9. REQUISITOS NÃO FUNCIONAIS 

- RNF01 - _Feedback_ Visual de Processamento: O sistema deve exibir indicadores de progresso sempre que uma requisição for enviada ao _back-end_ , ao módulo de OCR ou durante a geração de conteúdo pela IAG. 

84 

- RNF02 - Prevenção de Erros: Ações destrutivas (ex: exclusão de um Concurso, exclusão de Histórico de Redações ou de sua própria conta) devem obrigatoriamente exigir uma etapa de confirmação (Dialog duplo) para mitigar erros acidentais. 

- _Critério de Ajuste:_ A proporção de exclusões acidentais reportadas deve ser nula, assegurada pela fricção intencional colocada nas ações de exclusão. 

- RNF03 - O sistema deve apresentar uma interface fluida e de fácil assimilação, garantindo uma curva de aprendizado baixa para que a utilização das funcionalidades principais (como cadastro, anexação de editais e geração de conteúdos via IAG) não requeira a leitura de manuais ou suporte a tutoriais extensos. Para isso, o design de interface deve empregar metáforas visuais familiares ao cotidiano do estudante, tais como ícones de "Pastas" para a organização de concursos, "Cartões" físicos para a exibição de _flashcards_ e "Calendários" para o planejamento do cronograma dinâmico. 

- Critério de Ajuste: Um usuário iniciante, sem treinamento prévio, deve ser capaz de gerar a sua primeira prova com o auxílio da IAG em menos de 10 minutos após a conclusão do seu cadastro no sistema. 

- RNF04 - Suporte a Leitores de Tela: Os elementos interativos da tela (botões, imagens extraídas do OCR e gráficos de desempenho) devem conter descrições curtas e atributos de semântica configurados para que leitores de tela nativos consigam verbalizar as ações para deficientes visuais. 

- RNF05 - Armazenamento Local Protegido: Os dados sensíveis, como tokens de sessão de usuário, devem ser salvos no dispositivo móvel utilizando bibliotecas como se surgir outra biblioteca melhor, não vai poder usar??? 

- _EncryptedSharedPreferences_ . 

- RNF06 - Privacidade de dúvidas: As dúvidas dos usuários com o tutor não devem ser salvas para garantir sigilo total das dúvidas e informações. 

- RNF07 - Rastreabilidade de Submissões à IAG: O sistema deve manter no servidor um registro histórico (log) contendo o timestamp (data e hora) e a origem das requisições enviadas ao módulo de Inteligência Artificial, permitindo a auditoria de consumo de recursos e a investigação de possíveis abusos da ferramenta. 

85 

# 2.10. ESPECIFICAÇÃO TÉCNICA 

# **2.10.1 Caso de Uso** 

FIGURA 22 - Diagrama de caso de uso 



<!-- Start of picture text -->
eave<br>intigénce Ail<br>y Paramettzst<br>conciseANN\ ante Fashear<br>a SosimentePo<br>Goss cesta<br>imotgénca sacar<br>Srecvien<br>Center Prova a<br>(PDF)<br>esenperoProgreso e<br>/Avaliar Redacao vid<br>cee<br>‘Consultarde RedacéesHistorico \\ 5<br>Ante Cronograma ia<br>Tindalos Aer Gent<br>iteramaraeTutora Verificar se é Ator ou Componente Externo<br><!-- End of picture text -->

Fonte: O autor(2026).<sup>1</sup> 

> 1 Para melhor visualização acesse: <u>https://drive.google.com/file/d/1DQtdEfRqV62npAOweHorriqtXydaDzA2/view?usp=drive_link</u> 

1 

86 

# **2.9.2 Sequência do Produto** 

FIGURA 23 - Diagrama de sequência (autenticação) 



<!-- Start of picture text -->
ULaplicativo Servidor Banco Local_aplicativo<br>Estudante (Concurseiro)<br>Insere E-mail e Senha ¢ clica em Entrar<br>° »<br>POST /login (Credenciais via HTTPS)<br>Retorma Token JWT (Sessao)<br>T ><br>°o Salva Token de forma criptografada<br>Ind Redireciona para o Dashboard (Visao Geral) °<br>ULaplicativo Servidor Banco Local_aplicativo<br>Estudante (Concurseiro)<br><!-- End of picture text -->

Fonte: O autor(2026).<sup>2</sup> 

FIGURA 24 - Diagrama de sequência (geração de simulados) 



<!-- Start of picture text -->
ULapicaivo Banco Local apieatvo ‘AP Google Gomi LLM) Sender<br>° Solo gararote de questes (Tema X) ,<br>Recupera Corto (Banca hcp, Perc 1)<br>e———r—ae_weweeowo><br>Retena paramos<br>+ °<br>secon Een Peng —oma ests ess JSON)<br>POST (Prompt Estruturado + Conte)<br>* OOTempo tiie Tineoud<br>+ Ene ero: “Servi insponvel nts novamert.” °> Fan na resposta> 485) °<br>[Sucesso)<br>+ °<br>Deserts vag sir SON<br>ae)<br>° Parse questes yeas (Cache Local) .<br>Oe<br>Rencateaetaca do Smusado =< Sncrorzadados—  om Backgoune (syne)<br>+ @<br>ULaptcatvo Banco Local apicatvo ‘AP Gone Genin Lim) Serie<br>Esudane (Concurso)<br><!-- End of picture text -->

Fonte: O autor(2026).<sup>3</sup> 



<!-- Start of picture text -->
2 Para  melhor  visualização  acesse:<br>https://drive.google.com/file/d/1BHfCkTGNhdhDX475m68BIkfZQNyJyWLT/view?usp=drive_link<br>3 Para  melhor  visualização  acesse:<br>https://drive.google.com/file/d/1OfVQQwd0sFRRpbNSZhdZt7FcemCWE7bG/view?usp=drive_link<br><!-- End of picture text -->

87 

FIGURA 25 - Diagrama de sequência (avaliação de redação via OCR) 



<!-- Start of picture text -->
UL epeatvo Banco Love apeatvo AP Googe Gon (LM) senior<br>estrig cesi<br>Trot a nace redagomanvscita<br>eeeo—o———“—<br>es<br>Exib tno pn rv humana<br>+ °<br>° Conta texto era env para conegto .<br>Recipe<br>eeo Nive de RigodeCor e g do wusro<br>POST (Teton Reda + Prompt asa)<br>oe* Reta 1SON Nea uaa + Feedback alt} °<br>oeSalvo Roda Avaindn<br>alg<br>xe Panel de Resutdos ca Recto eS) OO oe converoobeck psn Naver<br>+ )<br>UL spcatvo fanco Lael epeatvo AP Googe Gomi (LM) senior<br>Esuante (Concise)<br><!-- End of picture text -->

Fonte: O autor(2026).<sup>4</sup> 

FIGURA 26 - Diagrama de sequência (tutor interativo) 



<!-- Start of picture text -->
UL cat Benco Locel_apcatn API Gore Gein (LM)<br>Esuderi Carsai<br>Tem ano eo@ ona "Pergo Tuo<br>or error Caraga “ConenoAho" (A queso ou redo alu)<br>oat oe Prenat ere)<br>POST (Mensagem do Atuno Context Av<br>a oOFuga 02a+ Prompt Limit)<br>de Eacopo Detect]<br>Retoma mensagor de edrecanamoni (etesa do Prompt)<br>+ e<br>Bato: "Foco nos esis Vos var<br>+ & mati” °<br>ibid Vai]<br>Reloma expcago die 1 focada no ero 4<br>Rencrzaraposta no Chat nteratvo<br>+ °<br>UL apa Banco Local apliatno {Googe Gemin (LLM)<br>Estudart (Concurso)<br><!-- End of picture text -->

Fonte: O autor(2026).<sup>5</sup> 



<!-- Start of picture text -->
4 Para  melhor  visualização  acesse:<br>https://drive.google.com/file/d/1-doX63GFGJxphgBSUn63yZ8AINjfpYE_/view?usp=drive_link<br>5 Para  melhor  visualização  acesse:<br>https://drive.google.com/file/d/1yCF1H2VNH8Zlr93AGijX8weIq58ZQzYT/view?usp=drive_link<br><!-- End of picture text -->

88 

# **2.9.3 Diagrama de Entidade Relacionamento** 

FIGURA 27 - Diagrama de Entidade-Relacionamento 

rigor de correção relacionado com usuário? logo, o usuário não poderá criar um concurso com rigor fácil e depois criar um concurso com rigor dificil? 



<!-- Start of picture text -->
‘string | ur_anexo_edital \ date | data_envio<br>ax<br>possi/ a redação não faz parte do concurso?<br><!-- End of picture text -->

não poderá reaproveitar a questão/flashcard para outros concursos com a mesma disciplina? 

Fonte: O autor(2026).<sup>6</sup> 

6 

> 6 Para melhor visualização acesse: <u>https://drive.google.com/file/d/1aQ1BrN-QlfAiTfNrP4VzCqYWpuLZxfAM/view?usp=drive_link</u> 

89 

# **2.9.4 Diagrama de Classe** 

FIGURA 28 - Diagrama de classes 



<!-- Start of picture text -->
a [ee<br>Soa S ce<br>Seis “sorocestarimagemacRet: SU) ries<br><!-- End of picture text -->

Fonte: O autor(2026).<sup>7</sup> 

7 

> 7 Para melhor visualização acesse: <u>https://drive.google.com/file/d/1YpklNVgjznC-3F4IwCihc5mDKmVPkj__/view?usp=drive_link</u> 

90 

# **2.9.5 Diagrama de Componentes** 

FIGURA 29 - Diagrama de componentes 

melhorar. usar outra modelagem mais compatível. 

Fonte: O autor(2026). 

91 

# **REFERÊNCIAS** 

ALSHAIKH, Rana; AL-MALKI, Norah; ALMASRE, Maida. The implementation of the cognitive theory of multimedia learning in the design and evaluation of an AI educational video assistant utilizing large language models. Heliyon, v. 10, art.e25361, 2024. Disponível em: https://doi.org/10.1016/j.heliyon.2024.e25361. 

DAMAŠEVIČIUS, Robertas. Commentary: ChatGPT-supported student assessment – can we rely on it?. Journal of Research in Innovative Teaching & Learning, v. 17, n. 2, p. 414-416, 2024. Disponível em: https://doi.org/10.1108/JRIT-09-2024-195. 

KERN, Richard. Twenty-first century technologies and language education: Charting a path forward. The Modern Language Journal, v. 108, p. 515-533, 2024. Disponível em: <u>https://doi.org/10.1111/modl.12924</u> 

NIERO, Leonardo Paes. Cursinhos populares: problemática, histórico e atualidade para a educação popular de jovens e adultos. Revista Internacional de Educação de Jovens e Adultos, v. 5, n. 10, p. 31-49, 2022. 

THORNE, Steven L. Generative artificial intelligence, co-evolution, and language education. The Modern Language Journal, v. 108, p. 567-572, 2024. Disponível em: <u>https://doi.org/10.1111/modl.12932.</u> 

UNESCO. Guia para a IA generativa na educação e na pesquisa. Paris: Organização das Nações Unidas para a Educação, a Ciência e a Cultura, 2024. Disponível em: <u>https://unesdoc.unesco.org/ark:/48223/pf0000390241.</u> 

XU, Tianhui et al. Current Status of ChatGPT Use in Medical Education: Potentials, Challenges, and Strategies. Journal of Medical Internet Research, v. 26, art. e57896, 2024. Disponível em: https://www.jmir.org/2024/1/e57896. 

