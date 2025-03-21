package com.tutorapp.TutorReviews.data

import com.tutorapp.TutorReviews.data.network.response.ReviewResponse
import com.tutorapp.TutorReviews.data.network.response.TutorReviewsService

class TutorReviewsRepository {
    private val api = TutorReviewsService()
    suspend fun getReviews():List<ReviewResponse>{
        return api.getReviews()
    }
}