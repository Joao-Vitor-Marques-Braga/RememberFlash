package com.rememberflash.app.domain.usecase.question

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.rememberflash.app.data.local.pdf.LocalPdfExtractor
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.MockExamAttempt
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.QuestionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de uso responsável pela geração automatizada de Provas e Simulados do Concurso (RF009).
 *
 * 1. Verifica se o Concurso possui `syllabusPdfUri` cadastrado.
 * 2. Se presente, extrai o conteúdo programático usando [LocalPdfExtractor] e repassa as
 *    diretrizes/pesos de matérias no prompt enviado à API Gemini para balancear o lote de questões.
 * 3. Se o edital não possuir texto extraível (ou documento escaneado/sem OCR) ou se o usuário
 *    não anexou PDF, utiliza as disciplinas já cadastradas no banco como contingência (fallback).
 * 4. Permite a persistência da tentativa em `MockExamAttemptEntity` com histórico de acertos e tempo individual por questão.
 */
class GenerateContestMockExamUseCase @Inject constructor(
    private val contestRepository: ContestRepository,
    private val disciplineRepository: DisciplineRepository,
    private val questionRepository: QuestionRepository,
    private val generateQuestionsUseCase: GenerateQuestionsUseCase,
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    suspend operator fun invoke(
        contestId: Long,
        onProgress: (Int, Int, String) -> Unit
    ): Result<Unit> {
        return try {
            // 1. Carrega dados do Concurso
            val contestResult = contestRepository.getById(contestId)
            val contest = when (contestResult) {
                is Result.Success -> contestResult.data
                else -> return Result.error("Concurso não encontrado.")
            }

            // 2. Obtém todas as disciplinas ativas do concurso cadastradas no banco
            val disciplines = disciplineRepository.getByContest(contestId).first()
                .filter { it.isActive }

            if (disciplines.isEmpty()) {
                return Result.error("O concurso não possui nenhuma disciplina cadastrada.")
            }

            // 3. Verifica se possui PDF de edital anexado e tenta extrair o conteúdo programático
            val pdfUriStr = contest.syllabusPdfUri
            var editalText: String? = null

            if (!pdfUriStr.isNullOrBlank()) {
                val pdfUris = pdfUriStr.split("|").filter { it.isNotBlank() }
                val combinedTextBuilder = StringBuilder()

                withContext(Dispatchers.IO) {
                    pdfUris.forEach { uriString ->
                        try {
                            val pdfUri = Uri.parse(uriString)
                            val extracted = LocalPdfExtractor.extractText(context, pdfUri)
                            if (extracted.isNotBlank()) {
                                combinedTextBuilder.append(extracted).append("\n\n")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("GenerateContestMockExamUseCase", "Falha ao ler PDF do edital: $uriString", e)
                        }
                    }
                }

                val rawText = combinedTextBuilder.toString().trim()
                if (rawText.length >= 50) {
                    editalText = rawText
                }
            }

            val total = disciplines.size
            disciplines.forEachIndexed { index, discipline ->
                val step = index + 1
                onProgress(step, total, discipline.name)

                // Quantidade/peso proporcional do edital (weight) ou fallback padrão de 5 questões
                val quantity = if (discipline.weight > 0.0) discipline.weight.toInt() else 5

                // Se houver texto extraído do edital, elabora um direcionamento com o conteúdo programático da disciplina
                val themeDirective = if (!editalText.isNullOrBlank()) {
                    extractDisciplineGuideline(editalText, discipline.name)
                } else {
                    null
                }

                // Dispara a geração de questões daquela disciplina via UseCase
                val result = generateQuestionsUseCase(
                    disciplineId = discipline.id,
                    quantity = quantity,
                    theme = themeDirective
                )

                if (result is Result.Error) {
                    return Result.error("Falha ao gerar questões para ${discipline.name}: ${result.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("GenerateContestMockExamUseCase", "Erro ao gerar simulado do edital", e)
            Result.error("Erro ao gerar simulado completo: ${e.localizedMessage}", e)
        }
    }

    /**
     * Extrai trecho ou diretriz relevante do conteúdo programático do edital para guiar o prompt da disciplina.
     */
    private fun extractDisciplineGuideline(editalText: String, disciplineName: String): String {
        val lowerText = editalText.lowercase()
        val lowerDisc = disciplineName.lowercase().trim()
        val index = lowerText.indexOf(lowerDisc)
        return if (index != -1) {
            val start = index
            val end = (index + 800).coerceAtMost(editalText.length)
            "Diretrizes do Edital para $disciplineName: " + editalText.substring(start, end).trim()
        } else {
            "Conteúdo programático oficial do edital para a disciplina $disciplineName"
        }
    }

    /**
     * Persiste a tentativa do simulado em MockExamAttemptEntity com histórico de acertos e tempo individual por questão.
     */
    suspend fun recordAttempt(
        contestId: Long? = null,
        disciplineId: Long? = null,
        score: Int,
        totalQuestions: Int,
        answersMap: Map<Long, Int>,
        timesMap: Map<Long, Long>
    ): Result<Long> {
        val answersJsonString = gson.toJson(answersMap)
        val timesJsonString = gson.toJson(timesMap)

        val attempt = MockExamAttempt(
            contestId = contestId,
            disciplineId = disciplineId,
            score = score,
            totalQuestions = totalQuestions,
            answersJson = answersJsonString,
            timesJson = timesJsonString,
            createdAt = System.currentTimeMillis()
        )
        return questionRepository.saveMockExamAttempt(attempt)
    }
}
