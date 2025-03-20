package com.tutorapp.showTutors.data.network.response

import retrofit2.http.GET

interface ShowTutorsClient {
    @GET("v3/709ff37e-434a-448b-98c7-f6cafa19b6cd")
    suspend fun getTutors(): List<TutorResponse>
}