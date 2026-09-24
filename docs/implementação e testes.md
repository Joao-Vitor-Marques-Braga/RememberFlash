101 

# **3. IMPLEMENTAÇÃO** 

## 3.1. ARQUITETURA DE SOFTWARE 

A  arquitetura  do  sistema  foi  estruturada  seguindo  o  padrão _Clean  Architecture_ em conjunto  com  o  padrão  de  apresentação  MVVM _(Model-View-ViewModel)_ e  organização modular  por  funcionalidades _(feature-based)_ ,  de  forma  a  garantir  forte  separação  de responsabilidades, alta testabilidade e baixo acoplamento. 

A  camada  de  apresentação  foi  desenvolvida  utilizando  o _Jetpack  Compose_ ,  o  kit  de ferramentas  moderno  e  declarativo  do  Android  para  a  construção  de  interfaces  de  usuário reativas,  dinâmicas  e  fluidas.  O  estado  visual  de  cada  tela  é  gerenciado  de  forma  reativa  por _ViewModels_ , que expõem fluxos de dados unidirecionais  para a interface. 

A  camada  de  domínio  concentra  o  núcleo  das  regras  de  negócio  do  sistema,  sendo totalmente  independente  de _frameworks_ e  bibliotecas  externas.  Ela  é  composta  por  entidades de  domínio  puras,  contratos  de  repositórios  e  Casos  de  Uso,  responsáveis  por  orquestrar operações  complexas,  tais  como  o  algoritmo  de  repetição  espaçada,  a  geração  e replanejamento de cronogramas de estudo e a avaliação orientada de redações. 

Para  a  persistência  dos  dados,  foi  adotada  uma  estratégia  híbrida  ( _offline-first_ ).  No ambiente  local  do  dispositivo  móvel,  utilizou-se  o _Room  Database_ ,  uma  biblioteca  de abstração  sobre  o  banco  de  dados  relacional  SQLite,  responsável  por  armazenar  e  gerenciar com  integridade  referencial  as  tabelas  de  concursos,  disciplinas,  questões,  flashcards, simulados  e  metas  de  estudo,  permitindo  o  funcionamento  completo  da  aplicação  mesmo  sem conexão  com  a  internet.  Já  para  as  funcionalidades _online_ ,  sincronização  multi-dispositivo  e centralização  dos  dados  dos  usuários  na  nuvem,  foi  projetada  a  integração  com  um  servidor  de _backend_ que  utiliza  o  PostgreSQL,  um  sistema  gerenciador  de  banco  de  dados  relacional robusto  e  escalável.  Adicionalmente,  as  preferências  e  credenciais  sensíveis  de  sessão  do usuário  no  aplicativo  são  protegidas  localmente  por  meio  de  armazenamento  criptografado _(EncryptedSharedPreferences)._ 

A  camada  de  dados  integra  ainda  serviços  inteligentes  e  recursos  de  aprendizado  de máquina,  utilizando  o  Google  Gemini  para  geração  automatizada  de  conteúdo  pedagógico  e correção  analítica  de  redações,  além  do  Google  ML  Kit  para  reconhecimento  óptico  de caracteres (OCR) a partir de fotos e arquivos PDF. 

102 

Em  todas  as  camadas  da  aplicação  foi  adotada  a  linguagem  Kotlin,  que  adiciona tipagem  estática  robusta,  segurança  contra  valores  nulos  e  suporte  nativo  a  operações assíncronas  por  meio  de _Coroutines_ e _Flows_ .  Para  viabilizar  a  inversão  de  controle  e  o desacoplamento  entre  componentes,  utilizou-se  o _framework_ de  injeção  de  dependências _Dagger Hilt_ . 

As  camadas  da  aplicação  se  conectam  de  forma  integrada  e  unidirecional:  a  interface no _Jetpack  Compose_ captura  as  ações  do  usuário  e  as  encaminha  para  a _ViewModel_ ,  que aciona  os  Casos  de  Uso  correspondentes  na  camada  de  domínio.  Estes,  por  sua  vez,  aplicam as  regras  de  negócio  e  interagem  com  a  camada  de  dados  através  dos  repositórios  para consultar  ou  persistir  informações  no _Room_ ou  executar  requisições  inteligentes  à  API  do Gemini.  As  atualizações  de  dados  fluem  de  forma  reativa  de  volta  até  a  interface,  garantindo  o ciclo completo e consistente dos dados em toda a aplicação. 

## 3.2. GERÊNCIA DO PROJETO 

Para  o  controle  de  versões  e  rastreabilidade  do  código-fonte,  adotou-se  o  sistema  de controle  de  versão  distribuído  Git.  A  ferramenta  viabiliza  o  registro  histórico  das  alterações realizadas  ao  longo  do  ciclo  de  vida  do  software,  assegurando  auditoria,  integridade  e reversibilidade das modificações implementadas. 

A  hospedagem  remota  e  a  centralização  do  repositório  são  mantidas  na  plataforma GitHub,  permitindo  o  gerenciamento  do  fluxo  de  desenvolvimento,  a  integração  contínua  e  o acompanhamento sistemático da evolução da base de código. 

No  que  tange  à  gestão  temporal  e  ao  planejamento  das  entregas,  utilizou-se  o software _GanttProject_ para  a  modelagem  do  diagrama  de _Gantt_ e  acompanhamento  dos marcos  críticos  do  cronograma.  Complementarmente,  para  o  controle  operacional  das atividades  de  desenvolvimento,  empregou-se  a  metodologia  ágil _Kanban_ por  meio  da plataforma  Notion,  centralizando  o  detalhamento  de  tarefas,  a  definição  de  prazos  e  o  registro da documentação técnica das sprints. 

3.3. LINGUAGENS, BIBLIOTECAS, FRAMEWORKS E FERRAMENTAS 

O  desenvolvimento  do  sistema  contou  com  diferentes  tecnologias  que,  em  conjunto, possibilitaram  a  implementação  da  aplicação  móvel  com  suporte  a  inteligência  artificial  e funcionamento  offline-first.  Foram  utilizadas  linguagens  de  programação  modernas  e  com tipagem  estática,  além  de  bibliotecas  e _frameworks_ que  facilitaram  a  construção  de funcionalidades,  injeção  de  dependências,  persistência  e  reconhecimento  óptico  de  caracteres. 

103 

Também  foram  empregadas  ferramentas  de  apoio  para  automação  de _build_ ,  controle  de  versão e testes automatizados. 

## **3.3.1 Linguagens de programação** 

- Kotlin:  Linguagem  de  programação  moderna  com  tipagem  estática  adotada  como padrão  oficial  no  desenvolvimento  Android  nativo.  Destaca-se  pela  segurança  contra valores  nulos,  concisão  e  suporte  nativo  à  programação  assíncrona  por  meio  de corrotinas. 

- SQL:  Linguagem  utilizada  para  a  estruturação,  manipulação  e  consulta  dos  dados  nos bancos  de  dados  relacionais  da  aplicação  (SQLite  localmente  e  PostgreSQL  no ambiente em nuvem). 

## **3.3.2 Frameworks e bibliotecas (Frontend / Interface Móvel)** 

- AndroidX  Core  KTX:  Biblioteca  fundamental  do  ecossistema  Android  que  estende  as APIs  essenciais  do  sistema  operacional  com  recursos  idiomáticos,  concisos  e  seguros da linguagem Kotlin. 

- Jetpack  Compose  (Compose  BOM):  Kit  de  ferramentas  moderno  do  Google  para construção  de  interfaces  de  usuário  declarativas  em  Android  nativo.  Permite  criar componentes  visuais  reativos,  modulares  e  com  alto  desempenho  gráfico  sem  o  uso  de arquivos XML. 

- Compose  UI  &  Graphics:  Módulos  fundamentais  do  Jetpack  Compose  responsáveis pela  renderização  gráfica,  medição  de  layout,  desenho  de  primitivas  visuais  e manipulação de eventos de toque na tela. 

- Compose  UI  Tooling  &  Preview:  Bibliotecas  de  ferramentas  de  suporte  ao desenvolvimento  visual  que  viabilizam  a  visualização  prévia  imediata  (Preview)  de componentes  componíveis  diretamente  na  IDE  sem  necessidade  de  compilação  em dispositivo físico. 

- Material  Design  3  (Material3):  Biblioteca  oficial  de  componentes  de  interface  do Google  que  implementa  as  diretrizes  do  Material  You,  fornecendo  paleta  de  cores harmoniosa,  suporte  dinâmico  a  temas  claro  e  escuro,  tipografia  acessível  e componentes interativos padronizados. 

- AndroidX  Activity  Compose:  Biblioteca  responsável  por  integrar  o  ciclo  de  vida  da Activity  principal  do  Android  ao  ecossistema  de  árvores  de  nós  do  Jetpack  Compose, provendo o ponto de entrada (setContent) da interface gráfica. 

104 

- Navigation  Compose:  Biblioteca  responsável  pelo  gerenciamento  da  pilha  de navegação,  mapeamento  de  rotas  e  transições  fluidas  entre  telas  na  aplicação  de  forma declarativa e fortemente tipada. 

- Lifecycle  &  ViewModel  Compose:  Bibliotecas  do  AndroidX  que  integram  o  ciclo  de vida  dos  componentes  ao  Jetpack  Compose,  viabilizando  a  retenção  e  preservação  dos estados  de  tela  (UiState)  diante  de  rotações  de  tela  e  mudanças  de  configuração  do sistema operacional. 

- Hilt  Navigation  Compose:  Biblioteca  de  integração  entre  o  Dagger  Hilt  e  o  Navigation Compose,  permitindo  a  injeção  automática  de  dependências  em  ViewModels vinculados diretamente ao escopo das rotas do grafo de navegação. 

- Material  Icons  Extended:  Biblioteca  oficial  que  disponibiliza  uma  vasta  coleção  de ícones  vetoriais  padronizados  do  Material  Design,  utilizada  na  composição  das  ações  e navegação da interface do usuário. 

## **3.3.3 Frameworks e bibliotecas (Lógica de Negócio, IA, Nuvem e Persistência)** 

- Google  Generative  AI  SDK  (Gemini):  SDK  oficial  da  Google  para  integração  com  a família  de  modelos  Gemini,  empregado  na  geração  de  questões  de  simulados calibradas  por  banca,  elaboração  automatizada  de  flashcards  a  partir  de  textos, planejamento  do  cronograma  de  estudos  e  suporte  no  módulo  de  Tutoria  Interativa contextualizada. 

- Google  ML  Kit  (Text  Recognition):  Biblioteca  de  aprendizado  de  máquina  executada diretamente  no  hardware  do  dispositivo  (on-device)  para  Reconhecimento  Óptico  de Caracteres  (OCR),  utilizada  para  digitalizar  e  transcrever  fotos  e  imagens  de  redações com custo zero de nuvem e baixa latência. 

- PdfBox-Android:  Biblioteca  especializada  na  leitura,  interpretação  e  extração programática  de  texto  a  partir  de  documentos  em  formato  PDF,  utilizada  na  mineração contextual de conteúdos programáticos de editais de concursos públicos. 

- Room  Database  (Runtime  &  KTX):  Biblioteca  de  abstração  e  mapeamento objeto-relacional  (ORM)  do  Google  sobre  o  SQLite  local,  fornecendo  verificação  de consultas  SQL  em  tempo  de  compilação,  integridade  referencial  e  suporte  nativo  a fluxos  de  dados  assíncronos  e  reativos  por  meio  do  Room  KTX  integrado  a  Kotlin Coroutines e Flows. 

105 

- Supabase  Client  SDK  (Auth,  Postgrest  e  Storage):  Conjunto  oficial  de  bibliotecas  para conexão com a plataforma de Backend as a Service (BaaS) em nuvem: 

   - Supabase  Postgrest:  Responsável  pela  interface  RESTful  tipada  para sincronização,  consultas  e  mutações  de  dados  diretamente  no  banco  de  dados relacional PostgreSQL remoto. 

   - Supabase  Storage:  Módulo  voltado  para  o  upload,  gerenciamento  e recuperação segura de arquivos e documentos de editais anexados na nuvem. 

- Ktor  Client  (Core,  OkHttp  Engine,  Content  Negotiation  e  Serialization):  Cliente  HTTP assíncrono  multiplataforma  em  Kotlin,  utilizado  para  o  transporte  de  rede  e  integração de  baixo  nível  com  o  Supabase,  contando  com  o  motor  OkHttp,  negociação  de conteúdo e serialização de requisições. 

- Kotlinx  Coroutines  (Core  &  Android)  &  Flow:  Bibliotecas  oficiais  da  JetBrains  para gerenciamento  de  concorrência  estruturada,  execução  segura  de  rotinas  pesadas  em segundo  plano  (fora  da  thread  principal  de  interface)  e  propagação  reativa  de  fluxos assíncronos de dados. 

- Kotlinx  Serialization  JSON:  Framework  oficial  da  JetBrains  de  serialização  e desserialização  rápida  de  dados  estruturados  em  formato  JSON  com  geração  de  código em tempo de compilação, utilizado no tráfego de dados de rede e contratos DTO. 

- Dagger  Hilt:  Framework  de  injeção  de  dependências  padrão  da  plataforma  Android construído  sobre  o  Dagger,  responsável  por  automatizar  a  criação,  fornecimento  e destruição de instâncias desacopladas de classes, repositórios e casos de uso. 

- AndroidX  Security  Crypto  (EncryptedSharedPreferences):  Biblioteca  de  segurança que  provê  criptografia  simétrica  de  duas  camadas  associada  ao  hardware  de  segurança do  dispositivo  (Android  Keystore),  empregada  para  proteger  chaves  de  sessão  e  a chave  pessoal  de  API  do  Google  Gemini  fornecida  pelo  estudante  (arquitetura  Bring Your Own Key - BYOK). 

- Gson:  Biblioteca  do  Google  para  serialização  e  desserialização  de  objetos  em  JSON, utilizada  nos  conversores  de  tipo  do  banco  de  dados  local  (TypeConverters  do  Room) para  persistir  estruturas  complexas  (como  listas  de  itens  autorizados  e  proibidos)  em colunas relacionais. 

106 

## **3.3.4 Ferramentas de desenvolvimento** 

- Android  Studio  /  Antigravity  IDE:  Ambientes  de  desenvolvimento  integrado  (IDE) utilizados  na  codificação,  depuração,  análise  estática  de  código  e  emulação  da aplicação. 

- Git:  Sistema  de  controle  de  versão  distribuído,  utilizado  para  registrar  modificações  no código e gerenciar o histórico de versões. 

- GitHub:  Plataforma  de  hospedagem  remota  de  repositórios  Git,  utilizada  para centralizar o código-fonte e viabilizar o versionamento do projeto. 

- Gradle  (Kotlin  DSL):  Sistema  de  automação  de  compilação  (build)  e  gerenciador  de dependências do projeto Android. 

- KSP  (Kotlin  Symbol  Processing):  Mecanismo  de  processamento  de  anotações  em nível  de  compilador  Kotlin,  utilizado  pelo  Room  e  Hilt  para  geração  de  código otimizada e mais rápida. 

- PostgreSQL  /  Supabase  Dashboard:  Sistema  gerenciador  de  banco  de  dados  relacional e  console  administrativo  em  nuvem,  empregados  para  a  visualização  de  tabelas relacionais,  aplicação  de  migrações  e  monitoramento  de  integridade  e  segurança  RLS (Row Level Security). 

## **3.3.5 Ferramentas e bibliotecas de teste** 

- JUnit  4:  Framework  de  execução  de  testes  unitários  para  a  linguagem  Kotlin/Java  na Máquina  Virtual  Java  (JVM),  utilizado  para  validar  regras  de  negócio,  casos  de  uso  e algoritmos  determinísticos  (como  validação  matemática  de  CPF  e  recálculo  de cronograma). 

- Kotlinx  Coroutines  Test:  Utilitário  voltado  para  controle  temporal  determinístico  e sincronização  de  despachantes  de  corrotinas  (StandardTestDispatcher  e  runTest) durante a execução de testes assíncronos. 

- Mockito  Core  &  Mockito-Kotlin:  Framework  e  biblioteca  idiomática  de  simulação (mocking)  para  Kotlin,  empregados  no  isolamento  de  dependências  e  verificação  de chamadas de métodos durante os testes unitários da camada de domínio. 

- AndroidX  Arch  Core  Testing:  Fornece  regras  de  sincronização  de  tarefas  em  segundo plano (InstantTaskExecutorRule) para testes de componentes arquiteturais na JVM. 

107 

- AndroidX  Room  Testing:  Biblioteca  oficial  do  Google  destinada  à  criação  e  validação de  bancos  de  dados  relacionais  temporários  em  memória  (in-memory)  e  testes  de integridade de DAOs do Room. 

- AndroidX  Test  JUnit  (AndroidJUnit4):  Runner  oficial  do  ecossistema  AndroidX  para execução de testes que exigem o ambiente da plataforma Android. 

- Espresso  &  Compose  UI  Test:  Ferramentas  oficiais  para  criação  e  execução  de  testes automatizados  de  instrumentação  e  validação  de  fluxos  de  interação  na  interface  visual do usuário. 

## 3.4. PERSISTÊNCIA DE DADOS 

A  persistência  de  dados  assegura  a  disponibilidade  contínua  e  a  durabilidade  das informações  manipuladas  pelo  sistema,  impedindo  que  dados  do  estudante,  histórico  de resoluções  de  questões  e  parametrizações  de  estudos  sejam  perdidos  com  o  encerramento  do aplicativo ou desligamento do aparelho móvel. 

## **3.4.1 Banco de dados relacional local: Room Database (SQLite)** 

Em  nível  local,  as  informações  do  usuário  são  armazenadas  no  banco  de  dados SQLite  nativo  através  da  camada  de  abstração  do  Room  Database.  Foram  estruturadas entidades  relacionais  com  chaves  primárias  e  estrangeiras  representando  usuários,  concursos, disciplinas,  tópicos  temáticos, _flashcards_ ,  questões  de  simulados  e  cronogramas  diários  de estudos. 

Para  atender  à  Regra  de  Negócio  RN06,  implementou-se  a  técnica  de  exclusão  lógica _(Soft  Delete)_ na  entidade  de  concursos  por  meio  do  campo  booleano  isActive.  Quando  o estudante  opta  por  arquivar  ou  excluir  uma  pasta  de  concurso,  o  registro  não  é  expurgado fisicamente  do  banco  de  dados  SQLite,  mas  tem  seu  status  marcado  como  inativo  (isActive  = false),  o  que  preserva  integralmente  o  histórico  acumulado  de  métricas  e  resolução  de questões, permitindo a restauração futura do concurso sem corrupção de integridade. 

## **3.4.2 Sincronização e banco de dados relacional em nuvem: Supabase (PostgreSQL)** 

No  ambiente  em  nuvem,  a  persistência  centralizada  e  a  sincronização  são intermediadas  pela  plataforma  Supabase  através  de  um  banco  de  dados  relacional PostgreSQL.  Essa  estrutura  permite  que  dados  críticos  de  desempenho  do  estudante  e materiais  de  apoio  sejam  sincronizados  de  forma  segura  e  não  obstrutiva,  mantendo  backups persistentes e viabilizando o acesso contínuo e a integridade de dados multi-dispositivo. 

108 

## 3.5. TESTES AUTOMATIZADOS 

Os  testes  automatizados  desempenham  papel  indispensável  no  processo  de engenharia  de  software  do  RememberFlash,  garantindo  que  as  regras  de  negócio  essenciais  e as  restrições  estritas  do  aplicativo  sejam  rigorosamente  validadas  de  forma  repetível  e padronizada. 

## **3.5.1 Teste unitário no ecossistema Android** 

Na  camada  de  domínio  e  apresentação,  os  testes  unitários  foram  concebidos  com foco  na  validação  determinística  de  regras  críticas  sem  necessidade  de  emuladores  ou  conexão à  internet.  A  ordem  de  prioridade  da  suíte  de  testes  foi  estruturada  para  blindar  o  fluxo  central de valor pedagógico da aplicação: 

1. Cadastrar  Concursos  (RF003  /  RN04  /  RN06):  Validação  da  persistência  de diretrizes  do  certame,  validação  de  arquivos  de  edital  em  PDF,  parametrização predefinida  do  motor  de  IA  e  a  exclusão  lógica  (Soft  Delete)  com  preservação  de histórico. 

2. Gerar  Questões  e  Simulados  (RF008  /  RF009):  Validação  da  geração  de questões  calibradas  por  banca  e  matéria,  estruturação  de  simulados  proporcionais  e registro de métricas de tempo de resolução. 

3. Gerar  Cards  de  Estudo  (RF006  /  RF007):  Validação  da  criação  manual  e automatizada  de  flashcards  a  partir  da  extração  de  documentos  via  IA,  com  vinculação a disciplinas e tópicos programáticos. 

4. Gerar  Cronograma  Dinâmico  (RF013  /  RN05):  Validação  da  regra  estrita  de "Data  da  Prova"  futura  e  do  cálculo  proporcional  de  metas  diárias  de  horas  e  revisão com base nos dias disponíveis. 

5. Recalibrar  Cronograma  Autonomamente  (RF013  /  RN09):  Validação  do algoritmo  de  adaptação  pedagógica  que  aplica  multiplicador  adaptativo  de  foco  (1.3x  a 2.0x) sobre disciplinas com índice de acerto inferior a 70% após simulados. 

6. Restante  do  Sistema  (Ampla  Cobertura):  Validação  das  demais  regras  e componentes,  abrangendo  a  validação  matemática  de  CPF  e  unicidade  de  cadastro (RN01  /  RF001),  segurança  de  sessão  local  (RN02),  restrição  contextual  da  Tutoria Interativa  (RN07  /  RF014),  extração  OCR  de  redações  manuscritas  (RF011)  e transições  reativas  de  estados  de  tela  (Loading,  Success  e  Error)  nos  ViewModels  com Turbine. 

109 

## **3.5.2 Teste de análise estática** 

A  qualidade  e  segurança  do  código-fonte  foram  monitoradas  de  forma  contínua  por meio  da  ferramenta  Android  Lint  integrada  ao  fluxo  de  compilação  do  Android  Gradle  Plugin (AGP),  complementada  pelas  inspeções  estáticas  integradas  da  IDE  Android  Studio.  Esse mecanismo  possibilitou  a  detecção  precoce  de  inconsistências  de  sintaxe,  garantia  de compatibilidade  com  as  versões  de  API  do  Android  (minSdk  26  e  targetSdk  35),  eliminação de  potenciais  vazamentos  de  recursos  no  fechamento  de  arquivos  de  editais  e  prevenção  de recomposições desnecessárias de interface no Jetpack Compose. 

# **4. AVALIAÇÃO** 

## 4.1. VERIFICAÇÃO E AVALIAÇÃO 

A  etapa  de  verificação  e  avaliação  constitui  um  pilar  fundamental  da  engenharia  de software,  tendo  por  finalidade  constatar  empiricamente  se  o  produto  desenvolvido  atende  com precisão  aos  requisitos  funcionais  (RF001  a  RF014)  e  respeita  as  regras  de  negócio previamente  estabelecidas  (RN01  a  RN09).  No  projeto  RememberFlash,  a  estratégia  de avaliação  combinou  a  execução  de  uma  suíte  automatizada  de  testes  unitários  isolados  na JVM e inspeções rigorosas por meio de análise estática de código. 

## **4.1.1 Testes unitários** 

Para  assegurar  o  correto  funcionamento  e  a  estabilidade  dos  módulos  da  aplicação móvel,  foram  desenvolvidos  e  executados  testes  unitários  utilizando  o  framework  JUnit  4 integrado  às  bibliotecas  MockK,  Turbine  e  Kotlinx  Coroutines  Test.  A  priorização  dos  testes foi  estruturada  para  blindar  rigorosamente  o  pipeline  de  valor  central  do  concurseiro  no aplicativo: 

1.  Módulo  de  Cadastro  e  Gestão  de  Concursos:  Validação  da  persistência  de  diretrizes  do certame  (Banca,  Cargo,  Tipo  de  Questão  -  RF003),  validação  estrita  de  tamanho  e formato  de  editais  em  PDF  (até  15MB),  parametrização  dos  perfis  de  IA  sem  inserção de  prompt  livre  (RN04)  e  execução  de  exclusão  lógica  (RN06)  com  alternância  segura do status `isActive` sem perda de histórico. 

2.  Módulo  de  Geração  de  Questões  e  Simulados:  Validação  da  geração  procedimental  de questões  via  IAG  calibradas  por  nível  de  dificuldade  e  banca  examinadora  (RF008), 

110 

montagem  de  simulados  balanceados  proporcionais  ao  edital  (RF009)  e  contagem progressiva  do  tempo  de  resolução  por  questão  para  controle  de  ritmo  de  prova (RF010). 

3.  Módulo  de  Geração  e  Gestão  de  Flashcards:  Validação  da  criação  manual  e  geração automatizada  de  cartões  de  estudo  mnemônicos  a  partir  da  mineração  de  documentos via  IA  (RF006  /  RF007),  garantindo  o  correto  isolamento  e  vinculação  por  disciplina  e tópico temático. 

4.  Módulo  de  Geração  do  Cronograma  Dinâmico:  Validação  da  restrição  estrita  de  "Data da  Prova"  futura  (RN05  /  RF013),  distribuição  proporcional  da  carga  horária  semanal e cálculo de metas diárias de horas e revisão baseadas nos dias da semana disponíveis. 

5.  Módulo  de  Avaliação  de  Redações  via  OCR  e  IA  (essay):  Validação  do  fluxo  híbrido de  captura  textual:  extração  local  on-device  via  Google  ML  Kit  para  imagens contendo  textos  impressos  e  tipografados,  combinada  à  transcrição  de  caligrafia cursiva  e  manuscrita  executada  diretamente  pela  própria  IAG  multimodal  (Google Gemini);  validação  da  avaliação  analítica  com  rigor  pedagógico  parametrizado (flexível,  padrão  ou  rigoroso  -  RN04)  e  persistência  estruturada  do  histórico  de  notas  e feedbacks com critérios formativos de correção (RF011 / RF012). 

6.  Módulo  de  Recalibragem  Pedagógica  Autônoma  do  Cronograma:  Validação  do algoritmo  inteligente  que  analisa  o  aproveitamento  pós-simulado  (RN09  /  RF013), identificando  matérias  com  taxa  de  acertos  inferior  a  70%  e  aplicando  um multiplicador  adaptativo  de  reforço  (1.3x  a  2.0x)  para  redistribuir  a  rotina  de  estudos em prol das disciplinas deficitárias. 

7.  Restante do Sistema: Validação dos demais módulos fundamentais da aplicação: 

   - a.  Autenticação  e  Usuário  (`auth`  /  `user`):  Validação  matemática  rigorosa  de CPF  (algoritmo  dos  dígitos  verificadores  e  rejeição  de  11  dígitos  repetidos  - RN01  /  RF001),  integridade  de  tokens  e  controle  de  sessão  local  offline-first (RN02 / RF002). 

   - b.  Tutoria  Interativa:  Validação  das  salvaguardas  pedagógicas  que  travam conversação  livre  desconexa,  restringindo  o  prompt  da  IA  ao  contexto  de  erro na  questão  resolvida,  feedback  da  redação  ou  mineração  do  edital  (RN07  / RF014). 

111 

- c.  Estados  de  Interface  (ViewModels):  Auditoria  reativa  de  fluxos  `StateFlow` via  Turbine,  validando  a  alternância  consistente  de  estados  (Idle  ->  Loading  -> Success / Error). 

A  abordagem  adotada  permitiu  isolar  as  regras  de  negócio  de  dependências  de infraestrutura, simulando comportamentos de sucesso e de tratamento de exceções. 

No  módulo  1,  conforme  apresentado  na  Figura  30,  foi  realizado  um  teste  unitário  que valida  o  cenário  positivo  da  persistência  das  diretrizes  de  um  novo  concurso  sem  edital anexado,  executado  pelo  caso  de  uso  CreateContestUseCase.  Os  repositórios  de  concursos (ContestRepository)  e  de  disciplinas  (DisciplineRepository)  foram  configurados  com implementações  simuladas  (fake/mock),  permitindo  verificar  o  armazenamento  dos  dados  de forma  isolada  na  JVM.  Visto  que  o  certame  requer  a  definição  de  diretrizes  mandatórias (Banca  Organizadora,  Cargo  Pretendido  e  Formato  de  Questão  -  RF003)  e  que  o  cadastro  sem anexo  de  edital  deve  provisionar  uma  base  preliminar  de  estudos  para  o  usuário,  o  teste assegurou  que  o  método  insert  do  repositório  de  concursos  foi  executado  persistindo  com exatidão  as  informações  enviadas  e  definindo  o  certame  como  ativo  (isActive  =  true),  além  de confirmar  que  o  método  insert  do  repositório  de  disciplinas  foi  acionado  para  criar automaticamente  as  duas  disciplinas  padrão  de  "Conhecimentos  Gerais"  e  "Conhecimentos Específicos",  e  que  o  resultado  corresponde  ao  identificador  numérico  gerado  esperado  para  o concurso. 

112 

FIGURA 30 - Teste Unitário – Cadastro de Concurso e Criação de Disciplinas Básicas (RF003) 



<!-- Start of picture text -->
createContestUseCase should persist contest guidelines and create default disciplines when no POF is attached"() ~ runTest(testDispatcher<br>yllabusPdfUri = null,<br>assertliotNull (saved<br>assertTrue(disciplines.any { it.nane<br><!-- End of picture text -->

Fonte: O autor(2026). 

Em  seguida,  conforme  apresentado  na  Figura  31,  foi  realizado  um  teste  unitário  que valida  a  restrição  estrita  de  formato  e  tamanho  de  arquivos  de  edital  anexados  ao  formulário de  concurso,  executado  pelo _ViewModel  ContestFormViewModel_ .  O  componente  de apresentação  foi  instanciado  com  os  casos  de  uso  mockados  e  o  gerenciador  de  sincronização injetado.  Visto  que  o  requisito  RF003  determina  a  aceitação  restrita  de  editais  em  formato PDF  e  impõe  um  teto  dimensional  de  até  15MB  por  arquivo  para  mitigar  riscos  de  estouro  de memória  nativa  e  consumo  desnecessário  da  cota  multimodal  de  IAG,  o  teste  assegurou  que  o método _onPdfError_ intercepta  com  sucesso  arquivos  que  excedam  o  limite  estabelecido, rejeitando  a  inclusão  do  documento  na  lista  de  anexos  e  emitindo  a  notificação  de  erro correspondente  no  estado  da  interface,  assegurando  igualmente  que  a  inserção  de  um  arquivo válido remova alertas prévios e anexe o documento com sucesso. 

113 

FIGURA 31 - Teste Unitário – Validação de Anexo de Edital em PDF até 15MB (RF003) 



<!-- Start of picture text -->
ContestFormViewModel onPdfError should reject files exceeding the 15MB limit™() = runTest(testDispatcher<br>iewModel = ContestFormViewModel<br>savedStateHandle = SavedStateHandle(),<br>authRepository = fakeAuthRepository,<br>createContestUseCase = createContestUseCase,<br>updateContestUseCase = updateContestUseCase,<br>getContestByIdUseCase = getContestByIdUseCase,<br>syncManager = mockSyncManager<br>viewModel .onPdfError(fileSizeExceededMessage<br>state = viewModel.uistate.<br>assertEquals(fileSizeExceededMessage, state.error<br>assertTrue(state.pdfAttachments .isEmpty()<br>fun “ContestFormViewModel addPdfAttachment should accept valid PDF and clear errors”() = runTest(testDispatcher<br>viewModel = ContestFormViewModel<br>savedStateHandle = SavedStateHandle(),<br>authRepository = fakeAuthRepository,<br>createContestUseCase = createContestUseCase,<br>updateContestUseCase = updateContestUseCase,<br>getContestByIdUseCase = getContestByIdUseCase,<br>syncManager = mockSyncManager<br>viewModel .onPdfError("E t<br>assertEquals("E t » ViewHodel .uistate error<br>viewModel. addPdfattachment’ f, 1<br>state = viewModel.uistate<br>assertNull(state.error<br>assertEquals(1, state.pdfAttachments.size<br>assertEquals f", state.pdfAttachments[0] .name<br><!-- End of picture text -->

Fonte: O autor(2026). 

Adicionalmente,  conforme  apresentado  na  Figura  32,  foi  realizado  um  teste  unitário que  valida  a  parametrização  dos  perfis  de  Inteligência  Artificial  sem  inserção  de  prompt  livre, executado  pelo _ViewModel  ContestFormViewModel_ .  O  componente  de  apresentação  foi instanciado  com  os  casos  de  uso  mockados  e  repositórios  simulados.  Visto  que  a  regra  de negócio  RN04  e  o  requisito  RF005  determinam  que  o  comportamento  cognitivo  da  IAG generativa  deve  ser  calibrado  unicamente  por  meio  de  perfis  predefinidos  fechados (Dificuldade:  Fácil,  Médio,  Difícil;  Rigor  Pedagógico:  Flexível,  Padrão,  Rigoroso;  Tom  de Resposta:  Direto,  Explicativo,  Socrático),  mitigando  riscos  de  alucinação,  evasão  e  comandos livres  desconexos,  o  teste  assegurou  que  as  alterações  de  perfil  através  dos  métodos _onAiDifficultyChanged_ , _onAiRigorChanged_ e _onAiToneChanged_ atualizam  de  forma determinística  o  estado  da  tela,  e  que,  ao  submeter  o  formulário  via _onSaveClicked_ ,  esses 

114 

parâmetros  canônicos  são  persistidos  com  exatidão  na  entidade  do  concurso  no  repositório, sem qualquer exposição de campos de digitação aberta de prompts. 

FIGURA  32  -  Teste  Unitário  –  Teste  Unitário  –  Parametrização  de  Perfis  de  IA  com  padrões  pré-definidos (RN04, RF005) 



<!-- Start of picture text -->
ContestFormViewYodel should strictly configure predefined AI profiles without free prompt input”() = runTest(testDispatcher<br>viewModel - ContestFormViewModel<br>savedStateHandle = SavedStateHandle(),<br>authRepository = fakeAuthRepository,<br>createContestUseCase = createContestUseCase<br>updateContestUseCase = updateContestUseCase,<br>getContestByIdUseCase = getContestByIdUseCase,<br>syncManager = mockSyncManager<br>assertequals » viewHodel .uistate. aibifficulty<br>assertEquals 40", viewModel.uistate. -aiRigor<br>assertéquals » ViewHodel.uistate. aiTone<br>LewYodel .onAiDifficultyChanged<br>viewModel .onAiRigorChanged( “Rig<br>viewNodel .onAiTonechanged ts<br>state = viewModel.uistate<br>assertEquals("Dificil”, state.aidifficulty<br>assertéquals , state.aiRigor<br>assertEquals , state.aiTone<br>LewYodel .onTitleChanged: fcia Civil<br>viewHodel .onOrganizerChanged:<br>viewwodel .onJobPositionChanged<br>LewModel .onSaveClicked:<br>testScheduler. advanceUntilIdle<br>assertTrue(viewodel.uistate isSuccess<br>savedContest = fakeContestRepository.contests.first<br>assertEquals("Dificil”, savedContest .aibifficulty<br>assertEquals("Rig , savedContest.aiRigor<br>assertequals to", savedContest.aiTone<br><!-- End of picture text -->

Fonte: O autor(2026). 

Por  fim,  conforme  apresentado  na  Figura  33,  foi  realizado  um  teste  unitário  que valida  a  execução  de  exclusão  lógica  e  a  preservação  integral  do  histórico  acadêmico  do certame,  executado  pelo  caso  de  uso _SoftDeleteContestUseCase_ .  O  repositório  de  concursos  e o  de  disciplinas  foram  configurados  de  forma  isolada  na  JVM,  simulando  a  existência  prévia de  um  concurso  ativo  associado  a  matérias  cadastradas.  Visto  que  a  regra  de  negócio  RN06 determina  categoricamente  que  o  arquivamento  ou  exclusão  de  um  certame  não  deve  expurgar dados  físicos  do  banco  de  dados,  resguardando  o  histórico  de  simulados,  questões  e  métricas de  desempenho  do  estudante,  o  teste  assegurou  que  o  método _softDelete_ comutou  o  status _isActive_ para  false  sem  deletar  o  registro  nem  suas  disciplinas  vinculadas,  comprovando  ainda que  o  caso  de  uso _ReactivateContestUseCase_ é  capaz  de  restaurar  o  certame  ao  estado  ativo sem qualquer perda de consistência. 

115 

FIGURA 33 - Teste Unitário – Exclusão Lógica e Preservação de Histórico (RN06) 



<!-- Start of picture text -->
softDeleteContestUseCase should deactivate contest setting isActive to false without deleting data"() = runTest(testDispatcher) {<br>contest = Contest!<br>id = 101,<br>userId = testUser.id,<br>organizerName = “Cesgranrio”,<br>fakeContestRepository. insert (contest<br>fakeDisciplineRepository.insert (Discipline(contestId = 10L, name 8 )<br>result = softDeleteContestUsecase(10L<br>assertTrue(result is Result.Success<br>contestInDb ~ fakeContestRepository.contests.find { it.id -~ 101<br>assertNotNull (contest Indb<br>disciplinesindb ~ fakeDisciplineRepository.disciplines.filter { it.contestId -- 1@L<br>assertEquals(1, disciplinesInDb.size<br>softDeletecontestUseCase should fail when invalid contest ID is provided" () - runTest(testDispatcher<br>assertTrue(result is Result. Error<br><!-- End of picture text -->

Fonte: O autor(2026). 

A  Figura  34  ilustra  a  execução  com  êxito  da  suíte  de  testes  unitários  do  Módulo  de 

Concursos no ambiente de automação de testes do Android Studio via Gradle. 

FIGURA  34  -  Testes  Unitários  Aprovados  do  Módulo  de  Cadastro  e  Gestão  de  Concursos  (RF003,  RN04, RN06) 



<!-- Start of picture text -->
Class com.rememberflash.app.ContestTest<br>all > com.rememberflash.app > ContestTest<br>1 0 0 2.7728 100%<br>tests failures ignored duration eaiataes<br>Tests<br>Test Duration Result<br>ContestFormViewModel addPdtAttachment should accept valid PDF and clear errors 0.006s passed<br>ContestFormViewModel onPdfError should reject files exceeding the 15MB limit 0.005s passed<br>ContestFormViewModel onPdfError should reject invalid formats like docx or images 0.007s passed<br>ContestFormViewModel should require Organizer and Job Position when PDF edital is attached 0.0385 passed<br>ContestFormViewModel should strictly configure predefinedAl profiles without free prompt input 0.0995 passed<br>ContestFormViewModel should validate mandatory guidelines before saving without edital 0.004s passed<br>createContestUseCase should persist contest guidelines and create default disciplines when no PDF is attached 0.006s passed<br>getActiveContestsUseCase and getArchivedContestsUseCase<br>should filter correctly based on isActive flag 0.008s passed<br>reactivateContestUseCase should restore archived contest setting isActive to true 0021s passed<br>softDeleteContestUseCase should deactivate contest setting isActive to false without deleting data 0.007s passed<br>softDeleteContestUseCase should fail when invalid contest ID is provided 25718 passed<br><!-- End of picture text -->

Fonte: O autor(2026). 

116 

No  início  das  avaliações  do  Módulo  de  Questões  e  Simulados,  conforme  apresentado na  Figura  35,  realizou-se  um  teste  unitário  para  validar  o  fluxo  principal  de  geração automatizada  de  itens  avaliativos  via  Inteligência  Artificial  Generativa,  operacionalizado  pelo caso  de  uso _GenerateQuestionsUseCase_ .  Para  a  execução  do  teste,  os  repositórios  de disciplinas,  concursos  e  questões  foram  instanciados  com  implementações  simuladas,  e  o cliente  de  integração  com  a  API  Gemini  foi  mockado  para  retornar  um  payload  JSON estruturado,  contendo  enunciado,  alternativas,  gabarito  e  fundamentação  pedagógica.  Visto que  o  requisito  RF008  estabelece  que  as  questões  devem  ser  formuladas  sob  medida  segundo as  diretrizes  da  banca  examinadora  (ex.:  FGV),  formato  pretendido  (múltipla  escolha  com cinco  alternativas)  e  nível  de  complexidade  configurado  (ex.:  difícil),  o  teste  comprovou  que  o caso  de  uso  invocou  o  motor  de  IAG  com  os  parâmetros  exatos  de  calibração,  desserializou  o contrato  JSON  para  a  entidade  Question  com  a  marcação  de  proveniência  AI_GENERATED, limpou  os  registros  obsoletos  da  matéria  e  persistiu  com  sucesso  o  novo  lote  de  questões  no banco local. 

FIGURA 35 - Teste Unitário – Geração Procedimental de Questões via IAG (RF008) 

117 



<!-- Start of picture text -->
GenerateQuestions should call Gemini with correct parameters and save parsed questions (RF008)"() -<br>runTest<br>valid3sonResponse<br>trimndent<br>when mockGeminiClient .generateQuest ions(<br>disciplineName = “Direi 4 1M<br>format = a a (5 alternativas)”,<br>difficulty = :<br>quantity = 1,<br>‘thenReturn (valid3sonResponse<br>result<br>useCase(disciplineld = 10L, quantity = 1, theme = "Principios F is<br>assertTrue(result is Result.success<br>assertTrue( fakeQuest ionRepository.clearQuest ionsByDisciplineCalled<br>assertéquals(1, fakeQuestionRepository.savedQuestions. size<br>saved - fakeQuestionRepository.savedQuestions.first<br>assertEquals:<br>saved.statenent,<br>assertEquals(5, saved.options.size<br>assertEquals(®, saved.correctInde:<br>assertEquals(QuestionSource.AI_GENERATED, saved. source’<br><!-- End of picture text -->

Fonte: O autor(2026). 

Em  seguida,  conforme  apresentado  na  Figura  36,  foi  realizado  um  teste  unitário  que valida  a  orquestração  e  montagem  do  simulado  geral  do  certame  com  base  na  distribuição  de questões estipulada pela banca examinadora, executado pelo caso de uso _GenerateContestMockExamUseCase_ .  Os  repositórios  de  concursos,  disciplinas  e  questões foram  configurados  com  implementações  simuladas  na  JVM,  e  o  caso  de  uso  de  geração granular  foi  mockado  para  responder  com  êxito  a  cada  requisição  de  matéria.  Visto  que  o requisito  RF009  preconiza  que  o  simulado  geral  não  deve  gerar  um  número  arbitrário  de questões,  mas  sim  consolidar  a  prova  completa  respeitando  os  quantitativos  de  itens  definidos no  edital  do  certame  para  cada  disciplina  (neste  cenário,  10  questões  para  Direito Constitucional  e  10  questões  para  Direito  Administrativo,  totalizando  a  prova  da  banca  FGV), o  teste  assegurou  que  o  caso  de  uso  identificou  as  disciplinas  ativas  do  concurso,  obteve  a quantidade  de  questões  estipulada  para  cada  uma,  disparou  a  geração  das  respectivas  baterias, 

118 

emitiu  as  notificações  de  progresso  esperadas  a  cada  etapa  (onProgress)  e  concluiu  com 

sucesso a orquestração do simulado completo. 

FIGURA 36 - Teste Unitário – Montagem Balanceada de Simulado do Edital (RF009) 



<!-- Start of picture text -->
fun ~generateContestMockExam should orchestrate full mock exam with questions per discipline defined<br>{<br>runtest by exam board’() =<br>al contest =<br>Contest<br>id = 1L,<br>userId = “user 123",<br>title = “Concurso 13-60",<br>organizerName = "FGV",<br>syllabusPdfUri= null,<br>fakeContestRepository<br>insert (contest<br>fakeDisciplineRepository.<br>Discipline(id = 10L, insertcontestId = 1L, name = “Direito Constitucional”, weight = 10.0)<br>fakeDisciplineRepository.<br>Discipline(id = 20L, insertcontestId = 1L, name = “Direito Administrativo", weight = 10.0)<br>when” (mockGenerateQuestionsUseCase(10L, 10, null)).thenReturn(Result. success (Unit)<br>when” (mockGenerateQuestionsUseCase(2@L, 10, null)).thenReturn(Result. success (Unit)<br>ar progressCalls = @<br>al result =<br>useCase(1L) { current, total, _ -><br>progressCalls++<br>assertEquals(2, total)<br>assertTrue(result is Result.Success:<br>assertEquals(2, progressCalls:<br><!-- End of picture text -->

Fonte: O autor(2026). 

Por  fim,  conforme  apresentado  na  Figura  37,  foi  realizado  um  teste  unitário  que valida  a  persistência  estruturada  do  histórico  de  resolução  de  simulados  e  o  registro  individual do  tempo  despendido  por  questão,  executado  pelo  método _recordAttempt_ do  caso  de  uso _GenerateContestMockExamUseCase_ .  O  repositório  de  questões  foi  instanciado  com  uma implementação  simulada  na  JVM  para  recepcionar  os  registros  de  tentativa.  Visto  que  o requisito  RF010  estabelece  o  controle  de  ritmo  de  prova  por  meio  da  cronometragem progressiva  de  cada  questão  respondida  e  a  consolidação  dos  acertos  para  cálculo  estatístico de  desempenho,  o  teste  assegurou  que  o  caso  de  uso  converteu  adequadamente  os  mapas  de respostas  (answersMap)  e  de  tempos  gastos  em  milissegundos  (timesMap)  para  formato JSON  serializado  (answersJson  e  timesJson),  gravando  com  sucesso  a  entidade MockExamAttempt  com  a  pontuação  final  (2  de  3  acertos)  e  mantendo  a  integridade  temporal do simulado. 

119 

FIGURA 37 - Teste Unitário – Histórico de Resolução e Cronometragem por Questão (RF010) 



<!-- Start of picture text -->
recordAttempt should persist attempt in repository with answers history and per-question times>() =<br>runTest<br>answersMap = mapOf(1@1L to 2, 1@2L to @, 103L to 3<br>timesMap = mapOf(101L to 1500@L, 102L to 22@0@L, 103L to 185@0L<br>saveResult =<br>useCase.recordAttempt<br>contestId = 1L,<br>score = 2,<br>totalQuestions = 3,<br>answersMap = answersMap,<br>timesMap = timesMap,<br>assertTrue(saveResult is Result.Success<br>assertEquals(1, fakeQuestionRepository.savedAttempts.size<br>saved = fakeQuestionRepository.savedAttempts[@<br>assertEquals(1L, saved.contestId<br>assertEquals(2, saved.score<br>assertEquals(3, saved.totalQuestions<br>assertNotNull(saved.answersJson<br>assertTrue(saved. answersJson.contains("\"<br>assertNotNull(saved.timesJson<br>assertTrue(saved.timesJson!<br>! .contains("\"101\":15<br><!-- End of picture text -->

Fonte: O autor(2026). 

Complementando  a  validação  do  módulo  na  camada  de  apresentação  (MVVM), conforme demonstrado na Figura 38, realizou-se o teste unitário da classe QuestionResolveViewModel,  responsável  por  gerenciar  a  interação  do  usuário  durante  a resolução  do  simulado  em  tempo  real.  O  teste  utilizou  um  despachante  de  corrotinas controlado  (StandardTestDispatcher),  juntamente  com  implementações  simuladas  dos  casos de  uso  de  recuperação  de  disciplinas  e  questões,  além  de  um  SavedStateHandle  injetado.  Visto que  o  requisito  RF010  exige  resposta  imediata  na  experiência  de  resolução,  o  teste  assegurou que,  ao  selecionar  uma  alternativa  e  submeter  a  resposta,  o  ViewModel  avalia instantaneamente  o  gabarito,  reflete  o  acerto  ou  erro  no  estado  reativo  da  tela  (uiState.score), avança  progressivamente  entre  as  questões  e,  ao  atingir  o  último  item  da  lista,  comuta  o estado  da  prova  para  finalizado  (isFinished  =  true),  disparando  a  persistência  automática  da tentativa no repositório. 

FIGURA 38 - Teste Unitário – Resolução de Questões e Finalização de Simulado (RF010) 

120 



<!-- Start of picture text -->
nextQuestion should advance to next question and complete on last question = runTest (testDispatcher<br>testScheduler. advanceUntilidle<br>viewModel. selectoption(@<br>viewHodel. submitAnswer,<br>viewHodel .nextQuestion<br>state = viewModel.uistate.<br>assertEquals(1, state.currentIndex<br>assertFalse(state.isFinished<br>viewHodel. selectOption(1<br>viewModel . submitAnswer.<br>viewHodel .nextQuestion<br>testScheduler. advanceUntilidle<br>state - viewModel.uistate<br>assertTrue(state.isFinished<br>assertEquals(2, state. score<br>assertNotNull ( fakeQuestionRepo. savedAttempt<br>assertEquals(2, fakeQuestionRepo. savedAttempt?.score<br>assertEquals(2, fakeQuestionRepo. savedAttempt?. totalquestions<br><!-- End of picture text -->

Fonte: O autor(2026). 

A  Figura  39  ilustra  a  execução  com  êxito  da  suíte  de  testes  unitários  do  Módulo  de 

Questões e Simulados no ambiente de automação de testes. 

- FIGURA 39 - Testes Unitários Aprovados do Módulo de Questões e Simulados (RF008, RF009, RF010) 



<!-- Start of picture text -->
Package com.rememberflash.app<br>all > com.rememberflash.app<br>10 C) 0 2.1968 100%<br>tests failures ignored duration ot<br>Classes<br>Class Tests Failures Ignored Duration Success rate<br>GenerateContestMockExamUseCaseTest 2 oO 0 1.940s. 100%<br>GenerateQuestionsUseCaseTest 3 0 0 0.166s 100%<br>QuestionResolveViewModelTest 5 0 0 0.090s 100%<br><!-- End of picture text -->

Fonte: O autor(2026). 

No  início  das  avaliações  do  Módulo  de  Flashcards,  conforme  demonstrado  na  Figura 40,  realizou-se  um  teste  unitário  para  validar  as  regras  de  integridade  e  a  inicialização  de parâmetros  mnemônicos  na  criação  manual  de  cartões  de  estudo,  executado  pelo  caso  de  uso CreateFlashcardUseCase.  O  repositório  de  flashcards  foi  instanciado  com  uma  implementação simulada  em  memória  na  JVM.  Visto  que  o  requisito  RF006  exige  a  validação  estrita  dos dados  para  impedir  cartões  vazios  ou  órfãos  e  estabelece  a  adoção  do  algoritmo  de  repetição 

121 

espaçada  SM-2  para  orientar  as  futuras  revisões  ativas,  o  teste  assegurou  que  o  caso  de  uso barrou  tentativas  com  campos  vazios  ou  identificador  de  disciplina  nulo  e,  diante  de  dados válidos, persistiu o cartão com a marcação FlashcardSource.MANUAL. 

FIGURA 40 - Teste Unitário – Teste Unitário – Criação Manual e Inicialização SM-2 de Flashcard (RF006) 



<!-- Start of picture text -->
createFlashcard should succeed with valid data and initialize default SM-2 metrics’() = runTest<br>flashcard = Flashcard<br>disciplineld = 10L,<br>topicId = SL,<br>front = e éa a a iond ys<br>back = i 2 ,<br>source = FlashcardSource.MANUAL<br>al result = useCase(flashcard<br>assertIrue(result is Result.Success<br>assertEquals(1, fakeFlashcardRepository. flashcards. size<br>saved = fakeFlashcardRepository.flashcards[®<br>seca toile hl shed ee<br>assertEquals(SL, saved.topicId<br>assertEquals(FlashcardSource.MANUAL, saved.source<br>assertEquals(2.5, saved.easeFactor, 0.001<br>assertEquals(@, saved. interval<br>assertEquals(@, saved.repetitions<br><!-- End of picture text -->

Fonte: O autor(2026). 

Em  seguida,  conforme  apresentado  na  Figura  41,  foi  executado  um  teste  unitário  para validar  o  motor  de  mineração  e  síntese  de  flashcards  a  partir  de  documentos  de  estudo  via Inteligência Artificial Generativa, operacionalizado pelo caso de uso GenerateFlashcardsFromTextUseCase.  Para  o  teste,  o  cliente  de  integração  com  a  API  Gemini foi  mockado  para  responder  com  uma  estrutura  JSON  contendo  perguntas  e  respostas formuladas  a  partir  do  texto  de  entrada.  Visto  que  o  requisito  RF007  determina  que  o aplicativo  deve  ser  capaz  de  minerar  resumos  e  materiais  em  PDF  transformando-os  em cartões  estruturados  vinculados  à  disciplina  e  tópico  de  estudo,  o  teste  comprovou  que  o  caso de  uso  rejeitou  textos  insuficientes  ou  sem  camada  legível  de  OCR  (<  50  caracteres),  invocou a  IA  com  o  texto  fornecido,  processou  o  retorno  JSON  estruturado,  limitou  a  quantidade  ao teto  requisitado  e  persistiu  os  cartões  vinculados  à  disciplina  e  ao  tópico  com  a  marcação FlashcardSource.PDF_EXTRACT. 

122 

FIGURA 41 - Teste Unitário – Mineração Automatizada de Flashcards via IA (RF007) 



<!-- Start of picture text -->
fakeF lashcardRepository.flashcards[@].front<br><!-- End of picture text -->

Fonte: O autor(2026). 

As  Figuras  42  e  43  ilustram  a  execução  com  êxito  da  suíte  de  testes  unitários  do Módulo  de  Flashcards  no  ambiente  de  automação  de  testes,  sendo  7  testes  cujo  resultados atestaram  a  robustez  do  aplicativo  na  criação  manual  e  na  sintetização  inteligente  de documentos didáticos em cartões de memorização. 

FIGURA 42 - Testes Unitários Aprovados – Criação Manual e Inicialização SM-2 de Flashcards (RF006) 



<!-- Start of picture text -->
Class com.rememberflash.app.CreateFlashcardUseCaseTest<br>all > com.rememberflash.app > CreateFlashcardUseCaseTest<br>4 0 0 0.2518 100%<br>tests failures ignored duration successful<br>Tests<br>Test Duration Result<br>createFlashcard should fail when back is blank 0.005s passed<br>createFlashcard should fail when disciplineld is invalid 0.003s passed<br>createFlashcard should fail when front is blank 0.003s passed<br>createFlashcard should succeed with valid data and initialize default SM-2 metrics 0.240s passed<br><!-- End of picture text -->

Fonte: O autor(2026). 

123 

FIGURA 43 - Testes Unitários Aprovados – Mineração e Geração de Flashcards via IA (RF007) 



<!-- Start of picture text -->
Class com.rememberflash.app.GenerateFlashcardsFromTextUseCaseTest<br>all > com.rememberflash.app > GenerateFlashcardsFromTextUseCaseTest<br>3 0 0 1.470s 100%<br>tests failures ignored duration cen<br>Tests<br>Test Duration Result<br>useCase should generate flashcards and persist with correct discipline and topic 0.004s passed<br>useCase should respect quantity limit 0.081s passed<br>useCase should return error for text shorter than 50 chars 1.385s passed<br><!-- End of picture text -->

Fonte: O autor(2026). 

No  início  das  avaliações  do  cronograma,  conforme  demonstrado  na  Figura  44, realizou-se  um  teste  unitário  focado  na  regra  de  negócio  RN05,  executado  pela  classe ScheduleViewModel.  O  teste  utilizou  implementações  simuladas  de  repositórios  e  um despachante  de  teste  de  corrotinas  (StandardTestDispatcher).  Ao  simular  a  entrada  de  uma data  retroativa  no  formulário  (ontem),  o  teste  comprovou  que  o  ViewModel  interceptou  a solicitação  antes  de  qualquer  envio  de  dados,  comutou  o  estado  de  erro  da  tela  para  a mensagem  "A  data  da  prova  deve  ser  uma  data  futura  válida."  e  bloqueou  a  criação  do  plano, garantindo a restrição temporal estipulada pela RN05 e RF013. 

Figura 44 - Teste Unitário – Validação de Data Futura no Cronograma de Estudos (RN05, RF013) 

124 



<!-- Start of picture text -->
testViewModelValidatesFutureExamDate() = runTest<br>savedStateHandle = SavedStateHandle(mapOf( estI to 1L)<br>viewModel =<br>ScheduleViewModel<br>savedStateHandle = savedStateHandle,<br>authRepository = authRepository,<br>contestRepository = contestRepository,<br>disciplineRepository = disciplineRepository,<br>scheduleRepository = scheduleRepository,<br>generateStudyScheduleUseCase = useCase,<br>syncManager = mock(),<br>testDispatcher. scheduler.advanceUntilIdle<br>pastDate = System.currentTimeMillis - 24 * 6@ * 60 * 1000<br>viewModel. onExamDateChanged(pastDate<br>viewModel .onGenerateScheduleClicked<br>testDispatcher.scheduler. advanceUntilIdle<br>assertEquals<br>viewModel .uiState. -error,<br><!-- End of picture text -->

Fonte: O autor(2026). 

Em  seguida,  conforme  apresentado  na  Figura  45,  realizou-se  um  teste  unitário  para validar  a  orquestração  e  o  cálculo  distributivo  do  plano  de  estudos  personalizado,  executado pelo  caso  de  uso  GenerateStudyScheduleUseCase.  Para  o  teste,  os  repositórios  de  concursos, disciplinas  e  cronogramas  foram  instanciados  com  implementações  simuladas  (fakes),  e  o cliente  de  integração  com  a  IA  generativa  (GeminiScheduleClient)  foi  mockado  para responder  com  sessões  de  estudo  balanceadas  entre  as  matérias  cadastradas  (Língua Portuguesa  e  Direito  Constitucional).  Visto  que  o  requisito  RF013  preconiza  que  o cronograma  deve  distribuir  a  carga  horária  semanal  de  acordo  com  a  disponibilidade informada  pelo  estudante  (neste  cenário,  120  minutos  diários,  máximo  de  duas  disciplinas  por dia  e  dias  disponíveis  às  segundas,  terças  e  quartas-feiras),  o  teste  assegurou  que  o  caso  de  uso gerou  o  plano  com  sucesso  (id  =  100),  calculou  com  exatidão  a  divisão  temporal  em  sessões 

125 

de  60  minutos  para  cada  matéria  e  persistiu  as  metas  diárias  (DailyGoal)  correspondentes  no repositório, garantindo a consistência das diretrizes de estudo até a data da prova. 

FIGURA 45 - Teste Unitário – Geração de Cronograma Dinâmico e Metas Diárias (RF013) 



<!-- Start of picture text -->
testGenerateScheduleSuccessfully() = runTest<br>al fakeGeminiJson =<br>-trimIndent<br>geminiClient.resultJson = fakeGeminiJson<br>futureExamDate = System.currentTimeMillis() + 10 * 24 * 68 * 60 * 1000L<br>result = useCase<br>contestId = 1L,<br>examDateLong = futureExamDate,<br>minutesPerDay = 120,<br>maxSubjectsPerDay = 2,<br>availableDaysOfWeek = listOf("Segunda", “Terca", “Quart:<br>assertTrue(result.isSuccess<br>assertEquals(100L, result.getOrNull()<br>assertEquals(2, scheduleRepository.insertedGoals.size<br>assertEquals(6@, scheduleRepository.insertedGoals[@].targetMinutes<br>assertEquals(6@, scheduleRepository.insertedGoals[1].targetMinutes<br><!-- End of picture text -->

Fonte: O autor(2026). 

A  Figura  46  ilustra  a  execução  com  êxito  da  suíte  de  testes  unitários  do  Módulo  de 

Cronograma Dinâmico no ambiente de automação de testes. 

126 

FIGURA 46 - Teste Unitário – Transcrição Multimodal e Fallback de OCR na Redação (RF011) 



<!-- Start of picture text -->
Class com.rememberflash.app.ScheduleTest<br>all > com.rememberflash.app > ScheduleTest<br>5 0 0 1.691s 100%<br>tests failures ignored duration eneeeestil<br>Tests |<br>Test Duration Result<br>testGenerateScheduleClearsOldGoals 0.283s passed<br>testGenerateScheduleHandlesInsufficientTimeWarning 0.012s passed<br>testGenerateScheduleSuccessfully 0.005s passed<br>testGenerateScheduleWeeklyPlanLongTermUntil2027 0.015s passed<br>testViewModelValidatesFutureExamDate 1.376s passed<br><!-- End of picture text -->

Fonte: O autor(2026). 

A  execução  da  classe  ScheduleTest  resultou  em  100%  de  aproveitamento  em  todos os  5  cenários  avaliados,  com  tempo  total  de  execução  na  JVM  de  aproximadamente  5 segundos.  Os  resultados  atestaram  que  o  módulo  impede  anomalias  temporais  de agendamento  retroativo  (RN05)  e  calcula  com  exatidão  a  distribuição  horária  e  as  metas diárias  personalizadas  de  estudo  (RF013),  assegurando  a  flexibilidade  e  a  consistência requeridas na preparação de concurseiros. 

No  início  das  validações  de  redação,  conforme  apresentado  na  Figura  47,  realizou-se um  teste  unitário  no  caso  de  uso  ExtractTextFromImageUseCase  para  validar  a  resiliência  da esteira  de  OCR  e  transcrição  caligráfica.  O  teste  utilizou  implementações  mockadas  do transcrevedor  multimodal  (HandwrittenTranscriber),  do  extrator  óptico  local  (TextExtractor)  e do  repositório  de  autenticação  (AuthRepository).  Visto  que  o  requisito  RF011  demanda  alta fidelidade  na  leitura  de  redações  manuscritas  de  concursos,  o  teste  comprovou  que  o  caso  de uso  priorizou  o  motor  de  IAG  multimodal  do  Gemini  quando  a  chave  de  API  estava  presente e,  ao  simular  uma  falha  de  conexão  na  nuvem,  acionou  imediatamente  o  fallback  do  OCR local  pelo  ML  Kit,  garantindo  a  extração  do  texto  com  sucesso  mesmo  diante  de  oscilações  de conectividade. 

127 

FIGURA 47 - Teste Unitário – Transcrição Multimodal e Fallback de OCR na Redação (RF011) 



<!-- Start of picture text -->
extractText should prioritize AI handwriting transcription when Gemini API key is configured’() = runTest<br>when” (mockAuthRepository.hasGeminiApikey()) .thenReturn( t<br>when” (mockHandwrittenTranscriber. transcribeHandwritten(mockUri)<br>thenReturn| d g<br>al result = extractUseCase(mockUri<br>assertTrue(result is Result.Success<br>assertEquals Dir gura » (result as Result.Success)<br>extractText should fallback to local OCR when AI transcription fails or throws exception” () = runTest<br>when” (mockAuthReposi<br>when” (mockHandurittenTranscriber.tory.hasGeminiApikey())transcribeHandwritten(mockUri).thenReturn:<br>- thenThrow(RuntimeException( "Time e en”)<br>when” (mockTextExtractor.extractText<br>‘thenReturn("Texto extraido com suces(mockUri) t loca:<br>result = extractUseCase(mockUri<br>assertTrue(result is Result.Success<br>assertEquals("Texto extraido com suce s o pelo t local.", (result as Result.Success)<br><!-- End of picture text -->

Fonte: O autor(2026). 

Em  seguida,  conforme  ilustrado  na  Figura  48,  foi  executado  um  teste  unitário  no caso  de  uso  EvaluateEssayUseCase  para  comprovar  a  injeção  contextual  de  diretrizes pedagógicas  e  a  persistência  do  feedback  estruturado.  Para  o  teste,  o  repositório  do  concurso pai  (ContestRepository)  foi  configurado  com  a  banca  FGV,  perfil  de  rigor  "Rigoroso"  e  tom "Analítico  e  Crítico",  enquanto  o  avaliador  de  IA  (EssayEvaluator)  foi  mockado  para responder  com  um  payload  estruturado  de  correção  contendo  notas  por  competência,  pontos fortes,  oportunidades  de  melhoria  e  nota  geral  de  8.5.  Visto  que  as  regras  RF012  e  RN04 estabelecem  que  a  IA  deve  avaliar  a  redação  balizada  estritamente  pelo  perfil  configurado  no certame,  o  teste  assegurou  que  o  caso  de  uso  injetou  com  exatidão  a  banca  e  o  tom predefinidos,  realizou  o  parsing  seguro  do  JSON  retornado  e  persistiu  com  êxito  a  avaliação completa e a nota calculada na entidade Essay. 

FIGURA 48 - Teste Unitário – Correção de Redação com Injeção de Rigor e Tom da Banca (RF012, RN04) 

128 



Fonte: O autor(2026). 

A  Figura  49  ilustra  a  execução  com  êxito  da  suíte  de  testes  unitários  do  Módulo  de 

Redações no ambiente de automação de testes. 

- FIGURA 49 - Testes Unitários Aprovados do Módulo de Avaliação de Redações (RF011, RF012, RN04) 



<!-- Start of picture text -->
Class com.rememberflash.app.EssayTest<br>all > com.rememberflash.app > EssayTest<br>5 C) 0 1.6738 100%<br>tests failures ignored duration successful<br>Tests<br>Test Duration Result<br>evaluateEssay should fail when essay extractedText is blank 1.600s passed<br>evaluateEssay should inject parent contest banca, rigor and tone into Al evaluator and persist feedback with score 0.0125 passed<br>extractText should fallback to local OCR when Al transcription fails or throws exception 0.051s passed<br>extractText should prioritize Al handwriting transcription when Gemini API key is configured 0.005s passed<br>extractText should return friendly error when no text is found in image 0.005s passed<br><!-- End of picture text -->

Fonte: O autor(2026). 

129 

A  execução  da  classe  EssayTest  obteve  100%  de  aprovação  nos  5  cenários  avaliados. Os  resultados  atestaram  empiricamente  a  robustez  da  arquitetura  híbrida  de  digitalização  de textos  manuscritos  (RF011)  e  a  eficácia  da  correção  orientada  por  perfis  calibrados  de  IA  sem concessão  de  prompt  livre  (RF012,  RN04),  proporcionando  ao  estudante  um  diagnóstico pedagógico aprofundado e consistente com os critérios reais de bancas de concursos públicos. 

Conforme  ilustrado  na  Figura  50,  realizou-se  um  teste  unitário  no  caso  de  uso ProposeScheduleRecalculationUseCase  para  validar  a  lógica  matemática  de  recalibragem autônoma.  O  cenário  simulou  a  finalização  de  um  simulado  no  qual  o  concurseiro  obteve  50% de  acertos  em  Língua  Portuguesa  (5  de  10)  e  90%  em  Direito  Constitucional  (9  de  10).  O  teste comprovou  que  o  algoritmo  autônomo  interceptou  o  índice  inferior  a  70%  preconizado  pela RN09,  catalogou  Língua  Portuguesa  como  item  de  dificuldade  e  calculou  a  nova  proporção aplicando  o  multiplicador  de  reforço  de  1.5x,  elevando  a  cota  diária  de  Português  de  60  para 72  minutos  e  rebalanceando  Constitucional  para  48  minutos,  atendendo  plenamente  à adaptabilidade requerida pelo RF013. 

FIGURA 50 - Teste Unitário – Algoritmo Adaptativo de Recalibragem de Cronograma (RN09, RF013) 

130 



<!-- Start of picture text -->
1eL to Pair(S, 10),<br>2@L to Pair(9, 10<br>al result = useCase(contestId = 100L, disciplinePerformance = performanceMap<br>al proposal = (result as Result.Success) .data<br>assertEquals(1, proposal difficulties. size<br>difficulty - proposal.difficulties.first<br>assertEquals("Lingua Portuguesa”, difficulty.disciplineName<br>assertEquals(50.0, difficulty.accuracy, 0.01<br>compPortuguese = proposal.comparisonList.first { it.disciplineId == 1eL<br>assertTrue(compPortuguese.proposedMinutesPerDay > compConst .proposedMinutesPerDay<br>assertéquals(72, compPortuguese.proposedMinutesPerDay<br><!-- End of picture text -->

Fonte: O autor(2026). 

A Figura 51 ilustra a execução com êxito da suíte de testes unitários do Módulo de 

Recalibragem de Cronograma no ambiente de testes. 

FIGURA 51 - Testes Unitários Aprovados do Módulo de Recalibragem de Cronograma (RN09, RF013) 



<!-- Start of picture text -->
Class com.rememberflash.app.ScheduleRecalculationTest<br>all > com.rememberflash.app > ScheduleRecalculationTest<br>4 0 0 0.3298 100%<br>tests failures ignored duration successful<br>Tests<br>Test Duration Result<br>proposeRecalculation should fail when contest has no disciplines registered 0.004s passed<br>proposeRecalculation should fail when no active schedule exists for contest 0.300s passed<br>proposeRecalculation should identify deficit below 70 percent and apply adaptive difficulty multiplier 0.022s passed<br>proposeRecalculation should retain base schedule when all disciplines have accuracy above 70 percent 0.003s passed<br><!-- End of picture text -->

Fonte: O autor(2026). 

131 

Conforme apresentado na Figura 52, realizou-se um teste unitário no caso de uso RegisterUseCase para validar as regras de segurança cadastral e persistência de sessão. O primeiro cenário comprovou a aplicação estrita da RN01, demonstrando que o caso de uso consulta o repositório local Room e bloqueia imediatamente tentativas de cadastro com CPF duplicado, emitindo a mensagem "CPF já cadastrado no sistema". O segundo cenário validou o fluxo principal de sucesso (RF001, RF002 e RN02), assegurando que o sistema sanitiza a máscara do CPF, persiste o registro na base de dados e gera um token criptográfico de autenticação com extensão superior a 32 caracteres para garantir a sessão segura offline-first. 

FIGURA  52  -  Teste  Unitário  –  Unicidade  de  CPF  no  Room  e  Geração  de  Sessão  Criptográfica  (RN01,  RN02, RF001, RF002) 



<!-- Start of picture text -->
“registerUseCase should fail with AuthException message when CPF is already registered in Room’() = runTest<br>validCpf =<br>fakeUserRepository.saveUser<br>User(<br>id = sting. ,<br>cpf = validCpf<br>)<br>result = registerUseCase<br>name = drio",<br>email = il ,<br>password = €<br>assertTrue(result is Result.Error<br>assertEquals("CPF » (result as Result.Error).message:<br>registerUseCase should succeed for valid CPF and save cryptographic token session”() = runTest<br>validCpf = "11 777-35<br>result = registerUseCase<br>name = ,<br>cpf = validCpF,<br>email = 8 .<br>assertTrue(result is Result.Success<br>assertNotNull (fakeAuthRepository. savedToken<br>assertTrue(fakeAuthRepository.savedToken!!.length >= 32<br>assertEquals("Carlos Silva", fakeAuthRepository.savedUser? .name<br>assertEquals » fakeAuthRepository.savedUser?.cpf<br>assertEquals(1, fakeUserRepository.registeredUsers.size<br><!-- End of picture text -->

Fonte: O autor(2026). 

132 

A  Figura  53  ilustra  a  execução  com  êxito  da  suíte  de  testes  unitários  do  Módulo  de Autenticação no ambiente de automação de testes. 

FIGURA 53 - Testes Unitários Aprovados do Módulo de Autenticação e Usuário (RN01, RF001, RF002) 



<!-- Start of picture text -->
Class com.rememberflash.app.RegisterUseCaseTest<br>all > com.rememberflash.app > RegisterUseCaseTest<br>7 0 0 0.246s<br>tests failures ignored duration arses!<br>Tests<br>Test Duration Result<br>registerUseCase should fail with AuthException message when CPF is already registered in Room 0.003s passed<br>registerUseCase should succeed for valid CPF and save cryptographic token session 0.202s passed<br>validateCpf should pass for valid CPFs. 0.001s passed<br>validateCpf should throw AuthException when CPF has all identical digits 0.035s passed<br>validateCpf should throw AuthException when CPF has invalid length Os passed<br>validateCpf should throw AuthException when first check digit is mathematically invalid 0.005s passed<br>validateCpf should throw AuthException when second check digit is mathematically invalid Os passed<br><!-- End of picture text -->

Fonte: O autor(2026). 

Conforme  ilustrado  na  Figura  54,  realizou-se  um  teste  unitário  na  classe TutorChatViewModel  para  comprovar  a  segurança  dialógica  e  a  tolerância  a  falhas.  O primeiro  cenário  atestou  que  a  mensagem  enviada  pelo  usuário  invoca  a  IA  generativa balizada  pelo  contexto  da  dúvida  e  anexa  a  explicação  analítica  na  interface  (RN07,  RF014). O  segundo  cenário  comprovou  a  salvaguarda  de  resiliência  estipulada  pelo  requisito  RF014 (Cenário  Alternativo  A2):  ao  simular  uma  falha  de  conexão  com  a  API  na  nuvem,  o ViewModel  interceptou  a  exceção,  restaurou  a  pergunta  digitada  na  caixa  de  texto  (input)  e notificou  o  estudante  com  um  alerta  amigável  de  rede,  evitando  perda  de  dados  e  retrabalho de digitação. 

FIGURA  54  -  Teste  Unitário  –  Salvaguardas  Contextuais  e  Resiliência  de  Rede  na  Tutoria  Interativa  (RN07, RF014) 

133 



<!-- Start of picture text -->
~sendMessage should append user message and invoke Gemini with bounded context and append tutor reply” () = runTest<br>> when” (mockQuestionRepository.getQuestionById(42L)) . thenReturn(Result. success (fakeQuestion<br>7<br>geminiClient - mockGeminiClient,<br>savedStateHandle = savedStateHandle<br>testDispatcher. scheduler advanceUntilIdle<br>assertéquals(elaborateAiReply, state.messages[1].text<br>assertFalse(state.messages[1].isUser<br>assertéquals("", state-input<br>assertFalse(state. isthinking<br>assertNull(state-error<br><!-- End of picture text -->

Fonte: O autor(2026). 

A Figura 55 ilustra a execução com êxito da suíte de testes unitários do Módulo de 

Tutoria Interativa no ambiente de automação de testes. 

FIGURA 55 - Testes Unitários Aprovados do Módulo de Tutoria Interativa (RN07, RF014) 



<!-- Start of picture text -->
Class com.rememberflash.app. TutorChatViewModelTest<br>all > com.rememberflash.app > TutorChatViewModelTest<br>4 0 0 3.692s 100%<br>tests failures ignored duration ‘successful<br>Tests<br>Test Duration Result<br>loadActiveContext should bind essay context and set titleto Explicacao da Redacao 3.620s passed<br>loadActiveContext should bind question context and set title to Duvida sobre Questao 0.007s passed<br>sendMessage should append user message and invoke Gemini with bounded context and append tutor reply 0.050s passed<br>sendMessage should restore user input and set friendly error message on network failure 0.015 passed<br><!-- End of picture text -->

Fonte: O autor(2026). 

134 

Conforme  ilustrado  na  Figura  56,  realizou-se  um  teste  unitário  de  auditoria  reativa  via  Turbine para  monitorar  as  emissões  no  fluxo  StateFlow.  O  teste  comprovou  que  o  ciclo  de  vida  reativo atende  rigorosamente  ao  padrão  Unidirectional  Data  Flow  (UDF):  ao  disparar  a  autenticação, o  fluxo  emite  sequencialmente  o  estado  intermediário  de  carregamento  (isLoading  =  true), processa  a  requisição  na  JVM  e  finaliza  com  a  mutação  atômica  para  o  estado  de  sucesso (isSuccess  =  true,  isLoading  =  false),  garantindo  que  o  Jetpack  Compose  reaja deterministicamente sem concorrência ou recomposições instáveis. 

FIGURA 56 - Teste Unitário - Auditoria Reativa de Estados com Turbine (Idle, Loading, Success) 



<!-- Start of picture text -->
login flow should consistently transition from Idle to Loading to Success via Turbine*() = runTest(testDispatcher)<br>viewHodel .onEmai Changed ("Lu<br>viewHodel .onPasswordChanged:<br>viewHodel .uiState.test<br>initialstate = awaitItem()<br>assertFalse(initialstate.isLoading)<br>assertFalse(initialstate. issuccess)<br>assertNull (initialstate. error)<br>viewModel .onLoginClicked()<br>loadingState = awaitItem()<br>assertTrue(loadingState. isLoading)<br>assertFalse(loadingState.isSuccess)<br>assertNull (loadingState. error)<br>testScheduler.advanceUntilIdle()<br>1 successState = awaitItem()<br>assertFalse(successState.isLoading)<br>assertTrue(successState. isSuccess)<br>assertNull (successState. error)<br>cancelAndIgnoreRemainingEvents()<br><!-- End of picture text -->

Fonte: O autor(2026). 

A  Figura  57  ilustra  a  execução  com  êxito  da  suíte  de  testes  unitários  do  Módulo  de  Auditoria Reativa no ambiente de automação de testes. 

135 

FIGURA 57 - Testes Unitários Aprovados do Módulo de Auditoria Reativa (Turbine, StateFlow) 



<!-- Start of picture text -->
Class com.rememberflash.app. ViewModelStateFlowTest<br>all > com.rememberflash.app > ViewModelStateFlowTest<br>4 0 0 0.334s<br>tests failures ignored duration a ful<br>Tests<br>Test Duration Result<br>login flow should consistently transition from Idle to Loading to Error on authentication failure 0.005s passed<br>login flow should consistently transition from Idle to Loading to Success via Turbine 0.013s passed<br>onEmailChanged and onPasswordChanged should atomically clear error and preserve StateFlow consistency 0.006s passed<br>onLoginClicked with blank inputs should bypass Loading and immediately emit validation error 0.310s passed<br><!-- End of picture text -->

Fonte: O autor(2026). 

Conforme  evidenciado  na  Figura  58,  a  suíte  consolidada  de  testes  unitários  do aplicativo  RememberFlash  foi  executada  na  JVM  via  Gradle,  obtendo  100%  de aproveitamento  em  todos  os  módulos  arquiteturais  do  sistema.  Ao  todo,  foram  executados  85 testes  automatizados  distribuídos  entre  17  classes  de  teste,  cobrindo  de  ponta  a  ponta  as camadas  de  domínio,  casos  de  uso,  repositórios  locais,  orquestração  de  Inteligência  Artificial Generativa  e  componentes  de  interface  reativa.  A  bateria  completa  foi  concluída  em  apenas 3,468  segundos  sem  nenhuma  falha  ou  supressão  (0  failures,  0  ignored),  atestando empiricamente a estabilidade, a alta testabilidade e a conformidade do software desenvolvido. 

FIGURA 58 - Painel Geral Consolidado de Execução dos Testes Unitários Aprovados 

136 



<!-- Start of picture text -->
Package com.rememberflash.app<br>all > com.rememberflash.app<br>85 0 0 3.468s 100%<br>tests failures ignored duration successful<br>Classes<br>Class Tests Failures Ignored Duration Success rate<br>ContestTest " o o 24318 100%<br>CreateFlashoardUseCaseTest 4 o o 0.016s 100%<br>EssayTest 5 oO oO 0.209s 100%<br>GenerateContestMockExamUseCaseTest 2 o o 0.0478 100%<br>GenerateFlashcardsFromTextUseCase Test 3 oO oO 0.032s 100%<br>GenerateQuestionsUseCaseTest 3 o o 0.078 100%<br>LoginViewModelTest 5 o ° 0.0308 100%<br>PasswordResetUseCaseTest 8 oO oO 0.056s 100%<br>Profile ViewModelTest " o o 0.044 100%<br>QuestionResolveViewModelTest 5 o oO 0.073s 100%<br>RegisterUseCaseTest 7 oO oO 0.014s 100%<br>ScheduleRecalculationTest 4 oO oO 0.015s 100%<br>ScheduleTest 5 oO oO 0.148s 100%<br>TopicUseCaseTest 4 oO oO 0.021s 100%<br>TutorChatViewModelTest 4 o o 0.4758 100%<br>ViewModelStateFlowTest 4 oO oO 0.080s 100%<br><!-- End of picture text -->

Fonte: O autor(2026). 

## **4.1.2 Testes unitários** 

A análise estática de código no projeto foi realizada utilizando a ferramenta oficial Android Lint integrada à IDE Android Studio, com o objetivo de identificar possíveis falhas, inconsistências, gargalos de desempenho e más práticas de programação. Essa abordagem permitiu detectar problemas em tempo de desenvolvimento, antes mesmo da compilação e execução final da aplicação móvel. 

Durante esse processo, foram identificadas algumas inconsistências relevantes em diferentes camadas da arquitetura. 

Conforme a Figura 59, um dos problemas detectados pela análise estática na classe LoginUseCase ocorreu na rotina de autenticação de credenciais, onde o linter apontou um escopo de visibilidade desnecessariamente amplo para a variável authResult. A variável havia sido declarada previamente no escopo do método invoke, quando na verdade seu consumo limitava-se exclusivamente à ramificação de resultados. Essa prática mantinha a referência acessível além do necessário, contrariando o princípio de escopo mínimo recomendado pelas diretrizes do Kotlin. 

137 

FIGURA 59 - Análise Estática – Sugestão de Minimização de Escopo no LoginUseCase 



<!-- Start of picture text -->
i email: String, passwordKey: String): Result<Unit<br>email.isBl ) ass Key .isBl )<br>e Result. errc<br>> = @ authResult = R authenticateUser(email, passwordKey)<br>authResult<br>Result.Success -> {<br>Result.success unit<br>Result.Error -> Result.error(authResuLt. message)<br>Result. erron(<br>Result.e e. iz e)<br>Problems File Project Errors Compose<br>e Variable declaration could be moved into ‘when’<br><!-- End of picture text -->

Fonte: O autor(2026). 

Após  a  identificação  da  inconsistência,  o  código  foi  refatorado  conforme  ilustrado  na Figura  60,  aplicando  a  sugestão  da  IDE  para  encapsular  a  declaração  da  variável  diretamente no  cabeçalho  da  estrutura  condicional  (when  (val  authResult  =  ...)).  Essa  alteração  restringiu  a vida  útil  da  variável  estritamente  ao  bloco  de  checagem,  aprimorando  a  clareza  sintática  e prevenindo efeitos colaterais. 

FIGURA 60 – Análise Estática – Declaração Encapsulada no Bloco When (Resolvido) 

138 



<!-- Start of picture text -->
ke(email: String, passwordKey: String): Result<Unit<br>t Result. error(<br>yptoToken= UUID.randomUUI toString<br>Result.Error -> Result.error(authResult. )<br>} e: Exception) {<br>Result. e r c e : e<br>Problems File Project Errors ompose<br>LoginUseCase.kt<br>Typo: In word ‘senha’<br>B Typo: In word ‘obrigatério:<br>Typo: In word 'Erro'<br>Typo: In word ‘desconhecido’<br>Typo: In word 'Falha’<br>Typo: In word ‘autenticar<br><!-- End of picture text -->

Fonte: O autor(2026). 

Conforme a Figura 61, outro problema detectado pela análise estática na aba 

Problems da IDE ocorreu no caso de uso CreateContestUseCase, especificamente na rotina de processamento das URIs dos editais em PDF anexados. O analisador apontou a utilização da chamada estática legada Uri.parse(uriStr), emitindo o aviso de modernização "Use the KTX extension function 'String.toUri' instead?". O uso da sintaxe estática clássica do Java contrariava as convenções recomendadas pelo Google para projetos modernos em Kotlin, que preconizam o uso de funções de extensão do pacote AndroidX Core KTX. 

FIGURA 61 - Análise Estática – Sugestão de Uso de Extensão KTX no CreateContestUseCase 

139 



<!-- Start of picture text -->
if {it }<br>}<br><!-- End of picture text -->

Fonte: O autor(2026). 

Após a identificação da inconformidade, realizou-se a refatoração demonstrada na Figura 62, substituindo a invocação estática pela extensão idiomática uriStr.toUri(). A alteração eliminou o alerta na ferramenta de inspeção, conferindo maior concisão sintática e alinhamento às boas práticas arquiteturais da plataforma Android. 

FIGURA 62 - Análise Estática – Conversão de URI com Extensão KTX Aplicada (Resolvido) 

140 



<!-- Start of picture text -->
eateContestUsecase Ql<br>pdfUris= pafurist t filter { it ; }<br>combinedTextBuilder = StringBuilder()<br>nbinedTextBuilder. append(extracted) .appe<br>Result. erro<br>Problems File oject Errors Compose<br>ypo: In word 'Salvando’<br>rz ypo:In word ‘cone<br><!-- End of picture text -->

Fonte: O autor(2026). 

Conforme a Figura 63, o terceiro problema identificado pela análise estática na aba Problems ocorreu na classe GenerateQuestionsUseCase, especificamente no bloco de tratamento de falhas da desserialização do payload JSON de questões via Gson. O analisador apontou o aviso Parameter 'e' is never used, constatando que a variável de exceção foi instanciada no bloco catch (e: Exception), porém omitida no fluxo interno de resposta. Essa prática polui a assinatura com parâmetros não consumidos e oculta a causa raiz de eventuais inconsistências na resposta da IA generativa. 

FIGURA 62 - Análise Estática – Conversão de URI com Extensão KTX Aplicada (Resolvido) 

141 



<!-- Start of picture text -->
Ge teQuestionsUseCase @I<br>iscipline ie = discipline<br>nat = format<br>iffi = difficulty<br>antity = jantity<br>theme = effectiveTheme<br>fromJson(jsonResponse type<br>7 itch (_: Exception) {<br>i<br>Problems File Project Errors Compose<br>>, @GenerateQuestionsUseCase.kt<br>Typo: In word ‘Palavras'<br>fel Typo: In word 'bloqueadas'<br><!-- End of picture text -->

Fonte: O autor(2026). 

142 

## **REFERÊNCIAS** 

ALSHAIKH,  Rana;  AL-MALKI,  Norah;  ALMASRE,  Maida.  The  implementation  of  the cognitive  theory  of  multimedia  learning  in  the  design  and  evaluation  of  an  AI  educational video  assistant  utilizing  large  language  models.  Heliyon,  v.  10,  art.e25361,  2024.  Disponível em:  https://doi.org/10.1016/j.heliyon.2024.e25361 . 

DAMAŠEVIČIUS,  Robertas.  Commentary:  ChatGPT-supported  student  assessment  –  can  we rely  on  it?.  Journal  of  Research  in  Innovative  Teaching  &  Learning,  v.  17,  n.  2,  p.  414-416, 2024. Disponível em:  https://doi.org/10.1108/JRIT-09-2024-195 . 

KERN,  Richard.  Twenty-first  century  technologies  and  language  education:  Charting  a  path forward.  The  Modern  Language  Journal,  v.  108,  p.  515-533,  2024.  Disponível  em: <u>https://doi.org/10.1111/modl.12924</u> 

NIERO,  Leonardo  Paes.  Cursinhos  populares:  problemática,  histórico  e  atualidade  para  a educação  popular  de  jovens  e  adultos.  Revista  Internacional  de  Educação  de  Jovens  e  Adultos, v. 5, n. 10, p. 31-49, 2022. 

THORNE,  Steven  L.  Generative  artificial  intelligence,  co-evolution,  and  language  education. The Modern Language Journal, v. 108, p. 567-572, 2024. Disponível em: <u>https://doi.org/10.1111/modl.12932 .</u> 

UNESCO.  Guia  para  a  IA  generativa  na  educação  e  na  pesquisa.  Paris:  Organização  das Nações  Unidas  para  a  Educação,  a  Ciência  e  a  Cultura,  2024.  Disponível  em: <u>https://unesdoc.unesco.org/ark:/48223/pf0000390241 .</u> 

XU,  Tianhui  et  al.  Current  Status  of  ChatGPT  Use  in  Medical  Education:  Potentials, Challenges,  and  Strategies.  Journal  of  Medical  Internet  Research,  v.  26,  art.  e57896,  2024. Disponível em:  https://www.jmir.org/2024/1/e57896 . 

