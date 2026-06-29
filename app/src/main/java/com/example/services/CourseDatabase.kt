package com.example.services

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.models.Course
import com.example.models.ReminderEntity
import com.example.models.StudySession
import com.example.models.StudyGoal
import com.example.models.Grade

@Database(
    entities = [
        Course::class, 
        ReminderEntity::class,
        StudySession::class,
        StudyGoal::class,
        Grade::class
    ], 
    version = 10, 
    exportSchema = false
)
abstract class CourseDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao

    companion object {
        @Volatile
        private var INSTANCE: CourseDatabase? = null

        fun getDatabase(context: Context): CourseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CourseDatabase::class.java,
                    "courses_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
