package com.tutorapp.TutorReviews.data.network.response

import com.tutorapp.showTutors.core.network.RetrofitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TutorReviewsService {
    val retrofit = RetrofitHelper.getRetrofit()

    suspend fun getReviews():List<ReviewResponse>{
        return withContext(Dispatchers.IO){
            val response = retrofit.create(TutorReviewsClient::class.java).getReviews()
            response
        }
    }
}