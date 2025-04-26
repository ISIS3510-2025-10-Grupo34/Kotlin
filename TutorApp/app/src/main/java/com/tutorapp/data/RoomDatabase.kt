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
        InsightEntity::class,
        SessionDataEntity::class,
        DraftReviewEntity::class,

    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentFormDao(): StudentFormDao
    abstract fun tutorFormDao(): TutorFormDao
    abstract fun tutorProfileDao(): TutorProfileDao
    abstract fun sessionDataDao(): SessionDataDao
    abstract fun draftReviewDao(): DraftReviewDao


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
