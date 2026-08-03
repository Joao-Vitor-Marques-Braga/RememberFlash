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
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateContestUseCase @Inject constructor(
    private val contestRepository: ContestRepository,
    private val disciplineRepository: DisciplineRepository,
    private val geminiClient: GeminiClient,
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    suspend operator fun invoke(
        contest: Contest,
        onProgress: (String) -> Unit
    ): Result<Unit> {
        if (contest.id == 0L) {
            return Result.error("Concurso sem ID válido para atualização")
        }
        if (contest.title.isBlank()) {
            return Result.error("O título do concurso é obrigatório")
        }

        val pdfUriStr = contest.syllabusPdfUri
        val jobPosition = if (contest.description.startsWith("Cargo: ")) {
            contest.description.substringAfter("Cargo: ").substringBefore("\n\n").trim()
        } else {
            "Geral"
        }

        // Recupera o concurso atual no banco para comparar se houve mudança no PDF ou cargo
        val existingResult = contestRepository.getById(contest.id)
        val existingContest = (existingResult as? Result.Success)?.data
        val isPdfChanged = existingContest?.syllabusPdfUri != contest.syllabusPdfUri
        val isDescriptionChanged = existingContest?.description != contest.description
        val needsReextraction = (isPdfChanged || isDescriptionChanged) && !pdfUriStr.isNullOrBlank()

        // Se não houver PDF anexado ou se o PDF/cargo não mudou, apenas atualiza o concurso normalmente
        if (!needsReextraction) {
            onProgress("Atualizando dados do concurso...")
            return try {
                val updatedContest = contest.copy(updatedAt = System.currentTimeMillis())
                contestRepository.update(updatedContest)
            } catch (e: Exception) {
                Result.error("Falha ao atualizar concurso: ${e.localizedMessage}", e)
            }
        }

        // Caso haja PDF novo ou alteração de cargo com PDF, realiza a extração
        return try {
            onProgress("Lendo e extraindo texto dos arquivos PDF...")
            val pdfUris = pdfUriStr!!.split("|").filter { it.isNotBlank() }
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
                return Result.error("Não foi possível extrair nenhum texto legível dos arquivos PDF selecionados.")
            }

            onProgress("Analisando edital com Inteligência Artificial (Gemini)...")
            val jsonResponse = geminiClient.parseFullEditalText(rawText, jobPosition)

            val cleanedJson = cleanJsonResponse(jsonResponse)
            val type = object : TypeToken<ParsedEdital>() {}.type
            val parsedEdital: ParsedEdital = try {
                gson.fromJson(cleanedJson, type)
            } catch (e: Exception) {
                android.util.Log.e("UpdateContestUseCase", "Erro ao deserializar JSON da IA. Resposta: $jsonResponse", e)
                return Result.error("Falha ao analisar a resposta da IA.")
            }

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

            val updatedContest = contest.copy(
                title = finalTitle.ifBlank { contest.title },
                description = "Cargo: $jobPosition\n\n### LOG DE EXTRAÇÃO DA IA (GEMINI)\n" +
                        "- **Status**: Sucesso\n" +
                        "- **Modelo utilizado**: ${GeminiClient.MODEL_NAME}\n" +
                        "- **Caracteres extraídos**: ${rawText.length}\n" +
                        "- **Banca identificada**: ${finalOrganizer}\n" +
                        "- **Disciplinas extraídas**: ${parsedEdital.disciplines?.size ?: 0}\n\n" +
                        "#### Resposta JSON Bruta:\n$jsonResponse",
                organizerName = finalOrganizer.ifBlank { contest.organizerName.ifBlank { "Geral" } },
                questionType = finalQuestionType,
                examDateStr = parsedEdital.examDate ?: contest.examDateStr,
                examLocation = parsedEdital.examLocation ?: contest.examLocation,
                allowedPen = parsedEdital.allowedPen ?: contest.allowedPen,
                allowedItems = parsedEdital.allowedItems ?: contest.allowedItems,
                prohibitedItems = parsedEdital.prohibitedItems ?: contest.prohibitedItems,
                updatedAt = System.currentTimeMillis()
            )

            onProgress("Atualizando disciplinas e finalizando...")
            
            // 1. Atualiza o concurso no banco
            val updateResult = contestRepository.update(updatedContest)
            if (updateResult is Result.Success) {
                val discList = parsedEdital.disciplines
                if (!discList.isNullOrEmpty()) {
                    // Soft-deleta disciplinas antigas
                    val existingDisciplines = disciplineRepository.getByContest(contest.id).first()
                    existingDisciplines.forEach { discipline ->
                        disciplineRepository.delete(discipline.id)
                    }

                    // Insere as novas disciplinas extraídas
                    discList.forEach { parsedDisc ->
                        disciplineRepository.insert(
                            Discipline(
                                contestId = contest.id,
                                name = parsedDisc.name,
                                weight = parsedDisc.weight ?: 10.0
                            )
                        )
                    }
                }
            }
            updateResult
        } catch (e: Exception) {
            Result.error("Falha ao processar e atualizar o edital: ${e.localizedMessage}", e)
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

    private data class ParsedDiscipline(
        @SerializedName(value = "name", alternate = ["nome", "nome_disciplina", "disciplina"])
        val name: String,
        @SerializedName(value = "weight", alternate = ["peso", "questoes", "questões", "quantidade_questoes"])
        val weight: Double? = null
    )

    private data class ParsedEdital(
        @SerializedName(value = "title", alternate = ["title", "titulo", "título", "nome", "nome_concurso"])
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
