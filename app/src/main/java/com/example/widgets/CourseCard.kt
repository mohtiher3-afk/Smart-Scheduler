package com.example.widgets

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Link
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Course
import com.example.services.SchedulerUtils
import com.example.screens.LocalAppLanguage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseCard(
    course: Course,
    isCourseActive: Boolean,
    onCalculate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestAlarm: () -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: Context,
    onCourseUpdated: (Course) -> Unit,
    isSelectionModeActive: Boolean = false,
    isSelected: Boolean = false,
    onSelectedChange: (Boolean) -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val currentLang = LocalAppLanguage.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val calendarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val success = com.example.services.CalendarProviderHelper.syncCourseToCalendar(context, course)
            if (success) {
                val successMsg = if (currentLang == "en") {
                    "Successfully synchronized '${course.name}' sessions directly to your phone calendar!"
                } else {
                    "تمت مزامنة جميع محاضرات دورة '${course.name}' مباشرة في تقويم هاتفك بنجاح!"
                }
                Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
            } else {
                val failMsg = if (currentLang == "en") {
                    "Failed to synchronize sessions. Please verify calendar access."
                } else {
                    "فشل في مزامنة المحاضرات. يرجى التحقق من إعدادات التقويم."
                }
                Toast.makeText(context, failMsg, Toast.LENGTH_LONG).show()
            }
        } else {
            val deniedMsg = if (currentLang == "en") {
                "Calendar access permission was denied. Cannot sync directly."
            } else {
                "تم رفض صلاحية الوصول للتقويم. لا يمكن المزامنة مباشرة."
            }
            Toast.makeText(context, deniedMsg, Toast.LENGTH_LONG).show()
        }
    }

    val customCardColor = remember(course.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course.colorHex))
        } catch (e: Exception) {
            primaryColor
        }
    }

    var showLectureChecklist by remember { mutableStateOf(false) }
    var selectedLectureForNotes by remember { mutableStateOf(1) }
    var notesText by remember(selectedLectureForNotes, course) {
        mutableStateOf(course.getLectureNote(selectedLectureForNotes))
    }
    var lectureDateText by remember(selectedLectureForNotes, course) {
        mutableStateOf(course.getLectureMeetingDate(selectedLectureForNotes))
    }
    var lectureTimeText by remember(selectedLectureForNotes, course) {
        mutableStateOf(course.getLectureMeetingTime(selectedLectureForNotes))
    }
    var lectureZoomText by remember(selectedLectureForNotes, course) {
        mutableStateOf(course.getLectureMeetingZoom(selectedLectureForNotes))
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 150),
        label = "course_card_elevation"
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isPressed) 2.2.dp else 1.2.dp,
        animationSpec = tween(durationMillis = 150),
        label = "course_card_border_width"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "course_card_scale"
    )

    val completedSet = course.getCompletedLecturesSet()
    val targetCount = course.targetCount
    val remainingLectures = maxOf(0, targetCount - completedSet.size)
    val isFullyCompleted = remainingLectures == 0

    val cardBorderColor = if (isFullyCompleted) {
        customCardColor.copy(alpha = if (isPressed) 0.75f else 0.45f)
    } else {
        customCardColor.copy(alpha = if (isPressed) 0.5f else 0.2f)
    }

    val cardBorderWidth = if (isFullyCompleted) {
        if (isPressed) 2.4.dp else 1.6.dp
    } else {
        animatedBorderWidth
    }

    // Under Material 3, we use Card with modern surface colors Tinted Subtly by custom color
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = {
                    if (isSelectionModeActive) {
                        onSelectedChange(!isSelected)
                    } else {
                        showLectureChecklist = !showLectureChecklist
                    }
                },
                onLongClick = {
                    onLongClick()
                }
            )
            .testTag("course_card_${course.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullyCompleted) customCardColor.copy(alpha = 0.02f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        border = BorderStroke(cardBorderWidth, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(customCardColor)
            )
            AnimatedVisibility(
                visible = isSelectionModeActive,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectedChange(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = customCardColor,
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.testTag("course_card_checkbox_${course.id}")
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
            // Header: Name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .testTag("course_title_${course.id}")
                        )
                        if (course.category.isNotEmpty()) {
                            Surface(
                                color = customCardColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.8.dp, customCardColor.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = course.category,
                                    color = customCardColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    // Visual progress indicator at the top right below the name that dynamically fills up
                    Spacer(modifier = Modifier.height(6.dp))
                    val progressRatio = if (targetCount > 0) completedSet.size.toFloat() / targetCount.toFloat() else 0f
                    val percentageNum = (progressRatio * 100).toInt()
                    val animatedHeaderProgress by animateFloatAsState(
                        targetValue = progressRatio,
                        animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic),
                        label = "course_header_progress_anim"
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedHeaderProgress },
                            modifier = Modifier
                                .width(90.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = customCardColor,
                            trackColor = customCardColor.copy(alpha = 0.12f)
                        )
                        Text(
                            text = "$percentageNum%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = customCardColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Remaining Lectures Badge (Material Design 3 style matched to course signature color)
                    val remainingBadgeColor = customCardColor.copy(alpha = 0.12f)
                    val remainingBadgeTextColor = customCardColor
                    val remainingText = if (currentLang == "en") {
                        when {
                            remainingLectures == 0 -> "Fully Completed 🎉"
                            remainingLectures == 1 -> "1 lecture remaining ⏳"
                            remainingLectures == 2 -> "2 lectures remaining ⏳"
                            else -> "$remainingLectures lectures remaining ⏳"
                        }
                    } else {
                        when {
                            remainingLectures == 0 -> "مكتمل بالكامل 🎉"
                            remainingLectures == 1 -> "متبقي محاضرة واحدة ⏳"
                            remainingLectures == 2 -> "متبقي محاضرتان ⏳"
                            remainingLectures in 3..10 -> "متبقي $remainingLectures محاضرات ⏳"
                            else -> "متبقي $remainingLectures محاضرة ⏳"
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(remainingBadgeColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("course_remaining_badge_${course.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = remainingText,
                            color = remainingBadgeTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    // Active Badge
                    val badgeColor = if (isCourseActive) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
                    val badgeTextColor = if (isCourseActive) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    val badgeText = if (currentLang == "en") {
                        when (course.status) {
                            "نشط" -> "Active"
                            "منتهي" -> "Ended"
                            "متوقف" -> "Paused"
                            else -> course.status
                        }
                    } else {
                        course.status
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(badgeColor)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(10.dp))

            // Info details (3 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Days Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLang == "en") "Days" else "الأيام", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val daysTranslated = if (currentLang == "en") {
                        course.days
                            .replace("الأحد", "Sun")
                            .replace("الاحد", "Sun")
                            .replace("الاثنين", "Mon")
                            .replace("الثلاثاء", "Tue")
                            .replace("الأربعاء", "Wed")
                            .replace("الاربعاء", "Wed")
                            .replace("الخميس", "Thu")
                            .replace("الجمعة", "Fri")
                            .replace("السبت", "Sat")
                    } else {
                        course.days
                    }
                    Text(
                        text = daysTranslated,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Time Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLang == "en") "Time" else "التوقيت", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${course.timeStart} - ${course.timeEnd}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Lectures Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Book, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLang == "en") "Lectures" else "المحاضرات", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLang == "en") "${course.targetCount} lectures" else "${course.targetCount} محاضرة",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar & Lecture Checklist system
            val progress = if (targetCount > 0) completedSet.size.toFloat() / targetCount.toFloat() else 0f
            val percentage = (progress * 100).toInt()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(customCardColor.copy(alpha = 0.05f))
                    .border(
                        BorderStroke(1.dp, customCardColor.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = customCardColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == "en") "Academic Progress" else "مستوى التقدم الدراسي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (currentLang == "en") "${completedSet.size} of $targetCount lectures ($percentage%)" else "${completedSet.size} من $targetCount محاضرات ($percentage%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = customCardColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic),
                    label = "course_progress_anim"
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = customCardColor,
                    trackColor = customCardColor.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showLectureChecklist = !showLectureChecklist }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == "en") {
                            if (showLectureChecklist) "Hide Lectures Details" else "Select Completed Lectures"
                        } else {
                            if (showLectureChecklist) "إخفاء تفاصيل المحاضرات" else "تحديد المحاضرات المكتملة"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showLectureChecklist) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (showLectureChecklist) {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val chunkedList = (1..targetCount).chunked(3)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chunkedList.forEach { rowIndices ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowIndices.forEach { i ->
                                    val isDone = completedSet.contains(i)
                                    val bg = if (isDone) customCardColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    val border = if (isDone) BorderStroke(1.dp, customCardColor.copy(alpha = 0.35f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    val txtColor = if (isDone) customCardColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    
                                    Card(
                                        onClick = {
                                            val updatedCourse = course.toggleLectureCompleted(i)
                                            onCourseUpdated(updatedCourse)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = bg),
                                        border = border,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Icon(
                                                imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isDone) customCardColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (currentLang == "en") "Lecture $i" else "محاضرة $i",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = txtColor
                                            )
                                        }
                                    }
                                }
                                if (rowIndices.size < 3) {
                                    repeat(3 - rowIndices.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lecture Notes & Meetings Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        val originalNote = course.getLectureNote(selectedLectureForNotes)
                        val originalDate = course.getLectureMeetingDate(selectedLectureForNotes)
                        val originalTime = course.getLectureMeetingTime(selectedLectureForNotes)
                        val originalZoom = course.getLectureMeetingZoom(selectedLectureForNotes)
                        val isChanged = notesText != originalNote || lectureDateText != originalDate || lectureTimeText != originalTime || lectureZoomText != originalZoom

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLang == "en") "Lecture Details & Notes" else "تفاصيل وملاحظات اللقاء",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            if (isChanged) {
                                TextButton(
                                    onClick = {
                                        var updatedCourse = course.setLectureNote(selectedLectureForNotes, notesText)
                                        updatedCourse = updatedCourse.setLectureMeeting(selectedLectureForNotes, lectureDateText, lectureTimeText, lectureZoomText)
                                        onCourseUpdated(updatedCourse)
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = customCardColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLang == "en") "Save" else "حفظ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = customCardColor
                                    )
                                }
                            } else if (lectureDateText.isNotEmpty() && lectureTimeText.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Add to Calendar (.ics)
                                    TextButton(
                                        onClick = {
                                            val specificZoom = if (lectureZoomText.isNotEmpty()) lectureZoomText else course.zoomAccount
                                            val lectureNameTranslated = if (currentLang == "en") "Lec $selectedLectureForNotes" else "اللقاء $selectedLectureForNotes"
                                            SchedulerUtils.exportSingleToIcsFile(
                                                context = context,
                                                courseName = course.name,
                                                lectureNumStr = lectureNameTranslated,
                                                dateStr = lectureDateText,
                                                timeStart = lectureTimeText,
                                                timeEnd = course.timeEnd,
                                                zoomAccount = specificZoom
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = customCardColor
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (currentLang == "en") "Add to Calendar" else "إضافة للتقويم",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = customCardColor
                                        )
                                    }

                                    // Direct Open in local calendar (Insert option)
                                    IconButton(
                                        onClick = {
                                            val specificZoom = if (lectureZoomText.isNotEmpty()) lectureZoomText else course.zoomAccount
                                            SchedulerUtils.exportSingleToCalendar(
                                                context = context,
                                                courseName = "${course.name} - " + (if (currentLang == "en") "Lec $selectedLectureForNotes" else "لقاء $selectedLectureForNotes"),
                                                dateStr = lectureDateText,
                                                timeStart = lectureTimeText,
                                                timeEnd = course.timeEnd,
                                                zoomAccount = specificZoom
                                            )
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = if (currentLang == "en") "Direct Insert" else "إدراج مباشر بالتقويم",
                                            modifier = Modifier.size(14.dp),
                                            tint = customCardColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal list of chips to select lecture
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(targetCount) { index ->
                                val lectureNum = index + 1
                                val hasNote = course.getLectureNote(lectureNum).trim().isNotEmpty()
                                val hasMeetingInfo = course.getLectureMeetingDate(lectureNum).isNotEmpty() || course.getLectureMeetingTime(lectureNum).isNotEmpty()
                                val isDone = completedSet.contains(lectureNum)
                                
                                FilterChip(
                                    selected = selectedLectureForNotes == lectureNum,
                                    onClick = { selectedLectureForNotes = lectureNum },
                                    label = {
                                        Text(
                                            text = if (currentLang == "en") "Lec $lectureNum" else "لقاء $lectureNum",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    leadingIcon = if (isDone) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = if (selectedLectureForNotes == lectureNum) customCardColor else Color(0xFF10B981)
                                            )
                                        }
                                    } else if (hasNote || hasMeetingInfo) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.EditNote,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = customCardColor
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = customCardColor.copy(alpha = 0.15f),
                                        selectedLabelColor = customCardColor,
                                        selectedLeadingIconColor = customCardColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedLectureForNotes == lectureNum,
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        selectedBorderColor = customCardColor.copy(alpha = 0.5f),
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.5.dp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Toggler
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val isDone = completedSet.contains(selectedLectureForNotes)
                            Text(
                                text = if (currentLang == "en") "Lecture Status:" else "حالة اللقاء:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Completed Chip
                                FilterChip(
                                    selected = isDone,
                                    onClick = {
                                        if (!isDone) {
                                            val updatedCourse = course.toggleLectureCompleted(selectedLectureForNotes)
                                            onCourseUpdated(updatedCourse)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (currentLang == "en") "Completed" else "تم اللقاء",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                                        selectedLabelColor = Color(0xFF10B981)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isDone,
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        selectedBorderColor = Color(0xFF10B981),
                                        borderWidth = 1.dp
                                    )
                                )

                                // Pending Chip
                                FilterChip(
                                    selected = !isDone,
                                    onClick = {
                                        if (isDone) {
                                            val updatedCourse = course.toggleLectureCompleted(selectedLectureForNotes)
                                            onCourseUpdated(updatedCourse)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (currentLang == "en") "Pending" else "قيد الانتظار",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                        selectedLabelColor = Color(0xFFD97706)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = !isDone,
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        selectedBorderColor = Color(0xFFF59E0B),
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Date and Time Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Date field wrapped in clickable Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val initialDate = if (lectureDateText.isNotEmpty()) lectureDateText else {
                                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                            sdf.format(java.util.Date())
                                        }
                                        showDatePicker(context, initialDate) {
                                            lectureDateText = it
                                        }
                                    }
                            ) {
                                OutlinedTextField(
                                    value = lectureDateText,
                                    onValueChange = { lectureDateText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false, // click-only to trigger dialog
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = customCardColor
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (currentLang == "en") "Meeting Date" else "تاريخ اللقاء",
                                            fontSize = 10.sp
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            text = "YYYY-MM-DD",
                                            fontSize = 10.sp
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLeadingIconColor = customCardColor
                                    )
                                )
                            }

                            // Time field wrapped in clickable Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val initialTime = if (lectureTimeText.isNotEmpty()) lectureTimeText else course.timeStart
                                        showTimePicker(context, initialTime) {
                                            lectureTimeText = it
                                        }
                                    }
                            ) {
                                OutlinedTextField(
                                    value = lectureTimeText,
                                    onValueChange = { lectureTimeText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false, // click-only to trigger dialog
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = customCardColor
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (currentLang == "en") "Meeting Time" else "وقت اللقاء",
                                            fontSize = 10.sp
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            text = "00:00 م",
                                            fontSize = 10.sp
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLeadingIconColor = customCardColor
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Zoom Account/Link for this lecture
                        OutlinedTextField(
                            value = lectureZoomText,
                            onValueChange = { lectureZoomText = it },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Link,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = customCardColor
                                )
                            },
                            label = {
                                Text(
                                    text = if (currentLang == "en") "Zoom Account/Link (Optional)" else "رابط أو حساب Zoom (اختياري)",
                                    fontSize = 10.sp
                                )
                            },
                            placeholder = {
                                Text(
                                    text = if (course.zoomAccount.isNotEmpty()) course.zoomAccount else "example.zoom.us/j/...",
                                    fontSize = 10.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = customCardColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                focusedLabelColor = customCardColor,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 70.dp, max = 130.dp),
                            label = {
                                Text(
                                    text = if (currentLang == "en") {
                                        "Notes for Lec $selectedLectureForNotes"
                                    } else {
                                        "ملاحظات لقاء $selectedLectureForNotes"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            placeholder = {
                                Text(
                                    text = if (currentLang == "en") {
                                        "Key points, passcodes, reference links, drive folders..."
                                    } else {
                                        "النقاط الهامة، كلمات المرور، روابط مراجع، مجلد درايف..."
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = customCardColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                focusedLabelColor = customCardColor,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Zoom Capsule
            if (course.zoomAccount.trim().isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            clipboardManager.setText(AnnotatedString(course.zoomAccount))
                            val toastMsg = if (currentLang == "en") "Zoom link copied to clipboard" else "تم نسخ حساب زووم للمحفظة"
                            Toast
                                .makeText(context, toastMsg, Toast.LENGTH_SHORT)
                                .show()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = course.zoomAccount,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (currentLang == "en") "Copy" else "نسخ", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Rounded.ContentCopy, contentDescription = if (currentLang == "en") "Copy Zoom account" else "نسخ حساب زووم", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                Text(
                    text = if (currentLang == "en") "No Zoom account configured for this course" else "لم يتم تحديد حساب Zoom لهذه الدورة",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCalculate,
                        enabled = !isSelectionModeActive,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLang == "en") "Calculate times" else "احسب المواعيد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    var showCalendarMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        OutlinedButton(
                            onClick = { showCalendarMenu = true },
                            enabled = !isSelectionModeActive,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLang == "en") "Add to Calendar 📅" else "إضافة للتقويم 📅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        DropdownMenu(
                            expanded = showCalendarMenu,
                            onDismissRequest = { showCalendarMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(
                                            text = if (currentLang == "en") "Direct Phone Calendar Sync 📅" else "مزامنة مباشرة لتقويم الهاتف 📅",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (currentLang == "en") "Adds all sessions to your device calendar app" else "إدراج كافة اللقاءات مباشرة في تطبيق تقويم جهازك",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    showCalendarMenu = false
                                    // Direct Sync logic
                                    val hasCalendarPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.WRITE_CALENDAR
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                    if (hasCalendarPermission) {
                                        val success = com.example.services.CalendarProviderHelper.syncCourseToCalendar(context, course)
                                        if (success) {
                                            val successMsg = if (currentLang == "en") {
                                                "Successfully synchronized '${course.name}' sessions directly to your phone calendar!"
                                            } else {
                                                "تمت مزامنة جميع محاضرات دورة '${course.name}' مباشرة في تقويم هاتفك بنجاح!"
                                            }
                                            Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                                        } else {
                                            val failMsg = if (currentLang == "en") {
                                                "Failed to synchronize sessions. Please verify calendar access."
                                            } else {
                                                "فشل في مزامنة المحاضرات. يرجى التحقق من إعدادات التقويم."
                                            }
                                            Toast.makeText(context, failMsg, Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        calendarLauncher.launch(android.Manifest.permission.WRITE_CALENDAR)
                                    }
                                }
                            )
                            
                            DropdownMenuItem(
                                text = {
                                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(
                                            text = if (currentLang == "en") "Export ICS Calendar File 📄" else "تصدير ملف التقويم (.ics) 📄",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (currentLang == "en") "Generate shareable standard calendar file" else "توليد ملف تقويم قياسي للمشاركة والاستيراد",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    showCalendarMenu = false
                                    SchedulerUtils.exportCourseToCalendar(context, course)
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Test 5-second Instant Alarm Button
                    if (isCourseActive && course.zoomAccount.isNotEmpty()) {
                        IconButton(
                            onClick = onTestAlarm,
                            enabled = !isSelectionModeActive,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Icon(Icons.Rounded.Notifications, contentDescription = if (currentLang == "en") "Test alarm" else "تنبيه تجريبي", modifier = Modifier.size(18.dp))
                        }
                    }

                    // Edit
                    IconButton(
                        onClick = onEdit,
                        enabled = !isSelectionModeActive,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = if (currentLang == "en") "Edit" else "تعديل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        enabled = !isSelectionModeActive,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = if (currentLang == "en") "Delete" else "حذف",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
}

private fun showDatePicker(
    context: Context,
    currentDateStr: String,
    onDateSelected: (String) -> Unit
) {
    val date = try {
        val sdfStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        sdfStr.parse(currentDateStr)
    } catch (e: Exception) {
        null
    } ?: java.util.Date()
    
    val calendar = java.util.Calendar.getInstance()
    calendar.time = date

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedSelected = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedSelected)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}

private fun showTimePicker(
    context: Context,
    currentTimeStr: String,
    onTimeSelected: (String) -> Unit
) {
    val calendar = java.util.Calendar.getInstance()
    try {
        val clean = currentTimeStr.trim()
        val isPm = clean.contains("م") || clean.lowercase().contains("pm")
        val numbers = clean.replace(Regex("[^0-9:]"), "")
        val parts = numbers.split(":")
        if (parts.size >= 2) {
            var hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            if (isPm && hours < 12) hours += 12
            else if (!isPm && hours == 12) hours = 0
            calendar.set(java.util.Calendar.HOUR_OF_DAY, hours)
            calendar.set(java.util.Calendar.MINUTE, minutes)
        }
    } catch (e: Exception) {
        // fallback
    }

    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val isPm = hourOfDay >= 12
            val displayHour = when {
                hourOfDay == 0 -> 12
                hourOfDay > 12 -> hourOfDay - 12
                else -> hourOfDay
            }
            val amPmStr = if (isPm) "م" else "ص"
            val formattedSelected = String.format(java.util.Locale.US, "%02d:%02d %s", displayHour, minute, amPmStr)
            onTimeSelected(formattedSelected)
        },
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
        false // 12 hour format
    )
    timePickerDialog.show()
}

