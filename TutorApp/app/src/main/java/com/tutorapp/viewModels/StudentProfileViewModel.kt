package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.StudentProfileRequest
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Base64
import com.tutorapp.models.StudentProfileResponse
import com.tutorapp.models.dataSP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class StudentProfileViewModel : ViewModel() {
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

}

