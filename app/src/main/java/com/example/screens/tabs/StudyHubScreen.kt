package com.example.screens.tabs

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.models.Course
import com.example.screens.Loc
import com.example.screens.LocalAppLanguage
import com.example.screens.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = LocalAppLanguage.current
    val loc = remember(currentLang) { Loc(currentLang) }

    val courses by viewModel.allCourses.collectAsStateWithLifecycle()
    val activeCourse by viewModel.activeStudyCourse.collectAsStateWithLifecycle()
    val isSessionActive by viewModel.isStudyHubActive.collectAsStateWithLifecycle()
    val isImmersive by viewModel.isStudyHubImmersive.collectAsStateWithLifecycle()
    val stats by viewModel.studySessionStats.collectAsStateWithLifecycle()
    val pomodoroTime by viewModel.pomodoroRemainingTime.collectAsStateWithLifecycle()
    val pomodoroTotal by viewModel.pomodoroTotalTime.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.pomodoroIsRunning.collectAsStateWithLifecycle()
    val isFocusMode by viewModel.pomodoroIsFocus.collectAsStateWithLifecycle()

    var selectedCourseForStart by remember { mutableStateOf<Course?>(null) }
    var showSessionSummary by remember { mutableStateOf(false) }

    // Auto-select first active course if none selected
    LaunchedEffect(courses) {
        if (selectedCourseForStart == null && courses.isNotEmpty()) {
            selectedCourseForStart = courses.find { it.status == "نشط" } ?: courses.first()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!isImmersive && !isSessionActive) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (currentLang == "ar") "بيئة الدراسة المتكاملة" else "Study Hub Ultimate",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isSessionActive) {
                // START SCREEN
                StudyHubStartScreen(
                    currentLang = currentLang,
                    courses = courses,
                    selectedCourse = selectedCourseForStart,
                    onCourseSelected = { selectedCourseForStart = it },
                    onStartSession = {
                        selectedCourseForStart?.let { viewModel.startStudyHubSession(it) }
                    }
                )
            } else {
                // IMMERSIVE STUDY ENVIRONMENT
                activeCourse?.let { course ->
                    StudyEnvironment(
                        currentLang = currentLang,
                        course = course,
                        isImmersive = isImmersive,
                        stats = stats,
                        pomodoroTime = pomodoroTime,
                        pomodoroTotal = pomodoroTotal,
                        isTimerRunning = isTimerRunning,
                        isFocusMode = isFocusMode,
                        onToggleImmersive = { viewModel.toggleStudyHubImmersive() },
                        onToggleTimer = {
                            if (isTimerRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                        },
                        onEndSession = {
                            viewModel.endStudyHubSession()
                            showSessionSummary = true
                        },
                        onIncrementPage = { viewModel.incrementPagesRead() },
                        onUpdateNotes = { viewModel.updateStudyNotes(it) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showSessionSummary) {
        StudySessionSummaryDialog(
            currentLang = currentLang,
            stats = stats,
            onDismiss = { showSessionSummary = false }
        )
    }
}

@Composable
fun StudyHubStartScreen(
    currentLang: String,
    courses: List<Course>,
    selectedCourse: Course?,
    onCourseSelected: (Course) -> Unit,
    onStartSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.School,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (currentLang == "ar") "جاهز للانطلاق في رحلة دراسية؟" else "Ready to start studying?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (currentLang == "ar") "اختر المقرر وسنجهز لك البيئة المثالية للتركيز" else "Select a subject and we'll prepare the perfect environment",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (currentLang == "ar") "اختر المقرر الدراسي:" else "Select Course:",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        courses.forEach { course ->
            val isSelected = selectedCourse?.id == course.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onCourseSelected(course) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(course.colorHex)))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = course.name,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStartSession,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            enabled = selectedCourse != null
        ) {
            Icon(Icons.Rounded.FlashOn, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (currentLang == "ar") "ابدأ جلسة الدراسة الذكية" else "Start Smart Study Session",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun StudyEnvironment(
    currentLang: String,
    course: Course,
    isImmersive: Boolean,
    stats: MainViewModel.StudySessionStats,
    pomodoroTime: Int,
    pomodoroTotal: Int,
    isTimerRunning: Boolean,
    isFocusMode: Boolean,
    onToggleImmersive: () -> Unit,
    onToggleTimer: () -> Unit,
    onEndSession: () -> Unit,
    onIncrementPage: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    viewModel: MainViewModel
) {
    var activeTab by remember { mutableStateOf(0) }
    var notesText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(course.colorHex)))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isFocusMode) (if (currentLang == "ar") "وضع التركيز" else "Focus Mode") else (if (currentLang == "ar") "وقت الاستراحة" else "Break Time"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFocusMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .clickable { onToggleTimer() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Rounded.PauseCircleFilled else Icons.Rounded.PlayCircleFilled,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${pomodoroTime / 60}:${String.format("%02d", pomodoroTime % 60)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                Row {
                    IconButton(onClick = onToggleImmersive) {
                        Icon(
                            imageVector = if (isImmersive) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                            contentDescription = "Toggle Immersive"
                        )
                    }
                    IconButton(onClick = onEndSession) {
                        Icon(Icons.Rounded.Cancel, contentDescription = "End Session", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> PdfWorkspace(currentLang, onIncrementPage)
                1 -> AiTutorWorkspace(currentLang, course, viewModel)
                2 -> NotesWorkspace(currentLang, notesText, onUpdateNotes = {
                    notesText = it
                    onUpdateNotes(it)
                })
            }
        }

        NavigationBar(
            modifier = Modifier.height(72.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NavigationBarItem(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                icon = { Icon(Icons.Rounded.MenuBook, null) },
                label = { Text(if (currentLang == "ar") "المادة" else "Course") }
            )
            NavigationBarItem(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                icon = { Icon(Icons.Rounded.AutoAwesome, null) },
                label = { Text(if (currentLang == "ar") "المعلم" else "Tutor") }
            )
            NavigationBarItem(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                icon = { Icon(Icons.Rounded.HistoryEdu, null) },
                label = { Text(if (currentLang == "ar") "الملاحظات" else "Notes") }
            )
        }
    }
}

@Composable
fun PdfWorkspace(currentLang: String, onIncrementPage: () -> Unit) {
    var currentPage by remember { mutableStateOf(1) }
    val totalPages = 64

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (currentPage > 1) { currentPage--; onIncrementPage() } }) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, null)
                    }
                    Text(
                        text = "Page $currentPage / $totalPages",
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = { if (currentPage < totalPages) { currentPage++; onIncrementPage() } }) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null)
                    }
                }
                
                Row {
                    IconButton(onClick = { }) { Icon(Icons.Rounded.Search, null) }
                    IconButton(onClick = { }) { Icon(Icons.Rounded.Brush, null) }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp)
            ) {
                Column {
                    Text(
                        text = "Modern Computational Architecture",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    repeat(10) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .padding(vertical = 4.dp)
                                .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == "ar") "اسأل المعلم" else "Ask AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiTutorWorkspace(currentLang: String, course: Course, viewModel: MainViewModel) {
    val chatHistory by viewModel.aiChatHistory.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAiChatLoading.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.SmartToy, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (currentLang == "ar") "المعلم الذكي المساعد" else "Academic AI Copilot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatHistory) { (sender, msg) ->
                    val isUser = sender == "user"
                    ChatBubble(message = msg, isUser = isUser)
                }
                
                if (isLoading) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (currentLang == "ar") "اسأل المعلم عن شيء ما..." else "Ask the tutor something...") },
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        inputText = ""
                    }
                },
                modifier = Modifier.size(52.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun NotesWorkspace(currentLang: String, text: String, onUpdateNotes: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (currentLang == "ar") "مفكرة الدراسة الذكية" else "Smart Study Notes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = onUpdateNotes,
            modifier = Modifier.fillMaxSize(),
            placeholder = { Text(if (currentLang == "ar") "ابدأ بكتابة أفكارك العظيمة هنا..." else "Start writing your brilliant thoughts here...") },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun StudySessionSummaryDialog(
    currentLang: String,
    stats: MainViewModel.StudySessionStats,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆", fontSize = 52.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (currentLang == "ar") "رحلة دراسية مذهلة!" else "Amazing Study Journey!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "+${stats.xpEarned} XP Earned",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                SummaryStatItem(
                    icon = Icons.Rounded.Timer,
                    value = "${stats.durationSeconds / 60}m",
                    label = if (currentLang == "ar") "المدة" else "Duration"
                )
                SummaryStatItem(
                    icon = Icons.Rounded.MenuBook,
                    value = "${stats.pagesRead}",
                    label = if (currentLang == "ar") "صفحة" else "Pages"
                )
                SummaryStatItem(
                    icon = Icons.Rounded.HistoryEdu,
                    value = "${stats.notesTaken}",
                    label = if (currentLang == "ar") "ملاحظة" else "Notes"
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (currentLang == "ar") "حفظ وإنهاء الجلسة" else "Save & Finish Session")
                }
            }
        }
    }
}

@Composable
fun SummaryStatItem(icon: ImageVector, value: String, label: String) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = value, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
