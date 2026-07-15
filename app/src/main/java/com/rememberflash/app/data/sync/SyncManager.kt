package com.rememberflash.app.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.rememberflash.app.data.local.database.dao.ContestDao
import com.rememberflash.app.data.local.database.dao.DisciplineDao
import com.rememberflash.app.data.local.database.dao.FlashcardDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
    private val flashcardDao: FlashcardDao
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
                
                // Simulate network latency (2 seconds)
                delay(2000)

                if (unsyncedContests.isNotEmpty()) {
                    contestDao.markContestsAsSynced(unsyncedContests.map { it.id })
                }
                if (unsyncedDisciplines.isNotEmpty()) {
                    disciplineDao.markDisciplinesAsSynced(unsyncedDisciplines.map { it.id })
                }
                if (unsyncedFlashcards.isNotEmpty()) {
                    flashcardDao.markFlashcardsAsSynced(unsyncedFlashcards.map { it.id })
                }

                Log.d("SyncManager", "Sync successfully completed!")
            } catch (e: Exception) {
                Log.e("SyncManager", "Sync failed: ${e.localizedMessage}", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
