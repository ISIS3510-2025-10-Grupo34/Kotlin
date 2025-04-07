package com.tutorapp.models

data class PostTutoringSessionRequest(
    val cost: String,
    val dateTime: String,
    val courseId: String,
    val tutorId: String,
)