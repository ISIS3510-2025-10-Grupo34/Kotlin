package com.tutorapp.showTutors.domain

import com.tutorapp.showTutors.data.ShowTutorsRepository
import com.tutorapp.showTutors.data.network.response.TutorResponse

class ShowTutorsUseCase {
    private val repository = ShowTutorsRepository()

    suspend operator fun invoke(): List<TutorResponse>{
        return repository.getTutors()
    }

}