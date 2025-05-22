package com.tutorapp.models

data class BookedSession(
    val id: Int,
    val tutorName: String,
    val courseName: String,
    val cost: String,
    val dateTime: String,
    val student: String
)

data class BookedSessionsResponse(
    val booked_sessions: List<BookedSession>
) 