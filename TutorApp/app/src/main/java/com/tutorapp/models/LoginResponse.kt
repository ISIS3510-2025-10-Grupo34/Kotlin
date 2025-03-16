package com.tutorapp.models

data class LoginResponse(
    val data: TokenData
)

data class TokenData(
    val token: String
)
