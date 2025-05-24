package com.tutorapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookedSession(
    val id: Int,
    val tutorName: String,
    val courseName: String,
    val cost: String,
    val dateTime: String,
    val student: String
) : Parcelable

data class BookedSessionsResponse(
    val booked_sessions: List<BookedSession>
) 