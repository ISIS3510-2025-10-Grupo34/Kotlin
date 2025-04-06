package com.tutorapp.models

data class SearchResultResponse(
    val data: Map<String, University>
)

data class University(
    val id: Int,
    val courses: Map<String, Course>
)

data class Course(
    val id: Int,
    val tutors: List<Int>
)
