package com.tutorapp.models

import java.io.Serializable

data class SimilarTutorsResponse(
    val similar_tutor_reviews: List<SimilarTutorReview>
)

data class SimilarTutorReview(
    val similar_tutor_id: Int,
    val similarity_basis: List<String>,
    val best_reviews: List<BestReview>
)

data class BestReview(
    val rating: Int,
    val comment: String,
    val course_name: String,
    val review_date: String
): Serializable
