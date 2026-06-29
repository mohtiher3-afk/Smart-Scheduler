package com.example.services

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.models.Course
import com.example.models.ReminderEntity
import com.example.models.StudySession
import com.example.models.StudyGoal
import com.example.models.Grade

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE isDeleted = 0 ORDER BY id ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE syncStatus != 0 OR isDeleted = 1")
    suspend fun getUnsyncedCourses(): List<Course>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: Int): Course?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Update
    suspend fun updateCourse(course: Course)

    @Query("UPDATE courses SET isDeleted = 1, syncStatus = 2, lastUpdated = :timestamp WHERE id = :id")
    suspend fun softDeleteCourse(id: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun hardDeleteCourse(course: Course)

    @Query("SELECT * FROM reminders WHERE isDeleted = 0 ORDER BY timeInMillis ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE syncStatus != 0 OR isDeleted = 1")
    suspend fun getUnsyncedReminders(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isDeleted = 1, syncStatus = 2, lastUpdated = :timestamp WHERE id = :id")
    suspend fun softDeleteReminder(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM reminders WHERE courseId = :courseId")
    suspend fun hardDeleteRemindersByCourse(courseId: Long)

    @Query("UPDATE reminders SET isDeleted = 1, syncStatus = 2, lastUpdated = :timestamp WHERE courseId = :courseId")
    suspend fun softDeleteRemindersByCourse(courseId: Long, timestamp: Long = System.currentTimeMillis())

    // Study Sessions
    @Query("SELECT * FROM study_sessions WHERE isDeleted = 0 ORDER BY startTime DESC")
    fun getAllStudySessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySession): Long

    @Query("SELECT * FROM study_sessions WHERE syncStatus != 0 OR isDeleted = 1")
    suspend fun getUnsyncedStudySessions(): List<StudySession>

    // Study Goals
    @Query("SELECT * FROM study_goals WHERE isDeleted = 0 ORDER BY deadline ASC")
    fun getAllGoals(): Flow<List<StudyGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: StudyGoal): Long

    @Update
    suspend fun updateGoal(goal: StudyGoal)

    @Query("SELECT * FROM study_goals WHERE syncStatus != 0 OR isDeleted = 1")
    suspend fun getUnsyncedGoals(): List<StudyGoal>

    // Grades
    @Query("SELECT * FROM grades WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllGrades(): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE courseId = :courseId AND isDeleted = 0")
    fun getGradesForCourse(courseId: Int): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: Grade): Long

    @Query("SELECT * FROM grades WHERE syncStatus != 0 OR isDeleted = 1")
    suspend fun getUnsyncedGrades(): List<Grade>
}
