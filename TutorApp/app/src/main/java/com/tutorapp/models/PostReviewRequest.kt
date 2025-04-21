package com.tutorapp.models

data class PostReviewRequest (
    val tutoringSessionId: Int,
    val studentId: Int,
    val rating: Int,
    val comment: String,
)