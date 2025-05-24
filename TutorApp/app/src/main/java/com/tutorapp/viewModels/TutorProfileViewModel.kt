package com.tutorapp.viewModels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.*
import com.tutorapp.data.*
import com.tutorapp.models.*
import com.tutorapp.remote.RetrofitClient
import com.tutorapp.views.TutorProfileUiState // Ensure this import is correct
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException

class TutorProfileViewModel(
    app: Application,
    private val dao: TutorProfileDao
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(TutorProfileUiState())
    val uiState: StateFlow<TutorProfileUiState> = _uiState.asStateFlow()

    private var lastLoadedTutorId: Int? = null

    // Called for initial load or when a full load cycle (cache then optional server) is desired.
    fun loadTutorProfile(tutorId: Int) {
        lastLoadedTutorId = tutorId
        Log.d("TutorProfileVM", "loadTutorProfile called for tutorId: $tutorId")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Step 1: Load from cache
            var cachedProfileResponse: GetTutorProfileResponse? = null
            val profileEntity = dao.loadTutorProfile()
            if (profileEntity != null) {
                val reviews = dao.loadReviews(tutorId).map { Review(it.rating, it.comment) }
                cachedProfileResponse = GetTutorProfileResponse(
                    data = TutorData(
                        name = profileEntity.name,
                        university = profileEntity.university,
                        ratings = profileEntity.ratings,
                        reviews = reviews,
                        whatsappContact = profileEntity.whatsappContact,
                        subjects = profileEntity.subjects
                    )
                )
            }
            var cachedInsight: GetTimeToBookInsightResponse? = null
            dao.loadInsight()?.let { insightEntity ->
                cachedInsight = GetTimeToBookInsightResponse(insightEntity.message, insightEntity.time)
            }

            val internetCurrentlyAvailable = isInternetAvailable()
            if (cachedProfileResponse != null) {
                _uiState.update { state ->
                    state.copy(
                        profile = cachedProfileResponse,
                        insight = cachedInsight,
                        isStale = !internetCurrentlyAvailable,
                        isLoading = internetCurrentlyAvailable // Only keep loading if we plan to fetch
                    )
                }
            } else {
                _uiState.update { it.copy(isStale = !internetCurrentlyAvailable, isLoading = internetCurrentlyAvailable) }
            }

            // Step 2: If internet is available, try to refresh from server
            if (internetCurrentlyAvailable) {
                Log.d("TutorProfileVM", "Internet available in loadTutorProfile, attempting server refresh for $tutorId")
                refreshTutorProfileFromServer(tutorId)
            } else {
                Log.d("TutorProfileVM", "No internet for initial server refresh for $tutorId, relying on cache.")
                // If not loading from network, ensure isLoading is false.
                if(!internetCurrentlyAvailable) _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun attemptFetchWithRetry(
        tutorId: Int,
        maxRetries: Int = 2, // Total 3 attempts (1 initial + 2 retries)
        initialDelayMillis: Long = 1000L // Delay before the first retry
    ): Pair<retrofit2.Response<GetTutorProfileResponse>, retrofit2.Response<GetTimeToBookInsightResponse>> {
        var currentRetry = 0
        var currentDelay = initialDelayMillis
        while (true) {
            try {
                Log.d("TutorProfileVM_Retry", "Attempt ${currentRetry + 1} to fetch data for tutorId: $tutorId")
                val respP = RetrofitClient.instance.getTutorProfile(tutorId) // Suspend call
                val respI = RetrofitClient.instance.getTimeToBookInsight(tutorId) // Suspend call
                return Pair(respP, respI)
            } catch (e: Exception) {
                currentRetry++
                Log.e("TutorProfileVM_Retry", "Attempt $currentRetry failed for tutorId: $tutorId. Error: ${e.javaClass.simpleName} - ${e.message}")
                if (currentRetry > maxRetries || !isRetryableException(e)) {
                    Log.e("TutorProfileVM_Retry", "Max retries reached or non-retryable exception for tutorId: $tutorId. Rethrowing.")
                    throw e
                }
                Log.d("TutorProfileVM_Retry", "Waiting ${currentDelay}ms before next retry for tutorId: $tutorId.")
                delay(currentDelay)
                currentDelay *= 2
            }
        }
    }

    private fun isRetryableException(e: Exception): Boolean {
        return when (e) {
            is SocketTimeoutException,
            is SocketException,
            is IOException -> true
            else -> false
        }
    }
    private fun refreshTutorProfileFromServer(tutorId: Int) {
        Log.d("TutorProfileVM", "refreshTutorProfileFromServer called for tutorId: $tutorId. Current isLoading: ${_uiState.value.isLoading}")
        viewModelScope.launch {
            if (!_uiState.value.isLoading) { // Ensure isLoading is true if we are refreshing
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val (respP, respI) = attemptFetchWithRetry(tutorId)

                if (respP.isSuccessful && respP.body() != null && respI.isSuccessful && respI.body() != null) {
                    val perfil = respP.body()!!
                    val insight = respI.body()!!
                    Log.d("TutorProfileVM", "Successfully fetched profile & insight for $tutorId after retries (if any).")

                    dao.clearReviews(tutorId)
                    dao.saveTutorProfile(
                        TutorProfileEntity(
                            id = tutorId, name = perfil.data.name, university = perfil.data.university,
                            ratings = perfil.data.ratings, whatsappContact = perfil.data.whatsappContact, subjects = perfil.data.subjects
                        )
                    )
                    dao.saveReviews(perfil.data.reviews.map { review ->
                        // Ensure ReviewEntity is created correctly, assuming tutorId is part of it or handled by DAO
                        ReviewEntity(rating = review.rating, comment = review.comment)
                    })
                    dao.saveInsight(
                        InsightEntity(id = 1, message = insight.message, time = insight.time)
                    )

                    _uiState.update { state ->
                        state.copy(
                            profile = perfil, insight = insight,
                            isLoading = false, isStale = false, error = null
                        )
                    }
                } else {
                    val errorMsgBuilder = StringBuilder("Error fetching from server after retries for tutorId $tutorId: ")
                    if (!respP.isSuccessful) errorMsgBuilder.append("Profile Error Code=${respP.code()}(${respP.message()}). ")
                    if (!respI.isSuccessful) errorMsgBuilder.append("Insight Error Code=${respI.code()}(${respI.message()}).")
                    Log.e("TutorProfileVM", errorMsgBuilder.toString())
                    _uiState.update { it.copy(isLoading = false, error = errorMsgBuilder.toString(), isStale = true) }
                }
            } catch (e: Exception) {
                Log.e("TutorProfileVM", "Network operation failed definitively for $tutorId: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.localizedMessage}", isStale = true) }
            }
        }
    }

    /**
     * Called when the Activity starts, to ensure the UI reflects the current network state
     * and triggers a refresh if data is stale and network is available.
     */
    fun checkForStaleDataAndRefreshIfNeeded() {
        Log.d("TutorProfileVM", "checkForStaleDataAndRefreshIfNeeded called. Current state: ${_uiState.value}")
        val currentlyConnected = isInternetAvailable()
        val currentState = _uiState.value

        if (currentlyConnected) {
            if (currentState.isStale || currentState.error != null) {
                // If connected and data is stale or was an error, attempt refresh
                Log.d("TutorProfileVM", "Network is connected, and data is stale or has error. Triggering refresh.")
                if (!currentState.isLoading) { // Avoid concurrent refreshes
                    _uiState.update { it.copy(isLoading = true, error = null) } // isStale will be updated by refresh
                    lastLoadedTutorId?.let { tutorId ->
                        // Consider a small delay here too if needed, similar to notifyNetworkStatusChanged
                        // For now, direct refresh.
                        refreshTutorProfileFromServer(tutorId)
                    } ?: run {
                        Log.w("TutorProfileVM", "Cannot refresh on check: lastLoadedTutorId is null.")
                        _uiState.update { it.copy(isLoading = false, isStale = false) } // Can't refresh, assume not stale
                    }
                } else {
                    Log.d("TutorProfileVM", "Refresh check: Already loading, no new refresh triggered.")
                }
            } else {
                // Connected and not stale, ensure UI reflects this
                Log.d("TutorProfileVM", "Network is connected, data is not stale. Ensuring isStale = false.")
                _uiState.update { it.copy(isStale = false, isLoading = false) } // Ensure loading is also false
            }
        } else {
            // Not connected, ensure UI reflects stale state
            Log.d("TutorProfileVM", "Network is not connected. Ensuring isStale = true.")
            if (!currentState.isStale || currentState.isLoading) { // If not already stale, or if it was loading
                _uiState.update { it.copy(isStale = true, isLoading = false) }
            }
        }
    }

    fun notifyNetworkStatusChanged(isConnected: Boolean) {
        Log.d("TutorProfileVM", "notifyNetworkStatusChanged: isConnected = $isConnected. Current state: ${_uiState.value}")
        viewModelScope.launch {
            if (isConnected) {
                // This guard is important to prevent multiple refreshes if onAvailable fires rapidly
                // or if checkForStaleDataAndRefreshIfNeeded also triggers a refresh.
                if (_uiState.value.isLoading && (_uiState.value.isStale || _uiState.value.error != null) ) {
                    Log.d("TutorProfileVM", "notifyNetwork: Network back, but refresh likely already in progress due to stale/error. Ignoring direct refresh here.")
                    // If it's loading AND stale/error, it means a refresh was probably just triggered.
                    // We might just ensure isStale is false if the loading eventually succeeds.
                    // For now, let the ongoing load complete. If it fails, isStale will remain true.
                    // If it succeeds, isStale will become false.
                    return@launch
                }

                if (_uiState.value.isStale || _uiState.value.error != null) {
                    Log.d("TutorProfileVM", "notifyNetwork: Network back and data is stale/error. Adding small delay then triggering refresh.")
                    _uiState.update { it.copy(isLoading = true, error = null) } // isStale remains true from copy
                    delay(1500L) // Delay to allow network to stabilize
                    Log.d("TutorProfileVM", "notifyNetwork: Delay complete. Triggering refresh.")
                    lastLoadedTutorId?.let { tutorId ->
                        refreshTutorProfileFromServer(tutorId)
                    } ?: run {
                        Log.w("TutorProfileVM", "notifyNetwork: Network back, but lastLoadedTutorId is null. Cannot refresh.")
                        _uiState.update { it.copy(isLoading = false, isStale = false) }
                    }
                } else {
                    Log.d("TutorProfileVM", "notifyNetwork: Network back, data not stale and no error. Ensuring isStale=false.")
                    _uiState.update { it.copy(isStale = false, isLoading = false) } // Ensure loading is also false
                }
            } else { // isConnected is false
                Log.d("TutorProfileVM", "notifyNetwork: Network lost. Marking data as stale.")
                if (!_uiState.value.isStale || _uiState.value.isLoading) {
                    _uiState.update { it.copy(isStale = true, isLoading = false) }
                }
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        // ... (implementation remains the same)
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return false.also { Log.d("InternetCheck", "No active network") }
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false.also { Log.d("InternetCheck", "No capabilities") }
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        Log.d("InternetCheck", "isInternetAvailable: hasInternet=$isInternet, isValidated=$isValidated")
        return isInternet && isValidated
    }
}
