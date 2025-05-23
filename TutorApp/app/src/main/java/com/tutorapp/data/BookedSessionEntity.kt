package com.tutorapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "booked_sessions_calendar")
data class BookedSessionCalendarEntity(
    @PrimaryKey val id: Int,
    val tutorName: String,
    val courseName: String,
    val cost: String,
    val dateTime: String,
    val student: String
)

@Dao
interface BookedSessionCalendarDao {
    @Query("SELECT * FROM booked_sessions_calendar")
    suspend fun getAllSessions(): List<BookedSessionCalendarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<BookedSessionCalendarEntity>)

    @Query("DELETE FROM booked_sessions_calendar")
    suspend fun clearAll()
} 