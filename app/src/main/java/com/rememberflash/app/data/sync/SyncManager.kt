package com.rememberflash.app.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.rememberflash.app.BuildConfig
import com.rememberflash.app.data.local.database.dao.ContestDao
import com.rememberflash.app.data.local.database.dao.DisciplineDao
import com.rememberflash.app.data.local.database.dao.FlashcardDao
import com.rememberflash.app.data.local.database.dao.QuestionDao
import com.rememberflash.app.data.local.database.dao.ScheduleDao
import com.rememberflash.app.data.local.database.dao.TopicDao
import com.rememberflash.app.data.local.database.entity.ContestEntity
import com.rememberflash.app.data.local.database.entity.DailyGoalEntity
import com.rememberflash.app.data.local.database.entity.DisciplineEntity
import com.rememberflash.app.data.local.database.entity.FlashcardEntity
import com.rememberflash.app.data.local.database.entity.QuestionEntity
import com.rememberflash.app.data.local.database.entity.ScheduleEntity
import com.rememberflash.app.data.local.database.entity.TopicEntity
import com.rememberflash.app.data.remote.supabase.dto.ContestSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.DailyGoalSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.DisciplineSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.FlashcardSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.QuestionSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.StudyScheduleSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.TopicSupabaseDto
import com.rememberflash.app.data.local.preferences.SecurePreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: SecurePreferencesManager,
    private val contestDao: ContestDao,
    private val disciplineDao: DisciplineDao,
    private val topicDao: TopicDao,
    private val flashcardDao: FlashcardDao,
    private val questionDao: QuestionDao,
    private val scheduleDao: ScheduleDao,
    private val postgrest: Postgrest
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private fun isConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL.isNotBlank() &&
                !BuildConfig.SUPABASE_URL.contains("placeholder.supabase.co") &&
                BuildConfig.SUPABASE_ANON_KEY.isNotBlank() &&
                BuildConfig.SUPABASE_ANON_KEY != "placeholder-anon-key"
    }

    fun start() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        // Check initial state
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val initialOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _isOnline.value = initialOnline

        if (initialOnline) {
            triggerSync()
        }

        try {
            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d("SyncManager", "Network available (Online)")
                    val wasOffline = !_isOnline.value
                    _isOnline.value = true
                    if (wasOffline) {
                        triggerSync()
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d("SyncManager", "Network lost (Offline)")
                    _isOnline.value = false
                }
            })
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to register network callback: ${e.localizedMessage}", e)
        }
    }

    fun triggerSync(userId: String? = null) {
        if (_isSyncing.value) return
        val currentUserId = userId ?: preferencesManager.getUser()?.id
        scope.launch {
            try {
                if (!isConfigured()) {
                    Log.w("SyncManager", "Supabase credentials not configured in BuildConfig")
                    return@launch
                }

                _isSyncing.value = true

                // 1. Upstream Sync (envia dados locais não sincronizados para o Supabase)
                syncUpstreamInternal()

                // 2. Downstream Sync (baixa dados da nuvem para restaurar/atualizar o SQLite local)
                if (!currentUserId.isNullOrBlank()) {
                    performDownstreamSync(currentUserId)
                }

                Log.d("SyncManager", "Full bidirectional sync cycle completed!")
            } catch (e: Throwable) {
                Log.e("SyncManager", "Sync cycle failed: ${e.localizedMessage}", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    suspend fun syncNow(userId: String? = null): com.rememberflash.app.domain.common.Result<String> {
        val currentUserId = userId ?: preferencesManager.getUser()?.id
        if (!isConfigured()) {
            Log.e("SyncManager", "[SYNC-ERRO] Supabase não configurado no BuildConfig (URL ou AnonKey vazios).")
            return com.rememberflash.app.domain.common.Result.error("Credenciais do Supabase não configuradas no aplicativo.")
        }
        
        Log.i("SyncManager", "🔄 =================== [INÍCIO DA SINCRONIZAÇÃO MANUAL] ===================")
        Log.i("SyncManager", "👤 Usuário ID: ${currentUserId ?: "Desconhecido/Anônimo"}")
        Log.i("SyncManager", "🌐 Status da Conexão: ${if (_isOnline.value) "ONLINE" else "OFFLINE"}")
        Log.i("SyncManager", "🔗 Supabase URL: ${BuildConfig.SUPABASE_URL}")
        
        _isSyncing.value = true
        val errorList = mutableListOf<String>()

        return try {
            syncUpstreamInternal(errorList)
            if (!currentUserId.isNullOrBlank()) {
                performDownstreamSync(currentUserId, errorList)
            } else {
                Log.w("SyncManager", "⚠️ Downstream ignorado: Nenhum usuário autenticado encontrado.")
            }

            if (errorList.isNotEmpty()) {
                val errorSummary = errorList.joinToString("\n• ")
                Log.e("SyncManager", "❌ Sincronização concluída com erros:\n• $errorSummary")
                com.rememberflash.app.domain.common.Result.error("Falhas na sincronização:\n• $errorSummary")
            } else {
                Log.i("SyncManager", "✅ =================== [SINCRONIZAÇÃO CONCLUÍDA COM SUCESSO] ===================")
                com.rememberflash.app.domain.common.Result.success("Sincronização com o Supabase concluída com sucesso!")
            }
        } catch (e: Throwable) {
            Log.e("SyncManager", "❌ Erro inesperado durante sincronização manual: ${e.localizedMessage}", e)
            com.rememberflash.app.domain.common.Result.error("Falha ao sincronizar: ${e.localizedMessage ?: e.javaClass.simpleName}")
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun syncUpstreamInternal(errors: MutableList<String> = mutableListOf()) {
        val unsyncedContests = contestDao.getUnsyncedContests()
        val unsyncedDisciplines = disciplineDao.getUnsyncedDisciplines()
        val unsyncedTopics = topicDao.getUnsyncedTopics()
        val unsyncedFlashcards = flashcardDao.getUnsyncedFlashcards()
        val unsyncedQuestions = questionDao.getUnsyncedQuestions()
        val unsyncedSchedules = scheduleDao.getUnsyncedSchedules()
        val unsyncedGoals = scheduleDao.getUnsyncedDailyGoals()

        Log.i("SyncManager", "📤 [UPSTREAM] Verificando itens pendentes no banco local Room:")
        Log.i("SyncManager", "   - Concursos pendentes: ${unsyncedContests.size}")
        Log.i("SyncManager", "   - Disciplinas pendentes: ${unsyncedDisciplines.size}")
        Log.i("SyncManager", "   - Tópicos pendentes: ${unsyncedTopics.size}")
        Log.i("SyncManager", "   - Flashcards pendentes: ${unsyncedFlashcards.size}")
        Log.i("SyncManager", "   - Questões pendentes: ${unsyncedQuestions.size}")
        Log.i("SyncManager", "   - Cronogramas pendentes: ${unsyncedSchedules.size}")
        Log.i("SyncManager", "   - Metas diárias pendentes: ${unsyncedGoals.size}")

        if (unsyncedContests.isEmpty() &&
            unsyncedDisciplines.isEmpty() &&
            unsyncedTopics.isEmpty() &&
            unsyncedFlashcards.isEmpty() &&
            unsyncedQuestions.isEmpty() &&
            unsyncedSchedules.isEmpty() &&
            unsyncedGoals.isEmpty()
        ) {
            Log.i("SyncManager", "ℹ️ [UPSTREAM] Todos os dados locais já estão sincronizados com a nuvem.")
            return
        }

        // 1. Contests
        if (unsyncedContests.isNotEmpty()) {
            try {
                Log.d("SyncManager", "⬆️ Enviando ${unsyncedContests.size} concurso(s) para o Supabase (tabela 'contests')...")
                val dtos = unsyncedContests.map { entity ->
                    ContestSupabaseDto(
                        id = entity.id,
                        userId = entity.userId,
                        title = entity.title,
                        description = entity.description,
                        organizerName = entity.organizerName,
                        questionType = entity.questionType,
                        syllabusPdfUri = entity.syllabusPdfUri,
                        examDateStr = entity.examDateStr,
                        examLocation = entity.examLocation,
                        allowedPen = entity.allowedPen,
                        allowedItems = entity.allowedItems,
                        prohibitedItems = entity.prohibitedItems,
                        aiDifficulty = entity.aiDifficulty,
                        aiRigor = entity.aiRigor,
                        aiTone = entity.aiTone,
                        isActive = entity.isActive
                    )
                }
                dtos.chunked(50).forEach { chunk ->
                    postgrest.from("contests").upsert(chunk)
                }
                contestDao.markContestsAsSynced(unsyncedContests.map { it.id })
                Log.i("SyncManager", "✅ [UPSTREAM] ${unsyncedContests.size} concurso(s) enviados e marcados como sincronizados!")
            } catch (e: Throwable) {
                val msg = "Tabela 'contests': ${e.localizedMessage ?: e.javaClass.simpleName}"
                Log.e("SyncManager", "❌ [UPSTREAM-ERRO] $msg", e)
                errors.add(msg)
            }
        }

        // 2. Disciplines (ensuring parent contests exist in Supabase first)
        if (unsyncedDisciplines.isNotEmpty()) {
            try {
                val parentContestIds = unsyncedDisciplines.map { it.contestId }.distinct()
                val parentContests = contestDao.getByIds(parentContestIds).filter { !it.isSynced }
                if (parentContests.isNotEmpty()) {
                    Log.d("SyncManager", "⬆️ Assegurando envio prévio de ${parentContests.size} concurso(s) pai(s) não sincronizado(s)...")
                    val contestDtos = parentContests.map { entity ->
                        ContestSupabaseDto(
                            id = entity.id,
                            userId = entity.userId,
                            title = entity.title,
                            description = entity.description,
                            organizerName = entity.organizerName,
                            questionType = entity.questionType,
                            syllabusPdfUri = entity.syllabusPdfUri,
                            examDateStr = entity.examDateStr,
                            examLocation = entity.examLocation,
                            allowedPen = entity.allowedPen,
                            allowedItems = entity.allowedItems,
                            prohibitedItems = entity.prohibitedItems,
                            aiDifficulty = entity.aiDifficulty,
                            aiRigor = entity.aiRigor,
                            aiTone = entity.aiTone,
                            isActive = entity.isActive
                        )
                    }
                    contestDtos.chunked(50).forEach { chunk ->
                        postgrest.from("contests").upsert(chunk)
                    }
                    contestDao.markContestsAsSynced(parentContests.map { it.id })
                }

                Log.d("SyncManager", "⬆️ Enviando ${unsyncedDisciplines.size} disciplina(s) para o Supabase (tabela 'disciplines')...")
                val dtos = unsyncedDisciplines.map { entity ->
                    DisciplineSupabaseDto(
                        id = entity.id,
                        contestId = entity.contestId,
                        name = entity.name,
                        weight = entity.weight,
                        totalTopics = entity.totalTopics,
                        isActive = entity.isActive
                    )
                }
                dtos.chunked(50).forEach { chunk ->
                    postgrest.from("disciplines").upsert(chunk)
                }
                disciplineDao.markDisciplinesAsSynced(unsyncedDisciplines.map { it.id })
                Log.i("SyncManager", "✅ [UPSTREAM] ${unsyncedDisciplines.size} disciplina(s) enviadas e marcadas como sincronizadas!")
            } catch (e: Throwable) {
                val msg = "Tabela 'disciplines': ${e.localizedMessage ?: e.javaClass.simpleName}"
                Log.e("SyncManager", "❌ [UPSTREAM-ERRO] $msg", e)
                errors.add(msg)
            }
        }

        // 3. Topics (ensuring parent disciplines exist in Supabase first)
        if (unsyncedTopics.isNotEmpty()) {
            try {
                val parentDisciplineIds = unsyncedTopics.map { it.disciplineId }.distinct()
                val parentDisciplines = disciplineDao.getByIds(parentDisciplineIds).filter { !it.isSynced }
                if (parentDisciplines.isNotEmpty()) {
                    Log.d("SyncManager", "⬆️ Assegurando envio prévio de ${parentDisciplines.size} disciplina(s) pai(s) não sincronizada(s)...")
                    val disciplineDtos = parentDisciplines.map { entity ->
                        DisciplineSupabaseDto(
                            id = entity.id,
                            contestId = entity.contestId,
                            name = entity.name,
                            weight = entity.weight,
                            totalTopics = entity.totalTopics,
                            isActive = entity.isActive
                        )
                    }
                    disciplineDtos.chunked(50).forEach { chunk ->
                        postgrest.from("disciplines").upsert(chunk)
                    }
                    disciplineDao.markDisciplinesAsSynced(parentDisciplines.map { it.id })
                }

                Log.d("SyncManager", "⬆️ Enviando ${unsyncedTopics.size} tópico(s) para o Supabase (tabela 'topics')...")
                Log.d("SyncManager", "🔍 Amostra de tópicos: ${unsyncedTopics.take(3).map { "id=${it.id}, disc=${it.disciplineId}, contest=${it.contestId}, name=${it.name}" }}")
                
                var successCount = 0
                unsyncedTopics.chunked(20).forEach { topicChunk ->
                    try {
                        val dtos = topicChunk.map { entity ->
                            val cleanName = if (entity.name.length > 250) entity.name.take(247) + "..." else entity.name
                            val cleanDesc = if (entity.name.length > 250 && entity.description.isNullOrBlank()) {
                                entity.name
                            } else {
                                entity.description
                            }
                            TopicSupabaseDto(
                                id = entity.id,
                                disciplineId = entity.disciplineId,
                                contestId = entity.contestId,
                                name = cleanName,
                                description = cleanDesc,
                                orderIndex = entity.orderIndex,
                                createdAt = entity.createdAt
                            )
                        }
                        postgrest.from("topics").upsert(dtos)
                        topicDao.markTopicsAsSynced(topicChunk.map { it.id })
                        successCount += topicChunk.size
                        Log.d("SyncManager", "   ↳ Lote de ${topicChunk.size} tópicos salvo no Supabase com sucesso ($successCount/${unsyncedTopics.size})")
                    } catch (chunkError: Throwable) {
                        val msg = "Lote de tópicos [IDs: ${topicChunk.firstOrNull()?.id}..${topicChunk.lastOrNull()?.id}]: ${chunkError.localizedMessage ?: chunkError.javaClass.simpleName}"
                        Log.e("SyncManager", "❌ [UPSTREAM-ERRO] $msg", chunkError)
                        errors.add(msg)
                    }
                }
                Log.i("SyncManager", "✅ [UPSTREAM] $successCount de ${unsyncedTopics.size} tópico(s) sincronizados com sucesso!")
            } catch (e: Throwable) {
                val msg = "Tabela 'topics': ${e.localizedMessage ?: e.javaClass.simpleName}"
                Log.e("SyncManager", "❌ [UPSTREAM-ERRO] $msg", e)
                errors.add(msg)
            }
        }

        // 4. Flashcards
        if (unsyncedFlashcards.isNotEmpty()) {
            try {
                val parentDisciplineIds = unsyncedFlashcards.map { it.disciplineId }.distinct()
                val parentDisciplines = disciplineDao.getByIds(parentDisciplineIds).filter { !it.isSynced }
                if (parentDisciplines.isNotEmpty()) {
                    Log.d("SyncManager", "⬆️ Assegurando envio prévio de ${parentDisciplines.size} disciplina(s) pai(s) para os flashcards...")
                    val disciplineDtos = parentDisciplines.map { entity ->
                        DisciplineSupabaseDto(
                            id = entity.id,
                            contestId = entity.contestId,
                            name = entity.name,
                            weight = entity.weight,
                            totalTopics = entity.totalTopics,
                            isActive = entity.isActive
                        )
                    }
                    disciplineDtos.chunked(50).forEach { chunk ->
                        postgrest.from("disciplines").upsert(chunk)
                    }
                    disciplineDao.markDisciplinesAsSynced(parentDisciplines.map { it.id })
                }

                Log.d("SyncManager", "⬆️ Enviando ${unsyncedFlashcards.size} flashcard(s) para o Supabase (tabela 'flashcards')...")
                val dtos = unsyncedFlashcards.map { entity ->
                    FlashcardSupabaseDto(
                        id = entity.id,
                        disciplineId = entity.disciplineId,
                        topicId = entity.topicId,
                        front = entity.front,
                        back = entity.back,
                        source = entity.source,
                        nextReviewAt = entity.nextReviewAt,
                        easeFactor = entity.easeFactor,
                        interval = entity.interval,
                        repetitions = entity.repetitions,
                        tokensSpent = entity.tokensSpent,
                        createdAt = entity.createdAt
                    )
                }
                dtos.chunked(50).forEach { chunk ->
                    postgrest.from("flashcards").upsert(chunk)
                }
                flashcardDao.markFlashcardsAsSynced(unsyncedFlashcards.map { it.id })
                Log.i("SyncManager", "✅ [UPSTREAM] ${unsyncedFlashcards.size} flashcard(s) enviados e marcados como sincronizados!")
            } catch (e: Throwable) {
                val msg = "Tabela 'flashcards': ${e.localizedMessage ?: e.javaClass.simpleName}"
                Log.e("SyncManager", "❌ [UPSTREAM-ERRO] $msg", e)
                errors.add(msg)
            }
        }

        // 5. Questions (ensuring parent disciplines exist in Supabase first)
        if (unsyncedQuestions.isNotEmpty()) {
            try {
                val parentDisciplineIds = unsyncedQuestions.map { it.disciplineId }.distinct()
                val parentDisciplines = disciplineDao.getByIds(parentDisciplineIds).filter { !it.isSynced }
                if (parentDisciplines.isNotEmpty()) {
                    Log.d("SyncManager", "⬆️ Assegurando envio prévio de ${parentDisciplines.size} disciplina(s) pai(s) para as questões...")
                    val disciplineDtos = parentDisciplines.map { entity ->
                        DisciplineSupabaseDto(
                            id = entity.id,
                            contestId = entity.contestId,
                            name = entity.name,
                            weight = entity.weight,
                            totalTopics = entity.totalTopics,
                            isActive = entity.isActive
                        )
                    }
                    disciplineDtos.chunked(50).forEach { chunk ->
                        postgrest.from("disciplines").upsert(chunk)
                    }
                    disciplineDao.markDisciplinesAsSynced(parentDisciplines.map { it.id })
                }

                Log.d("SyncManager", "⬆️ Enviando ${unsyncedQuestions.size} questão(ões) para o Supabase (tabela 'questions')...")
                val dtos = unsyncedQuestions.map { entity ->
                    QuestionSupabaseDto(
                        id = entity.id,
                        disciplineId = entity.disciplineId,
                        topicId = entity.topicId,
                        statement = entity.statement,
                        optionsJson = entity.optionsJson,
                        correctIndex = entity.correctIndex,
                        explanation = entity.explanation,
                        source = entity.source,
                        chosenOption = entity.chosenOption,
                        isCorrect = entity.isCorrect,
                        answeredAt = entity.answeredAt,
                        tokensSpent = entity.tokensSpent,
                        createdAt = entity.createdAt
                    )
                }
                dtos.chunked(50).forEach { chunk ->
                    postgrest.from("questions").upsert(chunk)
                }
                questionDao.markQuestionsAsSynced(unsyncedQuestions.map { it.id })
                Log.i("SyncManager", "✅ [UPSTREAM] ${unsyncedQuestions.size} questão(ões) enviadas e marcadas como sincronizadas!")
            } catch (e: Throwable) {
                val msg = "Tabela 'questions': ${e.localizedMessage ?: e.javaClass.simpleName}"
                Log.e("SyncManager", "❌ [UPSTREAM-ERRO] $msg", e)
                errors.add(msg)
            }
        }

        // 6. Study Schedules
        if (unsyncedSchedules.isNotEmpty()) {
            try {
                Log.d("SyncManager", "⬆️ Enviando ${unsyncedSchedules.size} cronograma(s) para o Supabase (tabela 'study_schedules')...")
                val dtos = unsyncedSchedules.map { entity ->
                    val examDateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(entity.examDate))
                    val createdAtFormatted = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(entity.createdAt))
                    val lastRecalcFormatted = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(entity.lastRecalculatedAt))
                    StudyScheduleSupabaseDto(
                        id = entity.id,
                        contestId = entity.contestId,
                        examDate = examDateFormatted,
                        availableHoursPerDay = entity.availableHoursPerDay,
                        restDaysPerWeek = entity.restDaysPerWeek,
                        tokensSpent = entity.tokensSpent,
                        createdAt = createdAtFormatted,
                        lastRecalculatedAt = lastRecalcFormatted
                    )
                }
                dtos.chunked(50).forEach { chunk ->
                    postgrest.from("study_schedules").upsert(chunk)
                }
                scheduleDao.markSchedulesAsSynced(unsyncedSchedules.map { it.id })
                Log.i("SyncManager", "✅ [UPSTREAM] ${unsyncedSchedules.size} cronograma(s) enviados e marcados como sincronizados!")
            } catch (e: Throwable) {
                val msg = "Tabela 'study_schedules': ${e.localizedMessage ?: e.javaClass.simpleName}"
                Log.e("SyncManager", "❌ [UPSTREAM-ERRO] $msg", e)
                errors.add(msg)
            }
        }

        // 7. Daily Goals
        if (unsyncedGoals.isNotEmpty()) {
            try {
                Log.d("SyncManager", "⬆️ Enviando ${unsyncedGoals.size} meta(s) diária(s) para o Supabase (tabela 'daily_goals')...")
                val dtos = unsyncedGoals.map { entity ->
                    val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(entity.date))
                    DailyGoalSupabaseDto(
                        id = entity.id,
                        scheduleId = entity.scheduleId,
                        disciplineId = entity.disciplineId,
                        date = dateFormatted,
                        targetMinutes = entity.targetMinutes,
                        completedMinutes = entity.completedMinutes,
                        flashcardsTarget = entity.flashcardsTarget,
                        flashcardsCompleted = entity.flashcardsCompleted
                    )
                }
                dtos.chunked(50).forEach { chunk ->
                    postgrest.from("daily_goals").upsert(chunk)
                }
                scheduleDao.markDailyGoalsAsSynced(unsyncedGoals.map { it.id })
                Log.i("SyncManager", "✅ [UPSTREAM] ${unsyncedGoals.size} meta(s) diária(s) enviadas e marcadas como sincronizadas!")
            } catch (e: Throwable) {
                val msg = "Tabela 'daily_goals': ${e.localizedMessage ?: e.javaClass.simpleName}"
                Log.e("SyncManager", "❌ [UPSTREAM-ERRO] $msg", e)
                errors.add(msg)
            }
        }
    }

    private suspend fun performDownstreamSync(userId: String, errors: MutableList<String> = mutableListOf()) {
        try {
            Log.i("SyncManager", "⬇️ [DOWNSTREAM] Buscando dados da nuvem para o usuário: $userId...")

            val remoteContests = postgrest.from("contests").select {
                filter { eq("user_id", userId) }
            }.decodeList<ContestSupabaseDto>()

            Log.i("SyncManager", "📥 [DOWNSTREAM] Concursos recebidos do Supabase: ${remoteContests.size}")

            if (remoteContests.isNotEmpty()) {
                val contestEntities = remoteContests.map { dto ->
                    ContestEntity(
                        id = dto.id ?: 0L,
                        userId = dto.userId,
                        title = dto.title,
                        description = dto.description ?: "",
                        organizerName = dto.organizerName ?: "",
                        questionType = dto.questionType ?: "Múltipla Escolha",
                        syllabusPdfUri = dto.syllabusPdfUri,
                        examDateStr = dto.examDateStr,
                        examLocation = dto.examLocation,
                        allowedPen = dto.allowedPen,
                        allowedItems = dto.allowedItems,
                        prohibitedItems = dto.prohibitedItems,
                        aiDifficulty = dto.aiDifficulty ?: "Médio",
                        aiRigor = dto.aiRigor ?: "Padrão",
                        aiTone = dto.aiTone ?: "Explicativo",
                        isActive = dto.isActive ?: true,
                        isSynced = true
                    )
                }
                contestDao.insertAll(contestEntities)

                val contestIds = remoteContests.mapNotNull { it.id }
                if (contestIds.isNotEmpty()) {
                    val remoteDisciplines = postgrest.from("disciplines").select {
                        filter { isIn("contest_id", contestIds) }
                    }.decodeList<DisciplineSupabaseDto>()

                    Log.i("SyncManager", "📥 [DOWNSTREAM] Disciplinas recebidas do Supabase: ${remoteDisciplines.size}")

                    if (remoteDisciplines.isNotEmpty()) {
                        val disciplineEntities = remoteDisciplines.map { dto ->
                            DisciplineEntity(
                                id = dto.id ?: 0L,
                                contestId = dto.contestId,
                                name = dto.name,
                                weight = dto.weight ?: 1.0,
                                totalTopics = dto.totalTopics ?: 0,
                                isActive = dto.isActive ?: true,
                                isSynced = true
                            )
                        }
                        disciplineDao.insertAll(disciplineEntities)

                        val disciplineIds = remoteDisciplines.mapNotNull { it.id }

                        val remoteTopics = postgrest.from("topics").select {
                            filter { isIn("contest_id", contestIds) }
                        }.decodeList<TopicSupabaseDto>()

                        Log.i("SyncManager", "📥 [DOWNSTREAM] Tópicos recebidos do Supabase: ${remoteTopics.size}")

                        if (remoteTopics.isNotEmpty()) {
                            val topicEntities = remoteTopics.map { dto ->
                                TopicEntity(
                                    id = dto.id ?: 0L,
                                    disciplineId = dto.disciplineId,
                                    contestId = dto.contestId,
                                    name = dto.name,
                                    description = dto.description,
                                    orderIndex = dto.orderIndex ?: 0,
                                    createdAt = dto.createdAt ?: System.currentTimeMillis(),
                                    isSynced = true
                                )
                            }
                            topicDao.insertAll(topicEntities)
                        }

                        if (disciplineIds.isNotEmpty()) {
                            val remoteFlashcards = postgrest.from("flashcards").select {
                                filter { isIn("discipline_id", disciplineIds) }
                            }.decodeList<FlashcardSupabaseDto>()

                            Log.i("SyncManager", "📥 [DOWNSTREAM] Flashcards recebidos do Supabase: ${remoteFlashcards.size}")

                            if (remoteFlashcards.isNotEmpty()) {
                                val flashcardEntities = remoteFlashcards.map { dto ->
                                    FlashcardEntity(
                                        id = dto.id ?: 0L,
                                        disciplineId = dto.disciplineId,
                                        topicId = dto.topicId,
                                        front = dto.front,
                                        back = dto.back,
                                        source = dto.source ?: "MANUAL",
                                        nextReviewAt = dto.nextReviewAt,
                                        easeFactor = dto.easeFactor ?: 2.5,
                                        interval = dto.interval ?: 0,
                                        repetitions = dto.repetitions ?: 0,
                                        tokensSpent = dto.tokensSpent ?: 0,
                                        createdAt = dto.createdAt ?: System.currentTimeMillis(),
                                        isSynced = true
                                    )
                                }
                                flashcardDao.insertAll(flashcardEntities)
                            }

                            val remoteQuestions = postgrest.from("questions").select {
                                filter { isIn("discipline_id", disciplineIds) }
                            }.decodeList<QuestionSupabaseDto>()

                            Log.i("SyncManager", "📥 [DOWNSTREAM] Questões recebidas do Supabase: ${remoteQuestions.size}")

                            if (remoteQuestions.isNotEmpty()) {
                                val questionEntities = remoteQuestions.map { dto ->
                                    QuestionEntity(
                                        id = dto.id ?: 0L,
                                        disciplineId = dto.disciplineId,
                                        topicId = dto.topicId,
                                        statement = dto.statement,
                                        optionsJson = dto.optionsJson,
                                        correctIndex = dto.correctIndex,
                                        explanation = dto.explanation,
                                        source = dto.source ?: "MANUAL",
                                        chosenOption = dto.chosenOption,
                                        isCorrect = dto.isCorrect,
                                        answeredAt = dto.answeredAt,
                                        tokensSpent = dto.tokensSpent ?: 0,
                                        createdAt = dto.createdAt ?: System.currentTimeMillis(),
                                        isSynced = true
                                    )
                                }
                                questionDao.insertAll(questionEntities)
                            }
                        }

                        // Download study_schedules
                        val remoteSchedules = postgrest.from("study_schedules").select {
                            filter { isIn("contest_id", contestIds) }
                        }.decodeList<StudyScheduleSupabaseDto>()

                        Log.i("SyncManager", "📥 [DOWNSTREAM] Cronogramas recebidos do Supabase: ${remoteSchedules.size}")

                        if (remoteSchedules.isNotEmpty()) {
                            val scheduleEntities = remoteSchedules.map { dto ->
                                val parsedExamDate = try {
                                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dto.examDate ?: "")?.time ?: System.currentTimeMillis()
                                } catch (_: Exception) {
                                    System.currentTimeMillis()
                                }
                                val parsedCreatedAt = try {
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(dto.createdAt ?: "")?.time ?: System.currentTimeMillis()
                                } catch (_: Exception) {
                                    System.currentTimeMillis()
                                }
                                val parsedLastRecalc = try {
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(dto.lastRecalculatedAt ?: "")?.time ?: System.currentTimeMillis()
                                } catch (_: Exception) {
                                    System.currentTimeMillis()
                                }

                                ScheduleEntity(
                                    id = dto.id ?: 0L,
                                    contestId = dto.contestId,
                                    examDate = parsedExamDate,
                                    availableHoursPerDay = dto.availableHoursPerDay ?: 2.0,
                                    restDaysPerWeek = dto.restDaysPerWeek ?: 1,
                                    tokensSpent = dto.tokensSpent ?: 0,
                                    createdAt = parsedCreatedAt,
                                    lastRecalculatedAt = parsedLastRecalc,
                                    isSynced = true
                                )
                            }
                            scheduleEntities.forEach { sched ->
                                scheduleDao.insertSchedule(sched)
                            }

                            val scheduleIds = remoteSchedules.mapNotNull { it.id }
                            if (scheduleIds.isNotEmpty()) {
                                val remoteDailyGoals = postgrest.from("daily_goals").select {
                                    filter { isIn("schedule_id", scheduleIds) }
                                }.decodeList<DailyGoalSupabaseDto>()

                                Log.i("SyncManager", "📥 [DOWNSTREAM] Metas diárias recebidas do Supabase: ${remoteDailyGoals.size}")

                                if (remoteDailyGoals.isNotEmpty()) {
                                    val goalEntities = remoteDailyGoals.map { dto ->
                                        val parsedDate = try {
                                            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dto.date)?.time ?: System.currentTimeMillis()
                                        } catch (_: Exception) {
                                            System.currentTimeMillis()
                                        }
                                        DailyGoalEntity(
                                            id = dto.id ?: 0L,
                                            scheduleId = dto.scheduleId,
                                            date = parsedDate,
                                            disciplineId = dto.disciplineId,
                                            targetMinutes = dto.targetMinutes,
                                            completedMinutes = dto.completedMinutes ?: 0,
                                            flashcardsTarget = dto.flashcardsTarget ?: 0,
                                            flashcardsCompleted = dto.flashcardsCompleted ?: 0,
                                            isSynced = true
                                        )
                                    }
                                    scheduleDao.insertDailyGoals(goalEntities)
                                }
                            }
                        }
                    }
                }
            }
            Log.i("SyncManager", "✅ [DOWNSTREAM] Download e restauração da nuvem concluídos com sucesso.")
        } catch (e: Throwable) {
            val msg = "Download da nuvem (Downstream): ${e.localizedMessage ?: e.javaClass.simpleName}"
            Log.e("SyncManager", "❌ [DOWNSTREAM-ERRO] $msg", e)
            errors.add(msg)
        }
    }

    suspend fun deleteContestRemote(contestId: Long) {
        if (!isConfigured()) return
        try {
            Log.d("SyncManager", "Starting remote CASCADE delete for contest $contestId")
            val remoteDisciplines = try {
                postgrest.from("disciplines").select {
                    filter { eq("contest_id", contestId) }
                }.decodeList<DisciplineSupabaseDto>()
            } catch (e: Throwable) {
                emptyList()
            }
            val disciplineIds = remoteDisciplines.mapNotNull { it.id }

            if (disciplineIds.isNotEmpty()) {
                try {
                    postgrest.from("questions").delete {
                        filter { isIn("discipline_id", disciplineIds) }
                    }
                } catch (e: Throwable) {
                    Log.w("SyncManager", "Questions delete error: ${e.message}")
                }

                try {
                    postgrest.from("flashcards").delete {
                        filter { isIn("discipline_id", disciplineIds) }
                    }
                } catch (e: Throwable) {
                    Log.w("SyncManager", "Flashcards delete error: ${e.message}")
                }
            }

            try {
                postgrest.from("topics").delete {
                    filter { eq("contest_id", contestId) }
                }
            } catch (e: Throwable) {
                Log.w("SyncManager", "Topics delete error: ${e.message}")
            }

            try {
                postgrest.from("disciplines").delete {
                    filter { eq("contest_id", contestId) }
                }
            } catch (e: Throwable) {
                Log.w("SyncManager", "Disciplines delete error: ${e.message}")
            }

            try {
                postgrest.from("study_schedules").delete {
                    filter { eq("contest_id", contestId) }
                }
            } catch (_: Throwable) {}

            try {
                postgrest.from("schedules").delete {
                    filter { eq("contest_id", contestId) }
                }
            } catch (_: Throwable) {}

            postgrest.from("contests").delete {
                filter { eq("id", contestId) }
            }
            Log.d("SyncManager", "Successfully completed remote CASCADE delete for contest $contestId")
        } catch (e: Throwable) {
            Log.e("SyncManager", "Error deleting contest $contestId from Supabase: ${e.localizedMessage}", e)
        }
    }

    suspend fun deleteDisciplineRemote(disciplineId: Long) {
        if (!isConfigured()) return
        try {
            Log.d("SyncManager", "Starting remote CASCADE delete for discipline $disciplineId")
            try {
                postgrest.from("questions").delete {
                    filter { eq("discipline_id", disciplineId) }
                }
            } catch (e: Throwable) {
                Log.w("SyncManager", "Questions delete error: ${e.message}")
            }

            try {
                postgrest.from("flashcards").delete {
                    filter { eq("discipline_id", disciplineId) }
                }
            } catch (e: Throwable) {
                Log.w("SyncManager", "Flashcards delete error: ${e.message}")
            }

            try {
                postgrest.from("topics").delete {
                    filter { eq("discipline_id", disciplineId) }
                }
            } catch (e: Throwable) {
                Log.w("SyncManager", "Topics delete error: ${e.message}")
            }

            postgrest.from("disciplines").delete {
                filter { eq("id", disciplineId) }
            }
            Log.d("SyncManager", "Successfully completed remote CASCADE delete for discipline $disciplineId")
        } catch (e: Throwable) {
            Log.e("SyncManager", "Error deleting discipline $disciplineId from Supabase: ${e.localizedMessage}", e)
        }
    }

    suspend fun deleteTopicRemote(topicId: Long) {
        if (!isConfigured()) return
        try {
            Log.d("SyncManager", "Starting remote delete for topic $topicId")
            try {
                postgrest.from("questions").delete {
                    filter { eq("topic_id", topicId) }
                }
            } catch (_: Throwable) {}

            try {
                postgrest.from("flashcards").delete {
                    filter { eq("topic_id", topicId) }
                }
            } catch (_: Throwable) {}

            postgrest.from("topics").delete {
                filter { eq("id", topicId) }
            }
            Log.d("SyncManager", "Successfully deleted topic $topicId from Supabase")
        } catch (e: Throwable) {
            Log.e("SyncManager", "Error deleting topic $topicId from Supabase: ${e.localizedMessage}", e)
        }
    }

    suspend fun deleteFlashcardRemote(flashcardId: Long) {
        if (!isConfigured()) return
        try {
            postgrest.from("flashcards").delete {
                filter { eq("id", flashcardId) }
            }
            Log.d("SyncManager", "Successfully deleted flashcard $flashcardId from Supabase")
        } catch (e: Throwable) {
            Log.e("SyncManager", "Error deleting flashcard $flashcardId from Supabase: ${e.localizedMessage}", e)
        }
    }
}

