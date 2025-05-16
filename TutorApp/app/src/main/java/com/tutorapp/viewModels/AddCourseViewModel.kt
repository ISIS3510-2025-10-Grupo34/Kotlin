package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.data.InputCacheManager
import com.tutorapp.models.PostTutoringSessionRequest
import com.tutorapp.models.SearchResultResponse
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch

class AddCourseViewModel : ViewModel() {

    // --- Cache methods ---
    fun cacheInput(key: String, value: String) {
        InputCacheManager.put(key, value)
    }

    fun getCachedInput(key: String): String? {
        return InputCacheManager.get(key)
    }

    fun clearCache() {
        InputCacheManager.clear()
    }

    // --- Network methods ---
    fun getSearchResults(onResult: (Boolean, SearchResultResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSearchResults()
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

    fun getPriceEstimation(tutorId: Int, courseUniversityName: String, onResult: (Boolean, Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getPriceEstimation(tutorId, courseUniversityName)
                if (response.isSuccessful) {
                    onResult(true, response.body()?.data)
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                onResult(false, null)
            }
        }
    }

    fun postTutoringSession(
        tutorId: String,
        courseId: String,
        cost: String,
        dateTime: String,
        onResult: (Boolean, Int?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val requestBody = PostTutoringSessionRequest(
                    cost = cost,
                    dateTime = dateTime,
                    courseId = courseId,
                    tutorId = tutorId
                )
                val response = RetrofitClient.instance.postTutoringSession(requestBody)
                if (response.isSuccessful) {
                    onResult(true, response.body()?.sessionId)
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                onResult(false, null)
            }
        }
    }
}
