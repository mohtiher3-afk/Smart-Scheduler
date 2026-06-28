package com.example.screens.tabs

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.models.Course
import kotlin.math.roundToInt

private fun parseTimeToMinutes(timeStr: String): Int {
    try {
        val cleaned = timeStr.trim().lowercase()
        val isPm = cleaned.contains("م") || cleaned.contains("pm") || cleaned.contains("مساءً") || cleaned.contains("مساء")
        val timePart = cleaned.replace("[^0-9:]".toRegex(), "").trim()
        val parts = timePart.split(":")
        if (parts.size >= 2) {
            var h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            if (isPm && h < 12) h += 12
            if (!isPm && h == 12) h = 0
            return h * 60 + m
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardTab(
    courses: List<Course>,
    themeMode: String,
    dynamicColorEnabled: Boolean,
    onThemeChange: (String) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onCourseClick: (Course) -> Unit,
    onAddCourseClick: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val currentLang = remember { sharedPrefs.getString("app_language", "ar") ?: "ar" }

    if (courses.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_empty_state_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_study_empty),
                        contentDescription = "Welcome Illustration",
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (currentLang == "ar") "ابدأ رحلتك الأكاديمية الذكية 📚" else "Start Your Smart Academic Journey 📚",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (currentLang == "ar") {
                            "لا توجد دورات حالية لعرضها في لوحة البيانات. أضف محاضراتك ودوراتك التدريبية الآن لعرض رسوم الإنجاز التفاعلية والمقررات وتتبع تقدمك بدقة!"
                        } else {
                            "No active courses to display on your dashboard. Add your study schedules and lectures now to unlock beautiful interactive charts, progress tracking, and session alerts!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onAddCourseClick,
                        modifier = Modifier
                            .testTag("dashboard_empty_state_add_course_button")
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == "ar") "إضافة دورتك الأولى" else "Add Your First Course",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        return
    }

    // Calculations
    val totalCoursesCount = courses.size
    val completedCourses = courses.filter { it.targetCount > 0 && it.completedCount >= it.targetCount }
    val completedCoursesCount = completedCourses.size
    val remainingCoursesCount = totalCoursesCount - completedCoursesCount

    val totalLectures = courses.sumOf { it.targetCount }
    val completedLectures = courses.sumOf { it.completedCount }
    val remainingLectures = totalLectures - completedLectures

    val overallPercentage = if (totalLectures > 0) {
        (completedLectures.toFloat() / totalLectures * 100f)
    } else {
        0f
    }

    // Calculate Weekly Attendance Hours
    val totalWeeklyHours = courses.sumOf { course ->
        val startMin = parseTimeToMinutes(course.timeStart)
        val endMin = parseTimeToMinutes(course.timeEnd)
        var diffMin = endMin - startMin
        if (diffMin <= 0) diffMin = 120 // Fallback to 2 hours
        val durationHours = diffMin / 60.0
        val sessions = course.days.split("،", ",", " ", "-")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .size
            .coerceAtLeast(1)
        sessions * durationHours
    }

    val weeklyHoursFormatted = ((totalWeeklyHours * 10).roundToInt() / 10.0).toString()
    val monthlyCompletionPct = overallPercentage.roundToInt()

    val allLabel = if (currentLang == "ar") "الكل" else "All"
    var selectedCategoryFilter by remember { mutableStateOf(allLabel) }

    val availableCategories = remember(courses, currentLang) {
        val cats = courses.map { it.category.trim() }.filter { it.isNotEmpty() }.distinct()
        listOf(allLabel) + cats
    }

    val activeSelectedCategory = if (selectedCategoryFilter in availableCategories) {
        selectedCategoryFilter
    } else {
        allLabel
    }

    val filteredCoursesForProgress = remember(courses, activeSelectedCategory, currentLang) {
        if (activeSelectedCategory == allLabel) {
            courses
        } else {
            courses.filter { it.category.trim().equals(activeSelectedCategory.trim(), ignoreCase = true) }
        }
    }

    // Toggle between interactive custom Recharts & native view
    var useInteractiveRecharts by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Greeting Card
        item {
            val activeCoursesCount = courses.count { it.status == "نشط" }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentLang == "ar") "مرحباً بك مجدداً!" else "Welcome Back!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLang == "ar") {
                                "لديك $activeCoursesCount دورات نشطة لمتابعتها اليوم."
                            } else {
                                "You have $activeCoursesCount active courses to follow today."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Summary Dashboard Component
        item {
            val calendar = java.util.Calendar.getInstance()
            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            val todayArabicName = when (dayOfWeek) {
                java.util.Calendar.SUNDAY -> "الأحد"
                java.util.Calendar.MONDAY -> "الاثنين"
                java.util.Calendar.TUESDAY -> "الثلاثاء"
                java.util.Calendar.WEDNESDAY -> "الأربعاء"
                java.util.Calendar.THURSDAY -> "الخميس"
                java.util.Calendar.FRIDAY -> "الجمعة"
                java.util.Calendar.SATURDAY -> "السبت"
                else -> ""
            }
            val todayArabicAlt = when (dayOfWeek) {
                java.util.Calendar.SUNDAY -> "الاحد"
                java.util.Calendar.WEDNESDAY -> "الاربعاء"
                else -> todayArabicName
            }
            val activeCoursesToday = courses.filter { course ->
                course.status == "نشط" && (
                    course.days.contains(todayArabicName) || 
                    course.days.contains(todayArabicAlt) ||
                    (dayOfWeek == java.util.Calendar.SUNDAY && course.days.contains("Sunday", ignoreCase = true)) ||
                    (dayOfWeek == java.util.Calendar.MONDAY && course.days.contains("Monday", ignoreCase = true)) ||
                    (dayOfWeek == java.util.Calendar.TUESDAY && course.days.contains("Tuesday", ignoreCase = true)) ||
                    (dayOfWeek == java.util.Calendar.WEDNESDAY && course.days.contains("Wednesday", ignoreCase = true)) ||
                    (dayOfWeek == java.util.Calendar.THURSDAY && course.days.contains("Thursday", ignoreCase = true)) ||
                    (dayOfWeek == java.util.Calendar.FRIDAY && course.days.contains("Friday", ignoreCase = true)) ||
                    (dayOfWeek == java.util.Calendar.SATURDAY && course.days.contains("Saturday", ignoreCase = true))
                )
            }
            val upcomingSessionsTodayCount = activeCoursesToday.size

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Courses Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("summary_total_courses"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Book,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalCoursesCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentLang == "ar") "إجمالي المواد" else "Total Courses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Upcoming Sessions Today Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("summary_upcoming_sessions"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Today,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$upcomingSessionsTodayCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentLang == "ar") "محاضرات اليوم" else "Sessions Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Progress Percentage Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("summary_progress_percentage"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$monthlyCompletionPct%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentLang == "ar") "نسبة التقدم" else "Progress %",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Coordinated Theme Switching & Dynamic Color Control Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = if (currentLang == "ar") "المظهر ونظام الألوان الديناميكي" else "Theme & Dynamic Color",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Active indicator
                        val currentThemeText = when (themeMode) {
                            "dark" -> if (currentLang == "ar") "ليلي 🌙" else "Dark 🌙"
                            "light" -> if (currentLang == "ar") "نهاري ☀️" else "Light ☀️"
                            else -> if (currentLang == "ar") "تلقائي 📱" else "System 📱"
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = currentThemeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = if (currentLang == "ar") {
                            "قم بتهيئة مظهر لوحة التحكم يدويًا، أو فعّل وضع التبديل التلقائي ليتناغم تمامًا مع مظهر نظام تشغيل هاتفك الذكي."
                        } else {
                            "Customize your dashboard theme manually, or enable automatic switching to adapt seamlessly to your smartphone OS."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    // Theme selector buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val modes = listOf(
                            Triple("light", if (currentLang == "ar") "نهاري ☀️" else "Light ☀️", Icons.Rounded.LightMode),
                            Triple("dark", if (currentLang == "ar") "ليلي 🌙" else "Dark 🌙", Icons.Rounded.DarkMode),
                            Triple("system", if (currentLang == "ar") "تلقائي 📱" else "System 📱", Icons.Rounded.BrightnessAuto)
                        )
                        
                        modes.forEach { (modeKey, modeLabel, modeIcon) ->
                            val isSelected = themeMode == modeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { onThemeChange(modeKey) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = modeIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = modeLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Material 3 Dynamic Colors (Android 12+) toggle
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentLang == "ar") "ألوان ديناميكية (Material 3)" else "Dynamic Color (Material 3)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (currentLang == "ar") {
                                        "مستوحى من خلفية هاتفك لتجربة شخصية فريدة."
                                    } else {
                                        "Extracts color scheme from your phone wallpaper."
                                    },
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            }
        }

        // Mode selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Insights,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نوع المخطط البياني",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(if (useInteractiveRecharts) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { useInteractiveRecharts = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "تفاعلي (Recharts)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (useInteractiveRecharts) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(if (!useInteractiveRecharts) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { useInteractiveRecharts = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "سريع ومحلي",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!useInteractiveRecharts) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Summary Statistics row
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 3
            ) {
                // Metric 1: Completion
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "الإنجاز العام",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${overallPercentage.roundToInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Metric 2: Completed Courses
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "دورات مكتملة",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$completedCoursesCount/$totalCoursesCount",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // Metric 3: Total Lectures
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Book,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "المحاضرات",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$completedLectures/$totalLectures",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Metric 4: Average Weekly Attendance Hours
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (currentLang == "ar") "ساعات الحضور أسبوعياً" else "Weekly Attendance",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (currentLang == "ar") "$weeklyHoursFormatted س" else "$weeklyHoursFormatted hrs",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Metric 5: Monthly Course Completion Rate
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (currentLang == "ar") "معدل الإنجاز الشهري" else "Monthly Progress",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$monthlyCompletionPct%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }

        // Charts Section
        if (useInteractiveRecharts) {
            item {
                RechartsWebViewCard(
                    completedCoursesCount = completedCoursesCount,
                    remainingCoursesCount = remainingCoursesCount,
                    completedLectures = completedLectures,
                    remainingLectures = remainingLectures
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Local Chart 1
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "معدل إنجاز الدورات",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(100.dp)
                            ) {
                                BaseDoughnutChart(
                                    primary = completedCoursesCount,
                                    secondary = remainingCoursesCount,
                                    primaryColor = MaterialTheme.colorScheme.primary,
                                    secondaryColor = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val pct = if (totalCoursesCount > 0) (completedCoursesCount.toFloat() / totalCoursesCount * 100).roundToInt() else 0
                                    Text("$pct%", fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مكتملة: $completedCoursesCount", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("متبقية: $remainingCoursesCount", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Local Chart 2
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "إجمالي المحاضرات",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(100.dp)
                            ) {
                                BaseDoughnutChart(
                                    primary = completedLectures,
                                    secondary = remainingLectures,
                                    primaryColor = MaterialTheme.colorScheme.tertiary,
                                    secondaryColor = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val pct = if (totalLectures > 0) (completedLectures.toFloat() / totalLectures * 100).roundToInt() else 0
                                    Text("$pct%", fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("منجزة: $completedLectures", fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                Text("متبقية: $remainingLectures", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Leaderboard / Individual courses progress head
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (currentLang == "ar") "تقدّم الدورات الفردية" else "Individual Courses Progress",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Horizontal category filter chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(availableCategories) { cat ->
                        val isSelected = cat == activeSelectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        items(filteredCoursesForProgress, key = { it.id }) { course ->
            Box(
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
            ) {
                CoursePercentageProgressCard(course = course, onClick = { onCourseClick(course) })
            }
        }
    }
}

@Composable
fun RechartsWebViewCard(
    completedCoursesCount: Int,
    remainingCoursesCount: Int,
    completedLectures: Int,
    remainingLectures: Int,
    modifier: Modifier = Modifier
) {
    val cardBgHex = MaterialTheme.colorScheme.surface.toHex()
    val cardTextHex = MaterialTheme.colorScheme.onSurface.toHex()
    val cardBorderHex = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f).toHex()
    val primaryHex = MaterialTheme.colorScheme.primary.toHex()
    val secondaryHex = MaterialTheme.colorScheme.secondary.toHex()
    val tertiaryHex = MaterialTheme.colorScheme.tertiary.toHex()

    // We render the Recharts inside an Android WebView
    // HTML string built locally with React & Recharts CDN
    val htmlContent = """
        <!DOCTYPE html>
        <html lang="ar" dir="rtl">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    margin: 0;
                    padding: 4px;
                    font-family: system-ui, -apple-system, sans-serif;
                    background-color: transparent;
                    direction: rtl;
                }
                .container {
                    display: flex;
                    flex-direction: column;
                    gap: 12px;
                }
                .card {
                    background: $cardBgHex;
                    border-radius: 16px;
                    padding: 16px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.04);
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    border: 1px solid $cardBorderHex;
                    color: $cardTextHex;
                }
                .card-title {
                    font-size: 13px;
                    font-weight: 700;
                    color: $cardTextHex;
                    margin-bottom: 8px;
                    align-self: flex-start;
                }
                .chart-container {
                    width: 100%;
                    height: 180px;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                .stats {
                    display: flex;
                    justify-content: space-around;
                    width: 100%;
                    margin-top: 8px;
                    border-top: 1px solid $cardBorderHex;
                    padding-top: 8px;
                }
                .stat-item {
                    text-align: center;
                }
                .stat-value {
                    font-size: 16px;
                    font-weight: 800;
                }
                .stat-label {
                    font-size: 10px;
                    color: $cardTextHex;
                    opacity: 0.7;
                }
            </style>
            <!-- Loads React and Recharts UMD packages -->
            <script src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
            <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
            <script src="https://unpkg.com/recharts@2.10.3/umd/Recharts.js"></script>
        </head>
        <body>
            <div id="root"></div>

            <script>
                const { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } = window.Recharts;

                const courseData = [
                    { name: 'الدورات المكتملة', value: $completedCoursesCount },
                    { name: 'الدورات المتبقية', value: $remainingCoursesCount }
                ];

                const lectureData = [
                    { name: 'المحاضرات المنجزة', value: $completedLectures },
                    { name: 'المحاضرات المتبقية', value: $remainingLectures }
                ];

                function App() {
                    return React.createElement('div', { className: 'container' },
                        React.createElement('div', { className: 'card' },
                            React.createElement('div', { className: 'card-title' }, 'نسبة الدورات المنجزة مقابل الدورات المتبقية'),
                            React.createElement('div', { className: 'chart-container' },
                                React.createElement(ResponsiveContainer, { width: '100%', height: '100%' },
                                    React.createElement(PieChart, null,
                                        React.createElement(Pie, {
                                            data: courseData,
                                            cx: '50%',
                                            cy: '50%',
                                            innerRadius: 40,
                                            outerRadius: 60,
                                            paddingAngle: 5,
                                            dataKey: 'value'
                                        },
                                            React.createElement(Cell, { fill: '$primaryHex' }),
                                            React.createElement(Cell, { fill: '$cardBorderHex' })
                                        ),
                                        React.createElement(Tooltip, { 
                                            formatter: (value) => [value + ' دورة', 'العدد'],
                                            contentStyle: { direction: 'rtl', textAlign: 'right', fontSize: '11px', backgroundColor: '$cardBgHex', color: '$cardTextHex', border: '1px solid $cardBorderHex', borderRadius: '8px' }
                                        }),
                                        React.createElement(Legend, { verticalAlign: 'bottom', height: 24, iconSize: 10, style: { fontSize: '10px' } })
                                    )
                                )
                            ),
                            React.createElement('div', { className: 'stats' },
                                React.createElement('div', { className: 'stat-item' },
                                    React.createElement('div', { className: 'stat-value', style: { color: '$primaryHex' } }, $completedCoursesCount),
                                    React.createElement('div', { className: 'stat-label' }, 'منجزة')
                                ),
                                React.createElement('div', { className: 'stat-item' },
                                    React.createElement('div', { className: 'stat-value', style: { color: '$cardTextHex', opacity: 0.8 } }, $remainingCoursesCount),
                                    React.createElement('div', { className: 'stat-label' }, 'متبقية')
                                )
                            )
                        ),

                        React.createElement('div', { className: 'card' },
                            React.createElement('div', { className: 'card-title' }, 'إجمالي المحاضرات المنجزة والمتبقية'),
                            React.createElement('div', { className: 'chart-container' },
                                React.createElement(ResponsiveContainer, { width: '100%', height: '100%' },
                                    React.createElement(PieChart, null,
                                        React.createElement(Pie, {
                                            data: lectureData,
                                            cx: '50%',
                                            cy: '50%',
                                            innerRadius: 40,
                                            outerRadius: 60,
                                            paddingAngle: 5,
                                            dataKey: 'value'
                                        },
                                            React.createElement(Cell, { fill: '$tertiaryHex' }),
                                            React.createElement(Cell, { fill: '$cardBorderHex' })
                                        ),
                                        React.createElement(Tooltip, { 
                                            formatter: (value) => [value + ' محاضرة', 'العدد'],
                                            contentStyle: { direction: 'rtl', textAlign: 'right', fontSize: '11px', backgroundColor: '$cardBgHex', color: '$cardTextHex', border: '1px solid $cardBorderHex', borderRadius: '8px' }
                                        }),
                                        React.createElement(Legend, { verticalAlign: 'bottom', height: 24, iconSize: 10, style: { fontSize: '10px' } })
                                    )
                                )
                            ),
                            React.createElement('div', { className: 'stats' },
                                React.createElement('div', { className: 'stat-item' },
                                    React.createElement('div', { className: 'stat-value', style: { color: '$tertiaryHex' } }, $completedLectures),
                                    React.createElement('div', { className: 'stat-label' }, 'منجزة')
                                ),
                                React.createElement('div', { className: 'stat-item' },
                                    React.createElement('div', { className: 'stat-value', style: { color: '$cardTextHex', opacity: 0.8 } }, $remainingLectures),
                                    React.createElement('div', { className: 'stat-label' }, 'متبقية')
                                )
                            )
                        )
                    );
                }

                const r = ReactDOM.createRoot(document.getElementById('root'));
                r.render(React.createElement(App));
            </script>
        </body>
        </html>
    """.trimIndent()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(520.dp)
            .testTag("recharts_web_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(0) // Transparent background
                    loadDataWithBaseURL("https://localhost", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://localhost", htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun Color.toHex(): String {
    return String.format("#%02x%02x%02x", (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
}

@Composable
fun BaseDoughnutChart(
    primary: Int,
    secondary: Int,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val total = primary + secondary
    val primaryAngle = if (total > 0) (primary.toFloat() / total * 360f) else 0f
    val secondaryAngle = if (total > 0) 360f - primaryAngle else 360f

    Canvas(modifier = modifier) {
        val sizeMin = size.minDimension
        val strokeWidth = sizeMin * 0.15f
        val arcSize = sizeMin - strokeWidth
        val topLeftOffset = (sizeMin - arcSize) / 2f

        // Draw background arc
        drawArc(
            color = secondaryColor,
            startAngle = -90f + primaryAngle,
            sweepAngle = secondaryAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
            topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset)
        )

        // Draw foreground arc
        if (primaryAngle > 0f) {
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = primaryAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset)
            )
        }
    }
}

@Composable
fun CoursePercentageProgressCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (course.targetCount > 0) {
        course.completedCount.toFloat() / course.targetCount
    } else {
        0f
    }
    
    val courseColor = try {
        Color(android.graphics.Color.parseColor(course.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "course_progress_card_scale"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 650, easing = EaseInOutCubic),
        label = "course_progress_anim"
    )

    val isActive = course.status == "نشط"

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(courseColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.School,
                        contentDescription = null,
                        tint = courseColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = course.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = course.zoomAccount,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    if (course.category.isNotEmpty()) {
                        Surface(
                            color = courseColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(30.dp),
                            border = BorderStroke(1.dp, courseColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = course.category,
                                color = courseColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Active Badge
                    val badgeColor = if (isActive) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    val badgeTextColor = if (isActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                    val badgeText = if (isActive) "نشط" else "متوقف"
                    
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(30.dp),
                        border = BorderStroke(1.dp, badgeTextColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Days & Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = courseColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = course.days,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = courseColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${course.timeStart} - ${course.timeEnd}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar and Lecture counts
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المحاضرات: ${course.completedCount}/${course.targetCount}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = courseColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = courseColor,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
