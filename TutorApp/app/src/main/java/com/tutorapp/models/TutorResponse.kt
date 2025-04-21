package com.tutorapp.models

data class TutorResponse (
    val id: Int,
    val name: String,
    val email: String,
    val area_of_expertise: String,
    val major: String,
    val phone_number: String,
    val university: String
)