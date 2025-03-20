package com.tutorapp.showTutors.data.network.response

import retrofit2.http.GET

interface ShowTutorsClient {
    @GET("v3/55d3b0fc-6ba4-4ad5-b2b8-a00836428efb")
    suspend fun getTutors(): List<TutorResponse>
}