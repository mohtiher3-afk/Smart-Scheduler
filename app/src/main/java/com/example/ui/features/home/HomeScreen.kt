package com.example.ui.features.home

import android.app.Application
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.models.Course
import com.example.screens.Loc
import com.example.screens.LocalAppLanguage
import com.example.screens.MainViewModel
import com.example.screens.SettingsDialog
import com.example.ui.components.SmartFAB
import com.example.ui.components.SmartTopBar
import com.example.ui.material3_foundation.Dimens
import com.example.ui.navigation.BottomNavigation
import com.example.ui.navigation.Screen
import com.example.ui.navigation.SmartSchedulerNavGraph
import com.example.widgets.AddEditCourseDialog
import com.example.widgets.CustomInAppToast

import com.example.ui.features.home.GreetingCard
import com.example.ui.features.home.ProgressCard
import com.example.ui.features.home.QuickActions
import com.example.ui.features.home.UpcomingTasks
import com.example.ui.features.home.AiSuggestions
import com.example.ui.features.home.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = LocalAppLanguage.current
    val loc = remember(currentLang) { Loc(currentLang) }

    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val courses by viewModel.allCourses.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsStateWithLifecycle()
    val selectedSound by viewModel.alertSound.collectAsStateWithLifecycle()

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }

    val deepNavy = MaterialTheme.colorScheme.primary
    val accentBlue = MaterialTheme.colorScheme.secondary
    val lightGrayBg = MaterialTheme.colorScheme.background

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
            BottomNavigation(navController = navController)
        },
        floatingActionButton = {
            if (currentRoute == Screen.Schedule.route) {
                SmartFAB(
                    onClick = {
                        courseToEdit = null
                        showAddCourseDialog = true
                    },
                    icon = Icons.Rounded.Add,
                    contentDescription = loc.addCourse,
                    label = loc.addCourse,
                    expanded = true,
                    testTag = "add_course_fab"
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
                    com.example.widgets.CustomInAppToast(
                        toastData = toast,
                        currentLang = currentLang,
                        onDismiss = { viewModel.dismissCustomToast() }
                    )
                }
            }

            SmartSchedulerNavGraph(
                navController = navController,
                viewModel = viewModel,
                onAddCourseClick = {
                    courseToEdit = null
                    showAddCourseDialog = true
                },
                onEditCourseClick = { course ->
                    courseToEdit = course
                    showAddCourseDialog = true
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showAddCourseDialog) {
        AddEditCourseDialog(
            course = courseToEdit,
            existingCourses = courses,
            onDismiss = { showAddCourseDialog = false },
            onConfirm = { name, days, startTime, endTime, zoomAccount, targetCountVal, isActive, reminderLeadMinutes, colorHex, category ->
                if (courseToEdit == null) {
                    viewModel.addCourse(name, days, startTime, endTime, zoomAccount, targetCountVal, isActive, reminderLeadMinutes, colorHex, category)
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
                            colorHex = colorHex,
                            category = category
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
        SettingsDialog(
            currentLang = currentLang,
            themeMode = themeMode,
            dynamicColorEnabled = dynamicColorEnabled,
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
fun DashboardScreen(
    mainViewModel: MainViewModel,
    onAddCourseClick: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToReminders: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel()
) {
    val state by homeViewModel.dashboardState.collectAsStateWithLifecycle()
    val upcomingSessions by mainViewModel.upcomingLecturesAlerts.collectAsStateWithLifecycle()
    val currentLang = LocalAppLanguage.current

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GreetingCard(
                userName = state.userName,
                currentLanguage = currentLang,
                testTag = "dashboard_greeting_card"
            )
        }

        item {
            ProgressCard(
                completedLectures = state.completedLecturesCount,
                totalLectures = state.totalLecturesCount,
                totalHours = state.totalHours,
                activeCourses = state.activeCoursesCount,
                progressPercentage = state.progressPercentage,
                currentLanguage = currentLang,
                testTag = "dashboard_progress_card"
            )
        }

        item {
            QuickActions(
                onAddCourseClick = onAddCourseClick,
                onNavigateToSchedule = onNavigateToSchedule,
                onNavigateToCalculator = onNavigateToCalculator,
                onNavigateToReminders = onNavigateToReminders,
                currentLanguage = currentLang,
                testTag = "dashboard_quick_actions"
            )
        }

        item {
            UpcomingTasks(
                upcomingSessions = upcomingSessions,
                currentLanguage = currentLang,
                testTag = "dashboard_upcoming_tasks"
            )
        }

        item {
            AiSuggestions(
                suggestions = state.aiSuggestionsList,
                isRefreshing = state.isLoading,
                onRefreshClick = { homeViewModel.refreshSuggestions() },
                currentLanguage = currentLang,
                testTag = "dashboard_ai_suggestions"
            )
        }
    }
}

