package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.LoginRequest
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Base64
import org.json.JSONObject


class LoginViewModel : ViewModel() {
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                if (response.isSuccessful) {

                    val token = decodeJwt(response.body()?.data?.token)
                    onResult(true, token)

                } else {
                    onResult(false, response.errorBody()?.string() ?: "Login failed")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
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
