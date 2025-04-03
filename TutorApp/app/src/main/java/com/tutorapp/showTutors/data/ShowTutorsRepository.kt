package com.tutorapp.showTutors.data

import com.tutorapp.showTutors.data.network.response.ShowTutorsService
import com.tutorapp.models.TutorResponse

class ShowTutorsRepository {
    private val api = ShowTutorsService()
    suspend fun getTutors():List<TutorResponse>{
        return api.getTutors()
    }
}