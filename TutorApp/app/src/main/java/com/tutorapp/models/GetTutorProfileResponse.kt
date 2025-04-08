package com.tutorapp.models

data class GetTutorProfileResponse(
    val data: TutorData
)

data class TutorData(
    val name: String,
    val university: String,
    val ratings: Double,
    val reviews: List<Review>,
    val whatsappContact: String,
    val subjects: String
)

data class Review(
    val rating: Int,
    val comment: String
)