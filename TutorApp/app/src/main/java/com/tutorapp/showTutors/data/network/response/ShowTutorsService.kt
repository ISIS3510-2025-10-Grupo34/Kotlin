package com.tutorapp.showTutors.data.network.response

import com.tutorapp.models.TutorResponse
import com.tutorapp.models.TutorsResponse
import com.tutorapp.showTutors.core.network.RetrofitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShowTutorsService {
    val retrofit = RetrofitHelper.getRetrofit()

    suspend fun getTutors(): TutorsResponse {

        return withContext(Dispatchers.IO){
            val response = retrofit.create(ShowTutorsClient::class.java).getTutors()
            response
        }
    }
}