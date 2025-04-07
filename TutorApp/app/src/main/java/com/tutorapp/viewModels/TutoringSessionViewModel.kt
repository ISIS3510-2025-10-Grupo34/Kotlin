package com.tutorapp.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.SearchResultFilterResponse
import com.tutorapp.models.SearchResultResponse
import com.tutorapp.models.TutoringSession
import com.tutorapp.models.TutorsResponse
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer

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

    fun String.normalize(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
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
