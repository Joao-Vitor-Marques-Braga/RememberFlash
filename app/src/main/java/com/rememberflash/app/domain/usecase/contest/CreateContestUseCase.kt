package com.rememberflash.app.domain.usecase.contest

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rememberflash.app.data.local.pdf.LocalPdfExtractor
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.Contest
import com.rememberflash.app.domain.model.Discipline
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CreateContestUseCase @Inject constructor(
    private val contestRepository: ContestRepository,
    private val disciplineRepository: DisciplineRepository,
    private val geminiClient: GeminiClient,
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    suspend operator fun invoke(contest: Contest): Result<Long> {
        val now = System.currentTimeMillis()
        val pdfUriStr = contest.syllabusPdfUri

        // Se não houver PDF anexado, apenas insere o concurso normalmente
        if (pdfUriStr.isNullOrBlank()) {
            val titleToSave = contest.title.ifBlank { "Novo Concurso" }
            val contestToInsert = contest.copy(
                title = titleToSave,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
            val result = contestRepository.insert(contestToInsert)
            
            // Cria matérias padrão básicas caso não tenha edital
            if (result is Result.Success) {
                val contestId = result.data
                createDefaultDisciplines(contestId)
            }
            return result
        }

        return try {
            // 1. Extração de texto 100% local
            val pdfUri = Uri.parse(pdfUriStr)
            val rawText = LocalPdfExtractor.extractText(context, pdfUri)

            if (rawText.isBlank()) {
                // Fallback local se o PDF for escaneado/vazio
                val titleToSave = contest.title.ifBlank { "Novo Concurso" }
                val contestToInsert = contest.copy(
                    title = titleToSave,
                    createdAt = now,
                    updatedAt = now,
                    isActive = true
                )
                val result = contestRepository.insert(contestToInsert)
                if (result is Result.Success) {
                    createDefaultDisciplines(result.data)
                }
                return result
            }

            // 2. Recorte cirúrgico do texto local
            val header = LocalPdfExtractor.extractHeaderSnippet(rawText)
            val rules = LocalPdfExtractor.extractRulesSnippet(rawText)
            val syllabus = LocalPdfExtractor.extractSyllabusSnippet(rawText, contest.title)

            // 3. Chamada ao Gemini
            val jsonResponse = geminiClient.parseSyllabusAndRules(header, rules, syllabus)

            // 4. Deserialização do JSON da IA
            val type = object : TypeToken<ParsedEdital>() {}.type
            val parsedEdital: ParsedEdital = try {
                gson.fromJson(jsonResponse, type)
            } catch (e: Exception) {
                ParsedEdital() // fallback vazio se falhar a formatação do JSON
            }

            // 5. Mescla de dados digitados pelo usuário ( placeholders / auto-preenchimento )
            val finalTitle = if (contest.title.isBlank() && !parsedEdital.title.isNullOrBlank()) {
                parsedEdital.title
            } else {
                contest.title
            }

            val finalOrganizer = if (contest.organizerName.isBlank() && !parsedEdital.organizer.isNullOrBlank()) {
                parsedEdital.organizer
            } else {
                contest.organizerName
            }

            val finalQuestionType = if (contest.questionType.isBlank() && !parsedEdital.examFormat.isNullOrBlank()) {
                parsedEdital.examFormat
            } else {
                contest.questionType
            }

            val contestToInsert = contest.copy(
                title = finalTitle.ifBlank { "Novo Concurso" },
                organizerName = finalOrganizer.ifBlank { "Geral" },
                questionType = finalQuestionType,
                examDateStr = parsedEdital.examDate,
                examLocation = parsedEdital.examLocation,
                allowedPen = parsedEdital.allowedPen,
                allowedItems = parsedEdital.allowedItems ?: emptyList(),
                prohibitedItems = parsedEdital.prohibitedItems ?: emptyList(),
                createdAt = now,
                updatedAt = now,
                isActive = true
            )

            // 6. Insere concurso no banco
            val contestIdResult = contestRepository.insert(contestToInsert)

            if (contestIdResult is Result.Success) {
                val contestId = contestIdResult.data
                val discList = parsedEdital.disciplines
                
                if (!discList.isNullOrEmpty()) {
                    // Insere as disciplinas extraídas em cascata
                    discList.forEach { name ->
                        disciplineRepository.insert(
                            Discipline(
                                contestId = contestId,
                                name = name
                            )
                        )
                    }
                } else {
                    createDefaultDisciplines(contestId)
                }
            }

            contestIdResult
        } catch (e: Exception) {
            // Em caso de falha de conexão ou timeout da API, grava apenas os dados digitados e cria as matérias padrão
            val titleToSave = contest.title.ifBlank { "Novo Concurso" }
            val contestToInsert = contest.copy(
                title = titleToSave,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
            val result = contestRepository.insert(contestToInsert)
            if (result is Result.Success) {
                createDefaultDisciplines(result.data)
            }
            result
        }
    }

    private suspend fun createDefaultDisciplines(contestId: Long) {
        val defaultDisciplines = listOf("Conhecimentos Gerais", "Conhecimentos Específicos")
        defaultDisciplines.forEach { name ->
            disciplineRepository.insert(
                Discipline(
                    contestId = contestId,
                    name = name
                )
            )
        }
    }

    private data class ParsedEdital(
        val title: String? = null,
        val organizer: String? = null,
        val examFormat: String? = null,
        val examDate: String? = null,
        val examLocation: String? = null,
        val allowedPen: String? = null,
        val allowedItems: List<String>? = null,
        val prohibitedItems: List<String>? = null,
        val disciplines: List<String>? = null
    )
}
