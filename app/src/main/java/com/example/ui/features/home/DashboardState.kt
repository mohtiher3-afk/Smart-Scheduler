package com.example.ui.features.home

import com.example.models.Course
import com.example.models.SessionInfo

data class DashboardState(
    val userName: String = "Scholar",
    val activeCoursesCount: Int = 0,
    val completedLecturesCount: Int = 0,
    val totalLecturesCount: Int = 0,
    val totalHours: Double = 0.0,
    val nextLectureName: String? = null,
    val nextLectureTime: String? = null,
    val progressPercentage: Float = 0f,
    val upcomingLectures: List<SessionInfo> = emptyList(),
    val aiSuggestionsList: List<String> = emptyList(),
    val isLoading: Boolean = false
)
