package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val courseName = intent.getStringExtra("title") ?: intent.getStringExtra("COURSE_NAME") ?: "الدورة التدريبية"
        val zoomLink = intent.getStringExtra("zoom") ?: intent.getStringExtra("COURSE_ZOOM") ?: "sa.com.fin@..."
        val timeStr = intent.getStringExtra("message") ?: intent.getStringExtra("COURSE_TIME") ?: "موعد المحاضرة"
        
        // Play selected notification/alert sound
        try {
            val sharedPrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            val soundType = sharedPrefs.getString("alert_sound", "default") ?: "default"
            AlertSoundPlayer.playSound(context, soundType)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showNotification(context, courseName, zoomLink, timeStr)
    }

    private fun showNotification(context: Context, courseName: String, zoomLink: String, timeStr: String) {
        val channelId = "course_reminders_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Read app language from preferences (source of truth is theme_prefs)
        val sharedPrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val currentLang = sharedPrefs.getString("app_language", "ar") ?: "ar"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = if (currentLang == "ar") "تنبيهات مواعيد الدورات" else "Course Schedule Alerts"
            val channelDescription = if (currentLang == "ar") "تنبيهات للتذكير بمحاضرات Zoom وأوقات الدورات" else "Reminders for Zoom lectures and course times"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open app when clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            courseName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Polished dynamic content
        val title = if (currentLang == "ar") "⏰ حان وقت المحاضرة الآن: $courseName" else "⏰ Lecture Starting Now: $courseName"
        val content = if (currentLang == "ar") "التوقيت: $timeStr | الحساب/الرابط: $zoomLink" else "Time: $timeStr | Zoom/Link: $zoomLink"
        val subText = if (currentLang == "ar") "تذكير ذكي بالدورات" else "Smart Course Reminder"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System alarm icon
            .setContentTitle(title)
            .setContentText(content)
            .setSubText(subText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(courseName.hashCode(), notification)
    }
}
