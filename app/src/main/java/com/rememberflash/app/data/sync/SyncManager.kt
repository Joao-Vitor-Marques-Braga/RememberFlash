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
import com.rememberflash.app.data.local.database.dao.TopicDao
import com.rememberflash.app.data.local.database.entity.ContestEntity
import com.rememberflash.app.data.local.database.entity.DisciplineEntity
import com.rememberflash.app.data.local.database.entity.FlashcardEntity
import com.rememberflash.app.data.local.database.entity.QuestionEntity
import com.rememberflash.app.data.local.database.entity.TopicEntity
import com.rememberflash.app.data.remote.supabase.dto.ContestSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.DisciplineSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.FlashcardSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.QuestionSupabaseDto
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
            return com.rememberflash.app.domain.common.Result.error("Credenciais do Supabase não configuradas no aplicativo.")
        }
        _isSyncing.value = true
        return try {
            syncUpstreamInternal()
            if (!currentUserId.isNullOrBlank()) {
                performDownstreamSync(currentUserId)
            }
            Log.d("SyncManager", "Manual sync completed successfully!")
            com.rememberflash.app.domain.common.Result.success("Sincronização com o Supabase concluída!")
        } catch (e: Throwable) {
            Log.e("SyncManager", "Manual sync error: ${e.localizedMessage}", e)
            com.rememberflash.app.domain.common.Result.error("Falha ao sincronizar: ${e.localizedMessage}")
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun syncUpstreamInternal() {
        val unsyncedContests = contestDao.getUnsyncedContests()
        val unsyncedDisciplines = disciplineDao.getUnsyncedDisciplines()
        val unsyncedTopics = topicDao.getUnsyncedTopics()
        val unsyncedFlashcards = flashcardDao.getUnsyncedFlashcards()
        val unsyncedQuestions = questionDao.getUnsyncedQuestions()

        if (unsyncedContests.isEmpty() &&
            unsyncedDisciplines.isEmpty() &&
            unsyncedTopics.isEmpty() &&
            unsyncedFlashcards.isEmpty() &&
            unsyncedQuestions.isEmpty()
        ) {
            Log.d("SyncManager", "No upstream items to sync")
            return
        }

        Log.d(
            "SyncManager",
            "Upstream sync started: ${unsyncedContests.size} contests, ${unsyncedDisciplines.size} disciplines, " +
                    "${unsyncedTopics.size} topics, ${unsyncedFlashcards.size} flashcards, ${unsyncedQuestions.size} questions"
        )

        // 1. Contests
        if (unsyncedContests.isNotEmpty()) {
            try {
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
                        isActive = entity.isActive,
                        isSynced = true
                    )
                }
                postgrest.from("contests").upsert(dtos)
                contestDao.markContestsAsSynced(unsyncedContests.map { it.id })
                Log.d("SyncManager", "Successfully synced ${unsyncedContests.size} contests to Supabase")
            } catch (e: Throwable) {
                Log.e("SyncManager", "Error syncing contests to Supabase: ${e.localizedMessage}", e)
            }
        }

        // 2. Disciplines (ensuring parent contests exist in Supabase first)
        if (unsyncedDisciplines.isNotEmpty()) {
            try {
                val parentContestIds = unsyncedDisciplines.map { it.contestId }.distinct()
                val parentContests = contestDao.getByIds(parentContestIds)
                if (parentContests.isNotEmpty()) {
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
                            isActive = entity.isActive,
                            isSynced = true
                        )
                    }
                    postgrest.from("contests").upsert(contestDtos)
                    contestDao.markContestsAsSynced(parentContests.map { it.id })
                }

                val dtos = unsyncedDisciplines.map { entity ->
                    DisciplineSupabaseDto(
                        id = entity.id,
                        contestId = entity.contestId,
                        name = entity.name,
                        weight = entity.weight,
                        totalTopics = entity.totalTopics,
                        completedTopics = entity.completedTopics,
                        isActive = entity.isActive,
                        isSynced = true
                    )
                }
                postgrest.from("disciplines").upsert(dtos)
                disciplineDao.markDisciplinesAsSynced(unsyncedDisciplines.map { it.id })
                Log.d("SyncManager", "Successfully synced ${unsyncedDisciplines.size} disciplines to Supabase")
            } catch (e: Throwable) {
                Log.e("SyncManager", "Error syncing disciplines to Supabase: ${e.localizedMessage}", e)
            }
        }

        // 3. Topics (ensuring parent disciplines exist in Supabase first)
        if (unsyncedTopics.isNotEmpty()) {
            try {
                val parentDisciplineIds = unsyncedTopics.map { it.disciplineId }.distinct()
                val parentDisciplines = disciplineDao.getByIds(parentDisciplineIds)
                if (parentDisciplines.isNotEmpty()) {
                    val disciplineDtos = parentDisciplines.map { entity ->
                        DisciplineSupabaseDto(
                            id = entity.id,
                            contestId = entity.contestId,
                            name = entity.name,
                            weight = entity.weight,
                            totalTopics = entity.totalTopics,
                            completedTopics = entity.completedTopics,
                            isActive = entity.isActive,
                            isSynced = true
                        )
                    }
                    postgrest.from("disciplines").upsert(disciplineDtos)
                    disciplineDao.markDisciplinesAsSynced(parentDisciplines.map { it.id })
                }

                val dtos = unsyncedTopics.map { entity ->
                    TopicSupabaseDto(
                        id = entity.id,
                        disciplineId = entity.disciplineId,
                        contestId = entity.contestId,
                        name = entity.name,
                        description = entity.description,
                        isCompleted = entity.isCompleted,
                        orderIndex = entity.orderIndex,
                        createdAt = entity.createdAt,
                        isSynced = true
                    )
                }
                postgrest.from("topics").upsert(dtos)
                topicDao.markTopicsAsSynced(unsyncedTopics.map { it.id })
                Log.d("SyncManager", "Successfully synced ${unsyncedTopics.size} topics to Supabase")
            } catch (e: Throwable) {
                Log.e("SyncManager", "Error syncing topics to Supabase: ${e.localizedMessage}", e)
            }
        }

        // 4. Flashcards
        if (unsyncedFlashcards.isNotEmpty()) {
            try {
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
                        createdAt = entity.createdAt,
                        isSynced = true
                    )
                }
                postgrest.from("flashcards").upsert(dtos)
                flashcardDao.markFlashcardsAsSynced(unsyncedFlashcards.map { it.id })
                Log.d("SyncManager", "Successfully synced ${unsyncedFlashcards.size} flashcards to Supabase")
            } catch (e: Throwable) {
                Log.e("SyncManager", "Error syncing flashcards to Supabase: ${e.localizedMessage}", e)
            }
        }

        // 5. Questions (ensuring parent disciplines exist in Supabase first)
        if (unsyncedQuestions.isNotEmpty()) {
            try {
                val parentDisciplineIds = unsyncedQuestions.map { it.disciplineId }.distinct()
                val parentDisciplines = disciplineDao.getByIds(parentDisciplineIds)
                if (parentDisciplines.isNotEmpty()) {
                    val disciplineDtos = parentDisciplines.map { entity ->
                        DisciplineSupabaseDto(
                            id = entity.id,
                            contestId = entity.contestId,
                            name = entity.name,
                            weight = entity.weight,
                            totalTopics = entity.totalTopics,
                            completedTopics = entity.completedTopics,
                            isActive = entity.isActive,
                            isSynced = true
                        )
                    }
                    postgrest.from("disciplines").upsert(disciplineDtos)
                    disciplineDao.markDisciplinesAsSynced(parentDisciplines.map { it.id })
                }

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
                        createdAt = entity.createdAt,
                        isSynced = true
                    )
                }
                postgrest.from("questions").upsert(dtos)
                questionDao.markQuestionsAsSynced(unsyncedQuestions.map { it.id })
                Log.d("SyncManager", "Successfully synced ${unsyncedQuestions.size} questions to Supabase")
            } catch (e: Throwable) {
                Log.e("SyncManager", "Error syncing questions to Supabase: ${e.localizedMessage}", e)
            }
        }
    }

    private suspend fun performDownstreamSync(userId: String) {
        try {
            Log.d("SyncManager", "Starting downstream sync for user: $userId")

            val remoteContests = postgrest.from("contests").select {
                filter { eq("user_id", userId) }
            }.decodeList<ContestSupabaseDto>()

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

                    if (remoteDisciplines.isNotEmpty()) {
                        val disciplineEntities = remoteDisciplines.map { dto ->
                            DisciplineEntity(
                                id = dto.id ?: 0L,
                                contestId = dto.contestId,
                                name = dto.name,
                                weight = dto.weight ?: 1.0,
                                totalTopics = dto.totalTopics ?: 0,
                                completedTopics = dto.completedTopics ?: 0,
                                isActive = dto.isActive ?: true,
                                isSynced = true
                            )
                        }
                        disciplineDao.insertAll(disciplineEntities)

                        val disciplineIds = remoteDisciplines.mapNotNull { it.id }

                        val remoteTopics = postgrest.from("topics").select {
                            filter { isIn("contest_id", contestIds) }
                        }.decodeList<TopicSupabaseDto>()

                        if (remoteTopics.isNotEmpty()) {
                            val topicEntities = remoteTopics.map { dto ->
                                TopicEntity(
                                    id = dto.id ?: 0L,
                                    disciplineId = dto.disciplineId,
                                    contestId = dto.contestId,
                                    name = dto.name,
                                    description = dto.description,
                                    isCompleted = dto.isCompleted ?: false,
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
                    }
                }
            }
            Log.d("SyncManager", "Downstream sync finished successfully.")
        } catch (e: Throwable) {
            Log.e("SyncManager", "Downstream sync failed: ${e.localizedMessage}", e)
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

