package com.tutorapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookedSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookedSessions(sessions: List<BookedSessionEntity>)

    // Fetches sessions booked by students for a specific tutor
    @Query("SELECT * FROM booked_sessions WHERE tutorIdString = :tutorId AND studentName IS NOT NULL")
    suspend fun getBookedSessionsForTutor(tutorId: String): List<BookedSessionEntity>

    @Query("DELETE FROM booked_sessions WHERE tutorIdString = :tutorId AND studentName IS NOT NULL")
    suspend fun clearBookedSessionsForTutor(tutorId: String)

    // Optional: if you want to clear all booked sessions regardless of tutor (e.g., on logout)
    @Query("DELETE FROM booked_sessions")
    suspend fun clearAllBookedSessions()
}
