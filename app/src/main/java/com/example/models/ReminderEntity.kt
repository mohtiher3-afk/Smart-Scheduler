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
    val isEnabled: Boolean = true
)
