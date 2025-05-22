package com.tutorapp.views // Or your preferred package for UI states

import com.tutorapp.models.TutoringSession

data class BookedSessionsUiState(
    val sessions: List<TutoringSession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isStale: Boolean = false // Data might be outdated due to no internet
)
