package com.tutorapp.models

data class TutorResponse (
    val id: Int,
    val name: String,
    val email: String,
    val subject: String,
    val title: String,
    val description: String,
    val reviews_score: Float,
    val image_url: String,
)