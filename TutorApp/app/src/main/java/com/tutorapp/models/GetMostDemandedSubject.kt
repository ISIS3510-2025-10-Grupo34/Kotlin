package com.tutorapp.models

data class GetMostDemandedSubjectResponse(
    val most_demanded_subject: String,
    val course_id: Int,
    val university: String,
    val count: Int
)
