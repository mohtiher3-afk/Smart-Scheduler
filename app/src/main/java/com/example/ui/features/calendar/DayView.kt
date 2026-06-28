package com.example.ui.features.calendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Course
import com.example.ui.material3_foundation.AppTheme
import java.util.*

@Composable
fun DayView(
    selectedDate: Calendar,
    courses: List<Course>,
    currentLanguage: String = "ar",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Filter courses active on this day of the week
    val dayOfWeekIdx = selectedDate.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0
    val dayCourses = remember(courses, dayOfWeekIdx) {
        courses.filter { course ->
            course.status == "نشط" && parseDaysToIndices(course.days).contains(dayOfWeekIdx)
        }
    }

    // Standard hours to display in the day schedule (e.g., 7 AM to 11 PM)
    val hourRange = remember { 7..23 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("day_view_container")
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (dayCourses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentLanguage == "ar") "لا توجد محاضرات في هذا اليوم" else "No lectures scheduled for this day",
                    style = AppTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 12.dp)
            ) {
                hourRange.forEach { hour ->
                    val hourLabel = remember(hour, currentLanguage) {
                        formatHourLabel(hour, currentLanguage)
                    }

                    // Check which courses start during this hour or overlap with it
                    val coursesThisHour = dayCourses.filter { course ->
                        val startMin = parseTimeToMinutes(course.timeStart)
                        val courseStartHour = startMin / 60
                        courseStartHour == hour
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Hour sidebar
                        Text(
                            text = hourLabel,
                            style = AppTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            ),
                            modifier = Modifier
                                .width(65.dp)
                                .padding(top = 10.dp)
                        )

                        // Divider line & course blocks area
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .drawDividerLine()
                                .padding(bottom = 12.dp)
                        ) {
                            if (coursesThisHour.isNotEmpty()) {
                                coursesThisHour.forEach { course ->
                                    DayCourseBlock(
                                        course = course,
                                        context = context,
                                        currentLanguage = currentLanguage
                                    )
                                }
                            } else {
                                // Empty placeholder spacer to keep visual grid height
                                Spacer(modifier = Modifier.height(44.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCourseBlock(
    course: Course,
    context: Context,
    currentLanguage: String
) {
    val defaultColor = MaterialTheme.colorScheme.primary
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
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .testTag("day_course_${course.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = courseColor.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, courseColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawStartBorder(courseColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = AppTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${course.timeStart} - ${course.timeEnd}",
                    style = AppTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            if (course.zoomAccount.isNotBlank()) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(course.zoomAccount))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open Zoom", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = courseColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Videocam,
                        contentDescription = "Zoom",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentLanguage == "ar") "زووم" else "Zoom",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Draw a left border or right border depending on layout alignment (standard start border)
private fun Modifier.drawStartBorder(color: Color): Modifier = this.drawBehind {
    val strokeWidth = 4.dp.toPx()
    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(0f, this.size.height),
        strokeWidth = strokeWidth
    )
}

// Draw a subtle background line below elements to emulate a planner grid
private fun Modifier.drawDividerLine(): Modifier = this.drawBehind {
    drawLine(
        color = Color.LightGray.copy(alpha = 0.2f),
        start = androidx.compose.ui.geometry.Offset(0f, this.size.height),
        end = androidx.compose.ui.geometry.Offset(this.size.width, this.size.height),
        strokeWidth = 1.dp.toPx()
    )
}

private fun formatHourLabel(hour: Int, currentLanguage: String): String {
    val amPm = if (hour < 12) {
        if (currentLanguage == "ar") "ص" else "AM"
    } else {
        if (currentLanguage == "ar") "م" else "PM"
    }
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.US, "%02d:00 %s", h, amPm)
}

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
        // Fallback
    }
    return 8 * 60 // 8:00 AM as fallback
}
