package com.tutorapp.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.GetTutorProfileResponse
import com.tutorapp.models.PostTutorProfileLoadTimeRequest
import com.tutorapp.models.GetTimeToBookInsightResponse
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch

class TutorProfileViewModel: ViewModel()  {
    fun getTutorProfile(tutorId: Int, onResult: (Boolean, GetTutorProfileResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getTutorProfile(tutorId)
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

    fun postProfileLoadTime(loadTime: Float) {
        viewModelScope.launch {
            try {
                val body = PostTutorProfileLoadTimeRequest(
                    loadTime = loadTime
                )
                val response = RetrofitClient.instance.postTutorProfileLoadTime(body)
                Log.i("analytics", response.body()?.data ?: "")
            } catch (e: Exception) {
                Log.i("error", e.message ?: "unknown error")
            }
        }
    }

    fun getTimeToBookInsight(tutorId: Int, onResult: (Boolean, GetTimeToBookInsightResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getTimeToBookInsight(tutorId)
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