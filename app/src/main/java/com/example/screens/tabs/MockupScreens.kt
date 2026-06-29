package com.example.screens.tabs

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.models.Course
import com.example.screens.Loc
import com.example.ui.navigation.Screen
import java.util.Calendar

// Data models for Mockup Screens
data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val priority: String, // "High", "Medium", "Low"
    val isCompleted: Boolean = false,
    val category: String = "عام"
)

data class ExamItem(
    val id: String,
    val subject: String,
    val title: String,
    val date: String,
    val time: String,
    val room: String,
    val daysLeft: Int
)

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val courseName: String,
    val lastModified: String
)

data class FileItem(
    val id: String,
    val name: String,
    val size: String,
    val courseName: String,
    val fileType: String, // "PDF", "DOCX", "PPTX", "ZIP"
    val uploadDate: String
)

data class AchievementItem(
    val id: String,
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val progress: Float
)

// Shared Local States to make Mockup screens highly interactive and dynamic
object MockupDataStore {
    var tasksState = mutableStateListOf(
        TaskItem("1", "إعداد مقترح مشروع التخرج", "كتابة المقدمة والأهداف والمنهجية المتبعة للمشروع", "2025-07-05", "High", false, "نظم معلومات"),
        TaskItem("2", "تقرير معمل الفيزياء الثاني", "تسليم تقرير تجربة العدسات المقعرة والمحدبة", "2025-07-10", "Medium", false, "فيزياء العامة"),
        TaskItem("3", "حل مسائل الخوارزميات (البحث الثنائي)", "حل المسائل من 5 إلى 10 في الكتاب المقرر", "2025-06-30", "Low", true, "خوارزميات"),
        TaskItem("4", "مراجعة شابتر القوائم المالية", "تلخيص أهم القوانين والمصطلحات في المحاسبة", "2025-07-01", "High", false, "محاسبة مالية"),
        TaskItem("5", "تصميم هيكل قاعدة البيانات", "رسم مخطط الـ ERD لمشروع قواعد البيانات", "2025-07-06", "Medium", true, "قواعد بيانات")
    )

    var notesState = mutableStateListOf(
        NoteItem("1", "ملاحظات محاضرة خوارزميات ترتيب البيانات", "الخوارزميات الشهيرة تشمل المرج والترتيب السريع المعتمد على التجزئة. المرج خوارزمية مستقرة بكفاءة O(n log n) بينما الترتيب السريع أسرع عملياً لكنه غير مستقر في الحالة الأسوأ.", "خوارزميات", "2025-06-25"),
        NoteItem("2", "ملخص قوانين الحركة والموجات", "قوانين الحركة لنيوتن تشرح العلاقة بين حركة الجسم والقوى المؤثرة عليه. القوة تساوي الكتلة في التسارع. الموجات الصوتية هي موجات طولية تحتاج لوسط ناقل.", "فيزياء العامة", "2025-06-27"),
        NoteItem("3", "المفاهيم الأساسية للمحاسبة المزدوجة", "كل معاملة مالية تؤثر على حسابين على الأقل: أحدهما مدين والآخر دائن. الأصول = الالتزامات + حقوق الملكية. هذه هي المعادلة الميزانية الرئيسية.", "محاسبة مالية", "2025-06-28")
    )

    var examsState = mutableStateListOf(
        ExamItem("1", "خوارزميات وبنى بيانات", "الاختبار النصفي الأول", "2025-07-15", "09:00 - 10:30", "قاعة 101", 15),
        ExamItem("2", "فيزياء العامة", "اختبار معمل الفيزياء النهائي", "2025-07-10", "11:00 - 13:00", "مختبر 3", 10),
        ExamItem("3", "محاسبة مالية", "الاختبار النصفي الثاني", "2025-07-20", "14:00 - 15:30", "قاعة 205", 20),
        ExamItem("4", "نظم معلومات", "الاختبار النهائي للمقرر", "2025-07-28", "09:00 - 11:30", "قاعة 302", 28)
    )

    var filesState = mutableStateListOf(
        FileItem("1", "Syllabus_Algorithms_2025.pdf", "1.2 MB", "خوارزميات", "PDF", "2025-06-20"),
        FileItem("2", "Physics_Lab_Manual.pdf", "4.5 MB", "فيزياء العامة", "PDF", "2025-06-21"),
        FileItem("3", "Lecture_Slide_3_Financial_Analysis.pptx", "8.1 MB", "محاسبة مالية", "PPTX", "2025-06-22"),
        FileItem("4", "DB_Project_Requirements.docx", "540 KB", "قواعد بيانات", "DOCX", "2025-06-24"),
        FileItem("5", "Algorithms_Cheat_Sheet.zip", "3.1 MB", "خوارزميات", "ZIP", "2025-06-26")
    )
}

// ----------------- 3. TASKS SCREEN -----------------
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TasksTab(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = com.example.screens.LocalAppLanguage.current

    var selectedFilter by remember { mutableStateOf("All") } // "All", "To Do", "In Progress", "Done"
    var selectedPriorityFilter by remember { mutableStateOf("All") } // "All", "High", "Medium", "Low"
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Dialog state
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("Medium") }
    var newTaskCategory by remember { mutableStateOf("عام") }

    val filteredTasks = MockupDataStore.tasksState.filter { task ->
        val matchesStatus = when (selectedFilter) {
            "To Do" -> !task.isCompleted
            "Done" -> task.isCompleted
            else -> true
        }
        val matchesPriority = if (selectedPriorityFilter == "All") true else task.priority == selectedPriorityFilter
        matchesStatus && matchesPriority
    }

    val totalCount = MockupDataStore.tasksState.size
    val completedCount = MockupDataStore.tasksState.count { it.isCompleted }
    val todoCount = totalCount - completedCount

    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = {
                Text(
                    text = if (currentLang == "ar") "إضافة مهمة جديدة 📝" else "Add New Task 📝",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text(if (currentLang == "ar") "عنوان المهمة" else "Task Title") },
                        placeholder = { Text(if (currentLang == "ar") "أدخل العنوان..." else "Enter title...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = { newTaskDesc = it },
                        label = { Text(if (currentLang == "ar") "التفاصيل" else "Description") },
                        placeholder = { Text(if (currentLang == "ar") "أضف تفاصيل..." else "Add description...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = newTaskCategory,
                        onValueChange = { newTaskCategory = it },
                        label = { Text(if (currentLang == "ar") "التصنيف / المقرر" else "Category / Course") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (currentLang == "ar") "مستوى الأهمية (الأولوية):" else "Priority Level:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val priorities = listOf("High", "Medium", "Low")
                        priorities.forEach { pr ->
                            val isSel = newTaskPriority == pr
                            val color = when (pr) {
                                "High" -> MaterialTheme.colorScheme.error
                                "Medium" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { newTaskPriority = pr },
                                color = if (isSel) color else color.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, color)
                            ) {
                                Text(
                                    text = when(pr) {
                                        "High" -> if (currentLang == "ar") "عالية" else "High"
                                        "Medium" -> if (currentLang == "ar") "متوسطة" else "Medium"
                                        else -> if (currentLang == "ar") "منخفضة" else "Low"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else color,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isBlank()) {
                            Toast.makeText(context, if (currentLang == "ar") "يرجى إدخال عنوان المهمة" else "Please enter a task title", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        MockupDataStore.tasksState.add(
                            TaskItem(
                                id = (MockupDataStore.tasksState.size + 1).toString(),
                                title = newTaskTitle,
                                description = newTaskDesc,
                                date = "2025-07-01",
                                priority = newTaskPriority,
                                isCompleted = false,
                                category = newTaskCategory
                            )
                        )
                        Toast.makeText(context, if (currentLang == "ar") "تم إضافة المهمة بنجاح" else "Task added successfully", Toast.LENGTH_SHORT).show()
                        showAddTaskDialog = false
                        newTaskTitle = ""
                        newTaskDesc = ""
                        newTaskPriority = "Medium"
                        newTaskCategory = "عام"
                    }
                ) {
                    Text(if (currentLang == "ar") "إضافة" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text(if (currentLang == "ar") "إلغاء" else "Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (currentLang == "ar") "أنجز مهامك الدراسية ✍️" else "Complete Your Study Tasks ✍️",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (currentLang == "ar") {
                                "لديك $todoCount مهام متبقية و $completedCount مكتملة."
                            } else {
                                "You have $todoCount pending and $completedCount completed."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .drawBehind {
                                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                                drawCircle(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = Color(0xFF10B981),
                                    startAngle = -90f,
                                    sweepAngle = progress * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val pct = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100).toInt() else 0
                        Text(
                            text = "$pct%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Status Tabs & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filters
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("All", "To Do", "Done").forEach { filter ->
                        val isSel = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when (filter) {
                                    "To Do" -> if (currentLang == "ar") "قيد الإنجاز" else "To Do"
                                    "Done" -> if (currentLang == "ar") "مكتمل" else "Done"
                                    else -> if (currentLang == "ar") "الكل" else "All"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Plus Add Button
                Button(
                    onClick = { showAddTaskDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentLang == "ar") "مهمة" else "Task", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Priority Filter Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "High", "Medium", "Low").forEach { pr ->
                    val isSel = selectedPriorityFilter == pr
                    SuggestionChip(
                        onClick = { selectedPriorityFilter = pr },
                        label = {
                            Text(
                                text = when (pr) {
                                    "High" -> if (currentLang == "ar") "عالية" else "High"
                                    "Medium" -> if (currentLang == "ar") "متوسطة" else "Medium"
                                    "Low" -> if (currentLang == "ar") "منخفضة" else "Low"
                                    else -> if (currentLang == "ar") "الأولوية: الكل" else "Priority: All"
                                },
                                fontSize = 10.sp
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            labelColor = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Task Cards List
        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.PlaylistAddCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (currentLang == "ar") "لا توجد مهام مطابقة للفلتر!" else "No tasks match the filter!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                val priorityColor = when (task.priority) {
                    "High" -> Color(0xFFEF4444)
                    "Medium" -> Color(0xFFF59E0B)
                    else -> Color(0xFF3B82F6)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { isChecked ->
                                    val idx = MockupDataStore.tasksState.indexOfFirst { it.id == task.id }
                                    if (idx != -1) {
                                        MockupDataStore.tasksState[idx] = task.copy(isCompleted = isChecked)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (task.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = priorityColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = when(task.priority) {
                                                "High" -> if (currentLang == "ar") "عاجل 🔥" else "High"
                                                "Medium" -> if (currentLang == "ar") "متوسط ⚡" else "Medium"
                                                else -> if (currentLang == "ar") "منخفض ❄️" else "Low"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = priorityColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = task.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                MockupDataStore.tasksState.removeIf { it.id == task.id }
                                Toast.makeText(context, if (currentLang == "ar") "تم حذف المهمة" else "Task deleted", Toast.LENGTH_SHORT).show()
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                        ) {
                            Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}


// ----------------- 4. COURSES SCREEN -----------------
@Composable
fun CoursesScreen(
    courses: List<Course>,
    onAddCourseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang = com.example.screens.LocalAppLanguage.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (currentLang == "ar") "مساقاتي الدراسية 🎓" else "My Courses 🎓",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (currentLang == "ar") "إدارة فصولك والتقدم الأكاديمي" else "Manage your active terms and progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddCourseClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (currentLang == "ar") "إضافة مقرر" else "Add Course", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (courses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLang == "ar") "لا توجد كورسات مضافة بعد!" else "No courses added yet!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(courses) { course ->
                val progress = if (course.targetCount > 0) course.completedCount.toFloat() / course.targetCount else 0f
                val color = try {
                    Color(android.graphics.Color.parseColor(course.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = course.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            SuggestionChip(
                                onClick = {},
                                label = { Text(course.category, fontSize = 9.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = color.copy(alpha = 0.08f),
                                    labelColor = color
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconText(icon = Icons.Rounded.Schedule, text = "${course.timeStart} - ${course.timeEnd}")
                            IconText(icon = Icons.Rounded.CalendarToday, text = course.days)
                        }

                        if (course.zoomAccount.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            IconText(icon = Icons.Rounded.VideoCameraFront, text = course.zoomAccount)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLang == "ar") "نسبة التقدم بالمقرر" else "Course Progress",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${course.completedCount} / ${course.targetCount} (${(progress * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            color = color,
                            trackColor = color.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconText(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        Text(text = text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


// ----------------- 5. CALENDAR SCREEN -----------------
@Composable
fun CalendarScreen(
    courses: List<Course>,
    modifier: Modifier = Modifier
) {
    val currentLang = com.example.screens.LocalAppLanguage.current

    var selectedTab by remember { mutableStateOf("month") } // "month", "week", "day", "agenda"
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    val daysInMonth = 30
    val startOffset = 2 // Simulated Tuesday start for June 2025

    val currentMonthYear = if (currentLang == "ar") "يونيو 2025 📅" else "June 2025 📅"

    // Days representation
    val weekdays = if (currentLang == "ar") {
        listOf("أح", "إث", "ثلا", "أر", "خم", "جم", "سب")
    } else {
        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    }

    // Custom calendar calculations: simulate some lectures on Sunday/Wednesday
    val selectedDayLectures = remember(selectedDay, courses) {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JUNE, selectedDay)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayNameAr = when (dayOfWeek) {
            Calendar.SUNDAY -> "الأحد"
            Calendar.MONDAY -> "الاثنين"
            Calendar.TUESDAY -> "الثلاثاء"
            Calendar.WEDNESDAY -> "الأربعاء"
            Calendar.THURSDAY -> "الخميس"
            Calendar.FRIDAY -> "الجمعة"
            Calendar.SATURDAY -> "السبت"
            else -> ""
        }
        courses.filter { it.days.contains(dayNameAr) && it.status == "نشط" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle view tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "month" to (if (currentLang == "ar") "شهر" else "Month"),
                "week" to (if (currentLang == "ar") "أسبوع" else "Week"),
                "day" to (if (currentLang == "ar") "يوم" else "Day"),
                "agenda" to (if (currentLang == "ar") "أجندة" else "Agenda")
            ).forEach { (tab, title) ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (selectedTab) {
            "month" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header Month
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {}) { Icon(imageVector = Icons.Rounded.ChevronLeft, contentDescription = "Prev") }
                                    Text(text = currentMonthYear, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    IconButton(onClick = {}) { Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = "Next") }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Weekdays headers
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    weekdays.forEach { day ->
                                        Text(
                                            text = day,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Month grid layout (5 rows x 7 days)
                                for (row in 0 until 5) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        for (col in 0 until 7) {
                                            val slotIndex = row * 7 + col
                                            val dayNum = slotIndex - startOffset + 1
                                            if (dayNum in 1..daysInMonth) {
                                                val isSelected = selectedDay == dayNum
                                                val isToday = dayNum == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                                                val hasEvents = dayNum % 3 == 0 // simulated indicator

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .padding(2.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.primary
                                                            else if (isToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                            else Color.Transparent
                                                        )
                                                        .clickable { selectedDay = dayNum },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = "$dayNum",
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                            else if (isToday) MaterialTheme.colorScheme.secondary
                                                            else MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (hasEvents && !isSelected) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(MaterialTheme.colorScheme.tertiary)
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Selected Day Events Title
                    item {
                        Text(
                            text = (if (currentLang == "ar") "لقاءات يوم " else "Events of Day ") + "$selectedDay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Render selected day items
                    if (selectedDayLectures.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (currentLang == "ar") "لا توجد محاضرات مجدولة لهذا اليوم 🍃" else "No scheduled lectures for today 🍃",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(selectedDayLectures) { course ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(course.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = course.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "${course.timeStart} - ${course.timeEnd}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    if (course.zoomAccount.isNotEmpty()) {
                                        IconButton(onClick = {}) {
                                            Icon(imageVector = Icons.Rounded.Videocam, contentDescription = "Zoom", tint = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "week" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val daysOfWeek = if (currentLang == "ar") {
                        listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
                    } else {
                        listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                    }

                    items(daysOfWeek) { dayName ->
                        val dayLectures = courses.filter { it.days.contains(dayName) && it.status == "نشط" }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (dayLectures.isEmpty()) {
                                    Text(
                                        text = if (currentLang == "ar") "لا محاضرات ☕" else "No lectures ☕",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    dayLectures.forEach { course ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val c = try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(c))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = course.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(text = "${course.timeStart} - ${course.timeEnd}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "day" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (currentLang == "ar") "عرض اليوم المفصل 🗓️" else "Detailed Day View 🗓️",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = (if (currentLang == "ar") "يوم " else "Day ") + "$selectedDay يونيو 2025",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Timeline Hours
                                val hours = listOf("08:00", "10:00", "12:00", "14:00", "16:00")
                                hours.forEachIndexed { index, hour ->
                                    val matchedLecture = selectedDayLectures.find { l -> l.timeStart.startsWith(hour.substring(0, 2)) }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = hour, fontSize = 11.sp, modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .height(2.dp)
                                                .weight(1f)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                        if (matchedLecture != null) {
                                            val col = try { Color(android.graphics.Color.parseColor(matchedLecture.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                            Card(
                                                modifier = Modifier.padding(start = 8.dp).widthIn(max = 160.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(containerColor = col.copy(alpha = 0.15f)),
                                                border = BorderStroke(1.dp, col)
                                            ) {
                                                Text(
                                                    text = matchedLecture.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = col,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = if (currentLang == "ar") "فراغ 🍃" else "Free 🍃",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "agenda" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val activeCourses = courses.filter { it.status == "نشط" }
                    if (activeCourses.isEmpty()) {
                        item {
                            Text(
                                text = if (currentLang == "ar") "لا توجد دورات نشطة لعرضها في الأجندة." else "No active courses to show in agenda.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(activeCourses) { course ->
                            val col = try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, col.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = course.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = col)
                                        Text(text = course.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${course.days} • ${course.timeStart} - ${course.timeEnd}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(if (currentLang == "ar") "لقاءات متبقية: ${course.targetCount - course.completedCount}" else "Lectures left: ${course.targetCount - course.completedCount}", fontSize = 10.sp) }
                                        )
                                        if (course.zoomAccount.isNotEmpty()) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(if (currentLang == "ar") "زوم نشط 🎥" else "Zoom Active 🎥", fontSize = 10.sp) }
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
    }
}


// ----------------- 7. ANALYTICS SCREEN -----------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    courses: List<Course>,
    modifier: Modifier = Modifier
) {
    val currentLang = com.example.screens.LocalAppLanguage.current

    var selectedTimeSpan by remember { mutableStateOf("Weekly") }

    val totalCount = courses.size
    val activeCount = courses.count { it.status == "نشط" }
    val totalLectures = courses.sumOf { it.targetCount }
    val completedLectures = courses.sumOf { it.completedCount }
    val completionRate = if (totalLectures > 0) (completedLectures.toFloat() / totalLectures * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Summary Cards Grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$totalCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = if (currentLang == "ar") "المقررات" else "Courses", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$completionRate%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = if (currentLang == "ar") "معدل الإنجاز" else "Completion %", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "28.5h", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = if (currentLang == "ar") "ساعات التعلم" else "Study Hours", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Section Chart Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(3.dp)
            ) {
                listOf("Weekly", "Monthly", "Overview").forEach { span ->
                    val isSel = selectedTimeSpan == span
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(30.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTimeSpan = span }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when(span) {
                                "Weekly" -> if (currentLang == "ar") "أسبوعي" else "Weekly"
                                "Monthly" -> if (currentLang == "ar") "شهري" else "Monthly"
                                else -> if (currentLang == "ar") "نظرة عامة" else "Overview"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Weekly Study Hours Bar Chart (Canvas Custom Drawing)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (currentLang == "ar") "ساعات الدراسة والمراجعة هذا الأسبوع" else "Study Hours Distribution This Week",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Draw background lines
                        val step = canvasHeight / 4
                        for (i in 0..4) {
                            val y = i * step
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.25f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Weekly study data (simulated hours: Sun=4h, Mon=2h, Tue=6h, Wed=5h, Thu=3h, Fri=0h, Sat=4h)
                        val data = listOf(4f, 2f, 6f, 5f, 3f, 1f, 4f)
                        val maxVal = 8f
                        val barCount = data.size
                        val barSpacing = canvasWidth / (barCount + 1)

                        data.forEachIndexed { idx, value ->
                            val barWidth = 16.dp.toPx()
                            val barHeight = (value / maxVal) * (canvasHeight - 20.dp.toPx())
                            val x = barSpacing * (idx + 1) - barWidth / 2
                            val y = canvasHeight - barHeight

                            // Draw Rounded Bar
                            drawRoundRect(
                                color = if (idx == 2) Color(0xFF10B981) else Color(0xFF2563EB),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }

                    // Weekdays labels row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = if (currentLang == "ar") {
                            listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
                        } else {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        }
                        days.forEach { d ->
                            Text(
                                text = d,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Course Distribution Doughnut Chart (Canvas Custom Drawing)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (currentLang == "ar") "توزيع الجهد الدراسي والمحاضرات" else "Subject Time & Effort Balance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // Draw Doughnut/Pie Arc
                            val strokeWidth = 14.dp.toPx()
                            val sizeOffset = strokeWidth / 2
                            val centerOffset = sizeOffset

                            // Category 1: Algorithms (45%)
                            drawArc(
                                color = Color(0xFF2563EB),
                                startAngle = 0f,
                                sweepAngle = 162f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = Offset(centerOffset, centerOffset),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth)
                            )

                            // Category 2: Physics (30%)
                            drawArc(
                                color = Color(0xFF10B981),
                                startAngle = 162f,
                                sweepAngle = 108f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = Offset(centerOffset, centerOffset),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth)
                            )

                            // Category 3: Accounting (25%)
                            drawArc(
                                color = Color(0xFFF59E0B),
                                startAngle = 270f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = Offset(centerOffset, centerOffset),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth)
                            )
                        }

                        // Legend Description
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                            LegendItem(color = Color(0xFF2563EB), text = if (currentLang == "ar") "خوارزميات (45%)" else "Algorithms (45%)")
                            LegendItem(color = Color(0xFF10B981), text = if (currentLang == "ar") "فيزياء العامة (30%)" else "Physics (30%)")
                            LegendItem(color = Color(0xFFF59E0B), text = if (currentLang == "ar") "محاسبة مالية (25%)" else "Accounting (25%)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}


// ----------------- 8. NOTES SCREEN -----------------
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = com.example.screens.LocalAppLanguage.current

    var searchQuery by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    // Dialog form state
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteCourse by remember { mutableStateOf("عام") }

    val filteredNotes = MockupDataStore.notesState.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.content.contains(searchQuery, ignoreCase = true) ||
        it.courseName.contains(searchQuery, ignoreCase = true)
    }

    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = {
                Text(
                    text = if (currentLang == "ar") "إضافة ملاحظة جديدة 📝" else "Add New Note 📝",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text(if (currentLang == "ar") "عنوان الملاحظة" else "Note Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text(if (currentLang == "ar") "محتوى الملاحظة" else "Content") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 6
                    )

                    OutlinedTextField(
                        value = noteCourse,
                        onValueChange = { noteCourse = it },
                        label = { Text(if (currentLang == "ar") "المادة الأكاديمية" else "Subject / Course") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isBlank() || noteContent.isBlank()) {
                            Toast.makeText(context, if (currentLang == "ar") "يرجى ملء جميع الحقول" else "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        MockupDataStore.notesState.add(
                            NoteItem(
                                id = (MockupDataStore.notesState.size + 1).toString(),
                                title = noteTitle,
                                content = noteContent,
                                courseName = noteCourse,
                                lastModified = "2025-06-30"
                            )
                        )
                        Toast.makeText(context, if (currentLang == "ar") "تمت الإضافة بنجاح" else "Note added successfully", Toast.LENGTH_SHORT).show()
                        showAddNoteDialog = false
                        noteTitle = ""
                        noteContent = ""
                        noteCourse = "عام"
                    }
                ) {
                    Text(if (currentLang == "ar") "إضافة" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text(if (currentLang == "ar") "إلغاء" else "Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Plus Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (currentLang == "ar") "البحث عن ملاحظة..." else "Search notes...") },
                leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Button(
                onClick = { showAddNoteDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(54.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            }
        }

        // Notes List representation
        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentLang == "ar") "لا توجد أي ملاحظات مطابقة!" else "No notes found!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                IconButton(
                                    onClick = {
                                        MockupDataStore.notesState.removeIf { it.id == note.id }
                                        Toast.makeText(context, if (currentLang == "ar") "تم حذف الملاحظة" else "Note deleted", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(note.courseName, fontSize = 9.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    border = null
                                )

                                Text(
                                    text = note.lastModified,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ----------------- 9. FILES SCREEN -----------------
@Composable
fun FilesScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = com.example.screens.LocalAppLanguage.current

    var searchQuery by remember { mutableStateOf("") }

    val filteredFiles = MockupDataStore.filesState.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.courseName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (currentLang == "ar") "البحث عن ملف..." else "Search files...") },
                leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Button(
                onClick = {
                    Toast.makeText(context, if (currentLang == "ar") "محاكاة: جاري اختيار ملف من وحدة التخزين" else "Simulating: Selecting file from local storage", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(54.dp)
            ) {
                Icon(imageVector = Icons.Rounded.UploadFile, contentDescription = null)
            }
        }

        if (filteredFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentLang == "ar") "لا توجد ملفات متوفرة!" else "No files found!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredFiles, key = { it.id }) { file ->
                    val fileIcon = when (file.fileType) {
                        "PDF" -> Icons.Rounded.PictureAsPdf
                        "PPTX" -> Icons.Rounded.Slideshow
                        "ZIP" -> Icons.Rounded.FolderZip
                        else -> Icons.Rounded.Article
                    }

                    val fileColor = when (file.fileType) {
                        "PDF" -> Color(0xFFEF4444)
                        "PPTX" -> Color(0xFFF59E0B)
                        "ZIP" -> Color(0xFF8B5CF6)
                        else -> Color(0xFF3B82F6)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(fileColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = fileIcon, contentDescription = null, tint = fileColor, modifier = Modifier.size(24.dp))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = file.size, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.Gray))
                                        Text(text = file.courseName, fontSize = 10.sp, color = fileColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    Toast.makeText(context, if (currentLang == "ar") "تم بدء تنزيل الملف مجازياً" else "Simulated: File download started", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(imageVector = Icons.Rounded.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = {
                                    Toast.makeText(context, if (currentLang == "ar") "مشاركة الملف" else "Share file", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(imageVector = Icons.Rounded.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ----------------- 10. EXAMS SCREEN -----------------
@Composable
fun ExamsScreen(
    modifier: Modifier = Modifier
) {
    val currentLang = com.example.screens.LocalAppLanguage.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = if (currentLang == "ar") "الاختبارات القادمة 📝" else "Upcoming Exams 📝",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (currentLang == "ar") "تتبع تواريخ وقاعات اختباراتك النصغية والنهائية" else "Track dates and rooms for your academic exams",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(MockupDataStore.examsState, key = { it.id }) { exam ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = exam.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = exam.title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            Text(
                                text = if (currentLang == "ar") "متبقي ${exam.daysLeft} أيام" else "${exam.daysLeft} days left",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Rounded.Event, contentDescription = null, size = 16.dp)
                            Text(text = exam.date, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null, size = 16.dp)
                            Text(text = exam.time, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Rounded.Room, contentDescription = null, size = 16.dp)
                            Text(text = exam.room, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Icon(imageVector: ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    )
}


// ----------------- 12. PROFILE SCREEN -----------------
@Composable
fun ProfileScreen(
    courses: List<Course>,
    modifier: Modifier = Modifier
) {
    val currentLang = com.example.screens.LocalAppLanguage.current

    val totalHours = 28.5
    val streakDays = 15

    val achievements = remember {
        listOf(
            AchievementItem("1", "بطل الحضور المتواصل 🏆", "أكملت حضور جميع محاضرات الأسبوع بنجاح", Icons.Rounded.CalendarMonth, true, 1f),
            AchievementItem("2", "المنظم الذكي 🏅", "قمت بجدولة أول دورتين باستخدام Gemini", Icons.Rounded.AutoAwesome, true, 1f),
            AchievementItem("3", "المخطط الملتزم ⚡", "أكملت 15 مهمة دراسية هذا الشهر", Icons.Rounded.AddTask, false, 0.65f),
            AchievementItem("4", "سيد التحليل الأكاديمي 📊", "استخدمت الحاسبة لتوزيع مهامك الدراسية", Icons.Rounded.Analytics, true, 1f)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header with Avatar
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 12.dp)) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎓", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Mohammed Scholar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "mohammed@aistudio.build", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Streak Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECE5)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "🔥", fontSize = 32.sp)
                    Column {
                        Text(
                            text = if (currentLang == "ar") "سلسلة الحضور المتواصل: $streakDays يومًا!" else "Attendance Streak: $streakDays Days!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (currentLang == "ar") "أنت تبلي بلاءً حسناً! واصل حضور محاضراتك بانتظام." else "Amazing consistency! Keep up the great work.",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        // Achievements List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentLang == "ar") "الإنجازات والأوسمة 🏅" else "Achievements & Badges 🏅",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(achievements, key = { it.id }) { ach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ach.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (ach.isUnlocked) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (ach.isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = ach.icon,
                            contentDescription = null,
                            tint = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ach.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (ach.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = ach.desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { ach.progress },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}


// ----------------- 4. MORE DIRECTORY TAB -----------------
@Composable
fun MoreTab(
    navController: NavController,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang = com.example.screens.LocalAppLanguage.current

    val catalogItems = remember {
        listOf(
            Triple(Screen.Courses.route, if (currentLang == "ar") "المقررات الدراسية" else "Courses", Icons.Rounded.School),
            Triple(Screen.Calendar.route, if (currentLang == "ar") "التقويم" else "Calendar", Icons.Rounded.CalendarMonth),
            Triple(Screen.Analytics.route, if (currentLang == "ar") "التحليلات" else "Analytics", Icons.Rounded.Analytics),
            Triple(Screen.Notes.route, if (currentLang == "ar") "الملاحظات" else "Notes", Icons.AutoMirrored.Rounded.Notes),
            Triple(Screen.Files.route, if (currentLang == "ar") "الملفات المرفقة" else "Files", Icons.Rounded.FolderOpen),
            Triple(Screen.Exams.route, if (currentLang == "ar") "الاختبارات" else "Exams", Icons.Rounded.Quiz),
            Triple(Screen.Profile.route, if (currentLang == "ar") "الملف الشخصي" else "Profile", Icons.Rounded.Person),
            Triple(Screen.SmartScheduler.route, if (currentLang == "ar") "مخطط المنهج الذكي" else "AI Planner", Icons.Rounded.AutoAwesome),
            Triple(Screen.Calculator.route, if (currentLang == "ar") "حاسبة اللقاءات" else "Study Calculator", Icons.Rounded.Calculate),
            Triple(Screen.StudyHub.route, if (currentLang == "ar") "🎓 مركز الدراسة" else "🎓 Study Hub", Icons.Rounded.TaskAlt)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = if (currentLang == "ar") "مكتشف الأقسام والميزات 🧭" else "App Feature Hub 🧭",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (currentLang == "ar") "جميع الأدوات والصفحات الأكاديمية تحت تصرفك" else "Browse all secondary screens and tools easily",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
            ) {
                items(catalogItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(item.first) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = item.third,
                                    contentDescription = item.second,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = item.second,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Quick Settings card trigger
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSettings() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentLang == "ar") "إعدادات التطبيق والمظهر" else "App Settings & Localization",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (currentLang == "ar") "تخصيص اللغات، الألوان الديناميكية، ونغمات الأجراس" else "Modify active sound files, themes, and translations",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
