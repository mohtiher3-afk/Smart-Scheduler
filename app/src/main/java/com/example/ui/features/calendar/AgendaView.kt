package com.example.ui.features.calendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Course
import com.example.models.SessionInfo
import com.example.ui.components.SmartEmptyState
import com.example.core.designsystem.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AgendaView(
    courses: List<Course>,
    currentLanguage: String = "ar",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Generate upcoming sessions for the next 14 days from active courses
    val agendaSessions = remember(courses) {
        generateUpcomingSessions(courses, daysAhead = 14)
    }

    // Group sessions by date string
    val groupedSessions = remember(agendaSessions) {
        agendaSessions.groupBy { it.dateString }
    }

    val sortedDates = remember(groupedSessions) {
        groupedSessions.keys.sorted()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("agenda_view_container")
            .background(AppTheme.colors.background)
    ) {
        if (courses.isEmpty() || agendaSessions.isEmpty()) {
            SmartEmptyState(
                title = if (currentLanguage == "ar") "أجندة فارغة" else "Empty Agenda",
                description = if (currentLanguage == "ar")
                    "يرجى تسجيل دورات دراسية وتفعيلها لتوليد جدول الأجندة التفاعلي."
                else
                    "Add active courses to generate your smart study agenda.",
                icon = Icons.Rounded.CalendarMonth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                sortedDates.forEach { dateStr ->
                    val sessions = groupedSessions[dateStr] ?: emptyList()
                    val formattedHeader = sessions.firstOrNull()?.let {
                        if (currentLanguage == "ar") "${it.dayName}، ${it.formattedDate}" else "${it.dayName}, ${it.dateString}"
                    } ?: dateStr

                    item {
                        Text(
                            text = formattedHeader,
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = AppTheme.colors.primary,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    items(sessions) { session ->
                        AgendaSessionItem(
                            session = session,
                            context = context,
                            currentLanguage = currentLanguage,
                            courseColorHex = getCourseColorHex(courses, session.courseId)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaSessionItem(
    session: SessionInfo,
    context: Context,
    currentLanguage: String,
    courseColorHex: String
) {
    val defaultColor = AppTheme.colors.primary
    val courseColor = remember(courseColorHex, defaultColor) {
        try {
            Color(android.graphics.Color.parseColor(courseColorHex))
        } catch (e: Exception) {
            defaultColor
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("agenda_session_${session.courseId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(courseColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.courseName,
                    style = AppTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Videocam,
                        contentDescription = "Time",
                        tint = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = session.timeStart,
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            if (session.zoomAccount.isNotBlank()) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(session.zoomAccount))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open Zoom link", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = courseColor.copy(alpha = 0.15f),
                        contentColor = courseColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (currentLanguage == "ar") "انضمام" else "Join",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Global utility helper to generate upcoming sessions for the next N days
fun generateUpcomingSessions(courses: List<Course>, daysAhead: Int): List<SessionInfo> {
    val list = mutableListOf<SessionInfo>()
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sdfOutput = SimpleDateFormat("d MMMM yyyy", Locale("ar"))

    val activeCourses = courses.filter { it.status == "نشط" }

    for (dayOffset in 0..daysAhead) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)

        val calendarDayNum = cal.get(Calendar.DAY_OF_WEEK)
        val mappedIdx = when (calendarDayNum) {
            Calendar.SUNDAY -> 0
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 0
        }

        activeCourses.forEach { course ->
            val indices = parseDaysToIndices(course.days)
            if (indices.contains(mappedIdx)) {
                // Generate a sessionoccurrence
                val dateStr = sdfDate.format(cal.time)
                val outStr = sdfOutput.format(cal.time)
                val dayArName = getArabicDayName(mappedIdx)

                list.add(
                    SessionInfo(
                        courseId = course.id,
                        courseName = course.name,
                        dateString = dateStr,
                        formattedDate = outStr,
                        dayName = dayArName,
                        timeStart = course.timeStart,
                        alarmTimeMillis = cal.timeInMillis,
                        sessionTimeMillis = cal.timeInMillis,
                        zoomAccount = course.zoomAccount,
                        reminderLeadMinutes = course.reminderLeadMinutes
                    )
                )
            }
        }
    }

    // Sort chronologically by date string and then start times
    return list.sortedWith(compareBy<SessionInfo> { it.dateString }.thenBy { it.timeStart })
}

private fun getArabicDayName(idx: Int): String {
    return when (idx) {
        0 -> "الأحد"
        1 -> "الاثنين"
        2 -> "الثلاثاء"
        3 -> "الأربعاء"
        4 -> "الخميس"
        5 -> "الجمعة"
        6 -> "السبت"
        else -> ""
    }
}

private fun getCourseColorHex(courses: List<Course>, courseId: Int): String {
    return courses.firstOrNull { it.id == courseId }?.colorHex ?: "#2563EB"
}
