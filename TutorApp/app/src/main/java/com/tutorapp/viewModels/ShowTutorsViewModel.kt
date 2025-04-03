package com.tutorapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.TutorResponse
import com.tutorapp.models.TutorsResponse
import com.tutorapp.showTutors.domain.ShowTutorsUseCase
import kotlinx.coroutines.launch

class ShowTutorsViewModel: ViewModel() {

    val showTutorsUseCase = ShowTutorsUseCase()

    private val _tutors = MutableLiveData<TutorsResponse>()
    val tutors: LiveData<TutorsResponse> = _tutors

    fun onStart(){
        viewModelScope.launch {
            try {
                val tutorList = showTutorsUseCase()
                _tutors.value = tutorList

            } catch (e: Exception) {

                println("Error fetching tutors: ${e.message}")

            }
        }

    }
}