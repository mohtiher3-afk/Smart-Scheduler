package com.example.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.PlayArrow

// Import architecture packages
import com.example.models.Course
import com.example.models.ReminderEntity
import com.example.screens.tabs.ScheduleTab
import com.example.screens.tabs.DashboardTab
import com.example.screens.tabs.CalculatorTab
import com.example.screens.tabs.RemindersTab
import com.example.screens.tabs.SmartSchedulerTab
import com.example.widgets.AddEditCourseDialog
import com.example.widgets.CustomInAppToast
import com.example.services.SchedulerUtils

import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScheduleApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val currentLang = LocalAppLanguage.current
    val loc = remember(currentLang) { Loc(currentLang) }
    
    val courses by viewModel.allCourses.collectAsStateWithLifecycle()
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    
    val selectedCourseId by viewModel.selectedCourseId.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()
    val calculatedSessions by viewModel.calculatedSessions.collectAsStateWithLifecycle()
    val upcomingLecturesAlerts by viewModel.upcomingLecturesAlerts.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(upcomingLecturesAlerts) {
        if (upcomingLecturesAlerts.isNotEmpty()) {
            viewModel.scheduleAlarmsForNextLectures(context)
            viewModel.checkAndSchedule15MinPreMeetingAlerts(context)
        }
    }

    var activeTab by remember { mutableStateOf(0) } // 0 = جدول الدورات, 1 = لوحة البيانات, 2 = الحاسبة الذكية, 3 = التنبيهات, 4 = المجدول الذكي

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }

    // Material 3 Coordinated Dynamic Theme Palette
    val deepNavy = MaterialTheme.colorScheme.primary
    val accentBlue = MaterialTheme.colorScheme.secondary
    val lightGrayBg = MaterialTheme.colorScheme.background
    val activeGreen = MaterialTheme.colorScheme.tertiary

    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(deepNavy, accentBlue)
                        )
                    )
                    .drawBehind {
                        // Drawing subtle soft overlays that resemble glowing background spheres
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = size.width * 0.35f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.15f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = size.width * 0.55f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.85f)
                        )
                    }
                    .statusBarsPadding()
                    .padding(top = 20.dp, bottom = 22.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = loc.settingsTitle,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Sparkles Icon",
                            tint = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = loc.appTitle,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("app_title")
                        )
                    }

                    val themeIcon = when (themeMode) {
                        "dark" -> Icons.Rounded.DarkMode
                        "light" -> Icons.Rounded.LightMode
                        else -> Icons.Rounded.BrightnessAuto
                    }
                    val themeLabel = when (themeMode) {
                        "dark" -> loc.darkTheme
                        "light" -> loc.lightTheme
                        else -> loc.systemTheme
                    }

                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = themeIcon,
                            contentDescription = themeLabel,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = loc.appSubtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.90f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val scale0 by animateFloatAsState(
                    targetValue = if (activeTab == 0) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "tab0_scale"
                )
                val scale1 by animateFloatAsState(
                    targetValue = if (activeTab == 1) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "tab1_scale"
                )
                val scale2 by animateFloatAsState(
                    targetValue = if (activeTab == 2) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "tab2_scale"
                )
                val scale3 by animateFloatAsState(
                    targetValue = if (activeTab == 3) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "tab3_scale"
                )
                val scale4 by animateFloatAsState(
                    targetValue = if (activeTab == 4) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "tab4_scale"
                )

                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Rounded.List, contentDescription = loc.tabSchedule, modifier = Modifier.graphicsLayer(scaleX = scale0, scaleY = scale0)) },
                    label = { Text(loc.tabSchedule, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_schedule")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Rounded.PieChart, contentDescription = loc.tabDashboard, modifier = Modifier.graphicsLayer(scaleX = scale1, scaleY = scale1)) },
                    label = { Text(loc.tabDashboard, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_dashboard")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { 
                        if (selectedCourseId == -1L && courses.isNotEmpty()) {
                            viewModel.selectCourseForCalculator(courses.first().id.toLong())
                        } else {
                            viewModel.recalculateSessions()
                        }
                        activeTab = 2 
                    },
                    icon = { Icon(Icons.Rounded.Calculate, contentDescription = loc.tabCalculator, modifier = Modifier.graphicsLayer(scaleX = scale2, scaleY = scale2)) },
                    label = { Text(loc.tabCalculator, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_calculator")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { 
                        BadgedBox(badge = {
                            if (reminders.isNotEmpty()) {
                                Badge(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.White) {
                                    Text(reminders.size.toString())
                                }
                            }
                        }) {
                            Icon(Icons.Rounded.Notifications, contentDescription = loc.tabAlerts, modifier = Modifier.graphicsLayer(scaleX = scale3, scaleY = scale3))
                        }
                    },
                    label = { Text(loc.tabAlerts, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_alerts")
                )
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = loc.tabSmartScheduler, modifier = Modifier.graphicsLayer(scaleX = scale4, scaleY = scale4)) },
                    label = { Text(loc.tabSmartScheduler, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_smart_scheduler")
                )
            }
        },
        floatingActionButton = {
            if (activeTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { 
                        courseToEdit = null
                        showAddCourseDialog = true 
                    },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = loc.addCourse) },
                    text = { Text(loc.addCourse, fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("add_course_fab")
                )
            }
        },
        containerColor = lightGrayBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val customToast by viewModel.customInAppToast.collectAsStateWithLifecycle()

            AnimatedVisibility(
                visible = customToast != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(99f)
            ) {
                customToast?.let { toast ->
                    CustomInAppToast(
                        toastData = toast,
                        currentLang = currentLang,
                        onDismiss = { viewModel.dismissCustomToast() }
                    )
                }
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    val duration = 450
                    val easing = FastOutSlowInEasing
                    if (initialState == 0 && targetState == 2) {
                        // Moving from List to Details: Material 3 Shared Axis (X/Y) with smooth upward slide, scale up, and fade
                        (fadeIn(animationSpec = tween(duration, easing = easing)) + 
                         scaleIn(initialScale = 0.92f, animationSpec = tween(duration, easing = easing)) +
                         slideInVertically(initialOffsetY = { it / 10 }, animationSpec = tween(duration, easing = easing)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(300, easing = easing)) + 
                                scaleOut(targetScale = 0.96f, animationSpec = tween(300, easing = easing))
                            )
                    } else if (initialState == 2 && targetState == 0) {
                        // Returning from Details to List: Material 3 Shared Axis transition returning to the main list
                        (fadeIn(animationSpec = tween(duration, easing = easing)) + 
                         scaleIn(initialScale = 0.96f, animationSpec = tween(duration, easing = easing)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(duration, easing = easing)) + 
                                scaleOut(targetScale = 0.92f, animationSpec = tween(duration, easing = easing)) +
                                slideOutVertically(targetOffsetY = { it / 10 }, animationSpec = tween(duration, easing = easing))
                            )
                    } else {
                        // Standard tab-switching sliding transition for other tabs
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = tween(duration, easing = easing)) { width -> width } + 
                             fadeIn(animationSpec = tween(duration, easing = easing))).togetherWith(
                                slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { width -> -width } + 
                                fadeOut(animationSpec = tween(duration, easing = easing))
                            )
                        } else {
                            (slideInHorizontally(animationSpec = tween(duration, easing = easing)) { width -> -width } + 
                             fadeIn(animationSpec = tween(duration, easing = easing))).togetherWith(
                                slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { width -> width } + 
                                fadeOut(animationSpec = tween(duration, easing = easing))
                            )
                        }
                    }
                },
                label = "tab_transition",
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->
                when (targetTab) {
                    0 -> ScheduleTab(
                        courses = courses,
                        upcomingLecturesAlerts = upcomingLecturesAlerts,
                        onCalculate = { course ->
                            viewModel.selectCourseForCalculator(course.id.toLong())
                            viewModel.recalculateSessions()
                            activeTab = 2
                        },
                        onEdit = { course ->
                            courseToEdit = course
                            showAddCourseDialog = true
                        },
                        onDelete = { course ->
                            viewModel.deleteCourse(course)
                            Toast.makeText(context, "تم حذف الدورة بنجاح", Toast.LENGTH_SHORT).show()
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
                        }
                    )
                    1 -> DashboardTab(
                        courses = courses,
                        themeMode = themeMode,
                        dynamicColorEnabled = dynamicColorEnabled,
                        onThemeChange = { viewModel.setThemeMode(it) },
                        onDynamicColorChange = { viewModel.setDynamicColorEnabled(it) },
                        onCourseClick = { course ->
                            viewModel.selectCourseForCalculator(course.id.toLong())
                            viewModel.recalculateSessions()
                            activeTab = 2
                        },
                        context = context
                    )
                    2 -> CalculatorTab(
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
                        }
                    )
                    3 -> RemindersTab(
                        reminders = reminders,
                        onDeleteReminder = { reminder ->
                            viewModel.deleteReminder(reminder)
                            Toast.makeText(context, "تم إلغاء التنبيه", Toast.LENGTH_SHORT).show()
                        },
                        context = context
                    )
                    4 -> SmartSchedulerTab(
                        viewModel = viewModel,
                        onCourseAddedAndNavigationRequested = {
                            activeTab = 0
                        }
                    )
                }
            }
        }
    }

    if (showAddCourseDialog) {
        AddEditCourseDialog(
            course = courseToEdit,
            existingCourses = courses,
            onDismiss = { showAddCourseDialog = false },
            onConfirm = { name, days, startTime, endTime, zoomAccount, targetCountVal, isActive, reminderLeadMinutes, colorHex ->
                if (courseToEdit == null) {
                    viewModel.addCourse(name, days, startTime, endTime, zoomAccount, targetCountVal, isActive, reminderLeadMinutes, colorHex)
                    val successMsg = if (currentLang == "ar") "تمت إضافة الدورة بنجاح" else "Course added successfully"
                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateCourse(
                        courseToEdit!!.copy(
                            name = name,
                            days = mapIndicesToArabicDays(days),
                            timeStart = startTime,
                            timeEnd = endTime,
                            zoomAccount = zoomAccount,
                            status = if (isActive) "نشط" else "غير نشط",
                            targetCount = targetCountVal,
                            reminderLeadMinutes = reminderLeadMinutes,
                            colorHex = colorHex
                        )
                    )
                    val successMsg = if (currentLang == "ar") "تم تحديث الدورة بنجاح" else "Course updated successfully"
                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                }
                showAddCourseDialog = false
            }
        )
    }

    if (showSettingsDialog) {
        val selectedSound = viewModel.alertSound.collectAsStateWithLifecycle().value
        SettingsDialog(
            currentLang = currentLang,
            themeMode = viewModel.themeMode.collectAsStateWithLifecycle().value,
            dynamicColorEnabled = viewModel.dynamicColorEnabled.collectAsStateWithLifecycle().value,
            selectedSound = selectedSound,
            onLangChange = { viewModel.setAppLanguage(it) },
            onThemeChange = { viewModel.setThemeMode(it) },
            onDynamicColorChange = { viewModel.setDynamicColorEnabled(it) },
            onSoundChange = { viewModel.setAlertSound(it) },
            onPlaySoundPreview = { sound -> viewModel.playAlertSoundPreview(context, sound) },
            onStopSoundPreview = { viewModel.stopAlertSoundPreview() },
            onDismiss = { 
                viewModel.stopAlertSoundPreview()
                showSettingsDialog = false 
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun SettingsDialog(
    currentLang: String,
    themeMode: String,
    dynamicColorEnabled: Boolean,
    selectedSound: String,
    onLangChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onSoundChange: (String) -> Unit,
    onPlaySoundPreview: (String) -> Unit,
    onStopSoundPreview: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loc = remember(currentLang) { Loc(currentLang) }
    
    // Collect new architectural state flows
    val syncState by com.example.services.CloudSyncManager.syncState.collectAsStateWithLifecycle()
    val lastSyncTime by com.example.services.CloudSyncManager.lastSyncTime.collectAsStateWithLifecycle()
    val remoteConfig by com.example.services.RemoteConfigManager.config.collectAsStateWithLifecycle()
    val isFetchingConfig by com.example.services.RemoteConfigManager.isFetching.collectAsStateWithLifecycle()
    val diagnosticLogs by com.example.services.DiagnosticLogger.logs.collectAsStateWithLifecycle()
    val courses by viewModel.allCourses.collectAsStateWithLifecycle()

    var showLogsConsole by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = loc.settingsTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Language Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = loc.selectLanguage,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ar" to "العربية", "en" to "English").forEach { (code, name) ->
                            val isSelected = currentLang == code
                            FilterChip(
                                selected = isSelected,
                                onClick = { onLangChange(code) },
                                label = { Text(name) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Theme Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = loc.selectTheme,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "system" to loc.systemTheme,
                            "light" to loc.lightTheme,
                            "dark" to loc.darkTheme
                        ).forEach { (mode, name) ->
                            val isSelected = themeMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { onThemeChange(mode) },
                                label = { Text(name, fontSize = 11.sp, maxLines = 1) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Sound Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (currentLang == "ar") "صوت جرس التنبيهات 🔔" else "Notification Alert Sound 🔔",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    var playingPreview by remember { mutableStateOf<String?>(null) }
                    
                    val soundOptions = remember(currentLang) {
                        listOf(
                            "default" to (if (currentLang == "ar") "النظام الافتراضي" else "System Default"),
                            "digital_beep" to (if (currentLang == "ar") "رنين رقمي ثنائي" else "Digital Beep"),
                            "soft_chime" to (if (currentLang == "ar") "جرس هادئ مزدوج" else "Soft Chime"),
                            "classic_bell" to (if (currentLang == "ar") "نغمة كلاسيكية" else "Classic Bell"),
                            "tech_alert" to (if (currentLang == "ar") "تنبيه تقني متصاعد" else "Tech Alert")
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        soundOptions.forEach { (soundKey, soundLabel) ->
                            val isSelected = selectedSound == soundKey
                            Surface(
                                onClick = { 
                                    onSoundChange(soundKey)
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onSoundChange(soundKey) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Text(
                                            text = soundLabel,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (playingPreview == soundKey) {
                                                onStopSoundPreview()
                                                playingPreview = null
                                            } else {
                                                onPlaySoundPreview(soundKey)
                                                playingPreview = soundKey
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (playingPreview == soundKey) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                            contentDescription = if (playingPreview == soundKey) "Stop" else "Play",
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = loc.enableDynamicColor,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = loc.dynamicColorDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = dynamicColorEnabled,
                                onCheckedChange = onDynamicColorChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ---- 1. OS Integration ----
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (currentLang == "ar") "التكامل مع نظام التشغيل 📱" else "OS Integration 📱",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (currentLang == "ar") {
                                    "يمكنك مزامنة مواعيد دوراتك التدريبية مباشرة مع تقويم أندرويد لتلقي التنبيهات على ساعتك الذكية."
                                } else {
                                    "Synchronize your course schedules directly with Android Calendar to receive alerts on your smartwatch."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    var successCount = 0
                                    val activeCourses = courses.filter { it.status == "نشط" }
                                    for (course in activeCourses) {
                                        val result = com.example.services.CalendarProviderHelper.syncCourseToCalendar(context, course)
                                        if (result) successCount++
                                    }
                                    if (successCount > 0) {
                                        val msg = if (currentLang == "ar") {
                                            "تم مزامنة $successCount دورات مع تقويم نظام أندرويد بنجاح!"
                                        } else {
                                            "Synced $successCount courses to Android Calendar successfully!"
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        com.example.services.DiagnosticLogger.log("INFO", "CalendarSync", msg)
                                    } else {
                                        val msg = if (currentLang == "ar") {
                                            "تنبيه: الرجاء التأكد من تفعيل صلاحيات التقويم للتطبيق في إعدادات نظام أندرويد."
                                        } else {
                                            "Warning: Please ensure Calendar write permissions are enabled in Android settings."
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        com.example.services.DiagnosticLogger.log("WARN", "CalendarSync", "فشل المزامنة لعدم توفر الصلاحية أو عدم وجود دورات نشطة.")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = if (currentLang == "ar") "مزامنة المواعيد مع تقويم النظام 📅" else "Sync to System Calendar 📅",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ---- 2. Cloud Backup & Sync ----
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (currentLang == "ar") "مزامنة سحابية وقاعدة بيانات آمنة ☁️" else "Cloud Sync & Secure Storage ☁️",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentLang == "ar") "آخر مزامنة ناجحة:" else "Last successful sync:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = lastSyncTime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            when (val state = syncState) {
                                is com.example.services.CloudSyncManager.SyncState.Syncing -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        LinearProgressIndicator(
                                            progress = state.progress,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            text = state.message,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                is com.example.services.CloudSyncManager.SyncState.Success -> {
                                    Text(
                                        text = state.message,
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                is com.example.services.CloudSyncManager.SyncState.Error -> {
                                    Text(
                                        text = state.error,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                else -> {}
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        com.example.services.CloudSyncManager.performCloudSync(context, courses)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                enabled = syncState !is com.example.services.CloudSyncManager.SyncState.Syncing
                            ) {
                                Text(
                                    text = if (currentLang == "ar") "بدء النسخ الاحتياطي السحابي السريع ☁️" else "Start Quick Cloud Backup ☁️",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ---- 3. Remote Server Message & Support ----
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (currentLang == "ar") "إعلانات ودعم المطورين عن بُعد 🌐" else "Remote Config & Developer Support 🌐",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = remoteConfig.announcement,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (currentLang == "ar") remoteConfig.motdArabic else remoteConfig.motdEnglish,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentLang == "ar") "الدعم الفني:" else "Support Contact:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = remoteConfig.supportContact,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        com.example.services.RemoteConfigManager.fetchLatestConfig()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isFetchingConfig
                            ) {
                                Text(
                                    text = if (isFetchingConfig) {
                                        if (currentLang == "ar") "جاري جلب البيانات..." else "Fetching config..."
                                    } else {
                                        if (currentLang == "ar") "تحديث إعلانات النظام المباشرة 🔄" else "Refresh System Announcements 🔄"
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ---- 4. Diagnostics Observability Console ----
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLang == "ar") "لوحة التشخيص ومراقبة الأداء البرمجي 🛠️" else "Diagnostics & Observability Console 🛠️",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { showLogsConsole = !showLogsConsole },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (showLogsConsole) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                contentDescription = "Toggle logs",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (showLogsConsole) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), // Deep console dark
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CONSOLE STDOUT / TRACE LOGS",
                                        color = Color(0xFF888888),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(
                                        onClick = { com.example.services.DiagnosticLogger.clearLogs() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("CLEAR", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (diagnosticLogs.isEmpty()) {
                                        Text(
                                            text = "[OK] No event traces logged yet. All systems nominal.",
                                            color = Color(0xFF10B981),
                                            fontSize = 10.sp
                                        )
                                    } else {
                                        diagnosticLogs.forEach { log ->
                                            val color = when (log.level) {
                                                "WARN" -> Color(0xFFF59E0B) // Amber
                                                "ERROR" -> Color(0xFFEF4444) // Red
                                                else -> Color(0xFF10B981) // Green
                                            }
                                            Text(
                                                text = "${log.timestamp} [${log.level}] <${log.tag}>: ${log.message}",
                                                color = color,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onStopSoundPreview()
                    onDismiss()
                }
            ) {
                Text(text = loc.close, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

private fun mapIndicesToArabicDays(indices: List<Int>): String {
    return indices.map { index ->
        when (index) {
            0 -> "الأحد"
            1 -> "الاثنين"
            2 -> "الثلاثاء"
            3 -> "الأربعاء"
            4 -> "الخميس"
            5 -> "الجمعة"
            6 -> "السبت"
            else -> ""
        }
    }.filter { it.isNotEmpty() }.joinToString("، ")
}

private fun showDatePicker(
    context: Context,
    currentDateStr: String,
    onDateSelected: (String) -> Unit
) {
    val date = SchedulerUtils.parseDate(currentDateStr) ?: Date()
    val calendar = Calendar.getInstance()
    calendar.time = date

    val datePickerDialog = DatePickerDialog(
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
