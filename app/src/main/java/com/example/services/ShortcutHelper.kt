package com.example.services

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.models.Course

object ShortcutHelper {
    private const val TAG = "ShortcutHelper"

    /**
     * Publishes dynamic shortcuts to the home screen for active courses.
     */
    fun updateDynamicShortcuts(context: Context, activeCourses: List<Course>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return
        }

        try {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
            
            // We can show up to 3 shortcuts
            val coursesToShow = activeCourses.filter { it.status == "نشط" }.take(3)
            
            val shortcuts = coursesToShow.mapIndexed { index, course ->
                // Create intent to open MainActivity with specific course parameter
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("shortcut_course_id", course.id.toLong())
                    putExtra("shortcut_course_name", course.name)
                }

                val label = if (course.name.length > 15) course.name.substring(0, 12) + "..." else course.name
                
                ShortcutInfo.Builder(context, "course_shortcut_${course.id}")
                    .setShortLabel(label)
                    .setLongLabel("فتح دورة: ${course.name}")
                    .setIcon(Icon.createWithResource(context, android.R.drawable.ic_menu_today))
                    .setIntent(intent)
                    .setRank(index)
                    .build()
            }

            shortcutManager.dynamicShortcuts = shortcuts
            Log.d(TAG, "Updated dynamic launcher shortcuts with ${shortcuts.size} active courses.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update dynamic shortcuts: ${e.message}", e)
        }
    }
}
