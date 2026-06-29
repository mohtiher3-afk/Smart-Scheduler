package com.example.screens

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray
import org.json.JSONObject

// Import models and services from Clean Architecture Packages
import com.example.models.Course
import com.example.models.ReminderEntity
import com.example.models.SessionResult
import com.example.models.SessionInfo
import com.example.models.InAppToastData
import com.example.services.CourseDatabase
import com.example.services.CourseRepository
import com.example.services.SchedulerUtils
import com.example.services.AlarmReceiver
import com.example.services.GeminiClient
import com.example.services.LocalStorageBackup
import com.example.services.CSVExporter
import com.example.ai.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CourseRepository

    val allCourses: StateFlow<List<Course>>
    val allReminders: StateFlow<List<ReminderEntity>>
    val allStudySessions: StateFlow<List<com.example.models.StudySession>>
    val allGoals: StateFlow<List<com.example.models.StudyGoal>>
    val allGrades: StateFlow<List<com.example.models.Grade>>

    // Calculated analytics
    val totalStudyMinutes: StateFlow<Long>
    val weeklyStudyStats: StateFlow<Map<Int, Long>> // DayOfWeek -> Minutes
    val currentGpa: StateFlow<Double>
    val productivityScore: StateFlow<Int>
    val upcomingLecturesAlerts: StateFlow<List<SessionInfo>>

    // Custom In-App Toast state
    private val _customInAppToast = MutableStateFlow<InAppToastData?>(null)
    val customInAppToast: StateFlow<InAppToastData?> = _customInAppToast.asStateFlow()

    private val shownAlertSessionKeys = mutableSetOf<String>()

    fun triggerCustomToast(toast: InAppToastData) {
        _customInAppToast.value = toast
    }

    fun dismissCustomToast() {
        _customInAppToast.value = null
    }

    // Calculator state
    private val _selectedCourseId = MutableStateFlow<Long>(-1L)
    val selectedCourseId: StateFlow<Long> = _selectedCourseId.asStateFlow()

    private val _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow("")
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val _calculatedSessions = MutableStateFlow<List<SessionResult>>(emptyList())
    val calculatedSessions: StateFlow<List<SessionResult>> = _calculatedSessions.asStateFlow()

    // Gemini AI states
    private val _aiInputText = MutableStateFlow("")
    val aiInputText: StateFlow<String> = _aiInputText.asStateFlow()

    private val _parsedCoursesPreview = MutableStateFlow<List<Course>>(emptyList())
    val parsedCoursesPreview: StateFlow<List<Course>> = _parsedCoursesPreview.asStateFlow()

    private val _parsedCoursePreview = MutableStateFlow<Course?>(null)
    val parsedCoursePreview: StateFlow<Course?> = _parsedCoursePreview.asStateFlow()

    private val _isAiParsing = MutableStateFlow(false)
    val isAiParsing: StateFlow<Boolean> = _isAiParsing.asStateFlow()

    private val _aiParsingError = MutableStateFlow<String?>(null)
    val aiParsingError: StateFlow<String?> = _aiParsingError.asStateFlow()

    private val _aiChatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiChatHistory: StateFlow<List<Pair<String, String>>> = _aiChatHistory.asStateFlow()

    private val _aiChatInput = MutableStateFlow("")
    val aiChatInput: StateFlow<String> = _aiChatInput.asStateFlow()

    private val _isAiChatLoading = MutableStateFlow(false)
    val isAiChatLoading: StateFlow<Boolean> = _isAiChatLoading.asStateFlow()

    // --- AI Copilot OS States ---
    private val _aiRecommendations = MutableStateFlow<List<AiRecommendation>>(emptyList())
    val aiRecommendations = _aiRecommendations.asStateFlow()

    private val _scheduleOptimization = MutableStateFlow<ScheduleOptimization?>(null)
    val scheduleOptimization = _scheduleOptimization.asStateFlow()

    private val _studyCoachFeedback = MutableStateFlow<StudyCoachFeedback?>(null)
    val studyCoachFeedback = _studyCoachFeedback.asStateFlow()

    private val _isAiUpdating = MutableStateFlow(false)
    val isAiUpdating = _isAiUpdating.asStateFlow()

    // Theme Preference State Flow
    private val sharedPrefs = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Language Preference State Flow
    private val _appLanguage = MutableStateFlow(sharedPrefs.getString("app_language", "ar") ?: "ar")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Dynamic Color Preference State Flow
    private val _dynamicColorEnabled = MutableStateFlow(sharedPrefs.getBoolean("dynamic_color", true))
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setDynamicColorEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("dynamic_color", enabled).apply()
        _dynamicColorEnabled.value = enabled
    }

    fun setThemeMode(mode: String) {
        sharedPrefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun toggleTheme() {
        val currentMode = _themeMode.value
        val isSystemDark = (getApplication<Application>().resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val isCurrentlyDark = when (currentMode) {
            "dark" -> true
            "light" -> false
            else -> isSystemDark
        }
        val nextMode = if (isCurrentlyDark) "light" else "dark"
        setThemeMode(nextMode)
    }

    fun setAppLanguage(lang: String) {
        sharedPrefs.edit().putString("app_language", lang).apply()
        _appLanguage.value = lang
    }

    fun toggleLanguage() {
        val nextLang = if (_appLanguage.value == "ar") "en" else "ar"
        setAppLanguage(nextLang)
    }

    // Alert Sound Preference State Flow
    private val _alertSound = MutableStateFlow(sharedPrefs.getString("alert_sound", "default") ?: "default")
    val alertSound: StateFlow<String> = _alertSound.asStateFlow()

    fun setAlertSound(sound: String) {
        sharedPrefs.edit().putString("alert_sound", sound).apply()
        _alertSound.value = sound
    }

    fun playAlertSoundPreview(context: Context, sound: String) {
        com.example.services.AlertSoundPlayer.playSound(context, sound)
    }

    fun stopAlertSoundPreview() {
        com.example.services.AlertSoundPlayer.stopSound()
    }

    // Splash state
    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    fun dismissSplash() {
        _showSplash.value = false
    }

    // Onboarding state
    private val _onboardingCompleted = MutableStateFlow(sharedPrefs.getBoolean("onboarding_completed", false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun completeOnboarding() {
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
        _onboardingCompleted.value = true
    }

    fun resetOnboarding() {
        sharedPrefs.edit().putBoolean("onboarding_completed", false).apply()
        _onboardingCompleted.value = false
    }

    // PIN Security lock state
    private val _pinLockEnabled = MutableStateFlow(sharedPrefs.getBoolean("pin_lock_enabled", false))
    val pinLockEnabled: StateFlow<Boolean> = _pinLockEnabled.asStateFlow()

    private val _userAuthenticated = MutableStateFlow(false)
    val userAuthenticated: StateFlow<Boolean> = _userAuthenticated.asStateFlow()

    private val _registeredPin = MutableStateFlow(sharedPrefs.getString("registered_pin", "1234") ?: "1234")
    val registeredPin: StateFlow<String> = _registeredPin.asStateFlow()

    fun enablePinLock(pin: String) {
        sharedPrefs.edit().putBoolean("pin_lock_enabled", true).putString("registered_pin", pin).apply()
        _pinLockEnabled.value = true
        _registeredPin.value = pin
    }

    fun disablePinLock() {
        sharedPrefs.edit().putBoolean("pin_lock_enabled", false).apply()
        _pinLockEnabled.value = false
    }

    fun authenticateUser(pin: String): Boolean {
        return if (pin == _registeredPin.value) {
            _userAuthenticated.value = true
            true
        } else {
            false
        }
    }

    fun logout() {
        _userAuthenticated.value = false
    }

    // Pomodoro Timer State
    private val _pomodoroIsRunning = MutableStateFlow(false)
    val pomodoroIsRunning: StateFlow<Boolean> = _pomodoroIsRunning.asStateFlow()

    private val _pomodoroRemainingTime = MutableStateFlow(25 * 60) // 25 min in seconds
    val pomodoroRemainingTime: StateFlow<Int> = _pomodoroRemainingTime.asStateFlow()

    private val _pomodoroTotalTime = MutableStateFlow(25 * 60)
    val pomodoroTotalTime: StateFlow<Int> = _pomodoroTotalTime.asStateFlow()

    private val _pomodoroIsFocus = MutableStateFlow(true)
    val pomodoroIsFocus: StateFlow<Boolean> = _pomodoroIsFocus.asStateFlow()

    private val _pomodoroCompletedCount = MutableStateFlow(sharedPrefs.getInt("pomodoro_completed_count", 0))
    val pomodoroCompletedCount: StateFlow<Int> = _pomodoroCompletedCount.asStateFlow()

    private val _pomodoroSelectedFocusMin = MutableStateFlow(sharedPrefs.getInt("pomodoro_focus_min", 25))
    val pomodoroSelectedFocusMin: StateFlow<Int> = _pomodoroSelectedFocusMin.asStateFlow()

    private val _pomodoroSelectedBreakMin = MutableStateFlow(sharedPrefs.getInt("pomodoro_break_min", 5))
    val pomodoroSelectedBreakMin: StateFlow<Int> = _pomodoroSelectedBreakMin.asStateFlow()

    private var pomodoroJob: kotlinx.coroutines.Job? = null

    fun setPomodoroDurations(focusMin: Int, breakMin: Int) {
        sharedPrefs.edit().putInt("pomodoro_focus_min", focusMin).putInt("pomodoro_break_min", breakMin).apply()
        _pomodoroSelectedFocusMin.value = focusMin
        _pomodoroSelectedBreakMin.value = breakMin
        resetPomodoro()
    }

    fun startPomodoro() {
        if (_pomodoroIsRunning.value) return
        _pomodoroIsRunning.value = true
        pomodoroJob = viewModelScope.launch {
            while (_pomodoroRemainingTime.value > 0) {
                kotlinx.coroutines.delay(1000)
                _pomodoroRemainingTime.value -= 1
            }
            // Completed!
            _pomodoroIsRunning.value = false
            if (_pomodoroIsFocus.value) {
                _pomodoroCompletedCount.value += 1
                sharedPrefs.edit().putInt("pomodoro_completed_count", _pomodoroCompletedCount.value).apply()
                _pomodoroIsFocus.value = false
                _pomodoroRemainingTime.value = _pomodoroSelectedBreakMin.value * 60
                _pomodoroTotalTime.value = _pomodoroSelectedBreakMin.value * 60
            } else {
                _pomodoroIsFocus.value = true
                _pomodoroRemainingTime.value = _pomodoroSelectedFocusMin.value * 60
                _pomodoroTotalTime.value = _pomodoroSelectedFocusMin.value * 60
            }
        }
    }

    fun pausePomodoro() {
        pomodoroJob?.cancel()
        _pomodoroIsRunning.value = false
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        _pomodoroIsRunning.value = false
        val mins = if (_pomodoroIsFocus.value) _pomodoroSelectedFocusMin.value else _pomodoroSelectedBreakMin.value
        _pomodoroRemainingTime.value = mins * 60
        _pomodoroTotalTime.value = mins * 60
    }

    fun togglePomodoroMode() {
        pomodoroJob?.cancel()
        _pomodoroIsRunning.value = false
        _pomodoroIsFocus.value = !_pomodoroIsFocus.value
        val mins = if (_pomodoroIsFocus.value) _pomodoroSelectedFocusMin.value else _pomodoroSelectedBreakMin.value
        _pomodoroRemainingTime.value = mins * 60
        _pomodoroTotalTime.value = mins * 60
    }

    // Habit classes
    data class Habit(
        val id: String,
        val name: String,
        val completedDates: Set<String>,
        val streak: Int
    )

    private val _habitsList = MutableStateFlow<List<Habit>>(emptyList())
    val habitsList: StateFlow<List<Habit>> = _habitsList.asStateFlow()

    // Study Hub Ultimate States
    data class StudySessionStats(
        val xpEarned: Int = 0,
        val pagesRead: Int = 0,
        val notesTaken: Int = 0,
        val flashcardsCreated: Int = 0,
        val focusScore: Int = 0,
        val startTime: Long = 0,
        val durationSeconds: Int = 0
    )

    private val _isStudyHubActive = MutableStateFlow(false)
    val isStudyHubActive: StateFlow<Boolean> = _isStudyHubActive.asStateFlow()

    private val _activeStudyCourse = MutableStateFlow<Course?>(null)
    val activeStudyCourse: StateFlow<Course?> = _activeStudyCourse.asStateFlow()

    private val _studySessionStats = MutableStateFlow(StudySessionStats())
    val studySessionStats: StateFlow<StudySessionStats> = _studySessionStats.asStateFlow()

    private val _studyHubNotes = MutableStateFlow("")
    val studyHubNotes: StateFlow<String> = _studyHubNotes.asStateFlow()

    private val _isStudyHubImmersive = MutableStateFlow(false)
    val isStudyHubImmersive: StateFlow<Boolean> = _isStudyHubImmersive.asStateFlow()

    fun startStudyHubSession(course: Course) {
        _activeStudyCourse.value = course
        _isStudyHubActive.value = true
        _studySessionStats.value = StudySessionStats(startTime = System.currentTimeMillis())
        _isStudyHubImmersive.value = true
        startPomodoro()
    }

    fun endStudyHubSession() {
        _isStudyHubActive.value = false
        _isStudyHubImmersive.value = false
        pausePomodoro()
        // Here we would typically save the session to the database
    }

    fun updateStudyNotes(notes: String) {
        _studyHubNotes.value = notes
        _studySessionStats.value = _studySessionStats.value.copy(
            notesTaken = _studySessionStats.value.notesTaken + 1
        )
    }

    // --- AI Copilot Methods ---
    fun refreshAiDashboard() {
        viewModelScope.launch {
            _isAiUpdating.value = true
            val recs = AiCopilotManager.getDashboardRecommendations(allCourses.value, appLanguage.value)
            _aiRecommendations.value = recs
            _isAiUpdating.value = false
        }
    }

    fun runScheduleOptimization() {
        viewModelScope.launch {
            _isAiUpdating.value = true
            val opt = AiCopilotManager.optimizeSchedule(allCourses.value, appLanguage.value)
            _scheduleOptimization.value = opt
            _isAiUpdating.value = false
        }
    }

    fun dismissOptimization() {
        _scheduleOptimization.value = null
    }

    fun updateStudyCoach() {
        viewModelScope.launch {
            val statsSummary = "Total Courses: ${allCourses.value.size}, Active: ${allCourses.value.count { it.status == "نشط" }}"
            val feedback = AiCopilotManager.getStudyCoachFeedback(statsSummary, appLanguage.value)
            _studyCoachFeedback.value = feedback
        }
    }

    fun incrementPagesRead() {
        _studySessionStats.value = _studySessionStats.value.copy(
            pagesRead = _studySessionStats.value.pagesRead + 1,
            xpEarned = _studySessionStats.value.xpEarned + 10
        )
    }

    fun toggleStudyHubImmersive() {
        _isStudyHubImmersive.value = !_isStudyHubImmersive.value
    }

    private fun loadHabits() {
        val habitsStr = sharedPrefs.getString("habits_list_json", "[]") ?: "[]"
        try {
            val arr = JSONArray(habitsStr)
            val list = mutableListOf<Habit>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val datesArr = obj.getJSONArray("completedDates")
                val dates = mutableSetOf<String>()
                for (j in 0 until datesArr.length()) {
                    dates.add(datesArr.getString(j))
                }
                list.add(
                    Habit(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        completedDates = dates,
                        streak = obj.optInt("streak", 0)
                    )
                )
            }
            _habitsList.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveHabits(list: List<Habit>) {
        try {
            val arr = JSONArray()
            list.forEach { habit ->
                val obj = JSONObject()
                obj.put("id", habit.id)
                obj.put("name", habit.name)
                val datesArr = JSONArray()
                habit.completedDates.forEach { datesArr.put(it) }
                obj.put("completedDates", datesArr)
                obj.put("streak", habit.streak)
                arr.put(obj)
            }
            sharedPrefs.edit().putString("habits_list_json", arr.toString()).apply()
            _habitsList.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addHabit(name: String) {
        val newList = _habitsList.value.toMutableList()
        newList.add(Habit(UUID.randomUUID().toString(), name, emptySet(), 0))
        saveHabits(newList)
    }

    fun toggleHabitDay(habitId: String, dateString: String) {
        val newList = _habitsList.value.map { habit ->
            if (habit.id == habitId) {
                val newDates = habit.completedDates.toMutableSet()
                if (newDates.contains(dateString)) {
                    newDates.remove(dateString)
                } else {
                    newDates.add(dateString)
                }
                
                // Calculate streak
                var currentStreak = 0
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val cal = Calendar.getInstance()
                
                // check backwards starting today
                while (true) {
                    val dateStr = sdf.format(cal.time)
                    if (newDates.contains(dateStr)) {
                        currentStreak++
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                    } else {
                        val todayStr = sdf.format(Date())
                        if (dateStr == todayStr) {
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                            val yesterdayStr = sdf.format(cal.time)
                            if (newDates.contains(yesterdayStr)) {
                                continue
                            }
                        }
                        break
                    }
                }
                habit.copy(completedDates = newDates, streak = currentStreak)
            } else {
                habit
            }
        }
        saveHabits(newList)
    }

    fun deleteHabit(habitId: String) {
        val newList = _habitsList.value.filter { habit -> habit.id != habitId }
        saveHabits(newList)
    }

    // Subtask classes
    data class Subtask(
        val id: String,
        val name: String,
        val isCompleted: Boolean
    )

    private val _subtasksList = MutableStateFlow<Map<Long, List<Subtask>>>(emptyMap())
    val subtasksList: StateFlow<Map<Long, List<Subtask>>> = _subtasksList.asStateFlow()

    private fun loadSubtasks() {
        val subtasksStr = sharedPrefs.getString("subtasks_list_json", "{}") ?: "{}"
        try {
            val obj = JSONObject(subtasksStr)
            val map = mutableMapOf<Long, List<Subtask>>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val keyStr = keys.next()
                val courseId = keyStr.toLong()
                val arr = obj.getJSONArray(keyStr)
                val list = mutableListOf<Subtask>()
                for (i in 0 until arr.length()) {
                    val subObj = arr.getJSONObject(i)
                    list.add(
                        Subtask(
                            id = subObj.getString("id"),
                            name = subObj.getString("name"),
                            isCompleted = subObj.getBoolean("isCompleted")
                        )
                    )
                }
                map[courseId] = list
            }
            _subtasksList.value = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveSubtasks(map: Map<Long, List<Subtask>>) {
        try {
            val obj = JSONObject()
            map.forEach { (courseId, list) ->
                val arr = JSONArray()
                list.forEach { subtask ->
                    val subObj = JSONObject()
                    subObj.put("id", subtask.id)
                    subObj.put("name", subtask.name)
                    subObj.put("isCompleted", subtask.isCompleted)
                    arr.put(subObj)
                }
                obj.put(courseId.toString(), arr)
            }
            sharedPrefs.edit().putString("subtasks_list_json", obj.toString()).apply()
            _subtasksList.value = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addSubtask(courseId: Long, name: String) {
        val map = _subtasksList.value.toMutableMap()
        val list = (map[courseId] ?: emptyList()).toMutableList()
        list.add(Subtask(UUID.randomUUID().toString(), name, false))
        map[courseId] = list
        saveSubtasks(map)
    }

    fun toggleSubtask(courseId: Long, subtaskId: String) {
        val map = _subtasksList.value.toMutableMap()
        val list = (map[courseId] ?: emptyList()).map { subtask ->
            if (subtask.id == subtaskId) {
                subtask.copy(isCompleted = !subtask.isCompleted)
            } else {
                subtask
            }
        }
        map[courseId] = list
        saveSubtasks(map)
    }

    fun deleteSubtask(courseId: Long, subtaskId: String) {
        val map = _subtasksList.value.toMutableMap()
        val list = (map[courseId] ?: emptyList()).filter { subtask -> subtask.id != subtaskId }
        map[courseId] = list
        saveSubtasks(map)
    }

    init {
        loadHabits()
        loadSubtasks()
        
        val courseDao = CourseDatabase.getDatabase(application).courseDao()
        repository = CourseRepository(courseDao)

        // Prepopulate if needed
        viewModelScope.launch {
            repository.ensurePrepopulated(application)
            
            // Phase 09: Prepopulate analytics data if empty
            val sessions = repository.allStudySessions.first()
            if (sessions.isEmpty()) {
                val cal = java.util.Calendar.getInstance()
                // Last 7 days mock sessions
                for (i in 0 until 7) {
                    val time = cal.timeInMillis - (i * 24 * 60 * 60 * 1000)
                    repository.insertStudySession(application, com.example.models.StudySession(
                        courseId = 1,
                        startTime = time,
                        durationMinutes = (30..180).random().toLong(),
                        focusScore = (80..100).random()
                    ))
                }
                
                repository.insertGoal(application, com.example.models.StudyGoal(
                    title = "إنهاء 20 ساعة دراسة هذا الأسبوع",
                    targetMinutes = 1200,
                    currentMinutes = 450,
                    deadline = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)
                ))

                repository.insertGrade(application, com.example.models.Grade(
                    courseId = 1,
                    score = 95.0,
                    maxScore = 100.0,
                    weight = 0.2,
                    category = "Exam"
                ))
                repository.insertGrade(application, com.example.models.Grade(
                    courseId = 2,
                    score = 88.0,
                    maxScore = 100.0,
                    weight = 0.3,
                    category = "Midterm"
                ))
            }
        }

        // Keep local tracks (decrypt zoom account details on the fly for the UI layer)
        allCourses = repository.allCourses
            .map { list ->
                val decryptedList = list.map { course ->
                    course.copy(zoomAccount = com.example.services.SecurityHelper.decryptZoomLink(course.zoomAccount))
                }
                // Automatically update Dynamic Shortcuts on launcher with latest active courses
                try {
                    com.example.services.ShortcutHelper.updateDynamicShortcuts(application, decryptedList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                decryptedList
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Initialize Cloud Sync and Remote Config
        viewModelScope.launch {
            try {
                com.example.services.CloudSyncManager.initLastSyncTime(application)
                com.example.services.RemoteConfigManager.fetchLatestConfig()
                com.example.services.DiagnosticLogger.log("INFO", "AppInit", "تم مزامنة إعدادات Remote Config وبدء التطبيق بسلاسة.")
            } catch (e: Exception) {
                com.example.services.DiagnosticLogger.log("ERROR", "AppInit", "خطأ أثناء تشغيل تهيئة الخدمات: ${e.message}")
            }
        }

        allReminders = repository.allReminders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allStudySessions = repository.allStudySessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allGoals = repository.allGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allGrades = repository.allGrades.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        totalStudyMinutes = allStudySessions.map { sessions ->
            sessions.sumOf { it.durationMinutes }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

        weeklyStudyStats = allStudySessions.map { sessions ->
            val now = System.currentTimeMillis()
            val weekStart = now - (7 * 24 * 60 * 60 * 1000)
            val stats = mutableMapOf<Int, Long>()
            // 1: Sunday, 7: Saturday
            (1..7).forEach { stats[it] = 0L }
            
            sessions.filter { it.startTime > weekStart }.forEach { session ->
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = session.startTime
                val day = cal.get(java.util.Calendar.DAY_OF_WEEK)
                stats[day] = (stats[day] ?: 0L) + session.durationMinutes
            }
            stats
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

        currentGpa = allGrades.map { grades ->
            if (grades.isEmpty()) 0.0
            else {
                // Simplified GPA calculation: average of (score/max * 4.0)
                val totalWeight = grades.sumOf { it.weight }
                if (totalWeight == 0.0) 0.0
                else {
                    val weightedSum = grades.sumOf { (it.score / it.maxScore) * 4.0 * it.weight }
                    weightedSum / totalWeight
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        productivityScore = allGoals.map { goals ->
            if (goals.isEmpty()) 100
            else {
                val completed = goals.count { it.isCompleted }
                (completed.toDouble() / goals.size * 100).toInt()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

        // Automatic Local Backup on changes
        viewModelScope.launch {
            allCourses.collect { courses ->
                if (courses.isNotEmpty()) {
                    LocalStorageBackup.saveCourses(application, courses)
                }
            }
        }

        viewModelScope.launch {
            allReminders.collect { reminders ->
                if (reminders.isNotEmpty()) {
                    LocalStorageBackup.saveReminders(application, reminders)
                }
            }
        }

        upcomingLecturesAlerts = allCourses
            .map { courses ->
                courses.mapNotNull { getNextUpcomingSession(it) }
                    .sortedBy { it.sessionTimeMillis }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Set default dates for calculator (Today to Today + 3 Months)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        _startDate.value = sdf.format(cal.time)
        cal.add(Calendar.MONTH, 3)
        _endDate.value = sdf.format(cal.time)

        // Launch background check loop for approaching meetings (every 10 seconds)
        viewModelScope.launch {
            while (true) {
                try {
                    checkApproachingMeetings()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                kotlinx.coroutines.delay(10000)
            }
        }
    }

    fun selectCourseForCalculator(courseId: Long) {
        _selectedCourseId.value = courseId
        recalculateSessions()
    }

    fun setStartDate(dateStr: String) {
        _startDate.value = dateStr
        recalculateSessions()
    }

    fun setEndDate(dateStr: String) {
        _endDate.value = dateStr
        recalculateSessions()
    }

    fun recalculateSessions() {
        val courseId = _selectedCourseId.value
        val start = _startDate.value
        val end = _endDate.value

        if (courseId == -1L || start.isEmpty() || end.isEmpty()) {
            _calculatedSessions.value = emptyList()
            return
        }

        val courses = allCourses.value
        val course = courses.find { it.id.toLong() == courseId }
        if (course == null) {
            _calculatedSessions.value = emptyList()
            return
        }

        // Parse days from Arabic string like "الأحد، الثلاثاء" to indices
        val dayIndices = parseDaysStringToIndices(course.days)

        val list = SchedulerUtils.calculateSessions(start, end, dayIndices)
        _calculatedSessions.value = list
    }

    // ---- Helper to convert Arabic day names to Indices ----
    private fun parseDaysStringToIndices(daysStr: String): List<Int> {
        val indices = mutableListOf<Int>()
        val clean = daysStr.replace("،", " ").replace(",", " ")
        if (clean.contains("الأحد") || clean.contains("الاحد")) indices.add(0)
        if (clean.contains("الاثنين") || clean.contains("الإثنين")) indices.add(1)
        if (clean.contains("الثلاثاء")) indices.add(2)
        if (clean.contains("الأربعاء") || clean.contains("الاربعاء")) indices.add(3)
        if (clean.contains("الخميس")) indices.add(4)
        if (clean.contains("الجمعة")) indices.add(5)
        if (clean.contains("السبت")) indices.add(6)
        return indices
    }

    private fun mapIndicesToArabicDays(indices: List<Int>): String {
        return indices.map { index ->
            when (index) {
                0 -> "الأحد"
                1 -> "الاثنين"
                2 -> "الثلاثاء"
                3 -> "الأربعاء"
                4 -> "الخميس"
                5 -> "الجمعة"
                6 -> "السبت"
                else -> ""
            }
        }.filter { it.isNotEmpty() }.joinToString("، ")
    }

    // ---- DB Actions ----
    fun addCourse(
        name: String,
        days: List<Int>,
        startTime: String,
        endTime: String,
        zoomAccount: String,
        targetCount: Int,
        isActive: Boolean,
        reminderLeadMinutes: Int = 15,
        colorHex: String = "#2563EB",
        category: String = "عام"
    ) {
        viewModelScope.launch {
            val daysStr = mapIndicesToArabicDays(days)
            // Securely Encrypt Zoom link before database write
            val encryptedZoom = com.example.services.SecurityHelper.encryptZoomLink(zoomAccount)
            val newCourse = Course(
                name = name,
                days = daysStr,
                timeStart = startTime,
                timeEnd = endTime,
                zoomAccount = encryptedZoom,
                status = if (isActive) "نشط" else "غير نشط",
                completedCount = 0,
                targetCount = targetCount,
                reminderLeadMinutes = reminderLeadMinutes,
                colorHex = colorHex,
                category = category
            )

            // Check for conflict overlaps
            val conflicts = com.example.services.ConflictDetector.findConflicts(newCourse, allCourses.value)
            if (conflicts.isNotEmpty()) {
                val firstConflict = conflicts[0]
                val conflictCourseName = firstConflict.secondCourse.name
                val conflictDays = firstConflict.conflictingDays.joinToString("، ")
                val conflictTime = firstConflict.timeSpan
                
                val isAr = _appLanguage.value == "ar"
                val warningTitle = if (isAr) {
                    "تنبيه تعارض: '${newCourse.name}' مع '$conflictCourseName'"
                } else {
                    "Conflict Warning: '${newCourse.name}' with '$conflictCourseName'"
                }
                
                val warningTimeStr = if (isAr) {
                    "تداخل في أيام ($conflictDays) في التوقيت ($conflictTime)"
                } else {
                    "Overlap on ($conflictDays) at ($conflictTime)"
                }

                com.example.services.DiagnosticLogger.log("WARN", "ConflictDetector", "وجد تعارض بين '${newCourse.name}' و '$conflictCourseName' في الأيام ${firstConflict.conflictingDays.joinToString()}.")
                triggerCustomToast(InAppToastData(
                    courseName = warningTitle,
                    zoomLink = "",
                    timeStr = warningTimeStr,
                    isTest = true
                ))
            } else {
                com.example.services.DiagnosticLogger.log("INFO", "DB", "تم إضافة الدورة الجديدة '${newCourse.name}' بنجاح.")
            }

            repository.insertCourse(getApplication(), newCourse)
            recalculateSessions()
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            // Securely Encrypt Zoom link before database write
            val encryptedCourse = course.copy(
                zoomAccount = com.example.services.SecurityHelper.encryptZoomLink(course.zoomAccount)
            )

            // Check for conflict overlaps
            val conflicts = com.example.services.ConflictDetector.findConflicts(course, allCourses.value)
            if (conflicts.isNotEmpty()) {
                val firstConflict = conflicts[0]
                val conflictCourseName = firstConflict.secondCourse.name
                val conflictDays = firstConflict.conflictingDays.joinToString("، ")
                val conflictTime = firstConflict.timeSpan
                
                val isAr = _appLanguage.value == "ar"
                val warningTitle = if (isAr) {
                    "تنبيه تعارض: '${course.name}' مع '$conflictCourseName'"
                } else {
                    "Conflict Warning: '${course.name}' with '$conflictCourseName'"
                }
                
                val warningTimeStr = if (isAr) {
                    "تداخل في أيام ($conflictDays) في التوقيت ($conflictTime)"
                } else {
                    "Overlap on ($conflictDays) at ($conflictTime)"
                }

                com.example.services.DiagnosticLogger.log("WARN", "ConflictDetector", "تعديل الدورة '${course.name}' أدى لتعارض مع '$conflictCourseName' في الأيام ${firstConflict.conflictingDays.joinToString()}.")
                triggerCustomToast(InAppToastData(
                    courseName = warningTitle,
                    zoomLink = "",
                    timeStr = warningTimeStr,
                    isTest = true
                ))
            } else {
                com.example.services.DiagnosticLogger.log("INFO", "DB", "تم تحديث الدورة '${course.name}' بنجاح.")
            }

            repository.updateCourse(getApplication(), encryptedCourse)
            recalculateSessions()
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            com.example.services.DiagnosticLogger.log("INFO", "DB", "تم حذف الدورة '${course.name}' بنجاح.")
            repository.deleteCourse(getApplication(), course)
            recalculateSessions()
        }
    }

    // ---- Alarms & Reminders ----
    fun triggerInstantTestAlarm(context: Context, courseName: String, zoom: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("title", "🔔 تنبيه تجريبي: $courseName")
                putExtra("message", "تبدأ محاضرتك الآن! انقر للانضمام عبر زووم.")
                putExtra("zoom", zoom)
                putExtra("courseId", 9999L)
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                pendingIntentFlags
            )

            val triggerTime = System.currentTimeMillis() + 5000 // In 5 seconds

            scheduleAlarmCompat(alarmManager, triggerTime, pendingIntent)

            Toast.makeText(context, "سيصلك التنبيه خلال 5 ثوانٍ!", Toast.LENGTH_SHORT).show()

            // Trigger beautiful custom in-app toast immediately!
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val testTimeStr = sdf.format(Date(triggerTime))
            _customInAppToast.value = InAppToastData(
                courseName = courseName,
                zoomLink = zoom,
                timeStr = testTimeStr,
                isTest = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "فشل في ضبط التنبيه التجريبي: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun toggleReminderForSession(
        context: Context,
        course: Course,
        sessionDate: String, // "yyyy-MM-dd"
        formattedDate: String
    ) {
        viewModelScope.launch {
            val existing = allReminders.value.find { 
                it.courseId == course.id.toLong() && it.sessionDate == sessionDate 
            }

            if (existing != null) {
                // Cancel existing reminder
                cancelSystemAlarm(context, existing)
                repository.deleteReminder(getApplication(), existing)
                Toast.makeText(context, "تم إلغاء التنبيه لـ $formattedDate", Toast.LENGTH_SHORT).show()
            } else {
                // Calculate target timeInMillis
                val targetTime = calculateAlarmTime(sessionDate, course.timeStart)
                if (targetTime < System.currentTimeMillis()) {
                    Toast.makeText(context, "تنبيه: هذا التاريخ في الماضي!", Toast.LENGTH_SHORT).show()
                }

                val reminder = ReminderEntity(
                    courseId = course.id.toLong(),
                    courseName = course.name,
                    sessionDate = sessionDate,
                    timeInMillis = targetTime,
                    isEnabled = true
                )

                val id = repository.insertReminder(getApplication(), reminder)
                val insertedReminder = reminder.copy(id = id)

                scheduleSystemAlarm(context, insertedReminder, course)
                Toast.makeText(context, "تم تفعيل تنبيه لمحاضرة $formattedDate", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            cancelSystemAlarm(getApplication(), reminder)
            repository.deleteReminder(getApplication(), reminder)
        }
    }

    // Analytics Helper Methods
    fun addStudySession(courseId: Int, durationMinutes: Long, focusScore: Int = 100) {
        viewModelScope.launch {
            repository.insertStudySession(
                getApplication(),
                com.example.models.StudySession(
                    courseId = courseId,
                    startTime = System.currentTimeMillis(),
                    durationMinutes = durationMinutes,
                    focusScore = focusScore
                )
            )
        }
    }

    fun addGoal(title: String, targetMinutes: Int, deadline: Long) {
        viewModelScope.launch {
            repository.insertGoal(
                getApplication(),
                com.example.models.StudyGoal(
                    title = title,
                    targetMinutes = targetMinutes,
                    deadline = deadline
                )
            )
        }
    }

    fun addGrade(courseId: Int, score: Double, maxScore: Double, weight: Double, category: String) {
        viewModelScope.launch {
            repository.insertGrade(
                getApplication(),
                com.example.models.Grade(
                    courseId = courseId,
                    score = score,
                    maxScore = maxScore,
                    weight = weight,
                    category = category
                )
            )
        }
    }

    fun clearAllReminders(context: Context) {
        viewModelScope.launch {
            allReminders.value.forEach { reminder ->
                cancelSystemAlarm(context, reminder)
                repository.deleteReminder(getApplication(), reminder)
            }
        }
    }

    private fun scheduleSystemAlarm(context: Context, reminder: ReminderEntity, course: Course) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("title", "⏳ موعد المحاضرة: ${course.name}")
                putExtra("message", "تبدأ محاضرتك الآن (${course.timeStart}). انقر للالتحاق بـ Zoom.")
                putExtra("zoom", course.zoomAccount)
                putExtra("courseId", reminder.id)
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.toInt(),
                intent,
                pendingIntentFlags
            )

            // Dynamic check for SCHEDULE_EXACT_ALARM on Android 12+
            scheduleAlarmCompat(alarmManager, reminder.timeInMillis, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelSystemAlarm(context: Context, reminder: ReminderEntity) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.toInt(),
                intent,
                pendingIntentFlags
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateAlarmTime(dateStr: String, startTimeStr: String): Long {
        // Parse dateStr (yyyy-MM-dd)
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdfDate.parse(dateStr) ?: return System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.time = date

        // Parse timeStr (e.g., "06:15 م" or "06:15 PM")
        try {
            val cleanTime = startTimeStr.trim()
            val isPm = cleanTime.contains("م") || cleanTime.lowercase().contains("pm")
            val numbersOnly = cleanTime.replace(Regex("[^0-9:]"), "")
            val parts = numbersOnly.split(":")
            var hours = parts[0].toInt()
            val minutes = parts[1].toInt()

            if (isPm && hours < 12) {
                hours += 12
            } else if (!isPm && hours == 12) {
                hours = 0
            }

            calendar.set(Calendar.HOUR_OF_DAY, hours)
            calendar.set(Calendar.MINUTE, minutes)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        } catch (e: Exception) {
            calendar.set(Calendar.HOUR_OF_DAY, 18)
            calendar.set(Calendar.MINUTE, 0)
        }

        return calendar.timeInMillis
    }

    fun getNextUpcomingSession(course: Course): SessionInfo? {
        if (course.status != "نشط") return null
        val dayIndices = parseDaysStringToIndices(course.days)
        if (dayIndices.isEmpty()) return null

        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfOutput = SimpleDateFormat("d MMMM yyyy", Locale("ar"))

        // We check up to 14 days in the future to find the next session
        for (i in 0..14) {
            val checkCal = Calendar.getInstance()
            checkCal.add(Calendar.DAY_OF_YEAR, i)
            
            val calendarDay = checkCal.get(Calendar.DAY_OF_WEEK)
            val mappedDayIdx = when (calendarDay) {
                Calendar.SUNDAY -> 0
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 0
            }

            if (dayIndices.contains(mappedDayIdx)) {
                // Parse timeStart for checkCal
                try {
                    val cleanTime = course.timeStart.trim()
                    val isPm = cleanTime.contains("م") || cleanTime.lowercase().contains("pm")
                    val numbersOnly = cleanTime.replace(Regex("[^0-9:]"), "")
                    val parts = numbersOnly.split(":")
                    if (parts.size >= 2) {
                        var hours = parts[0].toInt()
                        val minutes = parts[1].toInt()

                        if (isPm && hours < 12) {
                            hours += 12
                        } else if (!isPm && hours == 12) {
                            hours = 0
                        }

                        checkCal.set(Calendar.HOUR_OF_DAY, hours)
                        checkCal.set(Calendar.MINUTE, minutes)
                        checkCal.set(Calendar.SECOND, 0)
                        checkCal.set(Calendar.MILLISECOND, 0)

                        // If it's in the future
                        if (checkCal.timeInMillis > System.currentTimeMillis()) {
                            val sessionTime = checkCal.timeInMillis
                            val alarmTime = sessionTime - (course.reminderLeadMinutes * 60 * 1000) // dynamic minutes before
                            
                            val dbStr = sdfDate.format(checkCal.time)
                            val outStr = sdfOutput.format(checkCal.time)
                            val dayName = SchedulerUtils.getArabicDayName(mappedDayIdx)

                            return SessionInfo(
                                courseId = course.id,
                                courseName = course.name,
                                dateString = dbStr,
                                formattedDate = outStr,
                                dayName = dayName,
                                timeStart = course.timeStart,
                                alarmTimeMillis = alarmTime,
                                sessionTimeMillis = sessionTime,
                                zoomAccount = course.zoomAccount,
                                reminderLeadMinutes = course.reminderLeadMinutes
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    fun scheduleAlarmsForNextLectures(context: Context) {
        viewModelScope.launch {
            val alerts = upcomingLecturesAlerts.value
            for (alert in alerts) {
                if (alert.alarmTimeMillis > System.currentTimeMillis()) {
                    scheduleSystemAlarmForSession(context, alert)
                } else if (alert.sessionTimeMillis > System.currentTimeMillis()) {
                    // Under 15m remaining: Alert in 5 seconds
                    scheduleSystemAlarmForSession(context, alert.copy(alarmTimeMillis = System.currentTimeMillis() + 5000))
                }
            }
        }
    }

    /**
     * وظيفة برمجية مخصصة للتحقق من مواعيد اللقاءات القادمة للدورات النشطة
     * وجدولة إشعارات تنبيه دقيقة قبل بدء اللقاء بـ 15 دقيقة بالضبط.
     */
    fun checkAndSchedule15MinPreMeetingAlerts(context: Context) {
        viewModelScope.launch {
            val activeCourses = allCourses.value.filter { it.status == "نشط" }
            var scheduledCount = 0
            var alertNowCount = 0
            
            for (course in activeCourses) {
                val nextSession = getNextUpcomingSession(course) ?: continue
                
                // حساب وقت التنبيه بدقة قبل بدء اللقاء بـ 15 دقيقة
                val sessionTime = nextSession.sessionTimeMillis
                val fifteenMinutesMillis = 15L * 60 * 1000
                val alarmTime = sessionTime - fifteenMinutesMillis
                
                val currentTime = System.currentTimeMillis()
                
                if (alarmTime > currentTime) {
                    // إذا كان موعد التنبيه (15 دقيقة قبل اللقاء) لا يزال في المستقبل، نجدول المنبه بدقة
                    val updatedSession = nextSession.copy(
                        alarmTimeMillis = alarmTime,
                        reminderLeadMinutes = 15
                    )
                    scheduleSystemAlarmForSession(context, updatedSession)
                    scheduledCount++
                } else if (sessionTime > currentTime) {
                    // إذا كان اللقاء سيبدأ في غضون أقل من 15 دقيقة من الآن، نطلق تنبيهاً فورياً تنبيهاً للمستخدم
                    val instantSession = nextSession.copy(
                        alarmTimeMillis = currentTime + 3000, // يعقد بعد 3 ثوانٍ كتنبيه فوري
                        reminderLeadMinutes = 15
                    )
                    scheduleSystemAlarmForSession(context, instantSession)
                    alertNowCount++
                }
            }
            
            Log.d("AlarmsCheck", "تم فحص مواعيد المحاضرات: جدولة $scheduledCount منبهات (قبلها بـ 15 دقيقة) وإشعارات فورية لـ $alertNowCount لقاءات قريبة جداً.")
        }
    }

    private fun scheduleSystemAlarmForSession(context: Context, session: SessionInfo) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("title", "⏳ اقترب موعد المحاضرة: ${session.courseName}")
                putExtra("message", "تبدأ محاضرتك قريباً في ${session.timeStart}. انقر للانضمام عبر زووم.")
                putExtra("zoom", session.zoomAccount)
                putExtra("courseId", session.courseId.toLong() + 100000L) // Safe unique index offset
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                session.courseId + 20000, // Safe unique request code offset
                intent,
                pendingIntentFlags
            )

            scheduleAlarmCompat(alarmManager, session.alarmTimeMillis, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleAlarmCompat(alarmManager: AlarmManager, triggerTime: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    // ---- Gemini AI Actions ----
    fun setAiInputText(text: String) {
        _aiInputText.value = text
    }

    fun setAiChatInput(text: String) {
        _aiChatInput.value = text
    }

    fun clearParsedCoursePreview() {
        _parsedCoursesPreview.value = emptyList()
        _parsedCoursePreview.value = null
        _aiParsingError.value = null
    }

    fun clearChatHistory() {
        _aiChatHistory.value = emptyList()
    }

    fun updatePreviewCourse(index: Int, course: Course) {
        val current = _parsedCoursesPreview.value.toMutableList()
        if (index in current.indices) {
            current[index] = course
            _parsedCoursesPreview.value = current
            // Keep backwards-compatible field synchronized
            _parsedCoursePreview.value = current.firstOrNull()
        }
    }

    fun removePreviewCourse(index: Int) {
        val current = _parsedCoursesPreview.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _parsedCoursesPreview.value = current
            _parsedCoursePreview.value = current.firstOrNull()
        }
    }

    fun insertParsedCourseDirectly(course: Course) {
        viewModelScope.launch {
            // Securely Encrypt Zoom link before database write
            val encryptedZoom = com.example.services.SecurityHelper.encryptZoomLink(course.zoomAccount)
            val courseToSave = course.copy(zoomAccount = encryptedZoom)

            // Check for conflict overlaps
            val conflicts = com.example.services.ConflictDetector.findConflicts(courseToSave, allCourses.value)
            if (conflicts.isNotEmpty()) {
                com.example.services.DiagnosticLogger.log("WARN", "ConflictDetector", "وجد تعارض بين '${courseToSave.name}' و '${conflicts[0].secondCourse.name}'.")
            } else {
                com.example.services.DiagnosticLogger.log("INFO", "DB", "تم إضافة الدورة الجديدة '${courseToSave.name}' بنجاح.")
            }

            repository.insertCourse(getApplication(), courseToSave)
            recalculateSessions()
            
            // Remove from preview list
            val current = _parsedCoursesPreview.value.toMutableList()
            val foundIdx = current.indexOfFirst { it.name == course.name && it.timeStart == course.timeStart }
            if (foundIdx != -1) {
                current.removeAt(foundIdx)
            } else if (current.isNotEmpty()) {
                current.removeAt(0)
            }
            _parsedCoursesPreview.value = current
            _parsedCoursePreview.value = current.firstOrNull()

            if (current.isEmpty()) {
                _aiInputText.value = ""
            }
        }
    }

    fun saveAllPreviewCourses() {
        viewModelScope.launch {
            val list = _parsedCoursesPreview.value
            for (course in list) {
                val encryptedZoom = com.example.services.SecurityHelper.encryptZoomLink(course.zoomAccount)
                val courseToSave = course.copy(zoomAccount = encryptedZoom)
                
                // Check for conflicts
                val conflicts = com.example.services.ConflictDetector.findConflicts(courseToSave, allCourses.value)
                if (conflicts.isNotEmpty()) {
                    com.example.services.DiagnosticLogger.log("WARN", "ConflictDetector", "وجد تعارض بين '${courseToSave.name}' و '${conflicts[0].secondCourse.name}'.")
                }
                repository.insertCourse(getApplication(), courseToSave)
            }
            recalculateSessions()
            _parsedCoursesPreview.value = emptyList()
            _parsedCoursePreview.value = null
            _aiInputText.value = ""
            com.example.services.DiagnosticLogger.log("INFO", "DB", "تم حفظ جميع الدورات من المعاينة التفاعلية.")
        }
    }

    fun parseCourseWithAi(text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            _isAiParsing.value = true
            _parsedCoursesPreview.value = emptyList()
            _parsedCoursePreview.value = null
            _aiParsingError.value = null
            try {
                val schemaCourse = """
                {
                  "type": "OBJECT",
                  "properties": {
                    "courses": {
                      "type": "ARRAY",
                      "items": {
                        "type": "OBJECT",
                        "properties": {
                          "name": {
                            "type": "STRING",
                            "description": "Name of the course"
                          },
                          "days": {
                            "type": "ARRAY",
                            "items": {
                              "type": "INTEGER"
                            },
                            "description": "List of days: 0 for Sunday, 1 for Monday, 2 for Tuesday, 3 for Wednesday, 4 for Thursday, 5 for Friday, 6 for Saturday."
                          },
                          "startTime": {
                            "type": "STRING",
                            "description": "Start time, format HH:MM AM/PM or HH:MM Arabic like 06:15 م or 09:30 ص"
                          },
                          "endTime": {
                            "type": "STRING",
                            "description": "End time, format HH:MM AM/PM or HH:MM Arabic like 10:00 م or 11:30 ص"
                          },
                          "zoomAccount": {
                            "type": "STRING",
                            "description": "Zoom link if any, or empty string"
                          },
                          "targetCount": {
                            "type": "INTEGER",
                            "description": "Total number of lectures/sessions, e.g., 12. Default to 12 if not mentioned."
                          },
                          "isActive": {
                            "type": "BOOLEAN",
                            "description": "True if active, default true"
                          }
                        },
                        "required": ["name", "days", "startTime", "endTime"]
                      },
                      "description": "List of parsed courses found in the text (up to 3 courses)."
                    }
                  },
                  "required": ["courses"]
                }
                """.trimIndent()

                val systemInstruction = "Your job is to parse informal schedule messages into an array of course objects (up to 3 courses) matching the JSON schema. Map Arabic or English day names carefully to day indices where 0 is Sunday, 1 is Monday ... and 6 is Saturday. Ensure times are formatted cleanly, translating English AM/PM to ص/م (e.g. 6:15 PM remains 06:15 م, or 10:00 AM matches 10:00 ص)."

                val response = GeminiClient.generateContent(
                    prompt = text,
                    systemInstruction = systemInstruction,
                    responseJsonSchema = schemaCourse
                )

                if (response != null && !response.startsWith("Error:")) {
                    val json = JSONObject(response)
                    val jsonCourses = json.optJSONArray("courses") ?: org.json.JSONArray()
                    val previewList = mutableListOf<Course>()
                    
                    for (cIdx in 0 until jsonCourses.length()) {
                        val courseJson = jsonCourses.getJSONObject(cIdx)
                        val jsonDays = courseJson.optJSONArray("days")
                        val daysList = mutableListOf<Int>()
                        if (jsonDays != null) {
                            for (i in 0 until jsonDays.length()) {
                                daysList.add(jsonDays.getInt(i))
                            }
                        }

                        // Data Validation & Sanitization Layer
                        val rawName = courseJson.optString("name", "دورة جديدة بالذكاء الاصطناعي")
                        val sanitizedName = if (rawName.isBlank() || rawName.length < 2) "دورة جديدة بالذكاء الاصطناعي" else rawName
                        
                        val rawStart = courseJson.optString("startTime", "06:15 م")
                        val sanitizedStart = if (rawStart.isBlank()) "06:15 م" else rawStart
                        
                        val rawEnd = courseJson.optString("endTime", "10:00 م")
                        val sanitizedEnd = if (rawEnd.isBlank()) "10:00 م" else rawEnd
                        
                        val rawZoom = courseJson.optString("zoomAccount", "")
                        val sanitizedZoom = if (rawZoom.isNotEmpty() && !rawZoom.startsWith("http") && !rawZoom.contains("@")) {
                            "" // Sanitize non-URL non-email strings
                        } else rawZoom

                        // Construct a ready-to-insert course structure with Room database constraints
                        val preview = Course(
                            name = sanitizedName,
                            days = mapIndicesToArabicDays(daysList),
                            timeStart = sanitizedStart,
                            timeEnd = sanitizedEnd,
                            zoomAccount = sanitizedZoom,
                            status = if (courseJson.optBoolean("isActive", true)) "نشط" else "غير نشط",
                            completedCount = 0,
                            targetCount = courseJson.optInt("targetCount", 12)
                        )
                        previewList.add(preview)
                    }

                    _parsedCoursesPreview.value = previewList
                    _parsedCoursePreview.value = previewList.firstOrNull()
                    com.example.services.DiagnosticLogger.log("INFO", "AIParsing", "تم تحليل ${previewList.size} دورات وتنقيتها بنجاح.")
                } else {
                    _parsedCoursesPreview.value = emptyList()
                    _parsedCoursePreview.value = null
                    val errMsg = response ?: "فشل في تحليل البيانات باستخدام الذكاء الاصطناعي."
                    _aiParsingError.value = errMsg
                    com.example.services.DiagnosticLogger.log("ERROR", "AIParsing", "فشل الذكاء الاصطناعي: $errMsg")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _parsedCoursesPreview.value = emptyList()
                _parsedCoursePreview.value = null
                _aiParsingError.value = "حدث خطأ غير متوقع أثناء المعالجة: ${e.localizedMessage ?: e.message}"
            } finally {
                _isAiParsing.value = false
            }
        }
    }

    fun sendChatMessage(message: String) {
        val clean = message.trim()
        if (clean.isEmpty()) return

        val currentHistory = _aiChatHistory.value.toMutableList()
        currentHistory.add(Pair(clean, ""))
        _aiChatHistory.value = currentHistory
        _aiChatInput.value = ""
        _isAiChatLoading.value = true

        viewModelScope.launch {
            try {
                // Read current schedule information to provide personalized course study advises
                val scheduleContext = allCourses.value.joinToString("\n") { course ->
                    "- ${course.name}: أيام ${course.days} من ${course.timeStart} إلى ${course.timeEnd} (زووم: ${if (course.zoomAccount.isEmpty()) "لا يوجد" else course.zoomAccount})"
                }

                val systemPrompt = """
                    أنت مساعد أكاديمي وذكي جداً في تنظيم الجداول والخطط الدراسية وتنبيهات المحاضرات.
                    إليك قائمة بالدورات التدريبية المضافة حالياً في جدول المستخدم المباشر:
                    $scheduleContext
                    
                    يرجى تقديم التوجيهات والاستجابات بناءً على القواعد التالية:
                    1. الرد باللغة العربية الفصحى بأسلوب ودود، محفز، مشجع ومنظم للغاية.
                    2. إذا طلب خطة دراسية أو نصيحة، صمم خطة واقعية مستنداً إلى أيامه ومواعيده في الجدول الفعلي.
                    3. قم بالتركيز على أهمية الانضمام لزووم ومراجعة التسجيلات وابتكار استراتيجيات للالتزام والدراسة.
                    4. صمم ردودك باستخدام بطاقات وجداول ومنقّطات واضحة لتسهيل القراءة السريعة والمريحة بأناقة.
                """.trimIndent()

                val response = GeminiClient.generateContent(
                    prompt = clean,
                    systemInstruction = systemPrompt
                )

                val updatedHistory = _aiChatHistory.value.toMutableList()
                if (updatedHistory.isNotEmpty()) {
                    val lastIdx = updatedHistory.lastIndex
                    updatedHistory[lastIdx] = Pair(clean, response ?: "عذراً، لم أستطع معالجة الرد حالياً.")
                }
                _aiChatHistory.value = updatedHistory
            } catch (e: Exception) {
                e.printStackTrace()
                val updatedHistory = _aiChatHistory.value.toMutableList()
                if (updatedHistory.isNotEmpty()) {
                    val lastIdx = updatedHistory.lastIndex
                    updatedHistory[lastIdx] = Pair(clean, "عذراً، حدث خطأ أثناء الاتصال بـ Gemini: ${e.localizedMessage}")
                }
                _aiChatHistory.value = updatedHistory
            } finally {
                _isAiChatLoading.value = false
            }
        }
    }

    fun checkApproachingMeetings() {
        val now = System.currentTimeMillis()
        val currentAlerts = upcomingLecturesAlerts.value

        for (alert in currentAlerts) {
            val sessionTime = alert.sessionTimeMillis
            val diffMs = sessionTime - now
            val leadMs = alert.reminderLeadMinutes * 60 * 1000L

            // If starting in leadMinutes or less, and it is in the future
            // OR has just started within the last 5 minutes (300000 ms)
            val isApproaching = diffMs in 0L..leadMs
            val isJustStarted = diffMs in -300000L..0L

            if (isApproaching || isJustStarted) {
                val sessionKey = "${alert.courseId}_${alert.sessionTimeMillis}"
                if (!shownAlertSessionKeys.contains(sessionKey)) {
                    shownAlertSessionKeys.add(sessionKey)

                    val minLeft = (diffMs / (60 * 1000L)).coerceAtLeast(0L).toInt()
                    val timeString = if (minLeft > 0) {
                        if (appLanguage.value == "ar") "تبدأ بعد $minLeft دقيقة (${alert.timeStart})" else "Starts in $minLeft mins (${alert.timeStart})"
                    } else {
                        if (appLanguage.value == "ar") "تبدأ الآن! (${alert.timeStart})" else "Starts now! (${alert.timeStart})"
                    }

                    _customInAppToast.value = InAppToastData(
                        courseName = alert.courseName,
                        zoomLink = alert.zoomAccount,
                        timeStr = timeString,
                        isTest = false
                    )
                }
            }
        }

        // Check manual reminders/meetings registered in the reminders database
        val currentReminders = allReminders.value
        for (reminder in currentReminders) {
            if (!reminder.isEnabled) continue
            val sessionTime = reminder.timeInMillis
            val diffMs = sessionTime - now

            // Find lead minutes from corresponding course, default to 15
            val course = allCourses.value.find { it.id.toLong() == reminder.courseId }
            val leadMins = course?.reminderLeadMinutes ?: 15
            val leadMs = leadMins * 60 * 1000L

            val isApproaching = diffMs in 0L..leadMs
            val isJustStarted = diffMs in -300000L..0L

            if (isApproaching || isJustStarted) {
                val sessionKey = "reminder_${reminder.id}_${reminder.timeInMillis}"
                if (!shownAlertSessionKeys.contains(sessionKey)) {
                    shownAlertSessionKeys.add(sessionKey)

                    val minLeft = (diffMs / (60 * 1000L)).coerceAtLeast(0L).toInt()
                    val timeStart = course?.timeStart ?: "غير محدد"
                    val zoomAccount = course?.zoomAccount ?: "غير محدد"
                    val timeString = if (minLeft > 0) {
                        if (appLanguage.value == "ar") "تبدأ بعد $minLeft دقيقة ($timeStart)" else "Starts in $minLeft mins ($timeStart)"
                    } else {
                        if (appLanguage.value == "ar") "تبدأ الآن! ($timeStart)" else "Starts now! ($timeStart)"
                    }

                    _customInAppToast.value = InAppToastData(
                        courseName = reminder.courseName,
                        zoomLink = zoomAccount,
                        timeStr = timeString,
                        isTest = false
                    )
                }
            }
        }
    }

    fun exportAllDataToCSV(context: Context) {
        CSVExporter.exportScheduleToCSV(context, allCourses.value, allReminders.value)
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
