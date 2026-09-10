2.7. REQUISITOS FUNCIONAIS
2.7.1 Prioridade de Requisito

Essencial: requisitos sem os quais a aplicação não entrará em funcionamento.
Importante: requisitos sem os quais a aplicação, no entanto, não alcançará o objetivo de funcionamento.
Desejável: esse tipo de requisito não afeta diretamente o funcionamento do software, ou seja, o software pode funcionar de forma satisfatória sem tais requisitos.
2.7.2. Descrição dos Requisitos Funcionais
Quadro 2 RF01 - Manter Usuário
Requisito n°
RF001
Manter Usuário
Descrição
Permite ao usuário criar uma conta e autenticar-se de forma segura no aplicativo para ter acesso ao seu ambiente de estudos.
Prioridade
Essencial
Dependências
Nenhuma
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso, Diagrama de sequência (autenticação).

Fonte: O autor (2026).


Quadro 3 RF02 - Alterar Senha
Requisito n°
RF002
Alterar Senha
Descrição
Permite ao usuário solicitar a redefinição de sua senha de acesso via e-mail caso a tenha esquecido.
Prioridade
Essencial
Dependências
RF001
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).


Quadro 4 RF03 - Manter Concurso
Requisito n°
RF003
Manter Concurso
Descrição
Permite ao usuário adicionar, editar e excluir "Pastas" principais e definir parâmetros globais para elas, como a Banca Examinadora e o Tipo de Questão desejada (Múltipla escolha ou Verdadeiro/Falso). Adicionalmente, possibilita que o usuário anexe o arquivo PDF do edital do certame, que servirá como base de dados para a IAG estruturar o conteúdo.
Prioridade
Essencial
Dependências
RF001
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).
Quadro 5 RF04 - Manter Disciplina
Requisito n°
RF004
Manter Disciplina
Descrição
Permite ao estudante criar e excluir subcategorias ou disciplinas específicas dentro de uma pasta (ex: Interpretação de textos, Processos de construção textual) para organizar o material gerado.
Prioridade
Essencial
Dependências
RF003
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).


Quadro 6 RF05 - Parametrizar Inteligência Artificial Generativa
Requisito n°
RF005
Parametrizar Inteligência Artificial Generativa
Descrição
Permite ao usuário selecionar perfis predefinidos de comportamento da IAG para calibrar a geração de conteúdos.
Prioridade
Importante
Dependências
RF001
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).


Quadro 7 RF06 - Manter Flashcard
Requisito n°
RF006
Manter Flashcard
Descrição
Permite a inserção manual de novos cartões de estudo (flashcards) preenchendo um "Título" e uma "Descrição".
Prioridade
Importante 
Dependências
RF001
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).


Quadro 8 RF07 - Manter Flashcard via Documento PDF
Requisito n°
RF007
Manter Flashcards via Documento PDF
Descrição
Permite o upload de um arquivo PDF para que o sistema extraia o texto e solicite à IAG a criação automatizada de uma quantidade “x” de flashcards baseados no documento.
Prioridade
Essencial
Dependências
RF001
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).


Quadro 9 RF08 - Manter Questões via Inteligência Artificial Generativa
Requisito n°
RF008
Manter Questões via Inteligência Artificial Generativa
Descrição
Permite enviar requisições ao modelo de linguagem para gerar processualmente lotes com uma quantidade predefinida de questões baseadas na matéria atual e na banca.
Prioridade
Essencial
Dependências
RF001
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso, Diagrama de sequência (geração de simulados).

Fonte: O autor (2026).


Quadro 10 RF09 - Manter Prova via Edital (PDF)
Requisito n°
RF009
Manter Prova Edital (PDF)
Descrição
Permite ao usuário realizar o upload do documento do edital (em PDF) no momento do cadastro do concurso. O sistema extrai automaticamente as disciplinas e os pesos de cada matéria para solicitar à IAG a criação de um simulado completo, estruturado com questões proporcionais aos temas exigidos para a prova real.
Prioridade
Essencial
Dependências
RF003, RF005
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).


Quadro 11 RF10 - Visualizar Desempenho e Progresso
Requisito n°
RF010
Visualizar Desempenho e Progresso
Descrição
Exibe um painel analítico com métricas detalhadas de desempenho, incluindo barras de progresso, quantidade total de questões respondidas e a relação exata de Acertos (com porcentagem) e Erros por disciplina e simulados.
Prioridade
Essencial
Dependências
RF008, RF009
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).


Quadro 12 RF11 - Avaliar Redação via OCR
Requisito n°
RF011
Avaliar Redação via OCR
Descrição
Permite ao usuário submeter sua redação digitando-a em campo de texto livre ou enviando uma foto da folha manuscrita (usando OCR) para receber uma análise e nota do modelo de IAG.
Prioridade
Essencial
Dependências
RF005
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso, Diagrama de Sequência (avaliação de redação via OCR).

Fonte: O autor (2026).


Quadro 13 RF12 - Consultar Histórico de Redações
Requisito n°
RF012
Consultar Histórico de Redações
Descrição
Mantém um histórico das redações submetidas pelo estudante, apresentando o título, a data e hora do envio, e a nota obtida (ex: 7.5/10), permitindo a revisão do feedback.
Prioridade
Importante
Dependências
RF011
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).


Quadro 14 RF13 - Manter Cronograma Dinâmico
Requisito n°
RF013
Manter Cronograma Dinâmico
Descrição
Calcula e gera um cronograma de estudos distribuído em um calendário interativo. A geração é parametrizada selecionando a Pasta de Conteúdo, Data da Prova, Minutos por dia, Máximo de matérias diárias e os dias da semana disponíveis para estudo.
Prioridade
Essencial
Dependências
RF003
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso.

Fonte: O autor (2026).



Quadro 15 RF14 - Interagir Tutoria Interativa
Requisito n°
RF014
Interagir Tutoria Interativa
Descrição
Permite ao estudante solicitar explicações detalhadas sobre uma questão recém-respondida no simulado ou sobre o feedback recebido em uma redação. O sistema abre uma interface de chat contextual onde a IAG atua como um "tutor 1:1", sanando dúvidas específicas sobre o tema para garantir a compreensão do erro e o aprendizado ativo.
Prioridade
Importante
Dependências
RF008, RF009, RF011
Conflito
Nenhum
Material de Apoio
Diagrama de caso de uso, Diagrama de sequência (tutor interativo).

Fonte: O autor (2026).


2.7.3 Especificação dos Requisitos Funcionais
RF001 - Manter Usuário
1. BREVE DESCRIÇÃO
Permite que o estudante crie uma nova conta ou acesse o aplicativo móvel por meio de autenticação segura, estabelecendo uma sessão persistente para acessar o seu ambiente de estudos e histórico no banco de dados em nuvem.
2. ATORES
Estudante - Ator Principal
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Autenticação / Login)
O usuário abre o aplicativo móvel em seu dispositivo.
O sistema verifica localmente se já existe um Token de sessão válido.
Não havendo sessão, o sistema exibe a tela inicial de Login.
Quadro 16 - Detalhamento de Campos (Tela de Login)
Campo
Valor Default
Domínio de Valores
Tipo de Campo na tela
Obrigatório
E-mail
Branco
Formato de e-mail válido 
Campo de texto editável
Sim


Senha
Branco
Alfanumérico (mín. 6 caracteres)
Campo de senha (Mascarado)
Sim
Entrar




Botão de submit
Sim
Não tem uma conta? Cadastre-se




Botão de Link
Sim
Esqueci minha senha




Botão de Link
Não

Fonte: O autor(2026).

O usuário informa os dados de acesso (E-mail e Senha).
O usuário confirma a ação tocando no botão "Entrar".
O sistema exibe um indicador de carregamento e envia as credenciais ao back-end.
O back-end valida as credenciais no banco de dados.
Em caso de sucesso, o sistema recebe o Token de autorização, salva-o e redireciona o usuário para a tela inicial do aplicativo.

	
3.2. FLUXOS ALTERNATIVOS
A1 – Credenciais Inválidas (Erro no Login)
No passo 4 do fluxo básico, o sistema identifica que as credenciais informadas não conferem ou o usuário não existe.
O sistema oculta o indicador de carregamento.
O sistema exibe a mensagem de erro: "E-mail ou senha incorretos. Tente novamente." através de um aviso em tela.
O sistema limpa o campo de email e senha e mantém o usuário na tela de login.
A2 – Cadastro de Novo Usuário
No passo 3 do fluxo básico, o usuário toca no botão "Não tem uma conta? Cadastre-se".
O sistema navega para a tela de Criação de Conta.
O usuário preenche os campos requeridos (Nome, CPF, E-mail, Senha e Confirmação de Senha) e toca em "Criar Conta".
O sistema valida internamente se as senhas coincidem, se o CPF é válido e se o formato do e-mail é válido.
O sistema envia a requisição de cadastro para o back-end.
É verificado no banco de dados se o CPF e o e-mail são únicos. Sendo únicos, realiza o hash da senha e salva o registro.
O sistema exibe a mensagem "Conta criada com sucesso!", realiza o login automático e redireciona o usuário ao Dashboard.
O fluxo alternativo é encerrado.
Quadro 17 - Detalhamento de Campos (Tela de Cadastro)
Campo
Valor Default
Domínio de Valores
Tipo de Campo na tela
Obrigatório
Nome Completo
Branco
Formato de texto
Campo de texto editável
Sim


CPF
Branco
Numérico (11 dígitos com máscara de formatação)
Campo de texto editável
Sim
E-mail
Branco
Formato de e-mail válido
Campo de texto 
Sim
Senha
Branco
Alfanumérico (mín. 6 caracteres)
Campo de senha (Ofuscado)
Sim
Confirmar Senha
Branco
Alfanumérico (mín. 6 caracteres)
Campo de senha (Ofuscado)
Sim
Criar Conta




Botão de submit
Sim
Já tem uma conta? Entrar




Botão de link
Não

Fonte: O autor(2026).

	
A3 – Sessão Local já Existente (Auto-Login)
O sistema detecta que já existe um Token de acesso válido armazenado no dispositivo.
O sistema pula a tela de Login e redireciona o usuário diretamente para a tela inicial.
A4 - Recuperação de Senha (Esquecimento de Senha)
No passo 3 do fluxo básico, em vez de preencher as credenciais, o usuário aciona o botão de link "Esqueci a senha".
O sistema interrompe o fluxo de autenticação atual.
O sistema redireciona o usuário para a tela de Solicitação de Recuperação, iniciando o fluxo do requisito RF002 (Alterar Senha).
O fluxo de login é encerrado.
4. PRÉ-CONDIÇÕES
O dispositivo móvel deve possuir conexão ativa com a internet para comunicação com o sistema de validação ou ter acessado nas últimas 24 horas dias e validando o acesso por meio de token.
Para o login, o usuário deve estar previamente cadastrado no sistema.
5. PÓS-CONDIÇÕES
O estado de autenticação (Token JWT) é gravado no armazenamento local do dispositivo, garantindo o acesso seguro aos módulos restritos do aplicativo sem necessidade de novos logins a cada abertura.
6. PONTOS DE EXTENSÃO
Não se aplica.
7. PROTÓTIPO
FIGURA 2 - Tela de Login

Fonte: O autor(2026).


FIGURA 3 - Tela de Cadastro

Fonte: O autor(2026).


RF002 - Alterar Senha
1. BREVE DESCRIÇÃO
Permite que o usuário solicite a redefinição de sua senha de acesso caso a tenha esquecido.
2. ATORES
Estudante  - Ator Principal
Back-end (Sistema Externo / Serviço de E-mail)
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Recuperação de Senha via Código OTP)
Na tela de Login (RF001), o usuário aciona a opção "Esqueci minha senha".
O sistema navega para a tela de Recuperação de Conta, solicitando o endereço de e-mail.
O sistema valida a existência do e-mail no banco de dados.
O sistema gera um código numérico temporário, 6 dígitos, válido por 15 minutos, e o envia para o e-mail do usuário.
O sistema avança para a tela de "Redefinição de Senha", exibindo a mensagem: "Enviamos um código de 6 dígitos para o seu e-mail".
O usuário insere o código recebido, digita a nova senha e a confirmação da nova senha.
O sistema valida se o código confere e se ainda está no prazo de validade.
Em caso de sucesso,  é atualizado o registro no Banco de Dados.
O sistema exibe a mensagem visual "Senha alterada com sucesso!" e redireciona o usuário de volta para a tela de Login.

Quadro 18 - Detalhamento de Campos (Tela de Solicitação de Recuperação)
Campo
Valor Default
Domínio de Valores
Tipo de Campo na Tela
Obrigatoriedade
E-mail
Branco
Formato de e-mail
Campo de texto editável
Sim
Enviar Código




Botão de submit
Sim
Voltar ao Login




Botão de Link
Sim


Quadro 19 - Detalhamento de Campos (Tela de Redefinição de Senha)
Campo
Valor Default
Domínio de Valores
Tipo de Campo na Tela
Obrigatoriedade
Código de Segurança
Branco
Apenas Números (6 dígitos)
Campo numérico
Sim
Nova Senha
Branco
Alfanumérico (mín. 6 caracteres)
Campo de senha (Mascarado)
Sim
Confirmar Nova Senha
Branco
Alfanumérico (mín. 6 caracteres)
Campo de senha (Mascarado)
Sim
Redefinir Senha




Botão de submit
Sim

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – E-mail não encontrado ou inválido
O back-end não encontra o e-mail na base de dados.
Por diretrizes de segurança evitando  varredura de contas, retorna uma mensagem genérica, mas não envia o e-mail.
O sistema avança para a tela de "Redefinição de Senha" exibindo a mensagem padrão: "Se o e-mail constar em nossa base, você receberá um código em instantes".
O fluxo segue aguardando o usuário, protegendo a privacidade dos usuários reais.
A2 – Código de Segurança (OTP) Inválido ou Expirado
O back-end detecta que o código inserido pelo usuário está incorreto ou seu prazo de 15 minutos expirou.
O sistema oculta o loading e exibe a mensagem de erro: "Código inválido ou expirado. Verifique seu e-mail ou solicite um novo código."
O sistema destaca o campo "Código de Segurança" em vermelho e mantém o usuário na mesma tela.
A3 – Divergência na Confirmação da Nova Senha
O usuário digita valores diferentes nos campos "Nova Senha" e "Confirmar Nova Senha".
O sistema, através da ViewModel, validação local do aplicativo, sem ir à internet, detecta a divergência instantaneamente.
O sistema desabilita o botão "Redefinir Senha" e exibe um alerta abaixo do campo: "As senhas não coincidem".
O usuário corrige a digitação, o botão é habilitado, e o sistema retorna ao fluxo básico.
4. PRÉ-CONDIÇÕES
O dispositivo móvel deve possuir conexão ativa com a internet.
O usuário não pode estar autenticado (logado) no aplicativo no momento da solicitação.
5. PÓS-CONDIÇÕES
A credencial de acesso do usuário é atualizada (hash) no banco de dados. Qualquer sessão anterior que estivesse ativa em outros dispositivos utilizando o Token antigo será invalidada pelo back-end.
6. PONTOS DE EXTENSÃO
Não se aplica.
7. PROTÓTIPO
FIGURA 4 - Tela de Recuperação de Senha (Email de recuperação)

Fonte: O autor(2026).
FIGURA 5 - Tela de Recuperação de Senha (Código de Verificação)

Fonte: O autor(2026).


RF003 - Manter Concurso
1. BREVE DESCRIÇÃO
Permite ao estudante criar, visualizar, editar e excluir pastas estruturais denominadas "Concursos". Estas pastas centralizam as disciplinas, simulados e parâmetros globais (Banca Examinadora e Tipo de Questão) que irão direcionar o comportamento da Inteligência Artificial. Permite também o anexo do arquivo PDF do edital, que servirá de base contextual (RAG - Retrieval-Augmented Generation) para a API do Gemini gerar conteúdos precisos.

2. ATORES
Estudante (Concurseiro) - Ator Principal
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Criar Novo Concurso)
Na tela principal, o usuário aciona o Botão de submit "Novo Concurso".
O sistema apresenta a tela de formulário "Configurar Edital/Concurso".
O usuário preenche o Título da Pasta e seleciona a Banca Examinadora e o Tipo de Questão.
O usuário toca na opção "Anexar Edital (PDF)".
O sistema abre o seletor nativo de arquivos do Android.
O usuário confirma a criação tocando no botão "Salvar Concurso".
O sistema exibe um indicador de carregamento enquanto a ViewModel empacota os dados e envia (junto com o arquivo binário) para o back-end.
O back-end salva as informações no banco de dados, armazena o arquivo em nuvem e retorna o ID do concurso gerado.
O sistema exibe a mensagem "Concurso cadastrado com sucesso!", atualiza e encerra o caso de uso.
Quadro 20 - Detalhamento de Campos (Tela de Criação de Concurso)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Título do Concurso
Branco
Texto
Campo de texto editável
Sim
Banca Examinadora
Branco
Texto
Campo de texto editável
Sim
Tipo de Questão
Múltipla escolha
Múltipla Escolha, Certo/Errado
Botões de Seleção (Radio Buttons)
Sim
Anexo do Edital
Nenhum Arquivo
Formato .pdf
Botão de Upload / File Picker
Não
Salvar Concurso




Botão de submit
Sim

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Edição de Parâmetros do Concurso
No Dashboard, o usuário seleciona um Concurso já existente e aciona o ícone de "Editar" (lápis).
O sistema recupera as informações do banco de dados e preenche a tela do formulário.
O usuário modifica os dados desejados (ex: troca a banca examinadora ou faz o upload de um novo edital retificado).
O usuário toca em "Salvar Alterações".
O sistema envia a requisição de atualização para o back-end.
O sistema exibe a mensagem "Alterações salvas" e retorna à visualização da pasta.
A2 – Arquivo Anexado Inválido (Erro de Validação Local)
O usuário seleciona um arquivo com extensão diferente de .pdf (ex: um .docx ou .jpg) ou um PDF que excede o limite estipulado (ex: 10MB).
O sistema (através da validação da ViewModel) detecta a violação antes de consumir.
O sistema recusa o anexo e exibe uma mensagem de erro em vermelho sob o botão: "Formato inválido ou arquivo muito grande. Selecione um PDF de até 10MB."
O campo "Anexo do Edital" retorna ao estado "Nenhum arquivo".
A3 – Exclusão da Pasta (Exclusão em Cascata)
Na tela inicial, o usuário acessa as opções secundárias de um Concurso e seleciona "Excluir".
O sistema exibe um modal crítico de confirmação: "Tem certeza? Excluir este concurso apagará permanentemente todas as disciplinas, flashcards e desempenhos vinculados a ele."
O usuário confirma a exclusão.
O sistema dispara a requisição de exclusão (DELETE) para o back-end.
O banco de dados aplica a regra ON DELETE CASCADE, varrendo e excluindo todas as entidades secundárias atreladas ao ID deste concurso.
O sistema exibe "Concurso excluído" e atualiza a tela.
A4 – Reativação de Concurso Arquivado
O usuário acessa a área de "Concursos Arquivados" através das configurações do perfil.
O sistema lista as pastas de Concursos que possuem o status de inativas.
O usuário aciona a opção "Reativar" no concurso desejado.
O sistema dispara a requisição de reativação para o back-end.
O banco de dados reverte o status do concurso e de suas entidades filhas de "inativo" para "ativo".
O sistema exibe a mensagem de confirmação: "Concurso reativado com sucesso" e a pasta volta a ser exibida na tela inicial.
4. PRÉ-CONDIÇÕES
O usuário deve estar autenticado e com uma sessão ativa no aplicativo.
Para a etapa de anexo, o usuário deve conceder ao aplicativo a permissão do Android para acessar o armazenamento do dispositivo.
5. PÓS-CONDIÇÕES
Os dados do Concurso são gravados no banco de dados e sincronizados. Os parâmetros globais definidos aqui passarão a ser injetados automaticamente no prompt (Engenharia de Prompt) toda vez que a IAG for acionada dentro desta pasta.
6. PONTOS DE EXTENSÃO
Após a criação bem-sucedida, o sistema pode sugerir o redirecionamento imediato para o caso de uso de Manter Disciplina para que o estudante comece a organizar os subtópicos da pasta recém-criada.
7. PROTÓTIPO
FIGURA 6 -  Tela de menu

Fonte: O autor(2026).

FIGURA 7 - Tela de cadastro de concurso

Fonte: O autor(2026).

RF004 - Manter Disciplina
1. BREVE DESCRIÇÃO
Permite ao estudante criar, visualizar, editar e excluir subcategorias (Disciplinas) dentro de uma pasta estrutural (Concurso) previamente criada. Estas disciplinas funcionam como gavetas organizacionais onde os flashcards, questões geradas por IA e o desempenho do aluno serão armazenados de forma isolada.
2. ATORES
Estudante (Concurseiro) - Ator Principal
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Criar Nova Disciplina)
Na tela inicial, o usuário acessa uma pasta de "Concurso" previamente criada.
O sistema exibe a tela de detalhes do Concurso, listando as disciplinas já existentes (ou uma mensagem de estado vazio caso não haja nenhuma).
O usuário aciona o botão "Adicionar Disciplina" (ícone de +).
O sistema apresenta um componente sobreposto (Modal Bottom Sheet ou Dialog) contendo o campo de formulário.
O usuário digita o Nome da Disciplina e toca em "Salvar".
A ViewModel valida o preenchimento e dispara a requisição (POST) para o back-end, passando o ID do Concurso "Pai" e o nome da disciplina.
O sistema fecha o modal e atualiza a lista exibindo a nova disciplina na tela.
Quadro 21 - Detalhamento de Campos (Modal de Criação/Edição de Disciplina)	
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Nome da Disciplina
Branco
Texto
Campo de texto editável
Sim
Salvar




Botão de submit
Sim
Cancelar




Botão de Link
Não

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Edição do Nome da Disciplina
Na tela do Concurso, o usuário aciona o botão de "Opções", selecionando "Editar".
O sistema abre o modal já preenchido com o nome atual da disciplina.
O usuário altera o texto e toca em "Salvar".
O sistema dispara a requisição (PUT/PATCH) para o back-end.
O sistema atualiza e reflete a mudança visualmente na lista.
A2 – Nome em Branco ou Duplicado (Validação Local)
O usuário tenta salvar com o campo vazio ou com um nome que já existe dentro daquele mesmo Concurso.
O sistema faz uma validação na ViewModel e bloqueia a ação.
O sistema exibe uma mensagem de erro sob o campo de texto: "O nome não pode estar vazio" ou "Já existe uma disciplina com este nome neste concurso."
O usuário corrige a digitação e prossegue.
A3 – Exclusão de Disciplina (Exclusão em Cascata Parcial)
Na tela do Concurso, o usuário aciona a opção "Excluir" em uma disciplina específica.
O sistema exibe um alerta de segurança: "Tem certeza? Todos os flashcards, questões e históricos atrelados EXCLUSIVAMENTE a esta disciplina serão apagados."
O usuário confirma a exclusão.
O sistema envia a requisição de exclusão (DELETE) para o back-end.
O banco de dados apaga a disciplina e os dados atrelados a ela, mas mantém o Concurso Pai intacto.

4. PRÉ-CONDIÇÕES
O usuário deve estar autenticado.
O usuário precisa ter ao menos um Concurso cadastrado, pois a Disciplina é uma entidade "filha" e exige o ID do Concurso para existir no banco de dados relacional.
5. PÓS-CONDIÇÕES
A disciplina é persistida no banco de dados. A partir desse momento, ela passa a aparecer como uma opção válida nos filtros de geração de Questões, criação de Flashcards e estruturação do Cronograma Dinâmico.
6. PONTOS DE EXTENSÃO
Não se aplica.
7. PROTÓTIPO
FIGURA 8 - Tela do menu de concurso

Fonte: O autor(2026).

FIGURA 9 - Tela de cadastro de disciplina

Fonte: O autor(2026).


RF005 - Parametrizar Inteligencia Artificial
1. BREVE DESCRIÇÃO
Permite ao estudante (Concurseiro) configurar as preferências globais de comportamento do motor de IAG. Em vez de permitir o envio de prompts de texto livre (o que geraria riscos de "alucinação" da IAG), o sistema disponibiliza perfis predefinidos que calibram o nível de dificuldade das questões geradas, o rigor da correção do módulo OCR de redações e o estilo de resposta do Tutor Interativo.
2. ATORES
Estudante (Concurseiro) - Ator Principal
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Configurar Preferências da IA)
No menu principal ou na tela de "Perfil", o usuário seleciona a opção "Configurações da Inteligência Artificial".
O sistema recupera a preferência do banco de dados vinculada ao usuário logado e exibe as configurações atuais.
O usuário altera os parâmetros desejados nos seletores da interface.
O usuário toca no botão "Salvar Configurações".
O sistema atualiza os valores na ViewModel e dispara uma requisição (PUT/PATCH) para o back-end.
O back-end atualiza a preferência no banco de dados e retorna sucesso.
O sistema exibe a mensagem "Configurações da IA atualizadas com sucesso!" e retorna à tela anterior.
Quadro 22 - Detalhamento de Campos (Tela de Parametrização da IA)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Nível de Dificuldade das Questões
Médio
Fácil, Médio, Difícil
Campo de texto editável
Sim
Rigor na Correção da Redação
Padrão
Flexível, Padrão, Rígido
Campo de texto editável
Sim
Tom do Tutor Interativo
Explicativo
Direto/Objetivo, Explicativo/Detalhado
Botões de Seleção (Radio Buttons)
Sim
Chave de API (API Key)
Branco
Alfanumérico (Token do Google AI Studio)
Campo de texto (Ofuscado/Senha)
Sim (para usar recursos de IAG)

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Falha de Comunicação ou Instabilidade de Rede
A requisição para salvar as configurações no back-end falha devido à indisponibilidade de internet ou instabilidade no servidor em nuvem.
O sistema intercepta o erro através da ViewModel.
O sistema exibe um alerta na tela: "Não foi possível salvar suas preferências na nuvem. Verifique sua conexão e tente novamente."
Os parâmetros selecionados pelo usuário continuam retidos visualmente na interface para que ele não precise reconfigurar tudo.
4. PRÉ-CONDIÇÕES
O usuário deve estar autenticado no sistema.
5. PÓS-CONDIÇÕES
As preferências são aplicadas globalmente para a conta do usuário. A partir deste momento, todas as chamadas feitas para gerar simulados e questões ou corrigir redações passarão a incluir a string de configuração pré definida no escopo da requisição (prompt) enviada à API do Google Gemini.
6. PONTOS DE EXTENSÃO
Não se aplica.
RF006 - Manter Flashcard
1. BREVE DESCRIÇÃO
Permite ao estudante criar, editar e excluir cartões de estudo (flashcards) de forma manual. O usuário preenche o Título e a Descrição do cartão, vinculando-o a uma Disciplina específica.
2. ATORES
Estudante (Concurseiro) - Ator Principal
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Criar Novo Flashcard)
No aplicativo, o usuário acessa a tela de uma "Disciplina" previamente cadastrada.
O usuário aciona o Botão de submit "Novo Flashcard".
O sistema apresenta o modal de criação de cartão, contendo os campos de entrada para Título e Descrição.
O usuário toca no botão "Salvar".
O sistema (via ViewModel) valida se ambos os campos foram preenchidos corretamente.
O sistema dispara em segundo plano uma requisição (POST) para sincronizar o novo cartão com o back-end.
O sistema exibe a mensagem "Flashcard salvo com sucesso!", fecha o formulário e atualiza a lista de cartões da disciplina.
O caso de uso é encerrado.
Quadro 23 - Detalhamento de Campos (Tela de Criação/Edição de Flashcard)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Título
Branco
Texto
Campo de texto editável
Sim
Descrição
Branco
Texto
Campo de texto editável
Sim
Salvar




Botões de Ação
Sim
Cancelar




Botão de Link
Sim

Fonte: O autor(2026).

3.2. FLUXOS ALTERNATIVOS
A1 – Edição do Flashcard
Na lista de flashcards da disciplina, o usuário seleciona um cartão existente e aciona a opção "Editar".
O sistema abre a interface de formulário preenchida com os dados atuais do Título e Descrição.
O usuário modifica os textos desejados e toca em "Salvar".
O sistema valida os campos e dispara a requisição de atualização (PUT/PATCH) para o back-end.
O sistema exibe "Flashcard atualizado" e reflete as mudanças na tela.
A2 – Campos em Branco (Validação Local)
O usuário tenta salvar o cartão deixando Título ou Descrição em branco.
O sistema bloqueia o salvamento imediatamente.
O sistema destaca o campo vazio em vermelho e exibe a mensagem: "A frente e o verso do cartão são obrigatórios."
O usuário preenche os campos e prossegue com o salvamento.
A3 – Exclusão do Flashcard
Na lista de flashcards, o usuário aciona a opção "Excluir" (via ícone de lixeira).
O sistema solicita uma confirmação simples: "Deseja excluir este cartão?".
O usuário confirma.
O sistema remove o registro do banco de dados local, remove visualmente da lista e envia a instrução de exclusão (DELETE) para o back-end.
4. PRÉ-CONDIÇÕES
O usuário deve estar autenticado.
O usuário deve ter navegado até o interior de uma Disciplina para que o sistema saiba a qual categoria pertence o flashcard.
5. PÓS-CONDIÇÕES
O flashcard é armazenado localmente para permitir o estudo offline e sincronizado com a nuvem.
6. PONTOS DE EXTENSÃO
Não se aplica.
7. PROTÓTIPO
FIGURA 10 - Tela de perfil

Fonte: O autor(2026).

FIGURA 11 - Tela de Configurações da IAG

Fonte: O autor(2026).

RF007 - Manter Flashcards via Documento (PDF)
1. BREVE DESCRIÇÃO
Permite que o estudante automatize a criação de materiais de estudo enviando um arquivo PDF (ex: apostilas, resumos, leis). O sistema extrai o conteúdo textual do arquivo, empacota-o em um prompt estruturado (junto com a quantidade desejada de cartões) e envia à IAG para gerar um lote de flashcards focados nos pontos mais relevantes do documento. Os flashcards gerados são salvos automaticamente na Disciplina selecionada.
2. ATORES
Estudante (Concurseiro) - Ator Principal
API Google Gemini (Sistema Externo)
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Gerar Flashcards via IA)
Na tela de uma "Disciplina", o usuário aciona a opção "Gerar Flashcard”.
O sistema exibe o formulário de "Geração Automatizada".
O usuário seleciona a "Quantidade de Flashcards" desejada 5, 10, 15, 20 ou x (sendo este um campo numérico).
O usuário toca no botão "Selecionar Arquivo PDF".
O usuário seleciona o documento desejado.
O sistema valida o arquivo localmente (extensão e tamanho limite de 10MB) e exibe o nome do arquivo selecionado na tela.
O usuário toca em "Gerar Flashcards".
O sistema exibe uma tela de carregamento dinâmico e inicia as seguintes tarefas em background:
O Módulo Extrator analisa o arquivo PDF localmente e extrai todo o texto.
A ViewModel monta o payload, unindo o texto extraído, a quantidade solicitada e as regras de formatação.
A requisição é enviada à API do Google Gemini.
O Gemini retorna um array JSON estruturado com os pares "Título" e "Descrição".
O sistema processa o retorno, salva os novos flashcards no banco de dados local vinculados à Disciplina atual e dispara a sincronização com o banco de dados.
O sistema oculta o carregamento, exibe a mensagem "X flashcards gerados com sucesso!" e atualiza a lista visual na tela da disciplina.
Quadro 24 - Detalhamento de Campos (Tela de Geração via IA)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Quantidade
10
5, 10, 15, 20, x
Lista suspensa ou Campo de texto editável
Sim
Arquivo base
Branco
.pdf (Max 10MB)
Botão de Seleção de Arquivo
Sim
Gerar Flashcards




Botão de submit
Sim

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Arquivo PDF Inválido ou Muito Grande
A validação do sistema identifica que o arquivo excede o limite de 10MB ou não é um PDF.
O sistema recusa o anexo e exibe uma mensagem de erro: "O arquivo deve ser um PDF de até 10MB. Tente dividir o documento ou escolher um arquivo menor."
O botão "Gerar Flashcards" permanece desabilitado.
A2 – Arquivo PDF sem Texto (PDF em Formato de Imagem)
O Módulo Extrator analisa o PDF, mas não encontra texto selecionável.
A ViewModel interrompe o processo antes de consumir a cota da API do Gemini.
O sistema remove a tela de carregamento e exibe um Dialog de aviso: "Não foi possível ler o texto deste PDF. Certifique-se de que o documento não seja apenas uma imagem escaneada."
A3 – Falha de Retorno ou Timeout da IA
A API do Google Gemini demora mais que o limite estabelecido ou retorna um erro de processamento/indisponibilidade de serviço.
O sistema intercepta o erro, encerra o carregamento e exibe a mensagem: "A Criação automática demorou a responder ou o texto é muito complexo. Tente novamente em instantes."
O sistema não grava nenhum dado fragmentado no banco.
4. PRÉ-CONDIÇÕES
O usuário deve estar autenticado e com conexão estável à internet.
O usuário deve ter concedido permissão de leitura de arquivos ao aplicativo.
O arquivo PDF deve conter texto "copiável" (searchable text), não apenas imagens rasterizadas.
5. PÓS-CONDIÇÕES
Um lote de novos flashcards estruturados com Título e Descrição é gerado e salvo na base de dados, estando imediatamente disponível para as sessões de revisão ativa do aluno.
6. PONTOS DE EXTENSÃO
Não se aplica.
7. PROTÓTIPO
FIGURA 12 - Tela para criar flashcards via PDF

Fonte: O autor(2026).
FIGURA 13 - Tela para criar novo flashcard manual

Fonte: O autor(2026).
RF008 - Manter Questões via Inteligência Artificial
1. BREVE DESCRIÇÃO
Permite ao estudante gerar processualmente lotes de questões para treino. O sistema atua como um orquestrador cognitivo: ele captura o contexto do usuário (Banca Examinadora do Concurso, Nível de Dificuldade nas Preferências e a Disciplina atual), empacota essas diretrizes em um prompt estruturado no código-fonte (oculto ao usuário) e requisita à API do Google Gemini a criação de um JSON contendo o enunciado, as alternativas, a resposta correta e a justificativa instrucional para cada questão gerada.
2. ATORES
Estudante (Concurseiro) - Ator Principal
API Google Gemini (Sistema Externo)
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Gerar Lote de Questões)
Na tela de uma "Disciplina", o usuário aciona a opção "Gerar Questões".
O sistema exibe o formulário de parametrização do lote.
O usuário seleciona a quantidade de questões desejada e, opcionalmente, digita um tema específico.
A ViewModel executa a Engenharia de Prompt, concatenando:
Regra da Banca e Tipo de Questão (Múltipla Escolha ou Certo/Errado).
Nível de Dificuldade e Rigor.
Tema solicitado (se houver) e o formato obrigatório de saída (JSON).
A requisição HTTP é disparada para a API do Google Gemini.
A API processa a solicitação e retorna uma matriz de objetos JSON com as questões.
O sistema desserializa o JSON, valida a integridade dos dados e salva o lote de questões no banco de dados atrelado à Disciplina.
O sistema encerra o carregamento, exibe a mensagem "Lote de [X] questões gerado com sucesso!" e direciona o usuário imediatamente para a tela de Resolução (Simulado).
Quadro 25 - Detalhamento de Campos (Tela de Geração de Questões)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Tema Específico
Branco
Texto
Campo de texto editável
Não
Quantidade
5
5, 10, 15, 20
Lista Suspensa
Sim
Gerar Questões




Botão de submit
Sim

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Quebra de Contrato JSON (Falha de Formatação da IA)
A LLM sofre uma "alucinação" e retorna a resposta em texto plano ou com a estrutura JSON corrompida.
A ViewModel intercepta o erro durante a desserialização nativa.
O sistema aborta o salvamento para evitar dados corrompidos no banco.
O sistema exibe a mensagem: "Não foi possível estruturar as questões corretamente. Por favor, tente novamente."
O usuário retorna à tela para nova tentativa.
A2 – Bloqueio por Filtro de Segurança (Safety Settings)
O tema solicitado pelo usuário envolve termos sensíveis que a LLM não possui acesso.
O Google Gemini recusa a geração e retorna um código de bloqueio de segurança.
O sistema intercepta a recusa da API.
O sistema encerra o carregamento e exibe um Dialog de aviso: "O tema solicitado foi bloqueado pelas políticas de segurança da IA do Google."
A3 – Timeout da API
A conexão com a API do Gemini demora mais do que o tempo estipulado.
O sistema cancela a requisição para não travar o dispositivo.
4. PRÉ-CONDIÇÕES
O usuário deve estar logado.
O usuário deve possuir uma Pasta de Concurso configurada com a respectiva Banca Examinadora.
5. PÓS-CONDIÇÕES
O banco de dados passa a contar com novas instâncias da entidade Questão, permitindo que o aluno inicie a resolução.
6. PONTOS DE EXTENSÃO
Após gerar o lote, o usuário resolve as questões, gerando métricas de acertos e erros que acionam a Visualização de Desempenho.
Se, ao resolver uma questão, o aluno não entender a "justificativa" gerada, o sistema aciona o ponto de extensão para o Tutor Interativo.
7. PROTÓTIPO
FIGURA 14 - Tela de geração de questões

Fonte: O autor(2026).
RF009 - Manter Prova Edital (PDF)
1. BREVE DESCRIÇÃO
Permite ao estudante gerar uma Prova completa, estruturada de forma proporcional e fidedigna ao exame real. O sistema utiliza o documento PDF do edital para extrair, via mineração de texto, a grade de disciplinas e o peso/quantidade de questões de cada matéria. Com esses dados, o sistema aciona a IAG para gerar um lote de questões que respeitam rigorosamente a distribuição exigida pela banca examinadora.
2. ATORES
Estudante (Concurseiro) - Ator Principal
API Google Gemini (Sistema Externo)
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Gerar Prova Estruturada)
Na tela de um "Concurso", o usuário aciona a opção "Gerar Prova via Edital".
O sistema verifica que já existe um PDF do edital vinculado àquela pasta de Concurso.
O sistema exibe um indicador de carregamento dinâmico e executa as tarefas em background:
O Módulo Extrator lê o PDF e identifica a seção "Conteúdo Programático" e "Quadro de Provas/Pesos".
A ViewModel calcula a proporção matemática para formular questões.
O sistema injeta as regras da banca, o nível de dificuldade e a proporção de matérias em um prompt estruturado e envia para o Google Gemini.
O back-end retorna o JSON estruturado contendo toda a Prova.
O sistema salva no banco de dados.
O sistema exibe a mensagem "Prova gerada com sucesso! Estrutura baseada no edital." e libera o botão para iniciar a resolução.
Quadro 26 - Detalhamento de Campos (Tela de Geração de Prova)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Edital Base
[PDF vinculado ao Concurso]


Texto
Sim
Gerar Prova Completa




Botão Submit
Sim

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Concurso sem Edital Vinculado
O sistema detecta que o usuário não fez o upload do edital quando criou o Concurso.
O sistema exibe o botão "Anexar Edital (PDF)" na tela de configuração da prova.
O usuário aciona o botão, seleciona o arquivo no armazenamento do dispositivo, e o sistema vincula o PDF à pasta.
O sistema atualiza a tela e o fluxo retorna ao fluxo básico.
A2 – Edital sem Quadro de Pesos Claros (IA Flexível)
O módulo extrator e a IAG não conseguem localizar uma tabela exata de pesos e proporções no PDF.
A IAG identifica apenas as disciplinas exigidas, mas não a proporção entre elas.
O sistema aplica uma "Distribuição Igualitária".
O sistema gera a prova e, ao finalizar, exibe um alerta informativo: "O quadro de pesos não foi identificado claramente no edital. As questões foram distribuídas igualmente entre as matérias do conteúdo programático."
A3 – Documento Excede a Janela de Contexto (Tokens)
O sistema identifica que o edital é extremamente longo e excede o limite de tokens suportado em uma única requisição do Gemini.
A ViewModel intercepta essa limitação antes de gastar rede.
O sistema aplica uma regra de "Poda Heurística", descarta as páginas iniciais de disposições gerais e anexos finais, enviando apenas as páginas que contêm as palavras-chave "Conteúdo Programático" e "Quadro de Provas".
A requisição é feita com o texto otimizado e o fluxo retorna ao fluxo básico.
4. PRÉ-CONDIÇÕES
O usuário deve ter criado o concurso e as preferências da IAG devem estar parametrizadas.
Conexão ativa com a internet para requisição ao Gemini.
5. PÓS-CONDIÇÕES
Uma entidade de "Prova" é criada no banco de dados, contendo a lista completa de questões divididas por disciplinas, prontas para serem respondidas no ambiente de avaliação do aplicativo.
6. PONTOS DE EXTENSÃO
Após a finalização da resolução da prova, o sistema aciona o caso de uso de Visualizar Desempenho com o aproveitamento do aluno.
7. PROTÓTIPO
FIGURA 15 - Tela de geração de prova via IAG

Fonte: O autor(2026).
FIGURA 16 - Tela de resolução de prova

Fonte: O autor(2026).


RF010 - Visualizar Desempenho e Progresso
1. BREVE DESCRIÇÃO
Permite ao estudante acompanhar a sua evolução de aprendizado através de um painel analítico interativo. O sistema compila o histórico de resoluções de questões avulsas e provas, geradas previamente pela IAG, e exibe métricas detalhadas, incluindo barras de progresso, volume total de questões respondidas e a taxa exata de Acertos e Erros, em percentual e números absolutos, filtrados por disciplina ou por provas realizadas.
2. ATORES
Estudante - Ator Principal
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Visualizar Painel de Métricas)
No menu de navegação inferior do aplicativo, o usuário seleciona a aba "Desempenho".
O sistema roda em segundo plano uma rotina para sincronizar os dados locais com o back-end, garantindo que o painel exiba as informações mais recentes.
A ViewModel realiza o cálculo estatístico das métricas:
Soma do total de questões respondidas.
Cálculo percentual de Acertos vs. Erros geral.
Agrupamento de acertos e erros segmentados por Disciplina e por Prova.
O usuário interage com os filtros de tempo (ex: "Últimos 7 dias", "Mês Atual", "Geral") ou seleciona uma disciplina específica para detalhar os dados.
O sistema atualiza os gráficos instantaneamente com base nos filtros selecionados.
Quadro 27 - Detalhamento de Filtros e Elementos Visuais (Painel de Desempenho)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Período de Avaliação
Visão Geral
7 dias, 30 dias, Visão Geral
Filtro em formato de Botões
Sim
Filtro por Disciplina
Todas
[Lista de disciplinas cadastradas]
Lista Suspensa
Não
Rendimento Global
0%
0 a 100% 
Componente Visual
Sim
Desempenho por Matéria
0/0
Barras de progresso com %
Lista de Barras
Sim

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Ausência de Dados (Estado Vazio / Empty State)
O sistema identifica que o usuário recém-criou a conta e não possui nenhum histórico de questões respondidas.
O sistema não renderiza gráficos com valor "zero".
Em vez dos gráficos, o sistema exibe uma ilustração (Empty State) e a mensagem: "Você ainda não respondeu nenhuma questão.".
O sistema exibe um botão de atalho primário direcionando o usuário para a tela de concursos.
A2 – Modo Offline (Sem conexão com o Back-end)
O dispositivo não consegue se conectar à internet para checar atualizações com o back-end.
O sistema não interrompe a exibição da tela. Ele utiliza os dados cacheados na última sessão do banco de dados local.
O sistema exibe um pequeno aviso no topo da tela (ex: ícone de nuvem cortada): "Exibindo dados armazenados offline. Conecte-se à internet para sincronizar."
O usuário consegue visualizar os gráficos normalmente com base nos dados locais.
4. PRÉ-CONDIÇÕES
O usuário deve estar autenticado no sistema.
É altamente recomendável que o usuário já tenha respondido a Questões ou finalizado uma Prova para que os gráficos tenham insumos para exibição.
5. PÓS-CONDIÇÕES
O usuário adquire consciência de suas fraquezas e fortalezas acadêmicas. Os dados agregados nesta tela também servem de insumo algorítmico interno para a IAG, que poderá utilizá-los no futuro para criar cronogramas dinâmicos mais precisos focados nas matérias com menor rendimento.
6. PONTOS DE EXTENSÃO
Não se aplica.
7. PROTÓTIPO
FIGURA 17 - Tela de desempenho

Fonte: O autor(2026).


RF011 - Avaliar Redação via OCR
1. BREVE DESCRIÇÃO
Permite ao estudante submeter redações para avaliação automatizada. O sistema oferece duas vias de entrada: a digitação em um campo de texto livre ou o envio de uma fotografia da folha manuscrita. No segundo caso, o sistema aciona um módulo interno de Reconhecimento Óptico de Caracteres (OCR) para extrair o texto da imagem. Após a confirmação do texto pelo usuário, os dados são processados pela IAG, que retorna uma nota estruturada e um feedback formativo detalhado com base no rigor previamente parametrizado.
2. ATORES
Estudante (Concurseiro) - Ator Principal
API do Google Gemini (Sistema Externo)
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Submissão de Redação Manuscrita via Foto)
No menu principal do aplicativo, o usuário acessa o módulo de "Redações" e aciona o botão "Nova Redação".
O sistema exibe as opções de submissão:"Escanear Folha Manuscrita" ou “Digitar Redação”.
O usuário seleciona "Escanear Folha Manuscrita".
O sistema abre a interface de câmera nativa do dispositivo móvel ou permite selecionar da galeria.
O usuário captura a fotografia da folha de redação.
O sistema exibe uma tela de processamento e aciona o Módulo OCR, que mapeia os pixels da imagem e extrai os caracteres manuscritos em formato de texto.
O sistema apresenta o texto extraído em um campo de texto editável para que o usuário possa realizar a "Revisão Humana", corrigir palavras que possa ter interpretado com pouca clareza devido à caligrafia.
O usuário confere o texto e toca no botão "Avaliar Redação".
A ViewModel empacota o texto revisado juntamente com as regras de rigor de correção definidas nas preferências.
A requisição é disparada para a IAG, solicitando uma avaliação baseada em competências.
A IAG retorna um JSON contendo a Nota Final e os parágrafos de feedback corretivo.
O sistema salva a avaliação no banco de dados local e sincroniza as informações com o back-end em segundo plano.
O sistema exibe o "Painel de Resultados", detalhando a nota e as sugestões de melhoria.
Quadro 28 - Detalhamento de Campos (Tela de Revisão e Submissão)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Tema
Branco
Texto livre
Campo de texto editável
Sim
Texto Extraído (OCR)
[Texto lido pela câmera]
Texto livre
Campo de texto expansível
Sim
Texto Digitado
Branco
Texto livre
Campo de texto expansível e editável
Não
Avaliar Redação




Botão Submit
Sim

Fonte: O autor(2026).

3.2. FLUXOS ALTERNATIVOS
A1 – Submissão via Digitação Direta
O usuário seleciona a opção "Digitar Redação".
O sistema pula as etapas de acionamento de câmera e OCR e exibe diretamente a tela com um campo de texto em branco.
O usuário digita a sua redação manualmente pelo teclado do dispositivo móvel.
O usuário toca em "Avaliar Redação".
O fluxo retorna ao fluxo básico.
A2 – Falha na Extração OCR (Imagem Ilegível ou Borrada)
No passo 6 do fluxo básico, o Módulo OCR analisa a imagem, mas não atinge o nível mínimo de confiança para extrair sentenças lógicas.
O sistema aborta o preenchimento automático.
O sistema exibe um alerta de orientação: "Não foi possível ler o texto com clareza. Certifique-se de que a foto está bem iluminada e que a folha não está amassada."
O fluxo retorna a solicitação de uma nova captura de imagem.
A3 – Validação Local de Texto Insuficiente (Economia de Recursos)
O usuário tenta submeter o texto para avaliação.
A ViewModel realiza uma validação heurística local.
O sistema bloqueia a chamada de rede para a IAG.
O sistema exibe a mensagem de alerta: "Texto muito curto. Uma redação exige um mínimo de desenvolvimento para ser avaliada."
O usuário complementa o texto.
4. PRÉ-CONDIÇÕES
O usuário deve estar autenticado.
O aplicativo deve possuir as permissões de acesso à Câmera do dispositivo.
As "Configurações da Inteligência Artificial" devem estar parametrizadas para balizar o nível de rigor que a IAG utilizará na correção.
Conexão ativa com a internet.
5. PÓS-CONDIÇÕES
Uma nova entrada de Redação é salva no banco de dados, contendo o texto bruto submetido, a nota quantitativa final e o feedback qualitativo, permitindo a futura consulta de histórico pelo aluno.
6. PONTOS DE EXTENSÃO
Caso o estudante não compreenda algum apontamento gramatical feito pela avaliação da IAG, ele poderá acionar a funcionalidade de Tutoria Interativa diretamente da tela de feedback para tirar dúvidas sobre a correção.
O registro da nota gera insumos que atualizam automaticamente o Painel de Desempenho.
7. PROTÓTIPOS
FIGURA 18 - Tela de captura OCR
.
Fonte: O autor(2026).
FIGURA 19 - Tela de avaliação de redação

Fonte: O autor(2026).
RF012 - Consultar Histórico de Redações
1. BREVE DESCRIÇÃO
Permite ao estudante visualizar uma listagem cronológica de todas as redações submetidas e avaliadas pelo sistema. A interface apresenta um resumo de cada submissão (tema, data e nota final) e permite que o usuário acesse os detalhes completos da correção, possibilitando a revisão contínua do feedback gerado pela IAG.
2. ATORES
Estudante (Concurseiro) - Ator Principal
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Visualizar Lista e Detalhes da Redação)
No menu principal do aplicativo, o usuário acessa o módulo de "Redações".
O sistema consulta o banco de dados para recuperar a lista de redações já avaliadas.
O sistema exibe a "Lista de Redações", ordenada da mais recente para a mais antiga.
Cada item da lista (Card) apresenta as seguintes informações resumidas: Tema, Data e Hora da submissão, e a Nota Final obtida.
O usuário toca em um item específico da lista.
O sistema transita para a tela de "Detalhes da Avaliação", exibindo o texto original submetido e o feedback completo gerado pela IAG, notas por competência, sugestões de melhoria e correção gramatical.
O usuário analisa o feedback e pode retornar à lista.
Quadro 29 - Detalhamento de Campos (Tela de Histórico e Detalhes)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Lista de Redações
Vazia
Registros do banco de dados
Componente de Lista
Sim
Card de Resumo


Título, Data (DD/MM/AAAA) e Nota
Componente Visual
Sim
Texto Original


Texto submetido
Bloco de Texto
Sim
Feedback da IAG


Análise estruturada
Bloco de Texto 
Sim

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Histórico Vazio
O sistema identifica que não existem redações avaliadas vinculadas à conta do usuário.
O sistema exibe um estado vazio com uma ilustração e a mensagem: "Você ainda não submeteu nenhuma redação.".
O sistema exibe um Botão de submit primária "Nova Redação" que, se acionado, direciona o usuário para submeter sua redação.
A2 – Busca/Filtro no Histórico
O usuário possui dezenas de redações e deseja encontrar uma específica.
O usuário utiliza a barra de busca localizada no topo da tela.
O usuário digita uma palavra-chave presente no Título ou no Tema.
O sistema filtra a lista em tempo real, exibindo apenas os resultados correspondentes.
4. PRÉ-CONDIÇÕES
O usuário deve estar autenticado no aplicativo.
Para que o fluxo básico seja completo, o usuário deve ter submetido e avaliado pelo menos uma redação.
5. PÓS-CONDIÇÕES
O usuário consegue reavaliar seus erros passados, consolidando o aprendizado. Nenhuma alteração de dados ocorre neste fluxo, é uma operação de leitura/consulta.
6. PONTOS DE EXTENSÃO
Na tela de detalhes, o sistema pode oferecer um botão para acionar o Tutor Interativo, caso o aluno tenha dúvidas sobre algum ponto específico do feedback exibido.
7. PROTÓTIPO
FIGURA 20 - Tela de histórico de redações

Fonte: O autor(2026).


RF013 - Manter Cronograma Dinâmico
1. BREVE DESCRIÇÃO
Permite ao estudante gerar um planejamento de estudos automatizado e distribuído em um calendário interativo. A geração é impulsionada pela IAG, que recebe parâmetros de disponibilidade do usuário (Data da prova, tempo de estudo diário, limite de matérias por dia e dias da semana disponíveis) e distribui o conteúdo programático do edital de forma otimizada, criando sessões de estudo equilibradas até o dia do exame.
2. ATORES
Estudante (Concurseiro) - Ator Principal
IAG (Sistema Externo)
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Gerar Novo Cronograma)
No menu de navegação, o usuário seleciona o módulo de "Cronograma".
O sistema exibe o formulário de parametrização.
O usuário seleciona a Pasta de Conteúdo (Concurso) base para o planejamento.
O usuário preenche os parâmetros de disponibilidade: Data da Prova, Minutos diários de estudo, Máximo de matérias por dia, e assinala quais dias da semana tem disponíveis para estudar.
O usuário aciona o botão "Gerar Cronograma Dinâmico".
A ViewModel valida os dados inseridos.
O sistema exibe uma tela de carregamento e empacota as disciplinas da pasta e as restrições de tempo em um prompt estruturado.
A requisição é enviada à IAG, que calcula a distribuição proporcional das matérias, criando sessões de estudo lógicas.
O sistema recebe a resposta, processa a matriz de sessões e grava no banco de dados.
O sistema sincroniza os dados recém-criados com o back-end.
O sistema encerra o carregamento e renderiza um calendário interativo na tela, exibindo os blocos de estudo distribuídos dia a dia.
Quadro 30 - Detalhamento de Campos (Tela de Parametrização do Cronograma)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Concurso
Selecione…
Pastas de concursos criados
Lista Suspensa
Sim
Data da prova
Branco
Formato DD/MM/AAAA
Seletor de Data
Sim
Tempo Diário
120
30 a 720 (Minutos)
Campo Numérico
Sim
Máx. Matérias por dia
2
1 a 5
Campo Numérico
Sim
Dias disponíveis
Todos
Seg, Ter, Qua, Qui, Sex, Sáb, Dom
Caixas de Seleção
Sim
Gerar Cronograma




Botão de Submit
Sim

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Validação de Data Inválida
A ViewModel identifica que a "Data da Prova" informada é igual à data atual do sistema ou encontra-se no passado.
O sistema bloqueia a requisição antes de acionar a IAG, poupando processamento de rede.
O sistema destaca o campo de data em vermelho e exibe a mensagem de erro: "A data da prova deve ser uma data futura válida."
O usuário corrige a data e tenta novamente.
A2 – Recalcular/Ajustar Cronograma Existente
Na tela do calendário interativo, o usuário percebe que atrasou as matérias da semana ou sua rotina mudou, e aciona o botão "Recalcular Cronograma".
O sistema abre o formulário de parâmetros (já preenchido com os valores anteriores).
O usuário altera os dias disponíveis ou o tempo diário e aciona "Recalcular".
O sistema limpa as sessões de estudo futuras no banco de dados.
O sistema aciona a IAG para redistribuir o conteúdo faltante a partir da data atual até a data da prova.
O calendário é atualizado.
A3 – Tempo Restante Insuficiente
No passo 8 do fluxo básico, a IAG analisa que o volume de disciplinas é muito alto para o tempo escasso (ex: faltam 2 dias para a prova e o usuário colocou apenas 30 minutos diários disponíveis).
O sistema recebe um alerta da inteligência artificial de impossibilidade lógica de cobertura total do edital.
O sistema exibe um aviso: "O tempo selecionado é insuficiente para cobrir todas as matérias até a data da prova. O cronograma foi montado focando apenas nos tópicos de maior peso."
O sistema renderiza o calendário com a versão resumida dos estudos.
4. PRÉ-CONDIÇÕES
O usuário deve estar autenticado.
É obrigatória a existência de pelo menos uma pasta de Concurso (RF003) devidamente estruturada com suas respectivas disciplinas, para que a IAG saiba o que deve ser distribuído no tempo.
5. PÓS-CONDIÇÕES
A tabela de "Sessões de Cronograma" no banco de dados é populada. O usuário passa a receber indicativos visuais diários sobre o que deve estudar naquela data específica, mitigando a sobrecarga cognitiva da curadoria de material.
6. PONTOS DE EXTENSÃO
Não se aplica.
7. PROTÓTIPO
FIGURA 21 - Tela de geração de cronograma

Fonte: O autor(2026).


RF014 - Interagir Tutoria Interativa
1. BREVE DESCRIÇÃO
Permite ao estudante iniciar uma sessão de chat contextual com a IAG atuando sob a persona de um tutor particular (1:1). Esta funcionalidade é acionada a partir da tela de correção de uma questão ou do feedback de uma redação. O sistema injeta automaticamente o contexto do erro no prompt, permitindo que o aluno faça perguntas abertas (ex: "Por que a alternativa C está errada?") e receba explicações didáticas e focadas, garantindo a aprendizagem ativa e a mitigação de dúvidas pontuais.
2. ATORES
Estudante (Concurseiro) - Ator Principal
IAG (Sistema Externo)
3. FLUXO DE EVENTOS
3.1. FLUXO BÁSICO (Esclarecer Dúvida Contextual)
O usuário visualiza a correção de uma questão recém-respondida ou o feedback de uma redação.
O usuário aciona o Botão de submit "Dúvidas? Pergunte ao Tutor".
O sistema abre uma interface de chat interativo.
Em background, a ViewModel carrega o "Contexto Ativo", o enunciado da questão, a alternativa que o aluno marcou, a resposta correta e a justificativa técnica, para injetar silenciosamente na próxima requisição.
O usuário digita a sua dúvida no campo de texto e toca no ícone de "Enviar".
O sistema exibe o balão de mensagem do usuário e um indicador de digitação para a IAG.
O sistema empacota o Contexto Ativo, a mensagem do aluno e o Tom do Tutor em um prompt estruturado e dispara a requisição para a IAG.
A IAG processa a requisição e retorna uma explicação didática focada na dúvida do aluno.
O sistema renderiza o balão de resposta da IAG na interface de chat.
Quadro 29 - Detalhamento de Campos (Interface de Tutoria Interativa)
Campo
Valor default
Domínio de valores
Tipo de campo na tela
Obrigatoriedade
Mensagem
Branco
Texto livre
Campo de texto editável
Sim
Enviar
Desabilitado


Botão de submit
Sim
Encerrar Tutoria




Botão de link
Não

Fonte: O autor(2026).


3.2. FLUXOS ALTERNATIVOS
A1 – Fuga de Escopo (Context Boundary Violation)
O usuário digita uma pergunta totalmente desconexa do contexto educacional.
O sistema envia a mensagem para a IAG.
A IAG, instruída pelo prompt limitador configurado na arquitetura do aplicativo, recusa-se a mudar de assunto.
A IAG retorna uma resposta padronizada de redirecionamento "Como seu tutor de estudos, meu foco é ajudar você com o concurso. Vamos voltar à dúvida sobre o conteúdo“.
O sistema exibe a resposta na interface de chat, protegendo a ferramenta contra abusos de uso.
A2 – Timeout ou Indisponibilidade da IAG
A requisição para a IAG sofre falha de conexão, instabilidade no servidor da nuvem ou estoura o tempo limite.
O sistema intercepta o erro e remove o indicador de digitação.
O sistema exibe um alerta não-intrusivo: "Seu tutor virtual está indisponível no momento devido a falhas na rede. Tente enviar a mensagem novamente em alguns segundos."
O texto digitado pelo aluno permanece no campo de entrada para que ele não perca o que escreveu.
4. PRÉ-CONDIÇÕES
O usuário deve possuir conexão ativa com a internet.
A sessão de tutoria deve obrigatoriamente ser instanciada a partir de uma entidade educacional preexistente, uma Questão ou uma Redação, para que a IAG tenha um contexto base.
5. PÓS-CONDIÇÕES
A dúvida do estudante é sanada, reforçando a memorização do conteúdo. Por motivos de privacidade, a sessão de chat é puramente stateless (sem estado), sendo o histórico da interação mantido apenas em memória temporária durante a execução da tela e completamente destruído após o encerramento da tutoria, sem sofrer qualquer tipo de persistência local ou em nuvem.
6. PONTOS DE EXTENSÃO
Não se aplica.
7. PROTÓTIPO
FIGURA 22 - Tela de tutor

Fonte: O autor(2026).