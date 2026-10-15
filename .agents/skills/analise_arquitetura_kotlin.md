# Skill: Auditoria de Código Kotlin (Clean Architecture)

## Objetivo
Validar se a implementação técnica em Kotlin condiz com os requisitos do texto do TFC e segue as boas práticas.

## Instruções de Execução
1. **Inspeção de Camadas (Mapeamento):**
   - **Domain:** Verifique se as regras de negócio em `domain/usecase` não contêm dependências do framework Android ou bibliotecas externas de banco de dados (deve ser Kotlin Puro).
   - **Data:** Valide se os módulos em `di/` (como `SupabaseModule.kt` ou `DatabaseModule.kt`) estão injetando corretamente as dependências nas interfaces do repositório.
   - **Util:** Cheque se a classe `AesEncryptionUtil.kt` está implementando criptografia segura conforme descrito na seção teórica de segurança do TFC.
2. **Conformidade Textual:** Cruze os "Requisitos de Software" gerados pelo fluxo de texto com os arquivos das pastas `data`, `domain` e `remote`. Se o texto diz que o app faz sync offline, o `SyncManager.kt` precisa refletir isso.
