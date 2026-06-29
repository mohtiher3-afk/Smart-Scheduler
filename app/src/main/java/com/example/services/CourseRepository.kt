package com.example.services

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.example.models.Course
import com.example.models.ReminderEntity
import com.example.models.StudySession
import com.example.models.StudyGoal
import com.example.models.Grade

import com.example.sync.SyncManager

class CourseRepository(private val courseDao: CourseDao) {
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()

    suspend fun getCourseById(id: Int): Course? = courseDao.getCourseById(id)

    suspend fun insertCourse(context: Context, course: Course): Long {
        val id = courseDao.insertCourse(course.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
        SyncManager.scheduleSync(context)
        return id
    }

    suspend fun updateCourse(context: Context, course: Course) {
        courseDao.updateCourse(course.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
        SyncManager.scheduleSync(context)
    }

    suspend fun deleteCourse(context: Context, course: Course) {
        courseDao.softDeleteRemindersByCourse(course.id.toLong())
        courseDao.softDeleteCourse(course.id)
        SyncManager.scheduleSync(context)
    }

    val allReminders: Flow<List<ReminderEntity>> = courseDao.getAllReminders()

    suspend fun insertReminder(context: Context, reminder: ReminderEntity): Long {
        val id = courseDao.insertReminder(reminder.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
        SyncManager.scheduleSync(context)
        return id
    }

    suspend fun deleteReminder(context: Context, reminder: ReminderEntity) {
        courseDao.softDeleteReminder(reminder.id)
        SyncManager.scheduleSync(context)
    }

    // Analytics Methods
    val allStudySessions: Flow<List<StudySession>> = courseDao.getAllStudySessions()
    val allGoals: Flow<List<StudyGoal>> = courseDao.getAllGoals()
    val allGrades: Flow<List<Grade>> = courseDao.getAllGrades()

    suspend fun insertStudySession(context: Context, session: StudySession) {
        courseDao.insertStudySession(session.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
        SyncManager.scheduleSync(context)
    }

    suspend fun insertGoal(context: Context, goal: StudyGoal) {
        courseDao.insertGoal(goal.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
        SyncManager.scheduleSync(context)
    }

    suspend fun updateGoal(context: Context, goal: StudyGoal) {
        courseDao.updateGoal(goal.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
        SyncManager.scheduleSync(context)
    }

    suspend fun insertGrade(context: Context, grade: Grade) {
        courseDao.insertGrade(grade.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
        SyncManager.scheduleSync(context)
    }

    suspend fun ensurePrepopulated(context: Context) {
        val currentList = allCourses.first()
        if (currentList.isEmpty()) {
            // First try to load from LocalStorageBackup
            val backedUpCourses = LocalStorageBackup.loadCourses(context)
            if (backedUpCourses.isNotEmpty()) {
                for (course in backedUpCourses) {
                    courseDao.insertCourse(course.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
                }
                
                // Also restore backed up reminders
                val backedUpReminders = LocalStorageBackup.loadReminders(context)
                for (reminder in backedUpReminders) {
                    courseDao.insertReminder(reminder.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
                }
                SyncManager.scheduleSync(context)
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
                    colorHex = "#2563EB", // Navy Blue
                    category = "محاسبة"
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
                    colorHex = "#7C3AED", // Violet/Purple
                    category = "محاسبة"
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
                    colorHex = "#0D9488", // Teal
                    category = "محاسبة"
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
                    colorHex = "#DC2626", // Crimson Red
                    category = "إدارة مالية"
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
                    colorHex = "#059669", // Emerald Green
                    category = "إدارة مالية"
                )
            )
            for (course in defaultCourses) {
                courseDao.insertCourse(course.copy(syncStatus = 1, lastUpdated = System.currentTimeMillis()))
            }
            SyncManager.scheduleSync(context)
        }
    }
}
