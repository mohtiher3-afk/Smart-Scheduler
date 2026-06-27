package com.example.models

data class SessionInfo(
    val courseId: Int,
    val courseName: String,
    val dateString: String,      // e.g., "2026-06-22"
    val formattedDate: String,   // e.g., "22 يونيو 2026"
    val dayName: String,         // e.g., "الاثنين"
    val timeStart: String,       // e.g., "06:15 م"
    val alarmTimeMillis: Long,   // timestamp of lead minutes before the session
    val sessionTimeMillis: Long, // timestamp of exact session
    val zoomAccount: String,
    val reminderLeadMinutes: Int = 15
)
