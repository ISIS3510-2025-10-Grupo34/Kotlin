package com.tutorapp.models

data class SearchResultResponse (
    val data: SearchResulInfo
)

typealias SearchResulInfo = Map<String, Map<String, List<Int>>>