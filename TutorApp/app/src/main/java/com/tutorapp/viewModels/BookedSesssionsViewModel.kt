package com.tutorapp.viewModels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.data.BookedSessionDao
import com.tutorapp.data.BookedSessionEntity
import com.tutorapp.models.TutoringSession
import com.tutorapp.remote.RetrofitClient
import com.tutorapp.views.BookedSessionsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import retrofit2.Response

class BookedSesssionsViewModel(
    application: Application,
    private val bookedSessionDao: BookedSessionDao
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BookedSessionsUiState())
    val uiState: StateFlow<BookedSessionsUiState> = _uiState.asStateFlow()

    private var currentLoadedTutorId: Int? = null

    fun loadBookedSessions(tutorId: Int) {
        currentLoadedTutorId = tutorId
        Log.d("BookedSessionsVM", "loadBookedSessions called for tutorId: $tutorId")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Load from Cache (Room)
            try {
                val cachedEntities = withContext(Dispatchers.IO) {
                    bookedSessionDao.getBookedSessionsForTutor(tutorId.toString())
                }
                val cachedSessions = cachedEntities.map { entity ->
                    // Map Entity to Model
                    TutoringSession(
                        id = entity.id,
                        tutor = "", // Not stored in BookedSessionEntity, assumed to be current tutor
                        tutor_id = entity.tutorIdString,
                        tutor_phone = entity.tutorPhone,
                        course = entity.courseName,
                        university = entity.universityName,
                        cost = entity.cost,
                        date_time = entity.dateTime,
                        student = entity.studentName
                    )
                }
                Log.d("BookedSessionsVM", "Loaded ${cachedSessions.size} sessions from cache for tutor $tutorId")
                _uiState.update {
                    it.copy(
                        sessions = cachedSessions,
                        // isLoading will be set based on network availability next
                        isStale = !isInternetAvailable()
                    )
                }
            } catch (e: Exception) {
                Log.e("BookedSessionsVM", "Error loading sessions from cache for tutor $tutorId: ${e.message}", e)
                _uiState.update { it.copy(error = "Error loading cached sessions: ${e.localizedMessage}", isStale = !isInternetAvailable()) }
            }


            // 2. Fetch from Network if internet is available
            if (isInternetAvailable()) {
                Log.d("BookedSessionsVM", "Internet available, attempting server refresh for tutor $tutorId")
                // Update isLoading to true specifically for network fetch if not already true
                if (!_uiState.value.isLoading) _uiState.update { it.copy(isLoading = true) }
                refreshBookedSessionsFromServer(tutorId)
            } else {
                Log.d("BookedSessionsVM", "No internet for server refresh for tutor $tutorId, relying on cache.")
                _uiState.update { it.copy(isLoading = false) } // Ensure loading is false if no network attempt
            }
        }
    }

    private suspend fun attemptFetchBookedSessionsWithRetry(
        maxRetries: Int = 2,
        initialDelayMillis: Long = 1000L
    ): Response<List<TutoringSession>> { // Assuming API returns List<TutoringSession>
        var currentRetry = 0
        var currentDelay = initialDelayMillis
        while (true) {
            try {
                Log.d("BookedSessionsVM_Retry", "Attempt ${currentRetry + 1} to fetch booked sessions.")
                // The API endpoint `tutoringSessions()` fetches all sessions.
                // Filtering happens after fetching.
                val response = RetrofitClient.instance.tutoringSessions()
                return response
            } catch (e: Exception) {
                currentRetry++
                Log.e("BookedSessionsVM_Retry", "Attempt ${currentRetry} failed. Error: ${e.javaClass.simpleName} - ${e.message}")
                if (currentRetry > maxRetries || !isRetryableException(e)) {
                    Log.e("BookedSessionsVM_Retry", "Max retries reached or non-retryable exception. Rethrowing.")
                    throw e
                }
                Log.d("BookedSessionsVM_Retry", "Waiting ${currentDelay}ms before next retry.")
                delay(currentDelay)
                currentDelay *= 2
            }
        }
    }

    private fun isRetryableException(e: Exception): Boolean {
        return when (e) {
            is SocketTimeoutException, is SocketException, is IOException -> true
            else -> false
        }
    }

    private fun refreshBookedSessionsFromServer(tutorId: Int) {
        Log.d("BookedSessionsVM", "refreshBookedSessionsFromServer called for tutorId: $tutorId")
        viewModelScope.launch {
            if (!_uiState.value.isLoading) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val response = attemptFetchBookedSessionsWithRetry()

                if (response.isSuccessful && response.body() != null) {
                    val allSessions = response.body()!!
                    // Filter for the specific tutor and where student is not null
                    val filteredSessions = allSessions.filter {
                        it.student != null && it.tutor_id.toIntOrNull() == tutorId
                    }
                    Log.d("BookedSessionsVM", "Fetched ${filteredSessions.size} sessions from server for tutor $tutorId.")

                    filteredSessions.forEach( { session -> Log.d("Session", session.toString())})

                    // Convert Model to Entity and save to Room
                    val sessionEntities = filteredSessions.map { session ->
                        BookedSessionEntity(
                            id = session.id,
                            tutorIdString = session.tutor_id,
                            studentName = session.student,
                            courseName = session.course,
                            universityName = session.university,
                            dateTime = session.date_time,
                            cost = session.cost,
                            tutorPhone = session.tutor_phone
                        )
                    }

                    withContext(Dispatchers.IO) {
                        bookedSessionDao.clearBookedSessionsForTutor(tutorId.toString())
                        bookedSessionDao.insertBookedSessions(sessionEntities)
                    }

                    _uiState.update {
                        it.copy(
                            sessions = filteredSessions,
                            isLoading = false,
                            isStale = false,
                            error = null
                        )
                    }
                } else {
                    val errorMsg = "Error fetching sessions from server: Code=${response.code()} Message=${response.message()}"
                    Log.e("BookedSessionsVM", errorMsg)
                    _uiState.update { it.copy(isLoading = false, error = errorMsg, isStale = true) }
                }
            } catch (e: Exception) {
                Log.e("BookedSessionsVM", "Network operation failed for booked sessions: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.localizedMessage}", isStale = true) }
            }
        }
    }

    fun checkForStaleDataAndRefreshIfNeeded() {
        Log.d("BookedSessionsVM", "checkForStaleDataAndRefreshIfNeeded. Current state: ${_uiState.value}")
        val currentlyConnected = isInternetAvailable()
        val currentState = _uiState.value

        if (currentlyConnected) {
            if (currentState.isStale || currentState.error != null) {
                Log.d("BookedSessionsVM", "Network connected, data stale/error. Triggering refresh.")
                if (!currentState.isLoading) {
                    currentLoadedTutorId?.let { tutorId ->
                        _uiState.update { it.copy(isLoading = true, error = null) }
                        refreshBookedSessionsFromServer(tutorId)
                    } ?: Log.w("BookedSessionsVM", "Cannot refresh on check: currentLoadedTutorId is null.")
                } else {
                    Log.d("BookedSessionsVM", "Refresh check: Already loading.")
                }
            } else {
                Log.d("BookedSessionsVM", "Network connected, data not stale. Ensuring isStale = false.")
                _uiState.update { it.copy(isStale = false, isLoading = false) }
            }
        } else {
            Log.d("BookedSessionsVM", "Network not connected. Ensuring isStale = true.")
            if (!currentState.isStale || currentState.isLoading) {
                _uiState.update { it.copy(isStale = true, isLoading = false) }
            }
        }
    }

    fun notifyNetworkStatusChanged(isConnected: Boolean) {
        Log.d("BookedSessionsVM", "notifyNetworkStatusChanged: isConnected = $isConnected. Current state: ${_uiState.value}")
        viewModelScope.launch {
            if (isConnected) {
                if (_uiState.value.isLoading && (_uiState.value.isStale || _uiState.value.error != null)) {
                    Log.d("BookedSessionsVM", "Notify: Network back, refresh likely in progress. Ignoring.")
                    return@launch
                }
                if (_uiState.value.isStale || _uiState.value.error != null) {
                    Log.d("BookedSessionsVM", "Notify: Network back, data stale/error. Delaying then refreshing.")
                    _uiState.update { it.copy(isLoading = true, error = null) }
                    delay(1500L)
                    currentLoadedTutorId?.let { tutorId ->
                        refreshBookedSessionsFromServer(tutorId)
                    } ?: Log.w("BookedSessionsVM", "Notify: Cannot refresh, currentLoadedTutorId is null.")
                } else {
                    Log.d("BookedSessionsVM", "Notify: Network back, data not stale. Ensuring isStale=false.")
                    _uiState.update { it.copy(isStale = false, isLoading = false) }
                }
            } else {
                Log.d("BookedSessionsVM", "Notify: Network lost. Marking data as stale.")
                if (!_uiState.value.isStale || _uiState.value.isLoading) {
                    _uiState.update { it.copy(isStale = true, isLoading = false) }
                }
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
