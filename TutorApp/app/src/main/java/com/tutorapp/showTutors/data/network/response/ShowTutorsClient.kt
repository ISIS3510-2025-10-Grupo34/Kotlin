package com.tutorapp.showTutors.data.network.response

import com.tutorapp.models.TutorsResponse
import retrofit2.http.GET

interface ShowTutorsClient {
    @GET("tutors/")
    suspend fun getTutors(): TutorsResponse
}