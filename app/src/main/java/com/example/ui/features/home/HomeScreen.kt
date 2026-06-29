package com.example.ui.features.home

import android.app.Application
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.models.Course
import com.example.models.SessionInfo
import com.example.screens.Loc
import com.example.screens.LocalAppLanguage
import com.example.screens.MainViewModel
import com.example.screens.SettingsDialog
import com.example.ui.features.ai.*
import com.example.core.designsystem.theme.AppTheme
import com.example.ui.navigation.BottomNavigation
import com.example.ui.navigation.Screen
import com.example.ui.navigation.SmartSchedulerNavGraph
import com.example.widgets.AddEditCourseDialog
import com.example.widgets.CustomInAppToast
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// 1. FOCUS MODE STATE MANAGER
object FocusModeManager {
    var isFocusModeActive by mutableStateOf(false)
    var isTimerRunning by mutableStateOf(false)
    var timeLeftSeconds by mutableStateOf(25 * 60)
    var selectedSound by mutableStateOf("None")
    var totalFocusMinutesToday by mutableStateOf(45)
    var completedSessionsToday by mutableStateOf(2)
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

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = LocalAppLanguage.current
    val loc = remember(currentLang) { Loc(currentLang) }

    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val useNavigationRail = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded

    data class NavItem(val screen: Screen, val icon: ImageVector, val title: String)
    val navItems = remember(loc) {
        listOf(
            NavItem(Screen.Dashboard, Icons.Rounded.Home, if (currentLang == "ar") "الرئيسية" else "Home"),
            NavItem(Screen.Schedule, Icons.Rounded.CalendarMonth, if (currentLang == "ar") "الجدول" else "Schedule"),
            NavItem(Screen.Calendar, Icons.Rounded.DateRange, if (currentLang == "ar") "التقويم" else "Calendar"),
            NavItem(Screen.Analytics, Icons.Rounded.Analytics, if (currentLang == "ar") "التحليلات" else "Analytics"),
            NavItem(Screen.Notes, Icons.Rounded.Note, if (currentLang == "ar") "ملاحظات" else "Notes")
        )
    }

    val courses by viewModel.allCourses.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsStateWithLifecycle()
    val selectedSound by viewModel.alertSound.collectAsStateWithLifecycle()

    val aiRecommendations by viewModel.aiRecommendations.collectAsStateWithLifecycle()
    val coachFeedback by viewModel.studyCoachFeedback.collectAsStateWithLifecycle()
    val isAiUpdating by viewModel.isAiUpdating.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshAiDashboard()
        viewModel.updateStudyCoach()
    }

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val deepNavy = AppTheme.colors.primary
    val accentBlue = AppTheme.colors.secondary
    val lightGrayBg = AppTheme.colors.background

    LaunchedEffect(FocusModeManager.isTimerRunning) {
        if (FocusModeManager.isTimerRunning) {
            while (FocusModeManager.timeLeftSeconds > 0) {
                delay(1000)
                if (FocusModeManager.isTimerRunning) {
                    FocusModeManager.timeLeftSeconds--
                } else {
                    break
                }
            }
            if (FocusModeManager.timeLeftSeconds == 0) {
                FocusModeManager.isTimerRunning = false
                FocusModeManager.totalFocusMinutesToday += 25
                FocusModeManager.completedSessionsToday += 1
                FocusModeManager.timeLeftSeconds = 25 * 60
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (useNavigationRail) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    FloatingActionButton(onClick = { isFabExpanded = !isFabExpanded }, shape = CircleShape) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                    }
                }
            ) {
                navItems.forEach { item ->
                    NavigationRailItem(
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 10.sp) }
                    )
                }
            }
        }

        Scaffold(
            modifier = modifier.weight(1f),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(Brush.horizontalGradient(listOf(deepNavy, accentBlue)))
                        .statusBarsPadding()
                        .padding(top = 16.dp, bottom = 18.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Rounded.Settings, contentDescription = null, tint = Color.White)
                        }
                        
                        val syncState by com.example.services.CloudSyncManager.syncState.collectAsStateWithLifecycle()
                        IconButton(onClick = { navController.navigate(Screen.SyncCenter.route) }) {
                            Icon(
                                imageVector = when(syncState) {
                                    is com.example.services.CloudSyncManager.SyncState.Success -> Icons.Rounded.CloudDone
                                    is com.example.services.CloudSyncManager.SyncState.Syncing -> Icons.Rounded.Sync
                                    is com.example.services.CloudSyncManager.SyncState.Error -> Icons.Rounded.CloudOff
                                    else -> Icons.Rounded.CloudQueue
                                },
                                contentDescription = null,
                                tint = when(syncState) {
                                    is com.example.services.CloudSyncManager.SyncState.Success -> Color(0xFF4CAF50)
                                    is com.example.services.CloudSyncManager.SyncState.Error -> Color.Red
                                    else -> Color.White
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(loc.appTitle, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        IconButton(onClick = { viewModel.toggleTheme() }) {
                            val icon = when(themeMode) { "dark" -> Icons.Rounded.DarkMode; "light" -> Icons.Rounded.LightMode; else -> Icons.Rounded.BrightnessAuto }
                            Icon(icon, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            },
            bottomBar = {
                if (!useNavigationRail) {
                    BottomNavigation(navController = navController)
                }
            },
            floatingActionButton = {
                if (!useNavigationRail) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AiCopilotFloatingButton(onClick = { Toast.makeText(context, "AI Copilot active on $currentRoute", Toast.LENGTH_SHORT).show() }, modifier = Modifier.padding(bottom = 80.dp))
                        FloatingActionButton(onClick = { isFabExpanded = !isFabExpanded }, shape = CircleShape) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                        }
                    }
                }
            },
            containerColor = lightGrayBg
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                SmartSchedulerNavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    onAddCourseClick = { courseToEdit = null; showAddCourseDialog = true },
                    onEditCourseClick = { courseToEdit = it; showAddCourseDialog = true },
                    modifier = Modifier.fillMaxSize()
                )

                androidx.compose.animation.AnimatedVisibility(visible = FocusModeManager.isFocusModeActive, modifier = Modifier.fillMaxSize().zIndex(100f)) {
                    FocusModeOverlay(currentLang, deepNavy, accentBlue)
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentLang = currentLang,
            themeMode = themeMode,
            dynamicColorEnabled = dynamicColorEnabled,
            selectedSound = selectedSound,
            onLangChange = { viewModel.setAppLanguage(it) },
            onThemeChange = { viewModel.setThemeMode(it) },
            onDynamicColorChange = { viewModel.setDynamicColorEnabled(it) },
            onSoundChange = { viewModel.setAlertSound(it) },
            onPlaySoundPreview = { viewModel.playAlertSoundPreview(context, it) },
            onStopSoundPreview = { viewModel.stopAlertSoundPreview() },
            onDismiss = { showSettingsDialog = false },
            viewModel = viewModel,
            onNavigateToSyncCenter = {
                showSettingsDialog = false
                navController.navigate(Screen.SyncCenter.route)
            }
        )
    }

    if (showAddCourseDialog) {
        AddEditCourseDialog(course = courseToEdit, existingCourses = courses, onDismiss = { showAddCourseDialog = false }, onConfirm = { name, days, startTime, endTime, zoom, target, active, lead, color, cat ->
            if (courseToEdit == null) viewModel.addCourse(name, days, startTime, endTime, zoom, target, active, lead, color, cat)
            else viewModel.updateCourse(courseToEdit!!.copy(name=name, days=mapIndicesToArabicDays(days), timeStart=startTime, timeEnd=endTime, zoomAccount=zoom, status=if(active)"نشط" else "غير نشط", targetCount=target, reminderLeadMinutes=lead, colorHex=color, category=cat))
            showAddCourseDialog = false
        })
    }
}

@Composable
fun FocusModeOverlay(currentLang: String, deepNavy: Color, accentBlue: Color) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Text("${FocusModeManager.timeLeftSeconds / 60}:${String.format("%02d", FocusModeManager.timeLeftSeconds % 60)}", fontSize = 60.sp, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { FocusModeManager.isTimerRunning = !FocusModeManager.isTimerRunning }) { Text(if (FocusModeManager.isTimerRunning) "Pause" else "Start") }
                Button(onClick = { FocusModeManager.isFocusModeActive = false }) { Text("Exit") }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    mainViewModel: MainViewModel,
    onAddCourseClick: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToStudyHub: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel()
) {
    val state by homeViewModel.dashboardState.collectAsStateWithLifecycle()
    val upcomingSessions by mainViewModel.upcomingLecturesAlerts.collectAsStateWithLifecycle()
    val totalMinutes by mainViewModel.totalStudyMinutes.collectAsStateWithLifecycle()
    val gpa by mainViewModel.currentGpa.collectAsStateWithLifecycle()
    val currentLang = LocalAppLanguage.current
    val aiRecommendations by mainViewModel.aiRecommendations.collectAsStateWithLifecycle()
    val coachFeedback by mainViewModel.studyCoachFeedback.collectAsStateWithLifecycle()
    val isAiUpdating by mainViewModel.isAiUpdating.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isCustomizerOpen by remember { mutableStateOf(false) }
    var widgetList by remember { mutableStateOf(listOf("Morning Brief", "Quick Actions", "AI Copilot & Avatar", "Analytics Hub", "Achievements & Streaks", "Study Heatmap", "Schedule Timeline", "AI Study Insights")) }
    var visibleWidgets by remember { mutableStateOf(widgetList.associateWith { true }) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search...") })
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            widgetList.forEach { name ->
                if (visibleWidgets[name] == true) {
                    item {
                        when (name) {
                            "Morning Brief" -> MorningBrief(state, currentLang)
                            "Quick Actions" -> QuickActions(onAddCourseClick, onNavigateToSchedule, onNavigateToCalculator, onNavigateToReminders, onNavigateToStudyHub, currentLang)
                            "AI Copilot & Avatar" -> coachFeedback?.let { AiCoachFeedbackCard(it) }
                            "Analytics Hub" -> AnalyticsWidget(minutes = totalMinutes, gpa = gpa, onNavigate = onNavigateToAnalytics)
                            "Achievements & Streaks" -> AchievementsWidget(currentLang)
                            "Study Heatmap" -> StudyHeatmap(currentLang)
                            "Schedule Timeline" -> ScheduleTimeline(upcomingSessions, currentLang)
                            "AI Study Insights" -> AiRecommendationSection(recommendations = aiRecommendations, isLoading = isAiUpdating, onActionClick = { 
                                if (it == "OPTIMIZE_SCHEDULE") mainViewModel.runScheduleOptimization()
                                else if (it == "START_STUDY") FocusModeManager.isFocusModeActive = true
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MorningBrief(state: DashboardState, currentLanguage: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Morning Brief", fontWeight = FontWeight.Bold)
            Text("Lectures: ${state.lectureCount}, Tasks: ${state.assignmentCount}")
        }
    }
}

@Composable
fun QuickActions(onAddCourseClick: () -> Unit, onNavigateToSchedule: () -> Unit, onNavigateToCalculator: () -> Unit, onNavigateToReminders: () -> Unit, onNavigateToStudyHub: () -> Unit, currentLang: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onNavigateToSchedule) { Icon(Icons.Rounded.CalendarToday, null) }
        IconButton(onClick = onNavigateToCalculator) { Icon(Icons.Rounded.Calculate, null) }
        IconButton(onClick = onNavigateToStudyHub) { Icon(Icons.Rounded.AutoStories, null) }
    }
}

@Composable
fun AnalyticsWidget(minutes: Long, gpa: Double, onNavigate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onNavigate
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ملخص التحليلات", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("ساعات الدراسة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${minutes / 60}h ${minutes % 60}m", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("المعدل الحالي", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.2f", gpa), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AchievementsWidget(currentLang: String) { Card(Modifier.fillMaxWidth()) { Text("🏆 Streak: 5 days", Modifier.padding(16.dp)) } }
@Composable
fun StudyHeatmap(currentLang: String) { Card(Modifier.fillMaxWidth()) { Text("📊 Study Heatmap", Modifier.padding(16.dp)) } }
@Composable
fun ScheduleTimeline(sessions: List<SessionInfo>, currentLang: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("📅 Timeline", fontWeight = FontWeight.Bold)
            sessions.take(2).forEach { Text("• ${it.courseName}") }
        }
    }
}

data class DashboardState(val lectureCount: Int = 3, val assignmentCount: Int = 2)
class HomeViewModel : androidx.lifecycle.ViewModel() {
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState = _dashboardState.asStateFlow()
}
