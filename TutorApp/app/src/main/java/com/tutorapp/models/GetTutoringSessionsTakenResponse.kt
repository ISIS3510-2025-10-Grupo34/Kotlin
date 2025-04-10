package com.tutorapp.models

data class GetTutoringSessionsToReviewResponse(
    val data: List<TutoringSessionToReview>
)

data class TutoringSessionToReview(
    val id: Int,
    val tutorName: String,
    val tutorId: Int,
    val courseName: String,
    val course: Int,
    val cost: String,
    val dateTime: String,
    val student: Int
)