package com.tutorapp.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.PostFilterCounterIncreaseRequest
import com.tutorapp.models.SearchResultFilterResponse
import com.tutorapp.models.TutoringSession
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
                    sessions = response.body()?.filter { it.student == null } ?: emptyList()
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
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.tutor.contains(professor, ignoreCase = true) && element.course.contains(course, ignoreCase = true) && element.university.contains(university, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
            sessions = filteredList
        }
        else if(university.isNotEmpty() && course.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.course.contains(course, ignoreCase = true) && element.university.contains(university, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
            sessions = filteredList
        }
        else if(university.isNotEmpty() && professor.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.course.contains(professor, ignoreCase = true) && element.university.contains(university, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
            sessions = filteredList
        }
        else if(course.isNotEmpty() && professor.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.course.contains(professor, ignoreCase = true) && element.tutor.contains(professor, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
            sessions = filteredList
        }

        else if(university.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.university.contains(university, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
            sessions = filteredList
        }
        else if(professor.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.tutor.contains(professor, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
            sessions = filteredList
        }
        else if(course.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.course.contains(course, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
            sessions = filteredList
        }

        else{
            getAllSessions {  }
        }

    }

    fun getSearchResults(onResult: (Boolean, SearchResultFilterResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSearchResultsFilter()

                if (response.isSuccessful) {
                    onResult(true, response.body())
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                onResult(false, null)
            }
        }
    }

}
