package com.example.services

import android.content.Context
import android.util.Log
import com.example.models.Course
import com.example.models.ReminderEntity
import org.json.JSONArray
import org.json.JSONObject

object LocalStorageBackup {
    private const val TAG = "LocalStorageBackup"
    private const val PREFS_NAME = "course_schedule_local_storage"
    private const val KEY_COURSES = "courses_backup_json"
    private const val KEY_REMINDERS = "reminders_backup_json"

    fun saveCourses(context: Context, courses: List<Course>) {
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (course in courses) {
                val json = JSONObject()
                json.put("id", course.id)
                json.put("name", course.name)
                json.put("days", course.days)
                json.put("timeStart", course.timeStart)
                json.put("timeEnd", course.timeEnd)
                json.put("zoomAccount", course.zoomAccount)
                json.put("status", course.status)
                json.put("completedCount", course.completedCount)
                json.put("targetCount", course.targetCount)
                if (course.alarmHour != null) {
                    json.put("alarmHour", course.alarmHour)
                }
                if (course.alarmMinute != null) {
                    json.put("alarmMinute", course.alarmMinute)
                }
                json.put("isAlarmEnabled", course.isAlarmEnabled)
                json.put("completedLecturesCsv", course.completedLecturesCsv)
                json.put("reminderLeadMinutes", course.reminderLeadMinutes)
                json.put("notes", course.notes)
                json.put("colorHex", course.colorHex)
                json.put("lectureNotesJson", course.lectureNotesJson)
                json.put("lectureMeetingsJson", course.lectureMeetingsJson)
                json.put("category", course.category)
                jsonArray.put(json)
            }
            sharedPreferences.edit().putString(KEY_COURSES, jsonArray.toString()).apply()
            Log.d(TAG, "Courses successfully backed up to SharedPreferences (LocalStorage)")
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up courses to LocalStorage: ${e.message}", e)
        }
    }

    fun loadCourses(context: Context): List<Course> {
        val coursesList = mutableListOf<Course>()
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = sharedPreferences.getString(KEY_COURSES, null)
            if (!jsonStr.isNullOrEmpty()) {
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val course = Course(
                        id = json.optInt("id", 0),
                        name = json.optString("name", ""),
                        days = json.optString("days", ""),
                        timeStart = json.optString("timeStart", ""),
                        timeEnd = json.optString("timeEnd", ""),
                        zoomAccount = json.optString("zoomAccount", ""),
                        status = json.optString("status", "نشط"),
                        completedCount = json.optInt("completedCount", 0),
                        targetCount = json.optInt("targetCount", 12),
                        alarmHour = if (json.has("alarmHour") && !json.isNull("alarmHour")) json.getInt("alarmHour") else null,
                        alarmMinute = if (json.has("alarmMinute") && !json.isNull("alarmMinute")) json.getInt("alarmMinute") else null,
                        isAlarmEnabled = json.optBoolean("isAlarmEnabled", false),
                        completedLecturesCsv = json.optString("completedLecturesCsv", ""),
                        reminderLeadMinutes = json.optInt("reminderLeadMinutes", 15),
                        notes = json.optString("notes", ""),
                        colorHex = json.optString("colorHex", "#2563EB"),
                        lectureNotesJson = json.optString("lectureNotesJson", ""),
                        lectureMeetingsJson = json.optString("lectureMeetingsJson", ""),
                        category = json.optString("category", "عام")
                    )
                    coursesList.add(course)
                }
                Log.d(TAG, "Courses successfully loaded from SharedPreferences (LocalStorage). Total: ${coursesList.size}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading courses from LocalStorage: ${e.message}", e)
        }
        return coursesList
    }

    fun saveReminders(context: Context, reminders: List<ReminderEntity>) {
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (reminder in reminders) {
                val json = JSONObject()
                json.put("id", reminder.id)
                json.put("courseId", reminder.courseId)
                json.put("courseName", reminder.courseName)
                json.put("sessionDate", reminder.sessionDate)
                json.put("timeInMillis", reminder.timeInMillis)
                json.put("isEnabled", reminder.isEnabled)
                jsonArray.put(json)
            }
            sharedPreferences.edit().putString(KEY_REMINDERS, jsonArray.toString()).apply()
            Log.d(TAG, "Reminders successfully backed up to SharedPreferences (LocalStorage)")
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up reminders to LocalStorage: ${e.message}", e)
        }
    }

    fun loadReminders(context: Context): List<ReminderEntity> {
        val remindersList = mutableListOf<ReminderEntity>()
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = sharedPreferences.getString(KEY_REMINDERS, null)
            if (!jsonStr.isNullOrEmpty()) {
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val reminder = ReminderEntity(
                        id = json.optLong("id", 0L),
                        courseId = json.optLong("courseId", 0L),
                        courseName = json.optString("courseName", ""),
                        sessionDate = json.optString("sessionDate", ""),
                        timeInMillis = json.optLong("timeInMillis", 0L),
                        isEnabled = json.optBoolean("isEnabled", true)
                    )
                    remindersList.add(reminder)
                }
                Log.d(TAG, "Reminders successfully loaded from SharedPreferences (LocalStorage). Total: ${remindersList.size}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading reminders from LocalStorage: ${e.message}", e)
        }
        return remindersList
    }
}
