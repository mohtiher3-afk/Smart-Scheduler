package com.example.ui.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.example.ui.material3_foundation.Motion
import java.util.Calendar
import java.util.Locale

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
            com.example.ui.features.home.DashboardScreen(
                mainViewModel = viewModel,
                onAddCourseClick = onAddCourseClick,
                onNavigateToSchedule = {
                    navController.navigate(Screen.Schedule.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToCalculator = {
                    navController.navigate(Screen.Calculator.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToReminders = {
                    navController.navigate(Screen.Alerts.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
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
            ScheduleTab(
                courses = courses,
                upcomingLecturesAlerts = upcomingLecturesAlerts,
                onCalculate = { course ->
                    viewModel.selectCourseForCalculator(course.id.toLong())
                    viewModel.recalculateSessions()
                    navController.navigate(Screen.Calculator.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onEdit = onEditCourseClick,
                onDelete = { course ->
                    viewModel.deleteCourse(course)
                    val deleteMsg = if (currentLang == "ar") "تم حذف الدورة بنجاح" else "Course deleted successfully"
                    Toast.makeText(context, deleteMsg, Toast.LENGTH_SHORT).show()
                },
                onTestAlarm = { course ->
                    viewModel.triggerInstantTestAlarm(context, course.name, course.zoomAccount)
                },
                onTestUpcomingAlarm = { courseName, zoom ->
                    viewModel.triggerInstantTestAlarm(context, courseName, zoom)
                },
                clipboardManager = clipboardManager,
                context = context,
                onCourseUpdated = { updatedCourse ->
                    viewModel.updateCourse(updatedCourse)
                },
                onExportCSV = {
                    viewModel.exportAllDataToCSV(context)
                },
                onRefresh = {
                    viewModel.scheduleAlarmsForNextLectures(context)
                    viewModel.checkAndSchedule15MinPreMeetingAlerts(context)
                },
                onAddCourseClick = onAddCourseClick
            )
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
            SmartSchedulerTab(
                viewModel = viewModel,
                onCourseAddedAndNavigationRequested = {
                    navController.navigate(Screen.Schedule.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
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
            CalculatorTab(
                courses = courses,
                selectedCourseId = selectedCourseId,
                startDate = startDate,
                endDate = endDate,
                calculatedSessions = calculatedSessions,
                reminders = reminders,
                onCourseSelected = { viewModel.selectCourseForCalculator(it) },
                onStartDateClick = {
                    showDatePicker(context, startDate) { viewModel.setStartDate(it) }
                },
                onEndDateClick = {
                    showDatePicker(context, endDate) { viewModel.setEndDate(it) }
                },
                onToggleReminder = { session ->
                    val selectedCourse = courses.find { it.id.toLong() == selectedCourseId }
                    if (selectedCourse != null) {
                        viewModel.toggleReminderForSession(
                            context,
                            selectedCourse,
                            session.dateString,
                            session.formattedDate
                        )
                    }
                },
                context = context,
                onToggleSessionCompleted = { sessionIndex ->
                    val selectedCourse = courses.find { it.id.toLong() == selectedCourseId }
                    if (selectedCourse != null) {
                        viewModel.updateCourse(selectedCourse.toggleLectureCompleted(sessionIndex))
                    }
                },
                onCourseUpdated = { updatedCourse ->
                    viewModel.updateCourse(updatedCourse)
                },
                viewModel = viewModel
            )
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
            RemindersTab(
                reminders = reminders,
                onDeleteReminder = { reminder ->
                    viewModel.deleteReminder(reminder)
                    val cancelMsg = if (currentLang == "ar") "تم إلغاء التنبيه" else "Alert canceled"
                    Toast.makeText(context, cancelMsg, Toast.LENGTH_SHORT).show()
                },
                onClearAllReminders = {
                    viewModel.clearAllReminders(context)
                    val clearMsg = if (currentLang == "ar") "تم إلغاء كافة التنبيهات المجدولة" else "All scheduled alerts canceled"
                    Toast.makeText(context, clearMsg, Toast.LENGTH_SHORT).show()
                },
                selectedSound = selectedSound,
                onSoundChange = { viewModel.setAlertSound(it) },
                onPlaySoundPreview = { sound -> viewModel.playAlertSoundPreview(context, sound) },
                onStopSoundPreview = { viewModel.stopAlertSoundPreview() },
                onTestInstantAlert = { courseName, zoom ->
                    viewModel.triggerInstantTestAlarm(context, courseName, zoom)
                    val testMsg = if (currentLang == "ar") "سيصلك تنبيه تجريبي خلال 5 ثوانٍ ⏰" else "A test alert will arrive in 5 seconds ⏰"
                    Toast.makeText(context, testMsg, Toast.LENGTH_SHORT).show()
                },
                context = context
            )
        }
    }
}

private fun showDatePicker(
    context: Context,
    currentDateStr: String,
    onDateSelected: (String) -> Unit
) {
    val date = com.example.services.SchedulerUtils.parseDate(currentDateStr) ?: java.util.Date()
    val calendar = Calendar.getInstance()
    calendar.time = date

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedSelected = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedSelected)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}
