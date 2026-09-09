package com.rememberflash.app.domain.usecase.schedule

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.rememberflash.app.data.remote.gemini.GeminiScheduleClient
import com.rememberflash.app.data.remote.gemini.PromptTemplates
import com.rememberflash.app.domain.common.Result
import com.rememberflash.app.domain.model.DailyGoal
import com.rememberflash.app.domain.model.Discipline
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
    private val contestRepository: ContestRepository,
    private val disciplineRepository: DisciplineRepository,
    private val scheduleRepository: ScheduleRepository,
    private val geminiClient: GeminiScheduleClient
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

            // 2. Formatar parâmetros para a IA
            val todayDate = Date()
            val startDateStr = dateFormat.format(todayDate)
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

            val hasWeeklyPlan = !parsedResponse.weeklyPlan.isNullOrEmpty()
            val hasSessions = !parsedResponse.sessions.isNullOrEmpty()

            if (!hasWeeklyPlan && !hasSessions) {
                return Result.error("A IA não retornou nenhuma grade de estudos para os parâmetros selecionados.")
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
                restDaysPerWeek = daysBitmask,
                tokensSpent = com.rememberflash.app.data.remote.gemini.GeminiTokenTracker.lastTotalTokens,
                createdAt = existingSchedule?.createdAt ?: System.currentTimeMillis(),
                lastRecalculatedAt = System.currentTimeMillis()
            )

            val insertResult = scheduleRepository.insert(scheduleToSave)
            if (insertResult !is Result.Success) {
                val msg = (insertResult as? Result.Error)?.message ?: "Erro desconhecido"
                return Result.error("Falha ao salvar a estrutura do cronograma no banco: $msg")
            }
            val scheduleId = if (insertResult.data > 0L) {
                insertResult.data
            } else if (existingSchedule != null && existingSchedule.id > 0L) {
                existingSchedule.id
            } else {
                val fetched = scheduleRepository.getByContest(contestId)
                if (fetched is Result.Success && fetched.data != null && fetched.data.id > 0L) {
                    fetched.data.id
                } else {
                    insertResult.data
                }
            }

            // 8. Limpar metas diárias antigas
            scheduleRepository.clearDailyGoalsBySchedule(scheduleId)

            // 9. Gerar as metas diárias para todo o período até a data da prova
            val dailyGoals = mutableListOf<DailyGoal>()

            if (hasWeeklyPlan) {
                dailyGoals.addAll(
                    expandWeeklyPlanToCalendar(
                        scheduleId = scheduleId,
                        weeklyPlan = parsedResponse.weeklyPlan!!,
                        disciplines = disciplines,
                        availableDaysOfWeek = availableDaysOfWeek,
                        startDateMs = todayDate.time,
                        examDateMs = examDateLong
                    )
                )
            } else if (hasSessions) {
                dailyGoals.addAll(
                    expandSessionsToCalendar(
                        scheduleId = scheduleId,
                        sessions = parsedResponse.sessions!!,
                        disciplines = disciplines,
                        availableDaysOfWeek = availableDaysOfWeek,
                        startDateMs = todayDate.time,
                        examDateMs = examDateLong
                    )
                )
            }

            if (dailyGoals.isEmpty()) {
                return Result.error("Não foi possível gerar metas de estudo com os dias selecionados.")
            }

            // 10. Persistir as metas diárias no banco
            val goalsInsertResult = scheduleRepository.insertDailyGoals(dailyGoals)
            if (goalsInsertResult !is Result.Success) {
                val msg = (goalsInsertResult as? Result.Error)?.message ?: "Erro desconhecido"
                return Result.error("Falha ao persistir as metas diárias do cronograma no banco: $msg")
            }

            // 11. Atualizar ou limpar aviso do cronograma
            if (!parsedResponse.warning.isNullOrBlank()) {
                val updatedContest = contest.copy(
                    description = "${contest.description}\n\n[WarningCronograma]: ${parsedResponse.warning}"
                )
                contestRepository.update(updatedContest)
            } else {
                if (contest.description.contains("[WarningCronograma]")) {
                    val cleanDesc = contest.description.substringBefore("\n\n[WarningCronograma]").trim()
                    val updatedContest = contest.copy(description = cleanDesc)
                    contestRepository.update(updatedContest)
                }
            }

            Result.success(scheduleId)
        } catch (e: Exception) {
            Result.error("Erro inesperado ao gerar cronograma: ${e.localizedMessage}", e)
        }
    }

    private fun expandWeeklyPlanToCalendar(
        scheduleId: Long,
        weeklyPlan: List<ParsedDayPlan>,
        disciplines: List<Discipline>,
        availableDaysOfWeek: List<String>,
        startDateMs: Long,
        examDateMs: Long
    ): List<DailyGoal> {
        val dailyGoals = mutableListOf<DailyGoal>()

        val startCal = Calendar.getInstance().apply {
            timeInMillis = startDateMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            timeInMillis = examDateMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (!startCal.after(endCal)) {
            val dayOfWeekInt = startCal.get(Calendar.DAY_OF_WEEK)
            val dayOfWeekName = getDayOfWeekNormalizedName(dayOfWeekInt)

            val isAvailable = availableDaysOfWeek.any { isSameDayOfWeek(it, dayOfWeekName) }
            if (isAvailable) {
                val matchedPlan = weeklyPlan.firstOrNull { isSameDayOfWeek(it.dayOfWeek, dayOfWeekName) }
                val sessions = matchedPlan?.sessions ?: emptyList()

                sessions.forEach { session ->
                    val matchedDiscipline = findMatchingDiscipline(session.disciplineName, disciplines)
                    if (matchedDiscipline != null) {
                        dailyGoals.add(
                            DailyGoal(
                                scheduleId = scheduleId,
                                date = startCal.timeInMillis,
                                disciplineId = matchedDiscipline.id,
                                targetMinutes = session.minutes,
                                completedMinutes = 0,
                                flashcardsTarget = (session.minutes / 2).coerceAtLeast(5),
                                flashcardsCompleted = 0
                            )
                        )
                    }
                }
            }

            startCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dailyGoals
    }

    private fun expandSessionsToCalendar(
        scheduleId: Long,
        sessions: List<ParsedSession>,
        disciplines: List<Discipline>,
        availableDaysOfWeek: List<String>,
        startDateMs: Long,
        examDateMs: Long
    ): List<DailyGoal> {
        val dailyGoals = mutableListOf<DailyGoal>()

        // Verifica se as sessões têm datas explícitas
        val hasExplicitDates = sessions.any { !it.date.isNullOrBlank() }

        if (hasExplicitDates) {
            sessions.forEach { session ->
                val matchedDiscipline = findMatchingDiscipline(session.disciplineName, disciplines)
                if (matchedDiscipline != null) {
                    val dateVal = try {
                        val parsed = dateFormat.parse(session.date ?: "")
                        parsed?.time ?: startDateMs
                    } catch (e: Exception) {
                        startDateMs
                    }

                    dailyGoals.add(
                        DailyGoal(
                            scheduleId = scheduleId,
                            date = dateVal,
                            disciplineId = matchedDiscipline.id,
                            targetMinutes = session.minutes,
                            completedMinutes = 0,
                            flashcardsTarget = (session.minutes / 2).coerceAtLeast(5),
                            flashcardsCompleted = 0
                        )
                    )
                }
            }
        } else {
            // Se as sessões vieram em lista simples, distribui ciclicamente nos dias disponíveis
            val startCal = Calendar.getInstance().apply {
                timeInMillis = startDateMs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val endCal = Calendar.getInstance().apply {
                timeInMillis = examDateMs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            var sessionIndex = 0
            while (!startCal.after(endCal)) {
                val dayOfWeekInt = startCal.get(Calendar.DAY_OF_WEEK)
                val dayOfWeekName = getDayOfWeekNormalizedName(dayOfWeekInt)

                val isAvailable = availableDaysOfWeek.any { isSameDayOfWeek(it, dayOfWeekName) }
                if (isAvailable && sessions.isNotEmpty()) {
                    val session = sessions[sessionIndex % sessions.size]
                    sessionIndex++
                    val matchedDiscipline = findMatchingDiscipline(session.disciplineName, disciplines)
                    if (matchedDiscipline != null) {
                        dailyGoals.add(
                            DailyGoal(
                                scheduleId = scheduleId,
                                date = startCal.timeInMillis,
                                disciplineId = matchedDiscipline.id,
                                targetMinutes = session.minutes,
                                completedMinutes = 0,
                                flashcardsTarget = (session.minutes / 2).coerceAtLeast(5),
                                flashcardsCompleted = 0
                            )
                        )
                    }
                }
                startCal.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return dailyGoals
    }

    private fun findMatchingDiscipline(name: String, disciplines: List<Discipline>): Discipline? {
        val clean = name.trim().lowercase().removeAccents()
        return disciplines.firstOrNull {
            val discClean = it.name.trim().lowercase().removeAccents()
            discClean == clean || discClean.contains(clean) || clean.contains(discClean)
        } ?: disciplines.firstOrNull()
    }

    private fun String.removeAccents(): String {
        return java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    private fun getDayOfWeekNormalizedName(calendarDay: Int): String {
        return when (calendarDay) {
            Calendar.MONDAY -> "Segunda"
            Calendar.TUESDAY -> "Terça"
            Calendar.WEDNESDAY -> "Quarta"
            Calendar.THURSDAY -> "Quinta"
            Calendar.FRIDAY -> "Sexta"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> "Segunda"
        }
    }

    private fun isSameDayOfWeek(d1: String, d2: String): Boolean {
        val c1 = d1.trim().lowercase().replace("-feira", "")
        val c2 = d2.trim().lowercase().replace("-feira", "")
        if (c1 == c2) return true
        if (c1.startsWith("seg") && c2.startsWith("seg")) return true
        if ((c1.startsWith("ter") || c1.startsWith("terc")) && (c2.startsWith("ter") || c2.startsWith("terc"))) return true
        if (c1.startsWith("qua") && c2.startsWith("qua")) return true
        if (c1.startsWith("qui") && c2.startsWith("qui")) return true
        if (c1.startsWith("sex") && c2.startsWith("sex")) return true
        if ((c1.startsWith("sab") || c1.startsWith("sáb")) && (c2.startsWith("sab") || c2.startsWith("sáb"))) return true
        if (c1.startsWith("dom") && c2.startsWith("dom")) return true
        return false
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

    private data class ParsedDayPlan(
        @SerializedName("dayOfWeek") val dayOfWeek: String,
        @SerializedName("sessions") val sessions: List<ParsedSessionItem>? = null
    )

    private data class ParsedSessionItem(
        @SerializedName("disciplineName") val disciplineName: String,
        @SerializedName("minutes") val minutes: Int
    )

    private data class ParsedSession(
        @SerializedName("date") val date: String? = null,
        @SerializedName("disciplineName") val disciplineName: String,
        @SerializedName("minutes") val minutes: Int
    )

    private data class ParsedScheduleResponse(
        @SerializedName("warning") val warning: String? = null,
        @SerializedName("weeklyPlan") val weeklyPlan: List<ParsedDayPlan>? = null,
        @SerializedName("sessions") val sessions: List<ParsedSession>? = null
    )
}
