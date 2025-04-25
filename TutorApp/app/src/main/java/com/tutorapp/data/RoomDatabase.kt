package com.tutorapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentFormEntity::class,
        TutorFormEntity::class,
        TutorProfileEntity::class,
        ReviewEntity::class,
        InsightEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentFormDao(): StudentFormDao
    abstract fun tutorFormDao(): TutorFormDao
    abstract fun tutorProfileDao(): TutorProfileDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
