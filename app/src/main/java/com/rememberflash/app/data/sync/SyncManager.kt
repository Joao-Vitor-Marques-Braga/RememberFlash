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
import com.rememberflash.app.data.remote.supabase.dto.ContestSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.DisciplineSupabaseDto
import com.rememberflash.app.data.remote.supabase.dto.FlashcardSupabaseDto
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
    private val contestDao: ContestDao,
    private val disciplineDao: DisciplineDao,
    private val flashcardDao: FlashcardDao,
    private val postgrest: Postgrest
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

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

    fun triggerSync() {
        if (_isSyncing.value) return
        scope.launch {
            try {
                val unsyncedContests = contestDao.getUnsyncedContests()
                val unsyncedDisciplines = disciplineDao.getUnsyncedDisciplines()
                val unsyncedFlashcards = flashcardDao.getUnsyncedFlashcards()

                if (unsyncedContests.isEmpty() && unsyncedDisciplines.isEmpty() && unsyncedFlashcards.isEmpty()) {
                    Log.d("SyncManager", "No items to sync")
                    return@launch
                }

                _isSyncing.value = true
                Log.d("SyncManager", "Sync started: ${unsyncedContests.size} contests, ${unsyncedDisciplines.size} disciplines, ${unsyncedFlashcards.size} flashcards")

                if (BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
                    if (unsyncedContests.isNotEmpty()) {
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
                    }

                    if (unsyncedDisciplines.isNotEmpty()) {
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
                    }

                    if (unsyncedFlashcards.isNotEmpty()) {
                        val dtos = unsyncedFlashcards.map { entity ->
                            FlashcardSupabaseDto(
                                id = entity.id,
                                disciplineId = entity.disciplineId,
                                topicId = entity.topicId,
                                front = entity.front,
                                back = entity.back,
                                source = entity.source,
                                easeFactor = entity.easeFactor,
                                interval = entity.interval,
                                repetitions = entity.repetitions,
                                tokensSpent = entity.tokensSpent,
                                isSynced = true
                            )
                        }
                        postgrest.from("flashcards").upsert(dtos)
                        flashcardDao.markFlashcardsAsSynced(unsyncedFlashcards.map { it.id })
                    }
                } else {
                    Log.w("SyncManager", "Supabase credentials not configured in BuildConfig")
                }

                Log.d("SyncManager", "Sync successfully completed!")
            } catch (e: Throwable) {
                Log.e("SyncManager", "Sync failed: ${e.localizedMessage}", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
