package com.example.services

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.models.Course
import java.text.SimpleDateFormat
import java.util.*

object CalendarProviderHelper {
    private const val TAG = "CalendarProviderHelper"

    /**
     * Syncs a course schedule to the system calendar.
     * Returns true if successfully added/scheduled.
     */
    fun syncCourseToCalendar(context: Context, course: Course): Boolean {
        // Check permission first
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CALENDAR) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "No write calendar permission granted.")
            return false
        }

        try {
            val cr = context.contentResolver
            
            // Get Primary Calendar ID (usually ID = 1 or find the first writable one)
            val calendarId = getPrimaryCalendarId(context) ?: 1L

            // Parse course start/end hours
            val startHoursMinutes = parseTime(course.timeStart)
            val endHoursMinutes = parseTime(course.timeEnd)

            val daysOfWeek = parseDaysToCalendarDays(course.days)

            // We schedule sessions for the next 4 weeks
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            
            var syncCount = 0

            for (i in 0..28) { // 4 weeks of sessions
                val checkCal = Calendar.getInstance()
                checkCal.add(Calendar.DAY_OF_YEAR, i)
                val dayOfWeek = checkCal.get(Calendar.DAY_OF_WEEK)

                if (daysOfWeek.contains(dayOfWeek)) {
                    // Set correct hours and minutes
                    checkCal.set(Calendar.HOUR_OF_DAY, startHoursMinutes.first)
                    checkCal.set(Calendar.MINUTE, startHoursMinutes.second)
                    checkCal.set(Calendar.SECOND, 0)
                    checkCal.set(Calendar.MILLISECOND, 0)

                    val startTimeMillis = checkCal.timeInMillis

                    val endCal = checkCal.clone() as Calendar
                    endCal.set(Calendar.HOUR_OF_DAY, endHoursMinutes.first)
                    endCal.set(Calendar.MINUTE, endHoursMinutes.second)
                    val endTimeMillis = endCal.timeInMillis

                    // Insert event
                    val values = ContentValues().apply {
                        put(CalendarContract.Events.DTSTART, startTimeMillis)
                        put(CalendarContract.Events.DTEND, endTimeMillis)
                        put(CalendarContract.Events.TITLE, "📚 دورة: ${course.name}")
                        put(CalendarContract.Events.DESCRIPTION, "محاضرة مجدولة عبر تطبيق Smart Scheduler.\nرابط البث: ${course.zoomAccount}")
                        put(CalendarContract.Events.CALENDAR_ID, calendarId)
                        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                        if (course.zoomAccount.isNotEmpty()) {
                            put(CalendarContract.Events.EVENT_LOCATION, course.zoomAccount)
                        }
                    }

                    val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
                    if (uri != null) {
                        syncCount++
                    }
                }
            }
            Log.d(TAG, "Synced $syncCount sessions to system calendar for course: ${course.name}")
            return syncCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to calendar: ${e.message}", e)
            return false
        }
    }

    private fun getPrimaryCalendarId(context: Context): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
        try {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val idCol = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                    val primaryCol = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                    
                    val id = cursor.getLong(idCol)
                    val isPrimary = if (primaryCol >= 0) cursor.getInt(primaryCol) == 1 else false
                    
                    if (isPrimary) return id
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying calendars", e)
        }
        return null
    }

    private fun parseTime(timeStr: String): Pair<Int, Int> {
        try {
            val clean = timeStr.trim()
            val isPm = clean.contains("م") || clean.lowercase().contains("pm")
            val numbersOnly = clean.replace(Regex("[^0-9:]"), "")
            val parts = numbersOnly.split(":")
            var hours = parts[0].toInt()
            val minutes = parts[1].toInt()

            if (isPm && hours < 12) hours += 12
            else if (!isPm && hours == 12) hours = 0

            return Pair(hours, minutes)
        } catch (e: Exception) {
            return Pair(18, 0) // Default to 6:00 PM
        }
    }

    private fun parseDaysToCalendarDays(daysStr: String): List<Int> {
        val list = mutableListOf<Int>()
        val clean = daysStr.replace("،", " ").replace(",", " ")
        if (clean.contains("الأحد") || clean.contains("الاحد")) list.add(Calendar.SUNDAY)
        if (clean.contains("الاثنين") || clean.contains("الاثنين")) list.add(Calendar.MONDAY)
        if (clean.contains("الثلاثاء")) list.add(Calendar.TUESDAY)
        if (clean.contains("الأربعاء") || clean.contains("الاربعاء")) list.add(Calendar.WEDNESDAY)
        if (clean.contains("الخميس")) list.add(Calendar.THURSDAY)
        if (clean.contains("الجمعة")) list.add(Calendar.FRIDAY)
        if (clean.contains("السبت")) list.add(Calendar.SATURDAY)
        return list
    }
}
