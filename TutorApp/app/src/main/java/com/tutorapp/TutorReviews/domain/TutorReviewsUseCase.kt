package com.tutorapp.TutorReviews.domain

import com.tutorapp.TutorReviews.data.TutorReviewsRepository
import com.tutorapp.TutorReviews.data.network.response.ReviewResponse

class TutorReviewsUseCase {
    private val repository = TutorReviewsRepository()
    suspend operator fun invoke(): List<ReviewResponse>{
        return repository.getReviews()
    }
}