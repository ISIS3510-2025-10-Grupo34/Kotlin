package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.PostReviewRequest
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch

class WriteReviewViewModel: ViewModel() {
    fun postReview(tutoringSessionId: Int, studentId: Int, rating: Int, comment: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val requestBody = PostReviewRequest(
                    tutoringSessionId = tutoringSessionId,
                    studentId = studentId,
                    rating = rating,
                    comment = comment,
                )
                val response = RetrofitClient.instance.postReview(requestBody)
                if (response.isSuccessful) {
                    onResult(true, response.body()?.message!!)
                } else {
                    onResult(false, null.toString())
                }
            } catch (e: Exception) {
                onResult(false, null.toString())
            }
        }
    }
}