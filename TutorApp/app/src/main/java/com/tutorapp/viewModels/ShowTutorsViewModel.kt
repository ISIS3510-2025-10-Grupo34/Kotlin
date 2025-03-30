package com.tutorapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.TutorResponse
import com.tutorapp.showTutors.domain.ShowTutorsUseCase
import kotlinx.coroutines.launch

class ShowTutorsViewModel: ViewModel() {

    val showTutorsUseCase = ShowTutorsUseCase()

    private val _tutors = MutableLiveData<List<TutorResponse>>()
    val tutors: LiveData<List<TutorResponse>> = _tutors

    fun onStart(){
        viewModelScope.launch {
            try {
                val tutorList = showTutorsUseCase()
                val sortedList = tutorList.sortedByDescending { it.reviews_score }
                _tutors.value = sortedList

            } catch (e: Exception) {

                println("Error fetching tutors: ${e.message}")

            }
        }

    }
}