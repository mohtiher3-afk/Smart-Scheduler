package com.example.ai

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AiRecommendation(
    val title: String,
    val description: String,
    val actionLabel: String,
    val actionType: String // e.g., "START_STUDY", "OPTIMIZE_SCHEDULE", "REVIEW_NOTES"
)

@Immutable
@Serializable
data class ScheduleOptimization(
    val reason: String,
    val suggestions: List<MoveSuggestion>
)

@Immutable
@Serializable
data class MoveSuggestion(
    val taskName: String,
    val fromDay: String,
    val toDay: String,
    val confidence: Float
)

@Immutable
@Serializable
data class StudyCoachFeedback(
    val score: Int,
    val focusLevel: String,
    val weakSubject: String,
    val recommendation: String
)

@Immutable
@Serializable
data class WeeklyReport(
    val studyHours: Int,
    val bestSubject: String,
    val weakSubject: String,
    val completedTasks: Int,
    val attendanceRate: Int
)
