package com.tutorapp.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.GetTutorProfileResponse
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
}