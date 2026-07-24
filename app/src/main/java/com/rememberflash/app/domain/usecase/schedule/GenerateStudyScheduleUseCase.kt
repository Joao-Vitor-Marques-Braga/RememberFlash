package com.rememberflash.app.domain.usecase.schedule

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.rememberflash.app.data.remote.gemini.GeminiClient
import com.rememberflash.app.data.remote.gemini.PromptTemplates
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.StudySchedule
import com.rememberflash.app.domain.repository.ContestRepository
import com.rememberflash.app.domain.repository.DisciplineRepository
import com.rememberflash.app.domain.repository.ScheduleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GenerateStudyScheduleUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contestRepository: ContestRepository,
    private val disciplineRepository: DisciplineRepository,
    private val scheduleRepository: ScheduleRepository,
    private val geminiClient: GeminiClient
) {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend operator fun invoke(
        contestId: Long,
        examDateLong: Long,
        minutesPerDay: Int,
        maxSubjectsPerDay: Int,
        availableDaysOfWeek: List<String>
    ): Result<Long> {
        return try {
            // 1. Obter informações do concurso e suas disciplinas
            val contestResult = contestRepository.getById(contestId)
            if (contestResult !is Result.Success) {
                return Result.error("Concurso não encontrado.")
            }
            val contest = contestResult.data

            val disciplines = disciplineRepository.getByContest(contestId).first()
            if (disciplines.isEmpty()) {
                return Result.error("Esta pasta de concurso não possui nenhuma disciplina cadastrada. Por favor, cadastre as disciplinas antes de planejar o cronograma.")
            }

            // 2. Formatar parâmetros
            val startDateStr = dateFormat.format(Date())
            val endDateStr = dateFormat.format(Date(examDateLong))
            val disciplinePairs = disciplines.map { it.name to (it.weight ?: 10.0) }

            // 3. Montar e disparar prompt estruturado para o Gemini
            val prompt = PromptTemplates.buildStudySchedulePrompt(
                contestTitle = contest.title,
                disciplines = disciplinePairs,
                startDateStr = startDateStr,
                endDateStr = endDateStr,
                minutesPerDay = minutesPerDay,
                maxSubjectsPerDay = maxSubjectsPerDay,
                availableDaysOfWeek = availableDaysOfWeek
            )

            val rawResponse = geminiClient.generateStudySchedule(prompt)
            val cleanJson = cleanJsonResponse(rawResponse)

            // 4. Desserializar resposta
            val type = object : TypeToken<ParsedScheduleResponse>() {}.type
            val parsedResponse: ParsedScheduleResponse = try {
                gson.fromJson(cleanJson, type)
            } catch (e: Exception) {
                return Result.error("A resposta da IA não estava em formato JSON correto.\n\nResposta da IA:\n$rawResponse")
            }

            val parsedSessions = parsedResponse.sessions ?: emptyList()
            if (parsedSessions.isEmpty()) {
                return Result.error("A IA não gerou nenhuma sessão de estudos no período selecionado.")
            }

            // 5. Calcular a máscara de bits dos dias disponíveis
            val daysBitmask = calculateDaysBitmask(availableDaysOfWeek)

            // 6. Verificar cronograma existente
            val existingScheduleResult = scheduleRepository.getByContest(contestId)
            val existingSchedule = if (existingScheduleResult is Result.Success) existingScheduleResult.data else null

            // 7. Salvar Cronograma no Banco
            val hoursPerDay = minutesPerDay / 60.0
            val scheduleToSave = StudySchedule(
                id = existingSchedule?.id ?: 0L,
                contestId = contestId,
                examDate = examDateLong,
                availableHoursPerDay = hoursPerDay,
                restDaysPerWeek = daysBitmask, // Reusamos esse campo para a máscara de bits dos dias disponíveis
                createdAt = existingSchedule?.createdAt ?: System.currentTimeMillis(),
                lastRecalculatedAt = System.currentTimeMillis()
            )

            val insertResult = scheduleRepository.insert(scheduleToSave)
            if (insertResult !is Result.Success) {
                return Result.error("Falha ao salvar a estrutura do cronograma no banco.")
            }
            val scheduleId = insertResult.data

            // 8. Limpar metas diárias antigas
            scheduleRepository.clearDailyGoalsBySchedule(scheduleId)

            // 9. Mapear as sessões retornadas para DailyGoalEntity
            val dailyGoals = mutableListOf<DailyGoal>()
            parsedSessions.forEach { session ->
                // Tenta achar disciplina pelo nome exato ou contendo (case insensitive)
                val cleanSessionName = session.disciplineName.trim().lowercase()
                val matchedDiscipline = disciplines.firstOrNull {
                    it.name.trim().lowercase() == cleanSessionName || 
                    it.name.trim().lowercase().contains(cleanSessionName) ||
                    cleanSessionName.contains(it.name.trim().lowercase())
                }

                if (matchedDiscipline != null) {
                    val dateVal = try {
                        val parsedDate = dateFormat.parse(session.date)
                        parsedDate?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    dailyGoals.add(
                        DailyGoal(
                            scheduleId = scheduleId,
                            date = dateVal,
                            disciplineId = matchedDiscipline.id,
                            targetMinutes = session.minutes,
                            completedMinutes = 0,
                            flashcardsTarget = (session.minutes / 2).coerceAtLeast(5), // Meta sugerida de flashcards com base no tempo
                            flashcardsCompleted = 0
                        )
                    )
                }
            }

            // 10. Persistir as metas diárias no banco
            if (dailyGoals.isNotEmpty()) {
                val goalsInsertResult = scheduleRepository.insertDailyGoals(dailyGoals)
                if (goalsInsertResult !is Result.Success) {
                    return Result.error("Falha ao persistir as metas diárias do cronograma no banco.")
                }
            }

            // Se o Gemini retornou aviso de tempo insuficiente, repassamos como erro com dados salvos ou podemos retornar com sucesso
            // Mas no fluxo de eventos da regra de negócio (A3), ele salva uma versão compacta e avisa o usuário.
            // Para repassar o aviso do warning ao ViewModel de forma elegante, podemos salvar a mensagem no banco ou repassá-la
            // Se o warning não for nulo, salvamos nas preferências do concurso ou anexamos no log.
            // Vamos gravar o warning na descrição do concurso para que a UI do cronograma possa exibi-lo como banner de aviso!
            if (!parsedResponse.warning.isNullOrBlank()) {
                val updatedContest = contest.copy(
                    description = "${contest.description}\n\n[WarningCronograma]: ${parsedResponse.warning}"
                )
                contestRepository.insert(updatedContest) // Salva o contest com o warning no DB
            } else {
                // Limpa avisos anteriores se houver
                if (contest.description.contains("[WarningCronograma]")) {
                    val cleanDesc = contest.description.substringBefore("\n\n[WarningCronograma]").trim()
                    val updatedContest = contest.copy(description = cleanDesc)
                    contestRepository.insert(updatedContest)
                }
            }

            Result.success(scheduleId)
        } catch (e: Exception) {
            Result.error("Erro inesperado ao gerar cronograma: ${e.localizedMessage}", e)
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

    private fun calculateDaysBitmask(days: List<String>): Int {
        var mask = 0
        days.forEach { day ->
            val cleanDay = day.trim().lowercase()
            if (cleanDay.contains("seg")) mask = mask or 1
            if (cleanDay.contains("ter")) mask = mask or 2
            if (cleanDay.contains("qua")) mask = mask or 4
            if (cleanDay.contains("qui")) mask = mask or 8
            if (cleanDay.contains("sex")) mask = mask or 16
            if (cleanDay.contains("sáb") || cleanDay.contains("sab")) mask = mask or 32
            if (cleanDay.contains("dom")) mask = mask or 64
        }
        return mask
    }

    private data class ParsedSession(
        @SerializedName("date") val date: String,
        @SerializedName("disciplineName") val disciplineName: String,
        @SerializedName("minutes") val minutes: Int
    )

    private data class ParsedScheduleResponse(
        @SerializedName("warning") val warning: String? = null,
        @SerializedName("sessions") val sessions: List<ParsedSession>? = null
    )
}
