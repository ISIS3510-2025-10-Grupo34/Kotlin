package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.LoginRequest
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    onResult(true, response.body()?.token)
                } else {
                    onResult(false, response.errorBody()?.string() ?: "Login failed")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
