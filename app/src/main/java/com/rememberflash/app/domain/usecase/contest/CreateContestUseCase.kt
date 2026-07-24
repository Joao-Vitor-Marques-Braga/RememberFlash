package com.rememberflash.app.domain.usecase.contest

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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

    suspend operator fun invoke(
        contest: Contest,
        onProgress: (String) -> Unit
    ): Result<Long> {
        val now = System.currentTimeMillis()
        val pdfUriStr = contest.syllabusPdfUri
        val jobPosition = if (contest.description.startsWith("Cargo: ")) {
            contest.description.substringAfter("Cargo: ").substringBefore("\n\n").trim()
        } else {
            "Geral"
        }

        // Se não houver PDF anexado, apenas insere o concurso normalmente
        if (pdfUriStr.isNullOrBlank()) {
            onProgress("Salvando concurso no banco...")
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
            onProgress("Lendo e extraindo texto dos arquivos PDF...")
            // 1. Extração de texto de todos os PDFs anexados
            val pdfUris = pdfUriStr.split("|").filter { it.isNotBlank() }
            val combinedTextBuilder = StringBuilder()
            
            pdfUris.forEach { uriStr ->
                val pdfUri = Uri.parse(uriStr)
                val extracted = LocalPdfExtractor.extractText(context, pdfUri)
                if (extracted.isNotBlank()) {
                    combinedTextBuilder.append(extracted).append("\n\n")
                }
            }
            val rawText = combinedTextBuilder.toString().trim()

            if (rawText.isBlank()) {
                return Result.error("Não foi possível extrair nenhum texto legível dos arquivos PDF selecionados. Certifique-se de que os PDFs não contêm apenas imagens ou estão protegidos por senha.")
            }

            onProgress("Analisando edital com Inteligência Artificial (Gemini)...")
            // 2. Chamada direta ao Gemini passando 100% do texto do edital, anexos e o cargo pretendido
            val jsonResponse = geminiClient.parseFullEditalText(rawText, jobPosition)

            // 4. Deserialização do JSON da IA
            val cleanedJson = cleanJsonResponse(jsonResponse)
            val type = object : TypeToken<ParsedEdital>() {}.type
            val parsedEdital: ParsedEdital = try {
                gson.fromJson(cleanedJson, type)
            } catch (e: Exception) {
                android.util.Log.e("CreateContestUseCase", "Erro ao deserializar JSON da IA. Resposta: $jsonResponse", e)
                return Result.error("Falha ao analisar a resposta da IA. A resposta não estava em formato JSON válido.\n\nResposta da IA:\n$jsonResponse\n\nErro:\n${e.localizedMessage}")
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
                description = "Cargo: $jobPosition\n\n### LOG DE EXTRAÇÃO DA IA (GEMINI)\n" +
                        "- **Status**: Sucesso\n" +
                        "- **Modelo utilizado**: ${GeminiClient.MODEL_NAME}\n" +
                        "- **Caracteres extraídos**: ${rawText.length}\n" +
                        "- **Banca identificada**: ${finalOrganizer}\n" +
                        "- **Disciplinas extraídas**: ${parsedEdital.disciplines?.size ?: 0}\n\n" +
                        "#### Resposta JSON Bruta:\n$jsonResponse",
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
            onProgress("Processando disciplinas e finalizando...")
            val contestIdResult = contestRepository.insert(contestToInsert)

            if (contestIdResult is Result.Success) {
                val contestId = contestIdResult.data
                val discList = parsedEdital.disciplines
                
                if (!discList.isNullOrEmpty()) {
                    // Insere as disciplinas extraídas em cascata
                    discList.forEach { parsedDisc ->
                        disciplineRepository.insert(
                            Discipline(
                                contestId = contestId,
                                name = parsedDisc.name,
                                weight = parsedDisc.weight ?: 10.0
                            )
                        )
                    }
                } else {
                    createDefaultDisciplines(contestId)
                }
            }

            contestIdResult
        } catch (e: Exception) {
            android.util.Log.e("CreateContestUseCase", "Erro completo ao processar edital", e)
            val fullErrorLog = "Falha ao processar o edital: ${e.localizedMessage}\n\nDetalhes (Stack Trace):\n${e.stackTraceToString()}"
            Result.error(fullErrorLog, e)
        }
    }

    private fun cleanJsonResponse(rawResponse: String): String {
        var clean = rawResponse.trim()
        if (clean.startsWith("```")) {
            clean = clean.substringAfter("\n")
            if (clean.endsWith("```")) {
                clean = clean.substring(0, clean.length - 3)
            }
        }
        clean = clean.replace("```json", "").replace("```", "")
        return clean.trim()
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

    private data class ParsedDiscipline(
        @SerializedName(value = "name", alternate = ["nome", "nome_disciplina", "disciplina"])
        val name: String,
        @SerializedName(value = "weight", alternate = ["peso", "questoes", "questões", "quantidade_questoes"])
        val weight: Double? = null
    )

    private data class ParsedEdital(
        @SerializedName(value = "title", alternate = ["titulo", "título", "nome", "nome_concurso"])
        val title: String? = null,
        @SerializedName(value = "organizer", alternate = ["banca", "organizador", "banca_organizadora"])
        val organizer: String? = null,
        @SerializedName(value = "examFormat", alternate = ["formato_prova", "formato", "tipo_questao", "tipo_questoes"])
        val examFormat: String? = null,
        @SerializedName(value = "examDate", alternate = ["data_prova", "data", "data_prova_escrita"])
        val examDate: String? = null,
        @SerializedName(value = "examLocation", alternate = ["local_prova", "locais", "local", "cidade_prova"])
        val examLocation: String? = null,
        @SerializedName(value = "allowedPen", alternate = ["caneta_permitida", "caneta", "tipo_caneta"])
        val allowedPen: String? = null,
        @SerializedName(value = "allowedItems", alternate = ["itens_permitidos", "permitidos", "pode_levar"])
        val allowedItems: List<String>? = null,
        @SerializedName(value = "prohibitedItems", alternate = ["itens_proibidos", "proibidos", "nao_levar", "não_levar"])
        val prohibitedItems: List<String>? = null,
        @SerializedName(value = "disciplines", alternate = ["disciplinas", "materias", "matérias", "conteudo_programatico"])
        val disciplines: List<ParsedDiscipline>? = null
    )
}
