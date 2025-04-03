package com.tutorapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    suspend fun onFilterClick(university:String, course:String, professor:String){
        val tutorsResponse = showTutorsUseCase()
        if(university.isNotEmpty()  && professor.isNotEmpty()){
            val filteredList = tutorsResponse.tutors.filter { tutor ->
                tutor.university.contains(university, ignoreCase = true) &&
                        tutor.name.contains(professor, ignoreCase = true)
            }
            val tutorsFiltered = TutorsResponse (filteredList)
            _tutors.value = tutorsFiltered
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