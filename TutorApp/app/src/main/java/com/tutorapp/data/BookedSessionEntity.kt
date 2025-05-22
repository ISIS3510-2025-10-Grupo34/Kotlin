package com.tutorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "booked_sessions")
data class BookedSessionEntity(
    @PrimaryKey val id: Int, // API's session ID
    val tutorIdString: String, // API's tutor_id (keep as String from API then convert)
    val studentName: String?, // API's student (nullable as per your data)
    val courseName: String,
    val universityName: String,
    val dateTime: String, // Keep as String, formatting can be done in UI
    val cost: Double,
    val tutorPhone: String // From tutor_phone_number
    // Add other fields from TutoringSession if they need to be cached and displayed
    // e.g., tutorName (though for "booked sessions for this tutor", it might be redundant)
)
