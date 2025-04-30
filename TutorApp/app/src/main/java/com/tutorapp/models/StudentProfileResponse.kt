package com.tutorapp.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayInputStream

data class StudentProfileResponse(
    val data: dataSP
)
data class dataSP(
    val name: String,
    val university: String,
    val major: String,
    val profile_picture: String?,
    val learning_styles: List<String>)