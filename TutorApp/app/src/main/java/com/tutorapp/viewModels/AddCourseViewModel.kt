package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.SearchResulInfo
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch

class AddCourseViewModel: ViewModel()  {
    fun getSearchResults(onResult: (Boolean, SearchResulInfo?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSearchResults()

                if (response.isSuccessful) {
                    onResult(true, response.body()?.data)
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                onResult(false, null)
            }
        }
    }
}