package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.StudentProfileRequest
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Base64
import com.tutorapp.models.GetTutorProfileResponse
import com.tutorapp.models.GetTutoringSessionsToReviewResponse
import com.tutorapp.models.StudentProfileResponse
import com.tutorapp.models.dataSP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class StudentProfileViewModel : ViewModel() {

    private val _percentage = MutableStateFlow<Float>(0.0F)
    val percentage: StateFlow<Float> = _percentage
    var studentProfile: dataSP? = null
        private set
    fun studentProfile(id: String, onComplete: (dataSP?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.studentProfile(StudentProfileRequest(id))
                if (response.isSuccessful) {
                    println(response)
                    println(response.body())
                    studentProfile = response.body()?.data
                    withContext(Dispatchers.Main) {
                        onComplete(studentProfile)
                    }
                } else {

                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    fun getTutoringSessionsToReview(studentId: Int, onResult: (Boolean, GetTutoringSessionsToReviewResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getTutoringSessionsToReview(studentId)
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

    suspend fun getStudentProfileBody(id: String): StudentProfileResponse? {
        return try {
            val response = RetrofitClient.instance.studentProfile(StudentProfileRequest(id))
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
            null
        }
    }
    fun reviewPercentage(id: String) {
        viewModelScope.launch {
            val response = RetrofitClient.instance.reviewPercentage(id)
            println(response.body())
            println("aaaa")
            response.body()?.let { _percentage.value = it.percentage }

        }
    }




}

