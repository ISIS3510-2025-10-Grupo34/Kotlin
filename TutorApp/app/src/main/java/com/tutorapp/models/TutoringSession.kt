package com.tutorapp.models

data class TutoringSession(
    val id: Int,
    val tutor: String,
    val tutor_id: String,
    val tutor_phone: String,
    val course: String,
    val university: String,
    val cost: Double,
    val date_time: String,
    val student: String? = null
)