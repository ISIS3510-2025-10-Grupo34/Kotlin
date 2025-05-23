package com.tutorapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "booked_sessions")
data class BookedSessionEntity(
    @PrimaryKey val id: Int,
    val tutorName: String,
    val courseName: String,
    val cost: String,
    val dateTime: String,
    val student: String
)

@Dao
interface BookedSessionDao {
    @Query("SELECT * FROM booked_sessions")
    suspend fun getAllSessions(): List<BookedSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<BookedSessionEntity>)

    @Query("DELETE FROM booked_sessions")
    suspend fun clearAll()
} 