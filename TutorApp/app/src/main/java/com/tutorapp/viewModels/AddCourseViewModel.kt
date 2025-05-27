package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.data.InputCacheManager
import com.tutorapp.models.PostTutoringSessionRequest
import com.tutorapp.models.SearchResultResponse
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch
import com.tutorapp.models.GetTopPostingTimesResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddCourseViewModel : ViewModel() {

    // --- Cache methods ---
    fun cacheInput(key: String, value: String) {
        InputCacheManager.put(key, value)
    }

    fun getCachedInput(key: String): String? {
        return InputCacheManager.get(key)
    }

    fun clearCache() {
        InputCacheManager.clear()
    }

    // --- Network methods ---
    fun getSearchResults(onResult: (Boolean, SearchResultResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSearchResults()
                if (response.isSuccessful) {
                    onResult(true, response.body())
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                onResult(false, null)
            }
        }
    }

    fun getPriceEstimation(tutorId: Int, courseUniversityName: String, onResult: (Boolean, Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getPriceEstimation(tutorId, courseUniversityName)
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

    fun postTutoringSession(
        tutorId: String,
        courseId: String,
        cost: String,
        dateTime: String,
        onResult: (Boolean, Int?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val requestBody = PostTutoringSessionRequest(
                    cost = cost,
                    dateTime = dateTime,
                    courseId = courseId,
                    tutorId = tutorId
                )
                val response = RetrofitClient.instance.postTutoringSession(requestBody)
                if (response.isSuccessful) {
                    onResult(true, response.body()?.sessionId)
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                onResult(false, null)
            }
        }
    }

    // --- Top Posting Times Feature ---
    private val _topPostingHours = MutableStateFlow<List<String>>(emptyList())
    val topPostingHours: StateFlow<List<String>> = _topPostingHours.asStateFlow()

    private val _isLoadingTopHours = MutableStateFlow(false)
    val isLoadingTopHours: StateFlow<Boolean> = _isLoadingTopHours.asStateFlow()

    private val _topHoursError = MutableStateFlow<String?>(null)
    val topHoursError: StateFlow<String?> = _topHoursError.asStateFlow()

    private var topHoursFetchedSuccessfully = false

    fun fetchTopPostingTimes() {
        // If data is already fetched and available, and no error previously, don't refetch
        // unless a specific refresh mechanism is desired.
        if (topHoursFetchedSuccessfully && _topPostingHours.value.isNotEmpty() && _topHoursError.value == null) {
            _isLoadingTopHours.value = false // Ensure loading is false
            return
        }

        viewModelScope.launch {
            _isLoadingTopHours.value = true
            _topHoursError.value = null
            topHoursFetchedSuccessfully = false // Reset flag before fetch attempt
            try {
                val response = RetrofitClient.instance.getTopPostingTimes()
                if (response.isSuccessful) {
                    val rawData: List<GetTopPostingTimesResponse>? = response.body()
                    if (rawData.isNullOrEmpty()) {
                        _topPostingHours.value = emptyList()
                        // Consider if this is an error or just "no data"
                        // _topHoursError.value = "No top posting time data available."
                        topHoursFetchedSuccessfully = true // Fetched, but no data
                    } else {
                        _topPostingHours.value = rawData
                            .sortedByDescending { it.count } // Already sorted by API, but good practice
                            .map { "${it.hour}:00" } // Format as HH:00
                        topHoursFetchedSuccessfully = true
                    }
                } else {
                    _topPostingHours.value = emptyList()
                    _topHoursError.value = "Error fetching top hours: ${response.code()}"
                }
            } catch (e: Exception) {
                _topPostingHours.value = emptyList()
                _topHoursError.value = "Network error: Could not fetch top hours."
                // Log.e("AddCourseViewModel", "fetchTopPostingTimes error", e) // Good for debugging
            } finally {
                _isLoadingTopHours.value = false
            }
        }
    }
}
