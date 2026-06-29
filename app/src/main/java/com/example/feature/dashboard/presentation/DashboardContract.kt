package com.example.feature.dashboard.presentation

import com.example.models.Course
import com.example.models.StudyGoal
import com.example.models.StudySession
import com.example.models.Task

/**
 * State of the Dashboard Screen.
 * Contains all the data needed to render the UI.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val upcomingClasses: List<Course> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val activeGoals: List<StudyGoal> = emptyList(),
    val recentSessions: List<StudySession> = emptyList(),
    val focusScore: Int = 0,
    val studyStreak: Int = 0,
    val aiInsights: String? = null,
    val errorMessage: String? = null
)

/**
 * User actions and events from the Dashboard Screen.
 */
sealed interface DashboardUiEvent {
    data object OnRefresh : DashboardUiEvent
    data class OnCourseClicked(val courseId: String) : DashboardUiEvent
    data class OnTaskCompleted(val taskId: String, val completed: Boolean) : DashboardUiEvent
    data object OnStartStudySession : DashboardUiEvent
    data object OnDismissError : DashboardUiEvent
}

/**
 * One-off effects like Navigation, Toasts, or Snackbars.
 */
sealed interface DashboardUiEffect {
    data class NavigateToCourse(val courseId: String) : DashboardUiEffect
    data object NavigateToStudySession : DashboardUiEffect
    data class ShowToast(val message: String) : DashboardUiEffect
}
