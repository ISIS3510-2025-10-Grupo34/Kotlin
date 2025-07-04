package com.tutorapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [
        StudentFormEntity::class,
        TutorFormEntity::class,
        TutorProfileEntity::class,
        ReviewEntity::class,
        InsightEntity::class,
        SessionDataEntity::class,
        DraftReviewEntity::class,
        TutoringSessionEntity::class,
        CachedNotificationEntity::class,
        StudentProfileEntity::class,
        BookedSessionEntity::class,
        BookedSessionCalendarEntity::class,
        GamificationCacheEntity::class,
        CachedAchievementEntity::class

        
    ],
    version = 10

)

abstract class AppDatabase : RoomDatabase() {
    abstract fun studentFormDao(): StudentFormDao
    abstract fun tutorFormDao(): TutorFormDao
    abstract fun tutorProfileDao(): TutorProfileDao
    abstract fun sessionDataDao(): SessionDataDao
    abstract fun draftReviewDao(): DraftReviewDao
    abstract fun tutoringSessionDao(): TutoringSessionDao
    abstract fun cachedNotificationDao(): CachedNotificationDao
    abstract fun studentProfileDao(): StudentProfileDao

    abstract fun bookedSessionCalendarDao(): BookedSessionCalendarDao

    abstract fun bookedSessionDao(): BookedSessionDao
    abstract fun gamificationCacheDao(): GamificationCacheDao



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
