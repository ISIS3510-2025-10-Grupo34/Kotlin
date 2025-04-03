package com.tutorapp.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.TutoringSession
import com.tutorapp.models.TutorsResponse
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TutoringSessionViewModel : ViewModel() {

    var sessions by mutableStateOf<List<TutoringSession>>(emptyList())
        private set

    fun getAllSessions(onComplete: (List<TutoringSession>?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.tutoringSessions()

                if (response.isSuccessful) {
                    sessions = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        onComplete(sessions)
                    }
                } else {
                    println("Error: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        onComplete(null)
                    }
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }


    suspend fun onFilterClick(university:String, course:String, professor:String){
        val response = RetrofitClient.instance.tutoringSessions()
        if(university.isNotEmpty()  && professor.isNotEmpty() && course.isNotEmpty()){
            val filteredList = response.body() ?: emptyList()
            filteredList.filter { tutoringSession ->
                tutoringSession.university.contains(university, ignoreCase = true) &&
                        tutoringSession.tutor.contains(professor, ignoreCase = true)&&
                        tutoringSession.course.contains(course, ignoreCase = true)
            }
            sessions = filteredList
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
            getAllSessions {  }
        }

    }
}
