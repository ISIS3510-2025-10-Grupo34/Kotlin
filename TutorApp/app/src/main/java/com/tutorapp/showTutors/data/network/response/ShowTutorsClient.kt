package com.tutorapp.showTutors.data.network.response

import retrofit2.http.GET

interface ShowTutorsClient {
    @GET("v3/22cc9e28-d058-4c5d-b9d2-0a31e3534445")
    suspend fun getTutors(): List<TutorResponse>
}