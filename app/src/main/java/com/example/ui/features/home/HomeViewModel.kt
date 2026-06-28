package com.example.ui.features.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.Course
import com.example.models.SessionInfo
import com.example.services.CourseDatabase
import com.example.services.CourseRepository
import com.example.services.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "HomeViewModel"
    private val repository = CourseRepository(CourseDatabase.getDatabase(application).courseDao())

    private val _isRefreshingSuggestions = MutableStateFlow(false)
    val isRefreshingSuggestions = _isRefreshingSuggestions.asStateFlow()

    private val _aiSuggestions = MutableStateFlow<List<String>>(emptyList())

    // Convert time strings (e.g., "10:30 AM", "02:15 م") into absolute minutes
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
            Log.e(TAG, "Error parsing time: $timeStr", e)
        }
        return 0
    }

    // Map active database courses to dynamic dashboard statistics & suggestions
    val dashboardState: StateFlow<DashboardState> = combine(
        repository.allCourses,
        _aiSuggestions,
        _isRefreshingSuggestions
    ) { coursesList, aiSuggestions, refreshing ->
        if (coursesList.isEmpty()) {
            return@combine DashboardState(
                isLoading = refreshing,
                aiSuggestionsList = listOf(
                    "أضف دورتك الأولى للبدء في تتبع محاضراتك وجدولك الدراسي!",
                    "يمكنك استخدام المساعد الذكي لقراءة رابط زووم وجدولك تلقائياً.",
                    "استخدم الآلة الحاسبة المدمجة لتوزيع محاضراتك بالتساوي عبر الأسابيع."
                )
            )
        }

        val totalCourses = coursesList.size
        val activeCourses = coursesList.filter { it.status == "نشط" || it.status == "Active" }.size
        
        val totalLectures = coursesList.sumOf { it.targetCount }
        val completedLectures = coursesList.sumOf { it.completedCount }
        
        // Compute total hours of classes attended
        val totalMinutes = coursesList.sumOf { course ->
            val startMin = parseTimeToMinutes(course.timeStart)
            val endMin = parseTimeToMinutes(course.timeEnd)
            var duration = endMin - startMin
            if (duration <= 0) duration = 90 // Fallback: 1.5 hours per lecture
            course.completedCount * duration
        }
        val totalHoursValue = String.format(Locale.US, "%.1f", totalMinutes / 60.0).toDoubleOrNull() ?: 0.0

        val progressPct = if (totalLectures > 0) completedLectures.toFloat() / totalLectures else 0f

        // Get next course today or in the week
        val currentDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 // Sunday=0
        val nextCourse = coursesList.firstOrNull { course ->
            // Try to find course active today
            course.status == "نشط" && course.days.contains(getDayNameAr(currentDayIndex))
        } ?: coursesList.firstOrNull { it.status == "نشط" }

        val nextLectName = nextCourse?.name ?: "No lectures scheduled"
        val nextLectTime = nextCourse?.let { "${it.timeStart} - ${it.timeEnd}" } ?: "--:--"

        // If suggestions are empty, generate initial local rule-based ones
        val finalSuggestions = aiSuggestions.ifEmpty {
            generateRuleBasedSuggestions(coursesList, completedLectures, totalLectures)
        }

        DashboardState(
            userName = "Scholar",
            activeCoursesCount = activeCourses,
            completedLecturesCount = completedLectures,
            totalLecturesCount = totalLectures,
            totalHours = totalHoursValue,
            nextLectureName = nextLectName,
            nextLectureTime = nextLectTime,
            progressPercentage = progressPct,
            aiSuggestionsList = finalSuggestions,
            isLoading = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState(isLoading = true)
    )

    init {
        // Trigger automated suggestions fetch on start
        refreshSuggestions()
    }

    fun refreshSuggestions() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshingSuggestions.value = true
            try {
                val courses = repository.allCourses.firstOrNull() ?: emptyList()
                if (courses.isEmpty()) {
                    _aiSuggestions.value = listOf(
                        "أضف دورتك الأولى للبدء في تتبع محاضراتك وجدولك الدراسي!",
                        "يمكنك استخدام المساعد الذكي لقراءة رابط زووم وجدولك تلقائياً.",
                        "استخدم الآلة الحاسبة المدمجة لتوزيع محاضراتك بالتساوي عبر الأسابيع."
                    )
                    return@launch
                }

                // Call Gemini to generate highly intelligent advice
                val coursesJson = JSONArray().apply {
                    courses.forEach { c ->
                        put(org.json.JSONObject().apply {
                            put("name", c.name)
                            put("days", c.days)
                            put("time", "${c.timeStart}-${c.timeEnd}")
                            put("completed", c.completedCount)
                            put("target", c.targetCount)
                            put("category", c.category)
                        })
                    }
                }

                val prompt = """
                    You are an expert academic advisor and study scheduler. Analyze the student's current weekly schedule and course list, and generate exactly 3 short, specific, highly actionable tips or study suggestions in Arabic (with standard Arabic phrasing). Do not include any formatting like Markdown list markers, asterisks, or numbers. Put each suggestion on a new line.
                    
                    Courses list:
                    $coursesJson
                """.trimIndent()

                val systemInstruction = "You generate short academic study scheduling suggestions in Arabic. Each tip must be 1 sentence, actionable, and friendly."

                val response = GeminiClient.generateContent(prompt, systemInstruction)
                if (response != null && !response.startsWith("Error:")) {
                    val lines = response.lines()
                        .map { it.replace("[*•#\\-\\d.]".toRegex(), "").trim() }
                        .filter { it.length > 5 }
                        .take(3)
                    if (lines.isNotEmpty()) {
                        _aiSuggestions.value = lines
                        return@launch
                    }
                }

                // Fallback to rules-based if call failed or api key is blank
                _aiSuggestions.value = generateRuleBasedSuggestions(courses, courses.sumOf { it.completedCount }, courses.sumOf { it.targetCount })

            } catch (e: Exception) {
                Log.e(TAG, "Error generating AI suggestions", e)
            } finally {
                _isRefreshingSuggestions.value = false
            }
        }
    }

    private fun generateRuleBasedSuggestions(
        courses: List<Course>,
        completed: Int,
        total: Int
    ): List<String> {
        val list = mutableListOf<String>()
        val active = courses.filter { it.status == "نشط" || it.status == "Active" }
        
        if (active.isEmpty()) {
            list.add("تنبيه: جميع دوراتك المسجلة حالياً غير نشطة. يرجى تنشيط الدورات لمتابعة الحضور.")
        } else {
            val mostBehind = active.minByOrNull {
                if (it.targetCount > 0) it.completedCount.toFloat() / it.targetCount else 1.0f
            }
            if (mostBehind != null && mostBehind.completedCount < mostBehind.targetCount) {
                list.add("تبدو الدورة \"${mostBehind.name}\" بحاجة إلى مزيد من التركيز لرفع معدل الإنجاز الحالي.")
            } else {
                list.add("رائع! جدولك الدراسي يسير بخطى ممتازة ومتوازنة.")
            }
        }

        val totalProgress = if (total > 0) completed.toFloat() / total else 0f
        if (totalProgress > 0.8f) {
            list.add("أنت على وشك إكمال جميع محاضرات هذا الفصل الدراسي! واصل الجهد المتميز.")
        } else if (totalProgress > 0.4f) {
            list.add("لقد تخطيت حاجز 40% من محاضراتك. خطوة ممتازة نحو النجاح!")
        } else {
            list.add("ابدأ بتخصيص فترات مراجعة قصيرة يومياً لضمان ثبات المعلومات قبل المحاضرات القادمة.")
        }

        val hasZoom = courses.any { it.zoomAccount.isNotBlank() }
        if (!hasZoom) {
            list.add("نصيحة: أضف حساب زووم أو روابط البث المباشر لدوراتك لتسهيل الانضمام بضغطة زر واحدة.")
        } else {
            list.add("تأكد من ضبط نغمة التنبيه المناسبة وتفعيل الإشعارات قبل 15 دقيقة من موعد المحاضرة.")
        }

        return list
    }

    private fun getDayNameAr(dayIndex: Int): String {
        return when (dayIndex) {
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
}
