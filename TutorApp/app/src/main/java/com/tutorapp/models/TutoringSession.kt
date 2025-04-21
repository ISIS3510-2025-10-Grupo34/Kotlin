package com.tutorapp.models

data class TutoringSession(
    val tutor: String,
    val tutor_id: String,
    val tutor_phone_number: String,
    val course: String,
    val university: String,
    val cost: Double,
    val date_time: String,
    val student: String? = null
)