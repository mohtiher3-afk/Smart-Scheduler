package com.example.services

import java.text.SimpleDateFormat
import java.util.*
import com.example.models.SessionResult

object SchedulerUtils {

    // Parses "yyyy-MM-dd" to Date
    fun parseDate(dateStr: String): Date? {
        return try {
            val sdfStr = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdfStr.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    // Calculates session list between start and end date for active days (0=Sunday ... 6=Saturday)
    fun calculateSessions(
        startDateStr: String,
        endDateStr: String,
        activeDays: List<Int>
    ): List<SessionResult> {
        val results = mutableListOf<SessionResult>()
        if (activeDays.isEmpty()) return results

        val start = parseDate(startDateStr) ?: return results
        val end = parseDate(endDateStr) ?: return results

        if (start.after(end)) return results

        val calendar = Calendar.getInstance()
        calendar.time = start

        // Target Date inclusive
        val endCalendar = Calendar.getInstance()
        endCalendar.time = end
        endCalendar.add(Calendar.DAY_OF_YEAR, 1) // Make it inclusive of end date

        val sdfOutput = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
        val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        while (calendar.before(endCalendar)) {
            // Calendar Sunday is 1, Monday is 2 ... Saturday is 7
            // Convert to 0=Sunday ... 6=Saturday
            val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
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

            if (activeDays.contains(mappedDayIdx)) {
                val dbStr = sdfDb.format(calendar.time)
                val outStr = sdfOutput.format(calendar.time)
                val dayName = getArabicDayName(mappedDayIdx)

                results.add(
                    SessionResult(
                        dateString = dbStr,
                        formattedDate = outStr,
                        dayNameArabic = dayName
                    )
                )
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return results
    }

    // Returns dynamic hour difference between two Arabic PM/AM strings
    // e.g. "06:15 م" to "10:00 م" => 3.75 hours
    fun calculateHoursDifference(startTime: String, endTime: String): Double {
        return try {
            val startMinutes = convertTimeToMinutes(startTime)
            val endMinutes = convertTimeToMinutes(endTime)
            var diff = endMinutes - startMinutes
            if (diff < 0) {
                diff += 24 * 60 // Wraps over midnight
            }
            diff.toDouble() / 60.0
        } catch (e: Exception) {
            3.75 // Default fallback
        }
    }

    private fun convertTimeToMinutes(timeStr: String): Int {
        val clean = timeStr.trim()
        val isPm = clean.contains("م") || clean.lowercase().contains("pm")
        val numbers = clean.replace(Regex("[^0-9:]"), "")
        val parts = numbers.split(":")
        var hours = parts[0].toInt()
        val minutes = parts[1].toInt()

        if (isPm && hours < 12) {
            hours += 12
        } else if (!isPm && hours == 12) {
            hours = 0
        }
        return hours * 60 + minutes
    }

    fun getArabicDayName(dayIdx: Int): String {
        return when (dayIdx) {
            0 -> "الأحد"
            1 -> "الاثنين"
            2 -> "الثلاثاء"
            3 -> "الأربعاء"
            4 -> "الخميس"
            5 -> "الجمعة"
            6 -> "السبت"
            else -> "مجهول"
        }
    }

    fun getMillisFromDateTime(dateStr: String, timeStr: String): Long {
        return try {
            val date = parseDate(dateStr) ?: return System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.time = date
            
            val clean = timeStr.trim()
            val isPm = clean.contains("م") || clean.lowercase().contains("pm")
            val numbers = clean.replace(Regex("[^0-9:]"), "")
            val parts = numbers.split(":")
            var hours = parts[0].toIntOrNull() ?: 12
            val minutes = parts[1].toIntOrNull() ?: 0
            
            if (isPm && hours < 12) {
                hours += 12
            } else if (!isPm && hours == 12) {
                hours = 0
            }
            
            calendar.set(Calendar.HOUR_OF_DAY, hours)
            calendar.set(Calendar.MINUTE, minutes)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            calendar.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun parseDaysStringToIndices(daysStr: String): List<Int> {
        val indices = mutableListOf<Int>()
        val clean = daysStr.replace("،", " ").replace(",", " ")
        if (clean.contains("الأحد") || clean.contains("الاحد")) indices.add(0)
        if (clean.contains("الاثنين")) indices.add(1)
        if (clean.contains("الثلاثاء")) indices.add(2)
        if (clean.contains("الأربعاء") || clean.contains("الاربعاء")) indices.add(3)
        if (clean.contains("الخميس")) indices.add(4)
        if (clean.contains("الجمعة")) indices.add(5)
        if (clean.contains("السبت")) indices.add(6)
        return indices
    }

    fun exportCourseToCalendar(context: android.content.Context, course: com.example.models.Course) {
        val dayIndices = parseDaysStringToIndices(course.days)
        if (dayIndices.isEmpty()) {
            android.widget.Toast.makeText(context, "الرجاء تحديد أيام البث للدورة أولاً", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val sessions = mutableListOf<SessionResult>()
        val calendar = Calendar.getInstance()
        val sdfOutput = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
        val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        var count = 0
        for (i in 0..365) {
            if (count >= course.targetCount) break

            val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
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
                val dbStr = sdfDb.format(calendar.time)
                val outStr = sdfOutput.format(calendar.time)
                val dayName = getArabicDayName(mappedDayIdx)

                sessions.add(
                    SessionResult(
                        dateString = dbStr,
                        formattedDate = outStr,
                        dayNameArabic = dayName
                    )
                )
                count++
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (sessions.isEmpty()) {
            android.widget.Toast.makeText(context, "لا يمكن حساب المواعيد ببيانات الدورة الحالية", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        exportAllToIcsFile(
            context = context,
            courseName = course.name,
            sessions = sessions,
            timeStart = course.timeStart,
            timeEnd = course.timeEnd,
            zoomAccount = course.zoomAccount
        )
    }

    fun exportSingleToCalendar(
        context: android.content.Context,
        courseName: String,
        dateStr: String,
        timeStart: String,
        timeEnd: String,
        zoomAccount: String
    ) {
        try {
            val startMillis = getMillisFromDateTime(dateStr, timeStart)
            val endMillis = getMillisFromDateTime(dateStr, timeEnd)

            val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                data = android.provider.CalendarContract.Events.CONTENT_URI
                putExtra(android.provider.CalendarContract.Events.TITLE, courseName)
                putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "محاضرة بث مباشر عبر Zoom:\nالحساب: $zoomAccount")
                putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                if (endMillis > startMillis) {
                    putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                } else {
                    putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, startMillis + (2 * 60 * 60 * 1000))
                }
                putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, zoomAccount)
                putExtra(android.provider.CalendarContract.Events.ACCESS_LEVEL, android.provider.CalendarContract.Events.ACCESS_PRIVATE)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "لم نتمكن من فتح تطبيق التقويم", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun exportAllToIcsFile(
        context: android.content.Context,
        courseName: String,
        sessions: List<com.example.models.SessionResult>,
        timeStart: String,
        timeEnd: String,
        zoomAccount: String
    ) {
        if (sessions.isEmpty()) {
            android.widget.Toast.makeText(context, "لا توجد محاضرات في كشف الحساب الحالي للبرمجة الزمنية لتصديرها", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val sb = StringBuilder()
            sb.append("BEGIN:VCALENDAR\n")
            sb.append("VERSION:2.0\n")
            sb.append("PRODID:-//Smart Scheduler//NONSGML v1.0//EN\n")
            sb.append("CALSCALE:GREGORIAN\n")
            sb.append("METHOD:PUBLISH\n")

            val sdfIcs = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
            val nowStr = sdfIcs.format(Date())

            for ((index, s) in sessions.withIndex()) {
                val startMillis = getMillisFromDateTime(s.dateString, timeStart)
                val endMillis = getMillisFromDateTime(s.dateString, timeEnd)

                val startDate = Date(startMillis)
                val endDate = Date(if (endMillis > startMillis) endMillis else (startMillis + 2 * 60 * 60 * 1000))

                sb.append("BEGIN:VEVENT\n")
                sb.append("UID:course_${s.dateString}_${index}@course_schedule_app.com\n")
                sb.append("DTSTAMP:${nowStr}\n")
                sb.append("DTSTART:${sdfIcs.format(startDate)}\n")
                sb.append("DTEND:${sdfIcs.format(endDate)}\n")
                sb.append("SUMMARY:${courseName}\n")
                sb.append("DESCRIPTION:محاضرة بث مباشر عبر Zoom\\nالحساب: ${zoomAccount.replace(",", "\\,")}\n")
                sb.append("LOCATION:${zoomAccount.replace(",", "\\,")}\n")
                sb.append("END:VEVENT\n")
            }
            sb.append("END:VCALENDAR\n")

            val cacheDir = java.io.File(context.cacheDir, "calendar")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = java.io.File(cacheDir, "${courseName.replace(" ", "_").replace("/", "_")}_schedule.ics")
            file.writeText(sb.toString())

            val authority = "${context.packageName}.fileprovider"
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/calendar"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "تصدير جدول محاضرات: $courseName")
                putExtra(android.content.Intent.EXTRA_TEXT, "تجد في المرفقات ملف جدول المحاضرات الكامل بصيغة ICS لدورة: $courseName لتقويم الهاتف.")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "تصدير ومشاركة جدول المحاضرات الكامل"))
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "حدث خطأ أثناء تصدير ملف التقويم: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun exportSingleToIcsFile(
        context: android.content.Context,
        courseName: String,
        lectureNumStr: String,
        dateStr: String,
        timeStart: String,
        timeEnd: String,
        zoomAccount: String
    ) {
        if (dateStr.isEmpty() || timeStart.isEmpty()) {
            android.widget.Toast.makeText(context, "لا يوجد موعد محدد لتصديره", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val sb = StringBuilder()
            sb.append("BEGIN:VCALENDAR\n")
            sb.append("VERSION:2.0\n")
            sb.append("PRODID:-//Smart Scheduler//NONSGML v1.0//EN\n")
            sb.append("CALSCALE:GREGORIAN\n")
            sb.append("METHOD:PUBLISH\n")

            val sdfIcs = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
            val nowStr = sdfIcs.format(Date())

            val startMillis = getMillisFromDateTime(dateStr, timeStart)
            val endMillis = getMillisFromDateTime(dateStr, timeEnd)

            val startDate = Date(startMillis)
            val endDate = Date(if (endMillis > startMillis) endMillis else (startMillis + 2 * 60 * 60 * 1000))

            val eventSummary = "$courseName - $lectureNumStr"
            sb.append("BEGIN:VEVENT\n")
            sb.append("UID:course_single_${dateStr}_${lectureNumStr.hashCode()}@course_schedule_app.com\n")
            sb.append("DTSTAMP:${nowStr}\n")
            sb.append("DTSTART:${sdfIcs.format(startDate)}\n")
            sb.append("DTEND:${sdfIcs.format(endDate)}\n")
            sb.append("SUMMARY:${eventSummary}\n")
            sb.append("DESCRIPTION:محاضرة بث مباشر عبر Zoom\\n$lectureNumStr\\nالحساب: ${zoomAccount.replace(",", "\\,")}\n")
            sb.append("LOCATION:${zoomAccount.replace(",", "\\,")}\n")
            sb.append("END:VEVENT\n")
            
            sb.append("END:VCALENDAR\n")

            val cacheDir = java.io.File(context.cacheDir, "calendar")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val safeName = eventSummary.replace(" ", "_").replace("/", "_")
            val file = java.io.File(cacheDir, "${safeName}_meeting.ics")
            file.writeText(sb.toString())

            val authority = "${context.packageName}.fileprovider"
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/calendar"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "تصدير لقاء: $eventSummary")
                putExtra(android.content.Intent.EXTRA_TEXT, "تجد في المرفقات ملف موعد اللقاء بصيغة ICS لتقويم الهاتف.")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "إضافة وتصدير اللقاء إلى التقويم"))
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "حدث خطأ أثناء تصدير ملف التقويم: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}
