package com.example.ui.navigation

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
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {}
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
            DashboardTab(
                courses = courses,
                themeMode = themeMode,
                dynamicColorEnabled = dynamicColorEnabled,
                onThemeChange = { viewModel.setThemeMode(it) },
                onDynamicColorChange = { viewModel.setDynamicColorEnabled(it) },
                onCourseClick = { onEditCourseClick(it) },
                onAddCourseClick = onAddCourseClick,
                context = context
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
                    navController.navigate(Screen.Calculator.route)
                },
                onEdit = { onEditCourseClick(it) },
                onDelete = { viewModel.deleteCourse(it) },
                onTestAlarm = { viewModel.triggerInstantTestAlarm(context, it.name, it.zoomAccount) },
                onTestUpcomingAlarm = { name, zoom -> viewModel.triggerInstantTestAlarm(context, name, zoom) },
                clipboardManager = clipboardManager,
                context = context,
                onCourseUpdated = { viewModel.updateCourse(it) },
                onExportCSV = { viewModel.exportAllDataToCSV(context) }
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
                onCourseAddedAndNavigationRequested = { navController.popBackStack() }
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
                onStartDateClick = { showStartDatePicker = true },
                onEndDateClick = { showEndDatePicker = true },
                onToggleReminder = { session ->
                    val course = courses.find { it.id.toLong() == selectedCourseId }
                    if (course != null) {
                        viewModel.toggleReminderForSession(context, course, session.dateString, session.formattedDate)
                    }
                },
                context = context,
                onToggleSessionCompleted = { sessionNum ->
                    val course = courses.find { it.id.toLong() == selectedCourseId }
                    if (course != null) {
                        viewModel.updateCourse(course.toggleLectureCompleted(sessionNum))
                    }
                },
                onCourseUpdated = { viewModel.updateCourse(it) },
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
                onDeleteReminder = { viewModel.deleteReminder(it) },
                onClearAllReminders = { viewModel.clearAllReminders(context) },
                selectedSound = selectedSound,
                onSoundChange = { viewModel.setAlertSound(it) },
                onPlaySoundPreview = { viewModel.playAlertSoundPreview(context, it) },
                onStopSoundPreview = { viewModel.stopAlertSoundPreview() },
                onTestInstantAlert = { name, zoom -> viewModel.triggerInstantTestAlarm(context, name, zoom) },
                context = context
            )
        }

        composable(
            route = Screen.Tasks.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            TasksTab()
        }

        composable(
            route = Screen.More.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            MoreTab(
                navController = navController,
                onNavigateToSettings = onOpenSettings
            )
        }

        composable(
            route = Screen.Courses.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            CoursesScreen(
                courses = courses,
                onAddCourseClick = onAddCourseClick
            )
        }

        composable(
            route = Screen.SyncCenter.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.ui.features.sync.SyncCenterScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Analytics.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.ui.features.analytics.AnalyticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Calendar.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            com.example.ui.features.calendar.CalendarScreen(
                courses = courses,
                onCourseUpdated = { viewModel.updateCourse(it) }
            )
        }

        composable(
            route = Screen.Notes.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            NotesScreen()
        }

        composable(
            route = Screen.Files.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            FilesScreen()
        }

        composable(
            route = Screen.Exams.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            ExamsScreen()
        }

        composable(
            route = Screen.Profile.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            ProfileScreen(courses = courses)
        }

        composable(
            route = Screen.StudyHub.route,
            enterTransition = { slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } + fadeIn() },
            exitTransition = { slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } + fadeOut() }
        ) {
            StudyHubScreen(viewModel = viewModel)
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
