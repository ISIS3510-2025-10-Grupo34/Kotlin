package com.tutorapp.TutorReviews

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.TutorReviews.data.network.response.ReviewResponse
import com.tutorapp.TutorReviews.domain.TutorReviewsUseCase
import kotlinx.coroutines.launch

class TutorReviewsViewModel : ViewModel(){
    private val _reviews = MutableLiveData<List<ReviewResponse>>()
    val reviews : LiveData<List<ReviewResponse>> = _reviews

    val reviewsUseCase = TutorReviewsUseCase()

    fun onStart(){
        viewModelScope.launch {
            try{
                val reviewsList = reviewsUseCase()
                _reviews.value = reviewsList

            }catch (e:Exception){
                println("Error fetching reviews: ${e.message}")
            }
        }
    }


}