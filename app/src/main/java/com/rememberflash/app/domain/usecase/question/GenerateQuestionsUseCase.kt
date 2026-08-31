package com.rememberflash.app.domain.usecase.question

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Question
import com.rememberflash.app.domain.model.QuestionSource
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import javax.inject.Inject

class GenerateQuestionsUseCase @Inject constructor(
    private val disciplineRepository: DisciplineRepository,
    private val contestRepository: ContestRepository,
    private val questionRepository: QuestionRepository,
    private val geminiClient: GeminiClient,
    private val preferencesManager: SecurePreferencesManager
) {
    private val gson = Gson()

    // Palavras bloqueadas para simulação de filtro de segurança (A2)
    private val safetyKeywords = listOf("crime", "violência", "hacker", "terrorismo", "arma", "droga")

    suspend operator fun invoke(
        disciplineId: Long,
        quantity: Int,
        theme: String?
    ): Result<Unit> {
        // Validação de Filtro de Segurança Local (A2)
        if (!theme.isNullOrBlank()) {
            val lowercaseTheme = theme.lowercase()
            if (safetyKeywords.any { lowercaseTheme.contains(it) }) {
                return Result.error("O tema solicitado foi bloqueado pelas políticas de segurança da IA do Google.")
            }
        }

        return try {
            // 1. Carrega dados de contexto
            val disciplineResult = disciplineRepository.getById(disciplineId)
            val discipline = when (disciplineResult) {
                is Result.Success -> disciplineResult.data
                else -> return Result.error("Disciplina não encontrada")
            }

            val contestResult = contestRepository.getById(discipline.contestId)
            val contest = when (contestResult) {
                is Result.Success -> contestResult.data
                else -> return Result.error("Concurso não encontrado")
            }

            val difficulty = contest.aiDifficulty.ifBlank { preferencesManager.getDifficulty() }
            val format = if (contest.questionType.contains("Certo/Errado", ignoreCase = true)) {
                "Certo/Errado"
            } else {
                "Múltipla Escolha (5 alternativas)"
            }

            // 2. Chama o Gemini
            val jsonResponse = geminiClient.generateQuestions(
                disciplineName = discipline.name,
                banca = contest.organizerName.ifBlank { "Geral" },
                format = format,
                difficulty = difficulty,
                quantity = quantity,
                theme = theme
            )

            // 3. Desserialização e Validação do Contrato (A1)
            val type = object : TypeToken<Map<String, List<RawQuestion>>>() {}.type
            val data: Map<String, List<RawQuestion>> = try {
                gson.fromJson(jsonResponse, type)
            } catch (e: Exception) {
                return Result.error("Não foi possível estruturar as questões corretamente. Por favor, tente novamente.")
            }

            val rawQuestionsList = data["questoes"]
            if (rawQuestionsList.isNullOrEmpty()) {
                return Result.error("Não foi possível estruturar as questões corretamente. Por favor, tente novamente.")
            }

            // 4. Mapear para Modelo de Domínio e salvar
            val questions = rawQuestionsList.map { raw ->
                // Validações básicas de formato
                if (raw.statement.isBlank() || raw.options.isEmpty() || raw.correctIndex < 0) {
                    return Result.error("Não foi possível estruturar as questões corretamente. Por favor, tente novamente.")
                }
                Question(
                    disciplineId = disciplineId,
                    statement = raw.statement,
                    options = raw.options,
                    correctIndex = raw.correctIndex,
                    explanation = raw.explanation,
                    source = QuestionSource.AI_GENERATED,
                    tokensSpent = com.rememberflash.app.data.remote.gemini.GeminiTokenTracker.lastTotalTokens,
                    createdAt = System.currentTimeMillis()
                )
            }

            // Apaga as anteriores desta disciplina antes de salvar o novo lote
            questionRepository.clearQuestionsByDiscipline(disciplineId)
            questionRepository.saveQuestions(questions)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("GenerateQuestionsUseCase", "Erro ao gerar questões via IA", e)
            val msg = e.localizedMessage ?: e.message ?: e.toString()
            if (msg.contains("safety", ignoreCase = true) || msg.contains("blocked", ignoreCase = true)) {
                Result.error("O tema solicitado foi bloqueado pelas políticas de segurança da IA do Google. Erro: $msg")
            } else {
                Result.error("Erro detalhado da API Gemini: $msg")
            }
        }
    }

    private data class RawQuestion(
        val statement: String,
        val options: List<String>,
        val correctIndex: Int,
        val explanation: String?
    )
}
