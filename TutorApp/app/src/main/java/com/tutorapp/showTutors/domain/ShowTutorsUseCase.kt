package com.tutorapp.showTutors.domain

import com.tutorapp.showTutors.data.ShowTutorsRepository
import com.tutorapp.models.TutorsResponse

class ShowTutorsUseCase {
    private val repository = ShowTutorsRepository()

    suspend operator fun invoke(): TutorsResponse {
        return repository.getTutors()
    }

}