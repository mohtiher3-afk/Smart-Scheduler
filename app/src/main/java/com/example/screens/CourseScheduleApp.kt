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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
    val selectedSound by viewModel.alertSound.collectAsStateWithLifecycle()

    LaunchedEffect(upcomingLecturesAlerts) {
        if (upcomingLecturesAlerts.isNotEmpty()) {
            viewModel.scheduleAlarmsForNextLectures(context)
            viewModel.checkAndSchedule15MinPreMeetingAlerts(context)
        }
    }

    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val pinLockEnabled by viewModel.pinLockEnabled.collectAsStateWithLifecycle()
    val userAuthenticated by viewModel.userAuthenticated.collectAsStateWithLifecycle()
    val registeredPin by viewModel.registeredPin.collectAsStateWithLifecycle()

    LaunchedEffect(showSplash) {
        if (showSplash) {
            kotlinx.coroutines.delay(2200)
            viewModel.dismissSplash()
        }
    }

    if (showSplash) {
        SplashScreenView(currentLang)
    } else if (!onboardingCompleted) {
        OnboardingScreenView(currentLang, onGetStarted = { viewModel.completeOnboarding() })
    } else if (pinLockEnabled && !userAuthenticated) {
        PinLockScreenView(
            currentLang = currentLang,
            correctPin = registeredPin,
            onSuccess = { viewModel.authenticateUser(registeredPin) }
        )
    } else {
        com.example.ui.features.home.HomeScreen(
            viewModel = viewModel,
            modifier = modifier
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = loc.settingsTitle,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (currentLang == "ar") "تخصيص النظام والمزامنة والتشغيل" else "System configuration & preferences",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // CARD 1: Appearance & Language Settings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (currentLang == "ar") "اللغة والمظهر" else "Language & Theme",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Select Language
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = loc.selectLanguage,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("ar" to "العربية", "en" to "English").forEach { (code, name) ->
                                    val isSelected = currentLang == code
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onLangChange(code) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Select Theme
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = loc.selectTheme,
                                style = MaterialTheme.typography.bodySmall,
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
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onThemeChange(mode) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Dynamic Color Switch
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = loc.enableDynamicColor,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = loc.dynamicColorDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                                Switch(
                                    checked = dynamicColorEnabled,
                                    onCheckedChange = onDynamicColorChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }

                // CARD 2: Sound Alert Settings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (currentLang == "ar") "صوت جرس التنبيهات 🔔" else "Notification Alert Sound 🔔",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        var playingPreview by remember { mutableStateOf<String?>(null) }
                        var dropdownExpanded by remember { mutableStateOf(false) }

                        val soundOptions = remember(currentLang) {
                            listOf(
                                "default" to (if (currentLang == "ar") "النظام الافتراضي 📱" else "System Default 📱"),
                                "digital_beep" to (if (currentLang == "ar") "رنين رقمي ثنائي 📟" else "Digital Beep 📟"),
                                "soft_chime" to (if (currentLang == "ar") "جرس هادئ مزدوج 🔔" else "Soft Chime 🔔"),
                                "classic_bell" to (if (currentLang == "ar") "نغمة كلاسيكية 🎼" else "Classic Bell 🎼"),
                                "tech_alert" to (if (currentLang == "ar") "تنبيه تقني متصاعد ⚡" else "Tech Alert ⚡")
                            )
                        }

                        val selectedLabel = soundOptions.find { it.first == selectedSound }?.second ?: selectedSound

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentLang == "ar") "اختر نغمة التنبيه المفضلة:" else "Select alert sound preference:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Elegant Dropdown Selector Component
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.TopStart)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { dropdownExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("sound_selector_dropdown"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MusicNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = selectedLabel,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Preview button for the currently selected sound
                                        IconButton(
                                            onClick = {
                                                if (playingPreview == selectedSound) {
                                                    onStopSoundPreview()
                                                    playingPreview = null
                                                } else {
                                                    onPlaySoundPreview(selectedSound)
                                                    playingPreview = selectedSound
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (playingPreview == selectedSound) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                                contentDescription = "Preview sound",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = if (dropdownExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = "Expand sound selector",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    soundOptions.forEach { (soundKey, soundLabel) ->
                                        val isCurrent = selectedSound == soundKey
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    if (isCurrent) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Check,
                                                            contentDescription = "Selected",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.size(16.dp))
                                                    }
                                                    Text(
                                                        text = soundLabel,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            },
                                            trailingIcon = {
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
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (playingPreview == soundKey) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                                        contentDescription = "Preview sound option",
                                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                onSoundChange(soundKey)
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // CARD 3: System Calendar Integration
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (currentLang == "ar") "التكامل مع تقويم أندرويد 📅" else "System Calendar Sync 📅",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = if (currentLang == "ar") {
                                "مزامنة مواعيد دوراتك التدريبية النشطة مباشرة مع تقويم أندرويد لتلقي تنبيهات منسقة على هاتفك وساعتك الذكية."
                            } else {
                                "Synchronize your active course schedules directly with Android Calendar to receive alerts on your smartwatch."
                            },
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (currentLang == "ar") "مزامنة المواعيد مع التقويم" else "Sync to Calendar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // CARD 4: Cloud Sync & Backup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (currentLang == "ar") "النسخ الاحتياطي السحابي ☁️" else "Cloud Sync & Backup ☁️",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

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
                                fontWeight = FontWeight.Black,
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
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = state.message,
                                        fontSize = 10.sp,
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            is com.example.services.CloudSyncManager.SyncState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = state.error,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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
                            enabled = syncState !is com.example.services.CloudSyncManager.SyncState.Syncing,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (currentLang == "ar") "بدء النسخ الاحتياطي الفوري" else "Start Quick Backup",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // CARD 5: Remote Config & Bulletins
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (currentLang == "ar") "إعلانات وتحديثات المطور عن بُعد 🌐" else "Remote Bulletins & Announcements 🌐",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = remoteConfig.announcement,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (currentLang == "ar") remoteConfig.motdArabic else remoteConfig.motdEnglish,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLang == "ar") "الدعم الفني والاتصال:" else "Support & Contact:",
                                fontSize = 10.sp,
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
                            enabled = !isFetchingConfig,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(
                                    text = if (isFetchingConfig) {
                                        if (currentLang == "ar") "جاري التحديث..." else "Refreshing..."
                                    } else {
                                        if (currentLang == "ar") "تحديث إعلانات النظام مباشرة" else "Refresh Live Config"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // CARD 6: Diagnostics & Observability Console
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Terminal,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (currentLang == "ar") "لوحة تشخيص وتتبع الأداء 🛠️" else "Diagnostics Console 🛠️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { showLogsConsole = !showLogsConsole },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (showLogsConsole) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = "Toggle logs",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (showLogsConsole) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)), // Deep terminal black
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "CONSOLE STDOUT / EVENT TRACE",
                                            color = Color(0xFF757575),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                        TextButton(
                                            onClick = { com.example.services.DiagnosticLogger.clearLogs() },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text(
                                                text = "CLEAR",
                                                color = Color(0xFFEF4444),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
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
                                                text = "[OK] All system engines nominal. Trace is clean.",
                                                color = Color(0xFF10B981),
                                                fontSize = 9.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
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
                                                    fontSize = 8.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // CARD 7: Security Lock Settings (PIN Lock)
                val pinLockEnabled by viewModel.pinLockEnabled.collectAsStateWithLifecycle()
                val registeredPin by viewModel.registeredPin.collectAsStateWithLifecycle()
                var showChangePinDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (currentLang == "ar") "أمن وحماية التطبيق 🔐" else "App Security & Lock 🔐",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = if (currentLang == "ar") {
                                "قم بتأمين جدول دوراتك الأكاديمية وأجهزتك عن طريق تمكين قفل رمز PIN السري."
                            } else {
                                "Secure your academic timetable schedules and notes by enabling a security PIN lock."
                            },
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (currentLang == "ar") "تفعيل قفل رمز PIN" else "Enable PIN Lock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = pinLockEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        viewModel.enablePinLock("1234") // default pin
                                        Toast.makeText(context, if (currentLang == "ar") "تم تفعيل القفل بنجاح! الرمز الافتراضي: 1234" else "Lock enabled! Default PIN: 1234", Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.disablePinLock()
                                        Toast.makeText(context, if (currentLang == "ar") "تم تعطيل قفل رمز PIN بنجاح." else "PIN lock disabled successfully.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        if (pinLockEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (currentLang == "ar") "رمز القفل الحالي: $registeredPin" else "Current PIN: $registeredPin",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(
                                    onClick = { showChangePinDialog = true }
                                ) {
                                    Text(
                                        text = if (currentLang == "ar") "تغيير الرمز" else "Change PIN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (showChangePinDialog) {
                    var newPinInput by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showChangePinDialog = false },
                        title = { Text(if (currentLang == "ar") "تغيير رمز PIN" else "Change PIN") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = if (currentLang == "ar") "أدخل رمز PIN جديد يتكون من 4 أرقام:" else "Enter a new 4-digit security PIN:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = newPinInput,
                                    onValueChange = {
                                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                            newPinInput = it
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newPinInput.length == 4) {
                                        viewModel.enablePinLock(newPinInput)
                                        showChangePinDialog = false
                                        Toast.makeText(context, if (currentLang == "ar") "تم تغيير الرمز بنجاح!" else "PIN changed successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, if (currentLang == "ar") "الرجاء إدخال 4 أرقام بالضبط." else "Please enter exactly 4 digits.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(if (currentLang == "ar") "حفظ" else "Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showChangePinDialog = false }) {
                                Text(if (currentLang == "ar") "إلغاء" else "Cancel")
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStopSoundPreview()
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = loc.close,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
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

@Composable
fun SplashScreenView(currentLang: String) {
    val deepNavy = MaterialTheme.colorScheme.primary
    val accentBlue = MaterialTheme.colorScheme.secondary
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(deepNavy, accentBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
            
            Text(
                text = if (currentLang == "ar") "المجدول الذكي" else "Smart Scheduler",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            
            Text(
                text = if (currentLang == "ar") "مخطط ومجدول أكاديمي مدعوم بالذكاء الاصطناعي" else "AI-Powered Academic Planner",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun OnboardingScreenView(
    currentLang: String,
    onGetStarted: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    
    val onboardingData = remember(currentLang) {
        listOf(
            Triple(
                Icons.Rounded.AutoAwesome,
                if (currentLang == "ar") "جدولة وتخطيط ذكي" else "Smart AI Scheduling",
                if (currentLang == "ar") {
                    "قم بإنشاء وتوزيع محاضراتك ودوراتك تلقائيًا وبلمسة واحدة باستخدام مساعدنا الذكي."
                } else {
                    "Automatically schedule and distribute training courses and lectures using our AI assistant."
                }
            ),
            Triple(
                Icons.Rounded.Timeline,
                if (currentLang == "ar") "إحصائيات متقدمة ومتابعة" else "Advanced Insights & Analytics",
                if (currentLang == "ar") {
                    "تتبع تقدم محاضراتك ومستوى إنجازك الأكاديمي أولاً بأول من خلال رسوم بيانية تفاعلية متكاملة."
                } else {
                    "Track your course completion rate, remaining hours, and attendance consistency via interactive metrics."
                }
            ),
            Triple(
                Icons.Rounded.Timer,
                if (currentLang == "ar") "مؤقت بومودورو وعادات يومية" else "Focus & Daily Consistency",
                if (currentLang == "ar") {
                    "عزز تركيزك الأكاديمي بمؤقت بومودورو للدراسة العميقة، وتابع عاداتك اليومية وقائمة مهامك بكل سهولة."
                } else {
                    "Boost study focus with the integrated Pomodoro timer, maintain consistent habits, and complete course subtasks."
                }
            )
        )
    }
    
    val currentData = onboardingData[step]
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                onboardingData.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (step == index) 20.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (step == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(0.4f))
            
            // Visual Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentData.first,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(96.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Text Details
            Text(
                text = currentData.second,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = currentData.third,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.weight(0.6f))
            
            // Bottom Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onGetStarted
                ) {
                    Text(
                        text = if (currentLang == "ar") "تخطي" else "Skip",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Button(
                    onClick = {
                        if (step < onboardingData.size - 1) {
                            step++
                        } else {
                            onGetStarted()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (step == onboardingData.size - 1) {
                                if (currentLang == "ar") "ابدأ الآن" else "Get Started"
                            } else {
                                if (currentLang == "ar") "التالي" else "Next"
                            },
                            fontWeight = FontWeight.Black
                        )
                        Icon(
                            imageVector = if (currentLang == "ar") Icons.Rounded.ArrowBack else Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PinLockScreenView(
    currentLang: String,
    correctPin: String,
    onSuccess: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = if (currentLang == "ar") "أدخل رمز PIN لفتح التطبيق" else "Enter security PIN to unlock",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // Bullet indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0..3).forEach { index ->
                    val isFilled = index < pin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.5.dp,
                                if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                CircleShape
                            )
                    )
                }
            }
            
            if (errorMsg.isNotEmpty()) {
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Numeric Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "🔓")
                ).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { char ->
                            val isSpecial = char == "C" || char == "🔓"
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSpecial) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        when (char) {
                                            "C" -> {
                                                if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                errorMsg = ""
                                            }
                                            "🔓" -> {
                                                if (pin == correctPin) {
                                                    onSuccess()
                                                } else {
                                                    errorMsg = if (currentLang == "ar") "رمز المرور غير صحيح! حاول مجددًا." else "Incorrect PIN! Please try again."
                                                    pin = ""
                                                }
                                            }
                                            else -> {
                                                if (pin.length < 4) {
                                                    pin += char
                                                    errorMsg = ""
                                                    if (pin.length == 4) {
                                                        if (pin == correctPin) {
                                                            onSuccess()
                                                        } else {
                                                            errorMsg = if (currentLang == "ar") "رمز المرور غير صحيح! حاول مجددًا." else "Incorrect PIN! Please try again."
                                                            pin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSpecial) MaterialTheme.colorScheme.onSecondaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
