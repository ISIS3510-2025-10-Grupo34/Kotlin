package com.tutorapp.showTutors

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.showTutors.data.network.response.TutorResponse
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

    suspend fun onFilterClick(university:String, course:String, professor:String){
        val tutorList = showTutorsUseCase()
        if(university.isNotEmpty() && course.isNotEmpty() && professor.isNotEmpty()){
            val filteredList = tutorList.filter { tutor ->
                tutor.university.contains(university, ignoreCase = true) &&
                        tutor.course.contains(course, ignoreCase = true) &&
                        tutor.name.contains(professor, ignoreCase = true)
            }
            _tutors.value = filteredList
        }
        /**else if(university.isNotEmpty() && course.isNotEmpty()) {
            val filteredList = tutorList.filter { tutor ->
                tutor.university.contains(university, ignoreCase = true) &&
                        tutor.course.contains(course, ignoreCase = true)
            }
            _tutors.value = filteredList
            // Handle the case where one or more parameters are empty
            // You might want to show an error message or load all tutors
            // For example:
            //onStart() // Load all tutors if any parameter is empty
        }
        else if(university.isNotEmpty() && professor.isNotEmpty()){
            val filteredList = tutorList.filter { tutor ->
                tutor.university.contains(university, ignoreCase = true) &&
                        tutor.name.contains(professor, ignoreCase = true)
            }
            _tutors.value = filteredList
        }*/
        else{
            onStart()
        }

    }
}