package com.tutorapp.showTutors

data class Tutor(
    val id: Int,
    val name: String,
    val email: String,
    val subject: String,
    val title: String,
    val description: String,
    val reviews_score: Float,
    val image_url: String
)