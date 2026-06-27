package com.example.screens.tabs

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.models.Course
import com.example.models.SessionInfo
import com.example.services.SchedulerUtils
import com.example.screens.LocalAppLanguage
import com.example.screens.Loc
import com.example.widgets.CourseCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FilterChipM3(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "filter_chip_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
            )
        }
    }
}

private fun getCourseStatus(course: Course, currentLang: String): String {
    val completed = course.getCompletedLecturesSet().size
    val total = course.targetCount
    return if (currentLang == "en") {
        when {
            completed >= total && total > 0 -> "Completed"
            completed > 0 -> "Ongoing"
            else -> "Upcoming"
        }
    } else {
        when {
            completed >= total && total > 0 -> "مكتملة"
            completed > 0 -> "جارية"
            else -> "قادمة"
        }
    }
}

private fun getCourseSpecialty(name: String, currentLang: String): String {
    val upper = name.uppercase()
    val isFinance = upper.contains("CMA") || upper.contains("CPA") || upper.contains("SOCPA") || upper.contains("CIA") || 
            upper.contains("محاسب") || upper.contains("مالي") || upper.contains("ضريب") || upper.contains("زكاة") || 
            upper.contains("IFRS") || upper.contains("IAS") || upper.contains("تدقيق") || upper.contains("استثمار")
    val isMgmt = upper.contains("PMP") || upper.contains("MBA") || upper.contains("إدار") || upper.contains("مشروع") || 
            upper.contains("قياد") || upper.contains("بشرية") || upper.contains("تسويق") || upper.contains("عملاء") || 
            upper.contains("مبيعات")
    val isTech = upper.contains("برمج") || upper.contains("كود") || upper.contains("ويب") || upper.contains("تقني") || 
            upper.contains("شبك") || upper.contains("بيانات") || upper.contains("ذكاء") || upper.contains("حاسب") || 
            upper.contains("IT") || upper.contains("KOTLIN") || upper.contains("JAVA") || upper.contains("PYTHON")
    
    return if (currentLang == "en") {
        when {
            isFinance -> "Finance & Accounting"
            isMgmt -> "Management & Projects"
            isTech -> "Tech & Information"
            else -> "Other"
        }
    } else {
        when {
            isFinance -> "مالية ومحاسبة"
            isMgmt -> "إدارة ومشاريع"
            isTech -> "تقنية ومعلومات"
            else -> "أخرى"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTab(
    courses: List<Course>,
    upcomingLecturesAlerts: List<SessionInfo>,
    onCalculate: (Course) -> Unit,
    onEdit: (Course) -> Unit,
    onDelete: (Course) -> Unit,
    onTestAlarm: (Course) -> Unit,
    onTestUpcomingAlarm: (String, String) -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: Context,
    onCourseUpdated: (Course) -> Unit,
    onExportCSV: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val currentLang = LocalAppLanguage.current
    val loc = remember(currentLang) { Loc(currentLang) }

    val deepNavy = MaterialTheme.colorScheme.primary
    val accentBlue = MaterialTheme.colorScheme.secondary
    val activeGreen = MaterialTheme.colorScheme.tertiary

    val sharedPrefs = remember(context) {
        context.getSharedPreferences("CourseScheduleApp_Prefs", Context.MODE_PRIVATE)
    }
    var lastSyncTime by remember {
        mutableStateOf(
            sharedPrefs.getString("last_sync_time", null) ?: run {
                val now = java.text.SimpleDateFormat("yyyy/MM/dd hh:mm a", java.util.Locale(currentLang)).format(java.util.Date())
                sharedPrefs.edit().putString("last_sync_time", now).apply()
                now
            }
        )
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                onRefresh()
                delay(1200) // Delightful Material 3 synchronization animation delay
                val now = java.text.SimpleDateFormat("yyyy/MM/dd hh:mm a", java.util.Locale(currentLang)).format(java.util.Date())
                sharedPrefs.edit().putString("last_sync_time", now).apply()
                lastSyncTime = now
                isRefreshing = false
                val toastText = if (currentLang == "ar") "تمت إعادة مزامنة مواعيد المحاضرات والتنبيهات المجدولة بنجاح! 🔄" else "Lectures and alerts synchronized successfully! 🔄"
                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (courses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_study_banner),
                        contentDescription = "Welcome illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (currentLang == "ar") "ابدأ رحلة الدراسة الذكية" else "Start Smart Learning Journey",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (currentLang == "ar") {
                            "أضف دورتك التدريبية الأولى يدوياً للبدء في تنظيم وتنسيق جداول المحاضرات وحساب فتراتها وساعات الدراسة!"
                        } else {
                            "Add your first course manually to organize schedules, calculate periods, and track study hours!"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    } else {
        var searchQuery by remember { mutableStateOf("") }
        val allFilterVal = if (currentLang == "ar") "الكل" else "All"
        var selectedStatusFilter by remember { mutableStateOf(allFilterVal) }
        var selectedSpecialtyFilter by remember { mutableStateOf(allFilterVal) }

        val filteredCourses = remember(courses, searchQuery, selectedStatusFilter, selectedSpecialtyFilter, currentLang) {
            courses.filter { course ->
                val matchesSearch = course.name.contains(searchQuery, ignoreCase = true) || 
                                   course.zoomAccount.contains(searchQuery, ignoreCase = true) ||
                                   course.days.contains(searchQuery, ignoreCase = true)
                
                val matchesStatus = when (selectedStatusFilter) {
                    allFilterVal -> true
                    else -> getCourseStatus(course, currentLang) == selectedStatusFilter
                }
                
                val matchesSpecialty = when (selectedSpecialtyFilter) {
                    allFilterVal -> true
                    else -> getCourseSpecialty(course.name, currentLang) == selectedSpecialtyFilter
                }
                
                matchesSearch && matchesStatus && matchesSpecialty
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // High-visibility, prominent sticky Search and Filter Controls Panel at the top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("search_filter_control_panel"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header of Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == "ar") "البحث وتصنيف الدورات التدريبية" else "Search & Filter Course List",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Input field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_field"),
                        placeholder = {
                            Text(
                                if (currentLang == "ar") "ابحث باسم الدورة، حساب زوم أو أيام الحضور..." else "Search by name, zoom, days...",
                                fontSize = 11.sp
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = if (currentLang == "ar") "مسح البحث" else "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter chips: 1. Status Filter (مكتملة، جارية، قادمة)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == "ar") "تصنيف حسب حالة الدورة:" else "Classify by Course Status:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val statuses = if (currentLang == "ar") {
                            listOf(
                                "الكل" to "الكل 🌐",
                                "جارية" to "جارية (نشطة) ⏱️",
                                "مكتملة" to "مكتملة (منتهية) ✅",
                                "قادمة" to "قادمة (بدءاً قريباً) 🗓️"
                            )
                        } else {
                            listOf(
                                "All" to "All 🌐",
                                "Ongoing" to "Ongoing (Active) ⏱️",
                                "Completed" to "Completed ✅",
                                "Upcoming" to "Upcoming 🗓️"
                            )
                        }
                        items(statuses) { (value, label) ->
                            FilterChipM3(
                                text = label,
                                isSelected = selectedStatusFilter == value,
                                onClick = { selectedStatusFilter = value }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter chips: 2. Specialty/Category Filter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == "ar") "تصنيف حسب التخصص:" else "Filter by Academic Area:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val specialties = if (currentLang == "ar") {
                            listOf(
                                "الكل" to "الكل 🌐",
                                "مالية ومحاسبة" to "محاسبة ومالية 📊",
                                "إدارة ومشاريع" to "إدارة ومشاريع 💼",
                                "تقنية ومعلومات" to "تقنية ومعلومات 💻",
                                "أخرى" to "تخصصات أخرى 📌"
                            )
                        } else {
                            listOf(
                                "All" to "All 🌐",
                                "Finance & Accounting" to "Finance & Accounting 📊",
                                "Management & Projects" to "Management & Projects 💼",
                                "Tech & Information" to "Tech & Info 💻",
                                "Other" to "Other Areas 📌"
                            )
                        }
                        items(specialties) { (value, label) ->
                            FilterChipM3(
                                text = label,
                                isSelected = selectedSpecialtyFilter == value,
                                onClick = { selectedSpecialtyFilter = value }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp)
                            .testTag("sync_indicator_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.22f)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        )
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CalendarToday,
                                            contentDescription = "آخر مزامنة",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (currentLang == "ar") "مزامنة التقويم والتنبيهات المجدولة" else "Calendar & Scheduled Alerts Sync",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (currentLang == "ar") "تم ربط الجدولة ومواعيد البث المباشر بنجاح" else "Schedules and live broadcast times linked successfully",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                                
                                // Sync Badge
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondary)
                                        )
                                        Text(
                                            text = (if (currentLang == "ar") "مزامنة" else "Sync") + ": $lastSyncTime",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Divider(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                            text = if (currentLang == "ar") "هل ترغب في حفظ نسخة احتياطية من بياناتك؟" else "Would you like to export a backup of your data?",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )

                                TextButton(
                                    onClick = { onExportCSV() },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("export_csv_text_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = loc.exportCSV,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.img_study_banner),
                                contentDescription = "الدراسة المنظمة",
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (currentLang == "ar") "أهلاً بك في منظمك الدراسي والجامعي" else "Welcome to your Course Organizer",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentLang == "ar") {
                                        "تابع مواعيد ومحاضرات البث المباشر أولاً بأول، وفعل تنبيهات تذكير زووم المباشرة تلقائياً قبل البث بـ 15 أو 30 دقيقة."
                                    } else {
                                        "Follow your live broadcast schedules up-to-date, and automatically enable Zoom reminder notifications 15 or 30 minutes before broadcast."
                                    },
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

            if (upcomingLecturesAlerts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upcoming_alerts_panel"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (currentLang == "ar") "تنبيهات المحاضرات القادمة" else "Upcoming Lecture Alerts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    val leadMins = upcomingLecturesAlerts.firstOrNull()?.reminderLeadMinutes ?: 15
                                    Text(
                                        text = if (currentLang == "ar") "تنبيه تلقائي قبلها بـ $leadMins دقيقة" else "Auto-alerts $leadMins mins before",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            upcomingLecturesAlerts.take(3).forEach { alert ->
                                val isToday = alert.dateString == SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = alert.courseName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            val dayTranslated = if (currentLang == "en") {
                                                when (alert.dayName) {
                                                    "الأحد", "الاحد" -> "Sunday"
                                                    "الاثنين" -> "Monday"
                                                    "الثلاثاء" -> "Tuesday"
                                                    "الأربعاء", "الاربعاء" -> "Wednesday"
                                                    "الخميس" -> "Thursday"
                                                    "الجمعة" -> "Friday"
                                                    "السبت" -> "Saturday"
                                                    else -> alert.dayName
                                                }
                                            } else {
                                                alert.dayName
                                            }
                                            Text(
                                                text = if (currentLang == "ar") "الموعد: $dayTranslated، ${alert.formattedDate} ساعة ${alert.timeStart}" else "Time: $dayTranslated, ${alert.formattedDate} at ${alert.timeStart}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (isToday) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.errorContainer)
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = if (currentLang == "ar") "اليوم 🚨" else "Today 🚨",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = if (currentLang == "ar") "نشط ⏱️" else "Active ⏱️",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                }
                                            }
                                            
                                            IconButton(
                                                onClick = {
                                                    onTestUpcomingAlarm(alert.courseName, alert.zoomAccount)
                                                },
                                                modifier = Modifier.size(28.dp),
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Icon(Icons.Rounded.Notifications, contentDescription = if (currentLang == "ar") "فحص التنبيه" else "Test Alert", modifier = Modifier.size(14.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    val sessionLabel = if (currentLang == "ar") "موعد مجدول" else "Scheduled Session"
                                                    SchedulerUtils.exportSingleToIcsFile(
                                                        context = context,
                                                        courseName = alert.courseName,
                                                        lectureNumStr = sessionLabel,
                                                        dateStr = alert.dateString,
                                                        timeStart = alert.timeStart,
                                                        timeEnd = "",
                                                        zoomAccount = alert.zoomAccount
                                                    )
                                                },
                                                modifier = Modifier.size(28.dp),
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                                    contentColor = MaterialTheme.colorScheme.secondary
                                                )
                                            ) {
                                                Icon(Icons.Rounded.CalendarToday, contentDescription = if (currentLang == "ar") "إضافة للتقويم (.ics)" else "Add to calendar (.ics)", modifier = Modifier.size(14.dp))
                                            }
                                            
                                            if (alert.zoomAccount.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        try {
                                                            val webIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(
                                                                if (!alert.zoomAccount.startsWith("http")) "https://" + alert.zoomAccount else alert.zoomAccount
                                                            ))
                                                            context.startActivity(webIntent)
                                                        } catch(e: Exception) {
                                                            val invalidLinkText = if (currentLang == "ar") "الرابط غير صالح للفتح المباشر" else "Invalid zoom/account URL"
                                                            Toast.makeText(context, invalidLinkText, Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp),
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                                        contentColor = MaterialTheme.colorScheme.secondary
                                                    )
                                                ) {
                                                    Icon(Icons.Rounded.Link, contentDescription = if (currentLang == "ar") "رابط زووم" else "Zoom link", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = accentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == "ar") {
                                "انقر على رمز الجرس بجانب حساب Zoom لاختبار تنبيه فوري تجريبي للمحاضرة خلال 5 ثوانٍ!"
                            } else {
                                "Click the bell icon next to the Zoom link to fire a 5-second instant demo alarm test!"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (filteredCourses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (currentLang == "ar") "لم يتم تحديد أو العثور على أي دورات" else "No courses found matching",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (currentLang == "ar") "لا توجد نتائج تطابق شروط البحث أو التصفية الحالية." else "No search results match your active query.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedStatusFilter = allFilterVal
                                    selectedSpecialtyFilter = allFilterVal
                                }
                            ) {
                                Text(if (currentLang == "ar") "إعادة تعيين وبدء بحث جديد" else "Reset search filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredCourses) { course ->
                    val isCourseActive = course.status == "نشط"
                    CourseCard(
                        course = course,
                        isCourseActive = isCourseActive,
                        onCalculate = { onCalculate(course) },
                        onEdit = { onEdit(course) },
                        onDelete = { onDelete(course) },
                        onTestAlarm = { onTestAlarm(course) },
                        clipboardManager = clipboardManager,
                        context = context,
                        onCourseUpdated = onCourseUpdated
                    )
                }
            }
        }
    }
}
}
}
