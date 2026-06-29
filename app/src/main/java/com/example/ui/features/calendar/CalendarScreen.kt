package com.example.ui.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.models.Course
import com.example.screens.LocalAppLanguage
import com.example.screens.MainViewModel
import com.example.ui.components.SmartEmptyState
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.Dimens
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarViewType {
    Month, Week, Day, Agenda, Timeline
}

@Composable
fun CalendarScreen(
    courses: List<Course>,
    onCourseUpdated: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang = LocalAppLanguage.current

    var selectedView by remember { mutableStateOf(CalendarViewType.Month) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }

    // Synchronize currentMonth with selectedDate when active month shifts
    LaunchedEffect(selectedDate) {
        val temp = selectedDate.clone() as Calendar
        temp.set(Calendar.DAY_OF_MONTH, 1)
        currentMonth = temp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen_container")
            .background(AppTheme.colors.background)
    ) {
        // Upper Segmented Switcher for Calendar Views
        ViewSwitcherRow(
            selectedView = selectedView,
            onViewChange = { selectedView = it },
            currentLanguage = currentLang
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.Small))

        // Navigation Header (Chevrons & Text representation)
        CalendarNavigationHeader(
            selectedView = selectedView,
            selectedDate = selectedDate,
            currentMonth = currentMonth,
            currentLanguage = currentLang,
            onNavigatePrevious = {
                val newCal = selectedDate.clone() as Calendar
                when (selectedView) {
                    CalendarViewType.Month -> newCal.add(Calendar.MONTH, -1)
                    CalendarViewType.Week -> newCal.add(Calendar.WEEK_OF_YEAR, -1)
                    CalendarViewType.Day -> newCal.add(Calendar.DAY_OF_YEAR, -1)
                    else -> {}
                }
                selectedDate = newCal
            },
            onNavigateNext = {
                val newCal = selectedDate.clone() as Calendar
                when (selectedView) {
                    CalendarViewType.Month -> newCal.add(Calendar.MONTH, 1)
                    CalendarViewType.Week -> newCal.add(Calendar.WEEK_OF_YEAR, 1)
                    CalendarViewType.Day -> newCal.add(Calendar.DAY_OF_YEAR, 1)
                    else -> {}
                }
                selectedDate = newCal
            }
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.Medium))

        // Active View Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedView) {
                CalendarViewType.Month -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MonthView(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            courses = courses,
                            onDateSelected = { selectedDate = it },
                            currentLanguage = currentLang,
                            modifier = Modifier.padding(horizontal = AppTheme.spacing.Medium)
                        )

                        Spacer(modifier = Modifier.height(AppTheme.spacing.Medium))

                        // Selected day details
                        SelectedDayLessonsList(
                            selectedDate = selectedDate,
                            courses = courses,
                            currentLanguage = currentLang,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                CalendarViewType.Week -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        WeekView(
                            selectedDate = selectedDate,
                            courses = courses,
                            onDateSelected = { selectedDate = it },
                            currentLanguage = currentLang,
                            modifier = Modifier.padding(horizontal = AppTheme.spacing.Medium)
                        )

                        Spacer(modifier = Modifier.height(AppTheme.spacing.Medium))

                        SelectedDayLessonsList(
                            selectedDate = selectedDate,
                            courses = courses,
                            currentLanguage = currentLang,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                CalendarViewType.Day -> {
                    DayView(
                        selectedDate = selectedDate,
                        courses = courses,
                        currentLanguage = currentLang
                    )
                }
                CalendarViewType.Agenda -> {
                    AgendaView(
                        courses = courses,
                        currentLanguage = currentLang
                    )
                }
                CalendarViewType.Timeline -> {
                    TimelineView(
                        courses = courses,
                        onCourseUpdated = onCourseUpdated,
                        currentLanguage = currentLang
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewSwitcherRow(
    selectedView: CalendarViewType,
    onViewChange: (CalendarViewType) -> Unit,
    currentLanguage: String
) {
    val items = remember(currentLanguage) {
        if (currentLanguage == "ar") {
            listOf(
                Triple(CalendarViewType.Month, "شهر", Icons.Rounded.CalendarMonth),
                Triple(CalendarViewType.Week, "أسبوع", Icons.Rounded.ViewWeek),
                Triple(CalendarViewType.Day, "يوم", Icons.Rounded.CalendarToday),
                Triple(CalendarViewType.Agenda, "أجندة", Icons.Rounded.FormatListBulleted),
                Triple(CalendarViewType.Timeline, "خط زمني", Icons.Rounded.Timeline)
            )
        } else {
            listOf(
                Triple(CalendarViewType.Month, "Month", Icons.Rounded.CalendarMonth),
                Triple(CalendarViewType.Week, "Week", Icons.Rounded.ViewWeek),
                Triple(CalendarViewType.Day, "Day", Icons.Rounded.CalendarToday),
                Triple(CalendarViewType.Agenda, "Agenda", Icons.Rounded.FormatListBulleted),
                Triple(CalendarViewType.Timeline, "Timeline", Icons.Rounded.Timeline)
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.Medium, vertical = AppTheme.spacing.Small),
        shape = AppTheme.shapes.extraLarge,
        color = AppTheme.colors.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = Dimens.SpaceSmall
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.ExtraSmall),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { (viewType, label, icon) ->
                val isSelected = selectedView == viewType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(AppTheme.shapes.large)
                        .background(
                            color = if (isSelected) AppTheme.colors.primary else Color.Transparent
                        )
                        .clickable { onViewChange(viewType) }
                        .padding(vertical = AppTheme.spacing.Small, horizontal = AppTheme.spacing.ExtraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else AppTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(Dimens.IconSmall)
                        )
                        Spacer(modifier = Modifier.height(AppTheme.spacing.ExtraSmall))
                        Text(
                            text = label,
                            style = AppTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else AppTheme.colors.onSurfaceVariant
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarNavigationHeader(
    selectedView: CalendarViewType,
    selectedDate: Calendar,
    currentMonth: Calendar,
    currentLanguage: String,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit
) {
    if (selectedView == CalendarViewType.Agenda || selectedView == CalendarViewType.Timeline) {
        // No header navigation for full future lists
        return
    }

    val headerText = remember(selectedView, selectedDate, currentMonth, currentLanguage) {
        val locale = if (currentLanguage == "ar") Locale("ar") else Locale.ENGLISH
        when (selectedView) {
            CalendarViewType.Month -> {
                val sdf = SimpleDateFormat("MMMM yyyy", locale)
                sdf.format(currentMonth.time)
            }
            CalendarViewType.Week -> {
                val sdf = SimpleDateFormat("d MMMM yyyy", locale)
                val start = selectedDate.clone() as Calendar
                start.add(Calendar.DAY_OF_YEAR, -(selectedDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY))
                val end = start.clone() as Calendar
                end.add(Calendar.DAY_OF_YEAR, 6)
                "${sdf.format(start.time)} - ${sdf.format(end.time)}"
            }
            CalendarViewType.Day -> {
                val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
                sdf.format(selectedDate.time)
            }
            else -> ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev button
        IconButton(
            onClick = onNavigatePrevious,
            modifier = Modifier
                .size(Dimens.IconExtraLarge)
                .clip(CircleShape)
                .background(AppTheme.colors.surface),
            colors = IconButtonDefaults.iconButtonColors(contentColor = AppTheme.colors.primary)
        ) {
            Icon(
                imageVector = if (currentLanguage == "ar") Icons.Rounded.ChevronRight else Icons.Rounded.ChevronLeft,
                contentDescription = "Previous"
            )
        }

        Text(
            text = headerText,
            style = AppTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                color = AppTheme.colors.onSurface
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // Next button
        IconButton(
            onClick = onNavigateNext,
            modifier = Modifier
                .size(Dimens.IconExtraLarge)
                .clip(CircleShape)
                .background(AppTheme.colors.surface),
            colors = IconButtonDefaults.iconButtonColors(contentColor = AppTheme.colors.primary)
        ) {
            Icon(
                imageVector = if (currentLanguage == "ar") Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
                contentDescription = "Next"
            )
        }
    }
}

@Composable
private fun SelectedDayLessonsList(
    selectedDate: Calendar,
    courses: List<Course>,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    val dayOfWeekIdx = selectedDate.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0
    val activeLessons = remember(courses, dayOfWeekIdx) {
        courses.filter { course ->
            course.status == "نشط" && parseDaysToIndices(course.days).contains(dayOfWeekIdx)
        }.sortedBy { it.timeStart }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = if (currentLanguage == "ar") "محاضرات اليوم" else "Lectures on this day",
            style = AppTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (activeLessons.isEmpty()) {
            SmartEmptyState(
                title = if (currentLanguage == "ar") "يوم مريح وخالي!" else "No lectures today!",
                description = if (currentLanguage == "ar") "استغل اليوم للمراجعة أو الاسترخاء." else "Enjoy your study-free day!",
                icon = Icons.Rounded.CalendarToday,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.Small)
            ) {
                items(activeLessons) { course ->
                    SelectedLessonItem(
                        course = course,
                        currentLanguage = currentLanguage
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedLessonItem(
    course: Course,
    currentLanguage: String
) {
    val defaultColor = AppTheme.colors.primary
    val courseColor = remember(course.colorHex, defaultColor) {
        try {
            Color(android.graphics.Color.parseColor(course.colorHex))
        } catch (e: Exception) {
            defaultColor
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_lesson_${course.id}"),
        shape = AppTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(courseColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.School,
                        contentDescription = "Lesson",
                        tint = courseColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = course.name,
                        style = AppTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.ExtraSmall))
                    Text(
                        text = "${course.timeStart} - ${course.timeEnd}",
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            if (course.zoomAccount.isNotBlank()) {
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(course.zoomAccount))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open Zoom", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(courseColor),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Videocam,
                        contentDescription = "Zoom",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
