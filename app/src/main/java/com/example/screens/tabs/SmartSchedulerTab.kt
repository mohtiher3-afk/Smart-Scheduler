package com.example.screens.tabs

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Course
import com.example.screens.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartSchedulerTab(
    viewModel: MainViewModel,
    onCourseAddedAndNavigationRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // State flows from ViewModel
    val aiInputText by viewModel.aiInputText.collectAsState()
    val isAiParsing by viewModel.isAiParsing.collectAsState()
    val parsedCoursesPreview by viewModel.parsedCoursesPreview.collectAsState()
    val aiParsingError by viewModel.aiParsingError.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    
    val aiChatHistory by viewModel.aiChatHistory.collectAsState()
    val aiChatInput by viewModel.aiChatInput.collectAsState()
    val isAiChatLoading by viewModel.isAiChatLoading.collectAsState()

    var showConflictConfirmIdx by remember { mutableStateOf<Int?>(null) }

    // Internal tab toggle: 0 = Smart Course Parser, 1 = Calendar Assistant Chat
    var subTabSelected by remember { mutableStateOf(0) }

    val quickSuggestions = listOf(
        "أضف دورة بايثون الأحد والثلاثاء من 4م إلى 6م بـ 12 محاضرة",
        "دورة تصميم واجهات السبت والخميس من 8ص إلى 10ص مع رابط زوم zoom.us/j/123",
        "دورة إدارة الأعمال الإثنين والأربعاء من 6:30م إلى 8م"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("smart_scheduler_tab")
    ) {
        // High-end Arabic Subtab bar
        TabRow(
            selectedTabIndex = subTabSelected,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = subTabSelected == 0,
                onClick = { subTabSelected = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "جدولة بالذكاء الاصطناعي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                modifier = Modifier.testTag("subtab_parser")
            )
            Tab(
                selected = subTabSelected == 1,
                onClick = { subTabSelected = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مساعد التخطيط الدراسي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                modifier = Modifier.testTag("subtab_chat")
            )
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        if (subTabSelected == 0) {
            // Tab 0: Smart Course Parser
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Hero Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الجدولة الذكية بلمسة واحدة",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "اكتب تفاصيل دورتك بأسلوبك وسيقوم المساعد الذكي باستخراج المواعيد والأيام بدقة متناهية وإضافتها لجدولك.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Input Box Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "أدخل نص مواعيد الدورة التدريبية:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedTextField(
                                value = aiInputText,
                                onValueChange = { viewModel.setAiInputText(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("ai_schedule_input"),
                                placeholder = {
                                    Text(
                                        text = "مثال: أضف دورة هندسة البرمجيات الأحد والثلاثاء الساعة ٤م إلى ٦م بـ ١٠ محاضرات ورابط زوم zoom.us/myclass",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        lineHeight = 18.sp
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                maxLines = 4
                            )

                            // Quick suggestion helpers
                            Text(
                                text = "أو اختر نموذجاً جاهزاً لتجربته:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(quickSuggestions) { suggestion ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(30.dp)
                                            )
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                            .clickable { viewModel.setAiInputText(suggestion) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = suggestion,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { viewModel.parseCourseWithAi(aiInputText) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("ai_parse_button"),
                                enabled = aiInputText.trim().isNotEmpty() && !isAiParsing,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isAiParsing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جاري استخراج المواعيد...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تحليل وجدولة بالذكاء الاصطناعي", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // AI Parsing Error representation
                if (aiParsingError != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = aiParsingError ?: "",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // Parsed Preview Card
                item {
                    AnimatedVisibility(
                        visible = parsedCoursesPreview.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section Header with Save All option
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "المخططات المستخرجة من المساعد (${parsedCoursesPreview.size}):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row {
                                    if (parsedCoursesPreview.size > 1) {
                                        TextButton(
                                            onClick = {
                                                viewModel.saveAllPreviewCourses()
                                                Toast.makeText(context, "تم حفظ جميع الدورات المستخرجة بنجاح!", Toast.LENGTH_SHORT).show()
                                                onCourseAddedAndNavigationRequested()
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Rounded.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("حفظ الكل", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    TextButton(
                                        onClick = { viewModel.clearParsedCoursePreview() }
                                    ) {
                                        Text("إلغاء الكل", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            // Render each course in the list as editable form Card
                            parsedCoursesPreview.forEachIndexed { idx, course ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                                    border = BorderStroke(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Card Header: Editable Name
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.School,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "تعديل بيانات الدورة #${idx + 1}:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Editable Course Name Field
                                        var nameText by remember(course) { mutableStateOf(course.name) }
                                        OutlinedTextField(
                                            value = nameText,
                                            onValueChange = {
                                                nameText = it
                                                viewModel.updatePreviewCourse(idx, course.copy(name = it))
                                            },
                                            label = { Text("اسم الدورة التدريبية", fontSize = 12.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        // Selectable Days row using HorizontalScroll and customized chips
                                        val allArabicDays = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
                                        val currentDays = course.days.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("أيام الدراسة الأسبوعية:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                allArabicDays.forEach { dayName ->
                                                    val isSelected = currentDays.contains(dayName)
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                                else MaterialTheme.colorScheme.surfaceVariant
                                                            )
                                                            .clickable {
                                                                val updatedList = if (isSelected) {
                                                                    currentDays.filter { it != dayName }
                                                                } else {
                                                                    currentDays + dayName
                                                                }
                                                                val updatedDaysString = updatedList.joinToString(", ")
                                                                viewModel.updatePreviewCourse(idx, course.copy(days = updatedDaysString))
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = dayName,
                                                            fontSize = 11.sp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Editable Timing Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            var startText by remember(course) { mutableStateOf(course.timeStart) }
                                            var endText by remember(course) { mutableStateOf(course.timeEnd) }

                                            OutlinedTextField(
                                                value = startText,
                                                onValueChange = {
                                                    startText = it
                                                    viewModel.updatePreviewCourse(idx, course.copy(timeStart = it))
                                                },
                                                label = { Text("وقت البدء (مثال: 04:00 م)", fontSize = 11.sp) },
                                                modifier = Modifier.weight(1f),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = endText,
                                                onValueChange = {
                                                    endText = it
                                                    viewModel.updatePreviewCourse(idx, course.copy(timeEnd = it))
                                                },
                                                label = { Text("وقت الانتهاء (مثال: 06:00 م)", fontSize = 11.sp) },
                                                modifier = Modifier.weight(1f),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }

                                        // Lectures count and Zoom meeting account link
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            var targetCountText by remember(course) { mutableStateOf(course.targetCount.toString()) }
                                            var zoomText by remember(course) { mutableStateOf(course.zoomAccount) }

                                            OutlinedTextField(
                                                value = targetCountText,
                                                onValueChange = {
                                                    targetCountText = it
                                                    val parsedCount = it.toIntOrNull() ?: course.targetCount
                                                    viewModel.updatePreviewCourse(idx, course.copy(targetCount = parsedCount))
                                                },
                                                label = { Text("المحاضرات", fontSize = 11.sp) },
                                                modifier = Modifier.width(90.dp),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = zoomText,
                                                onValueChange = {
                                                    zoomText = it
                                                    viewModel.updatePreviewCourse(idx, course.copy(zoomAccount = it))
                                                },
                                                label = { Text("رابط زووم/اللقاء", fontSize = 11.sp) },
                                                modifier = Modifier.weight(1f),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                                shape = RoundedCornerShape(10.dp),
                                                singleLine = true
                                            )
                                        }

                                        val conflicts = remember(course, allCourses) {
                                            com.example.services.ConflictDetector.findConflicts(course, allCourses)
                                        }

                                        if (conflicts.isNotEmpty()) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Warning,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "تنبيه تعارض المواعيد! ⚠️",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = MaterialTheme.colorScheme.onErrorContainer
                                                        )
                                                        Text(
                                                            text = "تتداخل هذه الدورة مع '${conflicts[0].secondCourse.name}' في الأيام (${conflicts[0].conflictingDays.joinToString("، ")}).",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Action Button Row for this course
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (conflicts.isNotEmpty()) {
                                                        showConflictConfirmIdx = idx
                                                    } else {
                                                        viewModel.insertParsedCourseDirectly(course)
                                                        Toast.makeText(context, "تمت جدولة الدورة التدريبية '${course.name}' بنجاح!", Toast.LENGTH_SHORT).show()
                                                        if (parsedCoursesPreview.size <= 1) {
                                                            onCourseAddedAndNavigationRequested()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("ai_confirm_course_button_${idx}"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("تأكيد وحفظ هذه الدورة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.removePreviewCourse(idx)
                                                },
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.DeleteOutline,
                                                    contentDescription = "حذف المعاينة",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: AI Assistant Chat & Planner
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Intro helper
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحدّث مع المساعد لتصميم خطة دراسية تناسب مواعيدك الحالية.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chat history list
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    if (aiChatHistory.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "مستشارك الأكاديمي وجدولك الذكي جاهز!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "اسألني مثلاً: كيف أرتب أوقات المراجعة لمحاضراتي الحالية؟ أو اقترح خطة دراسية ملائمة.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(aiChatHistory) { chat ->
                                // User Message
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = chat.first,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }

                                // AI Response
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "مساعد التخطيط والجدولة",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            if (chat.second.isEmpty() && isAiChatLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text(
                                                    text = chat.second,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat Input field row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier
                            .size(44.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = "مسح المحادثة",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    OutlinedTextField(
                        value = aiChatInput,
                        onValueChange = { viewModel.setAiChatInput(it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input"),
                        placeholder = { Text("اكتب سؤالك بخصوص خطتك الدراسية...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (aiChatInput.trim().isNotEmpty() && !isAiChatLoading) {
                                viewModel.sendChatMessage(aiChatInput)
                            }
                        })
                    )

                    IconButton(
                        onClick = {
                            if (aiChatInput.trim().isNotEmpty() && !isAiChatLoading) {
                                viewModel.sendChatMessage(aiChatInput)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (aiChatInput.trim().isNotEmpty() && !isAiChatLoading)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                        enabled = aiChatInput.trim().isNotEmpty() && !isAiChatLoading
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Send,
                            contentDescription = "إرسال",
                            tint = if (aiChatInput.trim().isNotEmpty() && !isAiChatLoading)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }

    showConflictConfirmIdx?.let { idx ->
        val course = parsedCoursesPreview.getOrNull(idx)
        if (course != null) {
            val conflicts = com.example.services.ConflictDetector.findConflicts(course, allCourses)
            AlertDialog(
                onDismissRequest = { showConflictConfirmIdx = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text("تأكيد حفظ الدورة المتعارضة ⚠️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        text = "هذه الدورة المستخرجة تتداخل في مواعيدها مع دورة أخرى مسجلة حالياً:\n\n" +
                                "• الدورة المتعارضة: '${conflicts.firstOrNull()?.secondCourse?.name}'\n" +
                                "• في أيام: ${conflicts.firstOrNull()?.conflictingDays?.joinToString("، ")}\n" +
                                "• توقيت الدورة الأخرى: ${conflicts.firstOrNull()?.timeSpan}\n\n" +
                                "هل ترغب في جدولة وحفظ دورة '${course.name}' وتجاهل التعارض؟",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConflictConfirmIdx = null
                            viewModel.insertParsedCourseDirectly(course)
                            Toast.makeText(context, "تمت جدولة الدورة التدريبية '${course.name}' بنجاح!", Toast.LENGTH_SHORT).show()
                            if (parsedCoursesPreview.size <= 1) {
                                onCourseAddedAndNavigationRequested()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("تأكيد وحفظ الجدولة", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConflictConfirmIdx = null }) {
                        Text("إلغاء لتصحيح الوقت", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
