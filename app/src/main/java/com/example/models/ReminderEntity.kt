package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val courseName: String,
    val sessionDate: String, // "yyyy-MM-dd"
    val timeInMillis: Long,
    val isEnabled: Boolean = true,
    // Sync Metadata
    val remoteId: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: Int = 0 // 0: Synced, 1: Pending, 2: Deleted (Local Only)
)
