package com.example.services

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.example.models.Course
import com.example.models.ReminderEntity

class CourseRepository(private val courseDao: CourseDao) {
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()

    suspend fun getCourseById(id: Int): Course? = courseDao.getCourseById(id)

    suspend fun insertCourse(course: Course): Long = courseDao.insertCourse(course)

    suspend fun updateCourse(course: Course) = courseDao.updateCourse(course)

    suspend fun deleteCourse(course: Course) {
        courseDao.deleteRemindersByCourse(course.id.toLong())
        courseDao.deleteCourse(course)
    }

    val allReminders: Flow<List<ReminderEntity>> = courseDao.getAllReminders()

    suspend fun insertReminder(reminder: ReminderEntity): Long = courseDao.insertReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderEntity) = courseDao.deleteReminder(reminder)

    suspend fun ensurePrepopulated(context: Context) {
        val currentList = allCourses.first()
        if (currentList.isEmpty()) {
            // First try to load from LocalStorageBackup
            val backedUpCourses = LocalStorageBackup.loadCourses(context)
            if (backedUpCourses.isNotEmpty()) {
                for (course in backedUpCourses) {
                    courseDao.insertCourse(course)
                }
                
                // Also restore backed up reminders
                val backedUpReminders = LocalStorageBackup.loadReminders(context)
                for (reminder in backedUpReminders) {
                    courseDao.insertReminder(reminder)
                }
                return
            }

            val defaultCourses = listOf(
                Course(
                    name = "CMA PART 1 - May 12",
                    days = "الأحد، الثلاثاء",
                    timeStart = "06:15 م",
                    timeEnd = "10:00 م",
                    zoomAccount = "support.03@fin.com.sa",
                    status = "نشط",
                    completedCount = 5,
                    targetCount = 15,
                    colorHex = "#2563EB" // Navy Blue
                ),
                Course(
                    name = "CMA PART 1 - December 16",
                    days = "الأحد، الثلاثاء",
                    timeStart = "06:15 م",
                    timeEnd = "10:00 م",
                    zoomAccount = "support.04@fin.com.sa",
                    status = "نشط",
                    completedCount = 2,
                    targetCount = 15,
                    colorHex = "#7C3AED" // Violet/Purple
                ),
                Course(
                    name = "CMA PART 1 - February",
                    days = "الاثنين، الأربعاء",
                    timeStart = "06:00 م",
                    timeEnd = "10:00 م",
                    zoomAccount = "support.03@fin.com.sa",
                    status = "غير نشط",
                    completedCount = 0,
                    targetCount = 15,
                    colorHex = "#0D9488" // Teal
                ),
                Course(
                    name = "مدير الخزانة المعتمد (5️⃣ CTP)",
                    days = "الأحد، الثلاثاء",
                    timeStart = "06:25 م",
                    timeEnd = "10:00 م",
                    zoomAccount = "support.02@fin.com.sa",
                    status = "نشط",
                    completedCount = 8,
                    targetCount = 12,
                    colorHex = "#DC2626" // Crimson Red
                ),
                Course(
                    name = "دورة CTP-الجديدة (6️⃣ CTP)",
                    days = "الاثنين، الأربعاء",
                    timeStart = "06:10 م",
                    timeEnd = "10:00 م",
                    zoomAccount = "support.02@fin.com.sa",
                    status = "نشط",
                    completedCount = 4,
                    targetCount = 12,
                    colorHex = "#059669" // Emerald Green
                )
            )
            for (course in defaultCourses) {
                courseDao.insertCourse(course)
            }
        }
    }
}
