package com.tutorapp.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayInputStream

data class SearchResultResponse(
    val data: Map<String, Map<String, List<Int>>> // University name -> Course name -> List of students
)