package com.tutorapp.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginTokenDecoded(
    val id: Int,
    val email: String,
    val role: String,
    val exp: Long,
    val iat: Long,
    val error: String? = null
) : Parcelable
