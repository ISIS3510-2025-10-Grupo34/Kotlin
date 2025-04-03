package com.tutorapp.remote



import com.tutorapp.models.StudentProfileRequest
import com.tutorapp.models.StudentProfileResponse
import com.tutorapp.models.*


import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("register/")
    suspend fun register(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part profilePicture: MultipartBody.Part?,
        @Part idPicture: MultipartBody.Part?): Response<ApiResponse>

    @POST("login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("studentprofile/")
    suspend fun studentProfile(@Body request: StudentProfileRequest): Response<StudentProfileResponse>



}
