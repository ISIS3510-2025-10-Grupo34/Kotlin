package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.LoginRequest
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Base64
import android.util.Log
import com.google.gson.Gson

class LoginViewModel : ViewModel() {
    fun login(email: String, password: String, onResult: (Boolean, LoginTokenDecoded?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val token = decodeJwt(response.body()?.data?.token)
                    if (token != null) {
                        val tokenFormatted = Gson().fromJson(token, LoginTokenDecoded::class.java)
                        onResult(true, tokenFormatted)
                    } else {
                        onResult(false, null)
                    }
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                onResult(false, null)
            }
        }
    }

    private fun decodeJwt(token: String?): String? {
        return try {
            val parts = token?.split(".")
            if (parts?.size != 3) return null

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, Charsets.UTF_8)

            decodedString
        } catch (e: Exception) {
            println(e)
            null
        }
    }

}
