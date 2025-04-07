package com.tutorapp.models

data class TutoringSession(
    val tutor: String,
    val tutor_id: String,
    val course: String,
    val cost: Double,
    val date_time: String
)