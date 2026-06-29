package com.example.ui.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.models.Course
import com.example.screens.MainViewModel
import com.example.screens.LocalAppLanguage
import com.example.screens.tabs.*
import com.example.core.designsystem.theme.Motion
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun SmartSchedulerNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    onAddCourseClick: () -> Unit,
    onEditCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentLang = LocalAppLanguage.current

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val courses = viewModel.allCourses.collectAsStateWithLifecycle().value
    val reminders = viewModel.allReminders.collectAsStateWithLifecycle().value
    val selectedCourseId = viewModel.selectedCourseId.collectAsStateWithLifecycle().value
    val startDate = viewModel.startDate.collectAsStateWithLifecycle().value
    val endDate = viewModel.endDate.collectAsStateWithLifecycle().value
    val calculatedSessions = viewModel.calculatedSessions.collectAsStateWithLifecycle().value
    val upcomingLecturesAlerts = viewModel.upcomingLecturesAlerts.collectAsStateWithLifecycle().value
    val themeMode = viewModel.themeMode.collectAsStateWithLifecycle().value
    val dynamicColorEnabled = viewModel.dynamicColorEnabled.collectAsStateWithLifecycle().value
    val selectedSound = viewModel.alertSound.collectAsStateWithLifecycle().value

    val duration = Motion.NormalTween.durationMillis
    val easing = FastOutSlowInEasing

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(
            route = Screen.Dashboard.route,
            enterTransition = {
                slideInHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeOut()
            }
        ) {
            com.example.feature.dashboard.presentation.DashboardScreenRoot()
        }

        composable(
            route = Screen.Schedule.route,
            enterTransition = {
                slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut()
            }
        ) {
            com.example.feature.schedule.presentation.ScheduleScreenRoot()
        }

        composable(
            route = Screen.SmartScheduler.route,
            enterTransition = {
                slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut()
            }
        ) {
            com.example.feature.smartscheduler.presentation.SmartSchedulerScreenRoot()
        }

        composable(
            route = Screen.Calculator.route,
            enterTransition = {
                slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut()
            }
        ) {
            com.example.feature.calculator.presentation.CalculatorScreenRoot()
        }

        composable(
            route = Screen.Alerts.route,
            enterTransition = {
                slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut()
            }
        ) {
            com.example.feature.alerts.presentation.AlertsScreenRoot()
        }

        composable(
            route = Screen.Tasks.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.tasks.presentation.TasksScreenRoot()
        }

        composable(
            route = Screen.More.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.more.presentation.MoreScreenRoot()
        }

        composable(
            route = Screen.Courses.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.courses.presentation.CoursesScreenRoot()
        }

        composable(
            route = Screen.SyncCenter.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.synccenter.presentation.SyncCenterScreenRoot()
        }

        composable(
            route = Screen.Analytics.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.analytics.presentation.AnalyticsScreenRoot()
        }

        composable(
            route = Screen.Calendar.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.calendar.presentation.CalendarScreenRoot()
        }

        composable(
            route = Screen.Notes.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.notes.presentation.NotesScreenRoot()
        }

        composable(
            route = Screen.Files.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.files.presentation.FilesScreenRoot()
        }

        composable(
            route = Screen.Exams.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.exams.presentation.ExamsScreenRoot()
        }

        composable(
            route = Screen.Profile.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.profile.presentation.ProfileScreenRoot()
        }

        composable(
            route = Screen.StudyHub.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.feature.studyhub.presentation.StudyHubScreenRoot()
        }
    }

    if (showStartDatePicker) {
        M3DatePickerDialog(
            initialDateStr = startDate,
            currentLang = currentLang,
            onDismiss = { showStartDatePicker = false },
            onConfirm = {
                viewModel.setStartDate(it)
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        M3DatePickerDialog(
            initialDateStr = endDate,
            currentLang = currentLang,
            onDismiss = { showEndDatePicker = false },
            onConfirm = {
                viewModel.setEndDate(it)
                showEndDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3DatePickerDialog(
    initialDateStr: String,
    currentLang: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialMillis = remember(initialDateStr) {
        val date = com.example.services.SchedulerUtils.parseDate(initialDateStr)
        date?.time
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = millis
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH) + 1
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        val formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
                        onConfirm(formatted)
                    }
                }
            ) {
                Text(if (currentLang == "ar") "تأكيد" else "Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLang == "ar") "إلغاء" else "Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    ) {
        DatePicker(state = state)
    }
}
