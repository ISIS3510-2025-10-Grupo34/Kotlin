package com.tutorapp.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RegisterViewModel : ViewModel() {
    private val _universities = MutableStateFlow<List<String>>(emptyList())
    val universities: StateFlow<List<String>> = _universities
    private val _majors = MutableStateFlow<List<String>>(emptyList())
    val majors: StateFlow<List<String>> = _majors
    fun register(
        context: Context,
        name: String,
        email: String,
        password: String,
        phoneNumber: String?,
        profilePictureUri: Uri?,
        idPictureUri: Uri?,
        university: String,
        major: String?,
        expertise: String?,
        role: String,
        learningStyles: List<String>?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver

                // Convertir URI a File para Multipart
                fun uriToMultipart(uri: Uri?, paramName: String): MultipartBody.Part? {
                    uri ?: return null
                    val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
                    contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    return MultipartBody.Part.createFormData(paramName, file.name, requestBody)
                }

                val profilePicturePart = uriToMultipart(profilePictureUri, "profile_picture")
                val idPicturePart = uriToMultipart(idPictureUri, "id_picture")

                // Crear el request body para los demás datos
                val requestBodyMap = mapOf(
                    "name" to RequestBody.create("text/plain".toMediaTypeOrNull(), name),
                    "email" to RequestBody.create("text/plain".toMediaTypeOrNull(), email),
                    "password" to RequestBody.create("text/plain".toMediaTypeOrNull(), password),
                    "phone_number" to RequestBody.create("text/plain".toMediaTypeOrNull(), phoneNumber ?: ""),
                    "university" to RequestBody.create("text/plain".toMediaTypeOrNull(), university),
                    "major" to RequestBody.create("text/plain".toMediaTypeOrNull(), major ?: ""),
                    "area_of_expertise" to RequestBody.create("text/plain".toMediaTypeOrNull(), expertise ?: ""),
                    "role" to RequestBody.create("text/plain".toMediaTypeOrNull(), role),
                    "learning_styles" to RequestBody.create("text/plain".toMediaTypeOrNull(), learningStyles?.joinToString(",") ?: "")
                )

                // Enviar la solicitud al backend
                val response = RetrofitClient.instance.register(requestBodyMap, profilePicturePart, idPicturePart)

                if (response.isSuccessful) {
                    onResult(true, "")
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Registration failed"
                    onResult(false, errorMessage)
                }
            } catch (e: Exception) {
                onResult(false,e.message ?: "An error occurred")
            }
        }
    }
    fun universities(){
        viewModelScope.launch {
            val response = RetrofitClient.instance.universities()
            println(response.body())
            println("bbb")
            response.body()?.let { _universities.value=it.universities }

        }

    }
    fun majors(university: String){
        viewModelScope.launch {
            val response = RetrofitClient.instance.majors(university)
            println(response.body())
            println("aaaa")
            response.body()?.let { _majors.value=it.majors }

        }

    }
    fun email(email: String, onResult: (Boolean, String) -> Unit){
        viewModelScope.launch {
            val response = RetrofitClient.instance.email(email)

            if (response.isSuccessful) {
                onResult(true, "")
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Registration failed"
                onResult(false, errorMessage)
            }
        }
    }
}
