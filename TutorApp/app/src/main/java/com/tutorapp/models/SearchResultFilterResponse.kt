package com.tutorapp.models

data class SearchResultFilterResponse(
    val data: Map<String, UniversityFilter>
)

data class UniversityFilter(
    val id: Int,
    val courses: Map<String, CourseFilter>
)

data class CourseFilter(
    val id: Int,
    val tutors_names: List<String>
)