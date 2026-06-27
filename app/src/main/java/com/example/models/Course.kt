package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val days: String,         // e.g. "الأحد، الثلاثاء"
    val timeStart: String,    // e.g. "06:15 م"
    val timeEnd: String,      // e.g. "10:00 م"
    val zoomAccount: String,  // e.g. "support.03@fin.com.sa"
    val status: String,        // e.g. "نشط"
    val completedCount: Int, // e.g. how many completed sessions/rounds so far
    val targetCount: Int,    // e.g. target total sessions/rounds
    val alarmHour: Int? = null,
    val alarmMinute: Int? = null,
    val isAlarmEnabled: Boolean = false,
    val completedLecturesCsv: String = "",
    val reminderLeadMinutes: Int = 15,
    val notes: String = "",
    val colorHex: String = "#2563EB",
    val lectureNotesJson: String = "",
    val lectureMeetingsJson: String = ""
) {
    fun getLectureMeetingDate(lectureIndex: Int): String {
        return try {
            if (lectureMeetingsJson.isEmpty()) ""
            else {
                val json = org.json.JSONObject(lectureMeetingsJson)
                val meeting = json.optJSONObject(lectureIndex.toString())
                meeting?.optString("date", "") ?: ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getLectureMeetingTime(lectureIndex: Int): String {
        return try {
            if (lectureMeetingsJson.isEmpty()) ""
            else {
                val json = org.json.JSONObject(lectureMeetingsJson)
                val meeting = json.optJSONObject(lectureIndex.toString())
                meeting?.optString("time", "") ?: ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getLectureMeetingZoom(lectureIndex: Int): String {
        return try {
            if (lectureMeetingsJson.isEmpty()) ""
            else {
                val json = org.json.JSONObject(lectureMeetingsJson)
                val meeting = json.optJSONObject(lectureIndex.toString())
                meeting?.optString("zoom", "") ?: ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun setLectureMeeting(lectureIndex: Int, date: String, time: String, zoom: String): Course {
        val json = try {
            if (lectureMeetingsJson.isEmpty()) org.json.JSONObject()
            else org.json.JSONObject(lectureMeetingsJson)
        } catch (e: Exception) {
            org.json.JSONObject()
        }
        val meeting = org.json.JSONObject()
        meeting.put("date", date)
        meeting.put("time", time)
        meeting.put("zoom", zoom)
        json.put(lectureIndex.toString(), meeting)
        return this.copy(lectureMeetingsJson = json.toString())
    }

    fun getLectureNote(lectureIndex: Int): String {
        return try {
            if (lectureNotesJson.isEmpty()) ""
            else {
                val json = org.json.JSONObject(lectureNotesJson)
                json.optString(lectureIndex.toString(), "")
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun setLectureNote(lectureIndex: Int, note: String): Course {
        val json = try {
            if (lectureNotesJson.isEmpty()) org.json.JSONObject()
            else org.json.JSONObject(lectureNotesJson)
        } catch (e: Exception) {
            org.json.JSONObject()
        }
        json.put(lectureIndex.toString(), note)
        return this.copy(lectureNotesJson = json.toString())
    }

    fun getCompletedLecturesSet(): Set<Int> {
        val rawSet = if (completedLecturesCsv.isEmpty()) {
            if (completedCount > 0) {
                (1..completedCount).toSet()
            } else {
                emptySet()
            }
        } else {
            completedLecturesCsv.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
        }
        return rawSet.filter { it in 1..targetCount }.toSet()
    }

    fun toggleLectureCompleted(lectureIndex: Int): Course {
        val currentSet = getCompletedLecturesSet().toMutableSet()
        if (currentSet.contains(lectureIndex)) {
            currentSet.remove(lectureIndex)
        } else {
            currentSet.add(lectureIndex)
        }
        val newCsv = currentSet.sorted().joinToString(",")
        val newCount = currentSet.size
        return this.copy(
            completedLecturesCsv = newCsv,
            completedCount = newCount
        )
    }
}
