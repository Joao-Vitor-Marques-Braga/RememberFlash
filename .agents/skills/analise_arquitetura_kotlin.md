# Skill: Auditoria de Código Kotlin (Clean Architecture & DERS Traceability)

## Objetivo
Validar se a implementação técnica em Kotlin e os testes automatizados cobrem 100% dos requisitos descritos no DERS oficial (`docs/JoaoVitorMarquesBraga_relatorio_DERS.md`), respeitando a Clean Architecture e as boas práticas da plataforma Android.

## Instruções de Execução
1. **Inspeção de Camadas (Clean Architecture):**
   - **Domain:** Verificar se `domain/usecase` e entidades de domínio são Kotlin puro (isentas de referências ao framework Android, DAOs do Room ou bibliotecas de rede).
   - **Data:** Validar repositórios, injeção de dependências (`di/`) e persistência híbrida (Room para cache offline e Supabase Client para nuvem).
   - **Security:** Checar se credenciais e chave de API pessoal (BYOK) são custodiadas com `EncryptedSharedPreferences` / Android Keystore (conforme RNF05).

2. **Rastreabilidade Bidirecional (DERS ↔ Código ↔ Testes):**
   - Mapear cada RF e RN para sua implementação e teste correspondente:
     - **RF001/RF002/RN01:** `RegisterUseCaseTest`, `LoginUseCase` (validação de CPF único, hashing e sessão).
     - **RF003/RN04/RN06:** `CreateContestUseCase`, `SoftDeleteContestUseCase` (exclusão lógica com `isActive = false`, upload de PDF até 15MB e parâmetros fechados de IA).
     - **RF006/RF007:** Criação de flashcards manuais e mineração com LLM / SM-2.
     - **RF008/RF009/RF010:** Geração procedural de simulados, controle de tempo por questão e cálculo de métricas.
     - **RF011/RF012:** Esteira híbrida de OCR (ML Kit local + Gemini multimodal para caligrafia) e avaliação analítica da redação.
     - **RF013/RN05/RN09:** `GenerateStudyScheduleUseCase` e `ProposeScheduleRecalculationUseCase` (data futura e multiplicador de reforço para disciplinas < 70%).
     - **RF014/RN07:** Tutoria contextual restrita à questão, redação ou edital em memória temporária (*stateless*).

3. **Padrão de Saída (.agents/bugs_codigo.md):**
   - **[REQUISITO / CASO DE USO]**: Identificador (ex: RF003 / RN06)
   - **[PACOTE / CLASSE AUDITADA]**: Caminho no fonte (`app/src/main/...`)
   - **[DIVERGÊNCIA ENCONTRADA]**: O que o DERS estipula vs. o que o código/teste executa
   - **[AÇÃO CORRETIVA RECOMENDADA]**: Ajuste arquitetural ou de caso de teste necessário