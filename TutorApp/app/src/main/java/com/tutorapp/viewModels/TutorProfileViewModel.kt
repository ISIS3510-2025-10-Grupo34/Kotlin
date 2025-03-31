package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tutorapp.models.LoginRequest
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch

class TutorProfileViewModel: ViewModel()  {
    fun getTutorProfileInfo(tutorId: Int) {

    }
}