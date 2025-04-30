package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.SimilarTutorsResponse
import com.tutorapp.models.SimilarTutorReview
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class SimilarTutorsViewModel : ViewModel() {
    var similarTutors by mutableStateOf<List<SimilarTutorReview>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchSimilarTutors(tutorId: Int) {
        if (similarTutors.isNotEmpty()) return
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSimilarTutorsReviews(tutorId)
                if (response.isSuccessful) {
                    similarTutors = response.body()?.similar_tutor_reviews ?: emptyList()
                } else {
                    errorMessage = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Exception: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
