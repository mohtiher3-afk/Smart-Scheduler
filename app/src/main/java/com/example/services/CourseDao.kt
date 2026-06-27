package com.example.services

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.models.Course
import com.example.models.ReminderEntity

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY id ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: Int): Course?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("SELECT * FROM reminders ORDER BY timeInMillis ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE courseId = :courseId")
    suspend fun deleteRemindersByCourse(courseId: Long)
}
