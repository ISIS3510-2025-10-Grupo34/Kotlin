package com.tutorapp.showTutors.data.network.response

data class TutorResponse (
    val id: Int,
    val name: String,
    val email: String,
    val course: String,
    val university : String,
    val title: String,
    val description: String,
    val reviews_score: Float,
    val image_url: String,
    val phone: String
)