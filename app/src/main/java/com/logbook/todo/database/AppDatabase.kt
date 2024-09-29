package com.logbook.todo.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.logbook.todo.database.entities.SubTask
import com.logbook.todo.database.entities.Task

@Database(entities = [Task::class, SubTask::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "task_database"
                    ).build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error creating database: ${e.message}", e)
                    throw RuntimeException("Failed to create the database")
                }
            }
        }
    }
}