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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "تنبيهات مواعيد الدورات"
            val channelDescription = "تنبيهات للتذكير بمحاضرات Zoom وأوقات الدورات"
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System alarm icon
            .setContentTitle("حان موعد المحاضرة!  $courseName")
            .setContentText("التوقيت: $timeStr | الحساب: $zoomLink")
            .setSubText("تذكير ذكي بالدورات")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(courseName.hashCode(), notification)
    }
}
