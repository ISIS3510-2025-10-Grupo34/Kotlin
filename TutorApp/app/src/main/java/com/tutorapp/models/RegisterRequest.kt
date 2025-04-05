package com.tutorapp.models

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class RegisterRequest(
    val name: RequestBody,
    val email: RequestBody,
    val password: RequestBody,
    val phone_number: RequestBody?,
    val profile_picture: MultipartBody.Part?,
    val id_picture: MultipartBody.Part,
    val university: RequestBody,
    val major: RequestBody?,
    val area_of_expertise: RequestBody?,
    val role:RequestBody? ,
    val learning_styles: RequestBody?
)
