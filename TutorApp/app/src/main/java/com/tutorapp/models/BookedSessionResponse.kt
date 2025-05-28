package com.tutorapp.models

data class BookedSessionResponse(
    val id: Int,
    val tutorName: String,
    val courseName: String,
    val cost: String,
    val dateTime: String,
    val student: String
)
