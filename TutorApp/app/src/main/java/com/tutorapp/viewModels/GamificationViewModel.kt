package com.tutorapp.viewModels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.cache.LruGamificationCache
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.GamificationCacheEntity
import com.tutorapp.models.GamificationProfileResponse
import com.tutorapp.models.LeaderboardEntry
import com.tutorapp.models.UpdateGamificationRequest
import com.tutorapp.remote.NetworkUtils
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GamificationViewModel : ViewModel() {
    var profile by mutableStateOf<GamificationProfileResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchProfile(userId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getGamificationProfile(userId)
                if (response.isSuccessful) {
                    profile = response.body()
                } else {
                    errorMessage = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            } finally {
                isLoading = false
            }
        }
    }
}

