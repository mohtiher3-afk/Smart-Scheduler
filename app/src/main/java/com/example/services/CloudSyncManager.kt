package com.example.services

import android.content.Context
import com.example.models.Course
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object CloudSyncManager {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String>("لم يتم المزامنة بعد")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    sealed class SyncState {
        object Idle : SyncState()
        data class Syncing(val progress: Float, val message: String) : SyncState()
        data class Success(val message: String, val timestamp: String) : SyncState()
        data class Error(val error: String) : SyncState()
    }

    /**
     * Simulates cloud backup and restoration with real-time progress updates.
     */
    suspend fun performCloudSync(context: Context, localCourses: List<Course>) {
        _syncState.value = SyncState.Syncing(0.1f, "جاري الاتصال بالسيرفر السحابي الآمن...")
        delay(800)

        _syncState.value = SyncState.Syncing(0.3f, "جاري تشفير وتأمين روابط ومواعيد الدورات...")
        delay(900)

        // Simulate encrypting database and uploading chunks
        val encryptedDataSize = localCourses.sumOf { it.name.length + it.zoomAccount.length }
        _syncState.value = SyncState.Syncing(0.6f, "جاري رفع ${localCourses.size} دورة تدريبية ($encryptedDataSize بايت)...")
        delay(1000)

        _syncState.value = SyncState.Syncing(0.8f, "جاري دمج التعديلات والتحقق من تعارض البيانات...")
        delay(800)

        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault())
        val timestamp = sdf.format(Date())
        
        // Save sync state to secure preferences
        SecurityHelper.saveSecureSetting(context, "last_cloud_sync", timestamp)
        _lastSyncTime.value = timestamp
        
        _syncState.value = SyncState.Success("تمت المزامنة وحفظ النسخة الاحتياطية بنجاح!", timestamp)
    }

    fun initLastSyncTime(context: Context) {
        val last = SecurityHelper.getSecureSetting(context, "last_cloud_sync", "لم يتم المزامنة بعد")
        _lastSyncTime.value = last
    }
}
