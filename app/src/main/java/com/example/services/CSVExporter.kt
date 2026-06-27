package com.example.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.models.Course
import com.example.models.ReminderEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CSVExporter {
    private const val TAG = "CSVExporter"

    private fun escapeCsvField(field: String): String {
        val escaped = field.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"") || escaped.contains("\r")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    fun exportScheduleToCSV(context: Context, courses: List<Course>, reminders: List<ReminderEntity>) {
        try {
            val sb = java.lang.StringBuilder()
            // Add UTF-8 BOM to make sure Excel opens Arabic characters correctly
            sb.append('\uFEFF')

            // Section 1: Courses
            sb.append("النوع (Type),اسم الدورة (Course Name),الأيام (Days),وقت البدء (Start Time),وقت الانتهاء (End Time),رابط أو حساب زووم (Zoom Account),حالة الدورة (Status),المحاضرات المكتملة (Completed Lectures),العدد المستهدف (Target Count),تنبيه قبل بالدقائق (Reminder Lead Minutes),ملاحظات (Notes)\n")
            for (course in courses) {
                sb.append("دورة,")
                sb.append(escapeCsvField(course.name)).append(",")
                sb.append(escapeCsvField(course.days)).append(",")
                sb.append(escapeCsvField(course.timeStart)).append(",")
                sb.append(escapeCsvField(course.timeEnd)).append(",")
                sb.append(escapeCsvField(course.zoomAccount)).append(",")
                sb.append(escapeCsvField(course.status)).append(",")
                sb.append(escapeCsvField(course.completedCount.toString())).append(",")
                sb.append(escapeCsvField(course.targetCount.toString())).append(",")
                sb.append(escapeCsvField(course.reminderLeadMinutes.toString())).append(",")
                sb.append(escapeCsvField(course.notes)).append("\n")
            }

            sb.append("\n\n")

            // Section 2: Meetings / Reminders
            sb.append("النوع (Type),اسم الدورة (Course Name),تاريخ اللقاء (Meeting Date),تنبيه مفعل (Alarm Enabled)\n")
            for (reminder in reminders) {
                sb.append("لقاء,")
                sb.append(escapeCsvField(reminder.courseName)).append(",")
                sb.append(escapeCsvField(reminder.sessionDate)).append(",")
                sb.append(escapeCsvField(if (reminder.isEnabled) "نعم" else "لا")).append("\n")
            }

            // Write to a temporary file in the cache directory
            val cacheDir = File(context.cacheDir, "csv_exports")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "جدول_المحاضرات_واللقاءات_$timestamp.csv"
            val file = File(cacheDir, fileName)
            file.writeText(sb.toString(), Charsets.UTF_8)

            // Share the file
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "تصدير جدول المحاضرات واللقاءات (CSV)")
                putExtra(Intent.EXTRA_TEXT, "تجد في المرفقات ملف CSV الذي يحتوي على جدول المحاضرات الكامل وتفاصيل اللقاءات الحالية للدراسة والمراجعة.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "تصدير جدول ومحاضرات CSV"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حدث خطأ أثناء تصدير ملف الـ CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
