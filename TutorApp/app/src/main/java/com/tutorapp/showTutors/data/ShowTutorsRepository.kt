package com.tutorapp.showTutors.data

import com.tutorapp.showTutors.data.network.response.ShowTutorsService
import com.tutorapp.models.TutorResponse
import com.tutorapp.models.TutorsResponse

class ShowTutorsRepository {
    private val api = ShowTutorsService()
    suspend fun getTutors(): TutorsResponse {
        val response: TutorsResponse = api.getTutors()
        return response
    }
}