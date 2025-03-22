package com.tutorapp.TutorReviews.data.network.response

import retrofit2.http.GET

interface TutorReviewsClient {
    @GET("v3/d229fc96-f654-404b-98b3-2456c82c4684")
    suspend fun getReviews(): List<ReviewResponse>
}