package com.example.sync

import android.content.Context
import androidx.work.*
import com.example.models.Course
import com.example.models.ReminderEntity
import com.example.models.StudySession
import com.example.models.StudyGoal
import com.example.models.Grade
import com.example.services.CourseDatabase
import com.example.services.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = CourseDatabase.getDatabase(applicationContext)
            val dao = database.courseDao()
            
            // 1. Fetch unsynced local changes
            val unsyncedCourses = dao.getUnsyncedCourses()
            val unsyncedReminders = dao.getUnsyncedReminders()
            val unsyncedSessions = dao.getUnsyncedStudySessions()
            val unsyncedGoals = dao.getUnsyncedGoals()
            val unsyncedGrades = dao.getUnsyncedGrades()
            
            // 2. Perform Sync Logic (Upload changes, Download updates)
            // For Phase 08/09, we simulate the Cloud Sync interaction
            
            // Simulating Uploading local changes to Cloud
            if (unsyncedCourses.isNotEmpty() || unsyncedReminders.isNotEmpty() || 
                unsyncedSessions.isNotEmpty() || unsyncedGoals.isNotEmpty() || unsyncedGrades.isNotEmpty()) {
                // In a real app, this would be a Retrofit call to an API
                // For now we simulate success after a short delay
                kotlinx.coroutines.delay(1000)
                
                // After successful "upload", mark local as synced
                unsyncedCourses.forEach { course ->
                    if (course.isDeleted) {
                        dao.hardDeleteCourse(course)
                    } else {
                        dao.updateCourse(course.copy(syncStatus = 0, lastUpdated = System.currentTimeMillis()))
                    }
                }
                
                unsyncedReminders.forEach { reminder ->
                    if (reminder.isDeleted) {
                        dao.hardDeleteRemindersByCourse(reminder.courseId) // Simplified for mock
                    } else {
                        dao.updateReminder(reminder.copy(syncStatus = 0, lastUpdated = System.currentTimeMillis()))
                    }
                }

                unsyncedSessions.forEach { session ->
                    dao.insertStudySession(session.copy(syncStatus = 0, lastUpdated = System.currentTimeMillis()))
                }

                unsyncedGoals.forEach { goal ->
                    dao.updateGoal(goal.copy(syncStatus = 0, lastUpdated = System.currentTimeMillis()))
                }

                unsyncedGrades.forEach { grade ->
                    dao.insertGrade(grade.copy(syncStatus = 0, lastUpdated = System.currentTimeMillis()))
                }
            }
            
            // 3. Simulating Downloading remote changes
            // In a real app, fetch from server and merge using timestamps
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

object SyncManager {
    private const val SYNC_WORK_NAME = "SmartSchedulerSyncWork"

    fun scheduleSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.KEEP, syncRequest)
    }

    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                SYNC_WORK_NAME + "_periodic",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )
    }
}
