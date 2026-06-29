package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Int,
    val startTime: Long, // timestamp
    val durationMinutes: Long,
    val focusScore: Int = 100, // 0-100
    val sessionType: String = "STUDY", // STUDY, QUIZ, RECAP
    // Sync Metadata
    val remoteId: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: Int = 1 // 0: Synced, 1: Pending, 2: Deleted (Local Only)
)

@Entity(tableName = "study_goals")
data class StudyGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetMinutes: Int,
    val currentMinutes: Int = 0,
    val deadline: Long,
    val isCompleted: Boolean = false,
    // Sync Metadata
    val remoteId: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: Int = 1
)

@Entity(tableName = "grades")
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Int,
    val weight: Double, // percentage of final grade (e.g., 0.2 for 20%)
    val score: Double, // achieved score
    val maxScore: Double,
    val category: String, // Exam, Assignment, Project, Quiz
    val date: Long = System.currentTimeMillis(),
    // Sync Metadata
    val remoteId: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: Int = 1
)
