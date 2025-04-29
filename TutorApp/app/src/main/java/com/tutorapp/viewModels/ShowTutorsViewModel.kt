package com.tutorapp.viewModels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.tutorapp.data.TutoringSessionDao
import com.tutorapp.data.TutoringSessionEntity
import com.tutorapp.models.TutoringSession
import com.tutorapp.models.PostFilterCounterIncreaseRequest
import com.tutorapp.models.PostTimeToBookRequest
import com.tutorapp.models.SearchResultFilterResponse
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowTutorsViewModel(
    application: Application,
    private val tutoringSessionDao: TutoringSessionDao
) : AndroidViewModel(application) {

    var sessions by mutableStateOf<List<TutoringSession>>(emptyList())
        private set
    var emptyFilter by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isStale by mutableStateOf(false)
        private set

    init {
        loadInitialSessions()
    }

    fun loadInitialSessions() {
        emptyFilter = false
        isLoading = true
        isStale = true

        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Attempting to load from Room cache...")
                val cachedEntities = withContext(Dispatchers.IO) {
                    tutoringSessionDao.getAvailableSessions()
                }
                val cachedDomainSessions = mapEntitiesToDomain(cachedEntities)
                sessions = cachedDomainSessions
                Log.i("ViewModel", "Cache Load: Loaded ${cachedDomainSessions.size} sessions from Room. Data is initially STALE.")

                if (isNetworkAvailable()) {
                    Log.d("ViewModel", "Network available. Attempting refresh...")
                    try {
                        val response = RetrofitClient.instance.tutoringSessions()
                        if (response.isSuccessful) {
                            val networkSessions = response.body()?.filter { it.student == null } ?: emptyList()
                            val newEntities = mapDomainToEntities(networkSessions)

                            withContext(Dispatchers.IO) {
                                tutoringSessionDao.clearAll()
                                tutoringSessionDao.insertAll(newEntities)
                            }

                            sessions = networkSessions
                            isStale = false
                            Log.i("ViewModel", "Network Refresh: Success. Loaded ${networkSessions.size}. Data is FRESH.")

                        } else {
                            Log.e("ViewModel", "Network Refresh: API error ${response.code()}. Data remains STALE.")
                        }
                    } catch (e: Exception) {
                        Log.e("ViewModel", "Network Refresh: Exception ${e.message}. Data remains STALE.")
                    }
                } else {
                    Log.i("ViewModel", "Network Refresh: No connection. Data remains STALE.")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error during initial load process", e)
                sessions = emptyList()
                isStale = true
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun onFilterClick(university: String, course: String, professor: String) {
        val isFiltering = university.isNotEmpty() || course.isNotEmpty() || professor.isNotEmpty()

        if (!isFiltering) {
            Log.d("ViewModel", "Filters cleared, reloading initial sessions.")
            loadInitialSessions()
            return
        }

        Log.d("ViewModel", "Applying filter: U='$university', C='$course', P='$professor'")
        isLoading = true
        val sourceList = sessions
        val filteredList = sourceList.filter { session ->
            (university.isEmpty() || session.university.contains(university, ignoreCase = true)) &&
                    (course.isEmpty() || session.course.contains(course, ignoreCase = true)) &&
                    (professor.isEmpty() || session.tutor.contains(professor, ignoreCase = true))
        }
        sessions = filteredList
        emptyFilter = filteredList.isEmpty()
        isLoading = false
        Log.d("ViewModel", "Filter applied. Result count: ${filteredList.size}. EmptyFilter: $emptyFilter.")

        if (isNetworkAvailable()) {
            viewModelScope.launch {
                try {
                    if (university.isNotEmpty()) RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
                    if (course.isNotEmpty()) RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
                    if (professor.isNotEmpty()) RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
                } catch (e: Exception) {
                    Log.e("ViewModel", "Failed to increase filter count", e)
                }
            }
        }
    }

    private fun mapEntitiesToDomain(entities: List<TutoringSessionEntity>): List<TutoringSession> {
        return entities.map { entity ->
            TutoringSession(
                id = entity.id, tutor = entity.tutor, tutor_id = entity.tutor_id,
                tutor_phone_number = entity.tutor_phone_number, university = entity.university,
                course = entity.course, cost = entity.cost, date_time = entity.date_time,
                student = entity.student
            )
        }
    }

    private fun mapDomainToEntities(sessions: List<TutoringSession>): List<TutoringSessionEntity> {
        val defaultPhoneNumber = "3122773641"
        return sessions.map { session ->
            TutoringSessionEntity(
                id = session.id, tutor = session.tutor, tutor_id = session.tutor_id,
                tutor_phone_number = defaultPhoneNumber, university = session.university,
                course = session.course, cost = session.cost, date_time = session.date_time,
                student = session.student
            )
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    // --- FUNCIONES ADICIONALES COMPLETAS ---

    fun getSearchResults(onResult: (Boolean, SearchResultFilterResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSearchResultsFilter()

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

    fun postTimeToBook(timeToBook: Float, tutorId: Int) {
        viewModelScope.launch {
            try {
                val body = PostTimeToBookRequest(
                    duration = timeToBook,
                    tutorId = tutorId
                )
                val response = RetrofitClient.instance.postTimeToBook(body)
                // Log Analytics or handle response if needed
                Log.i("ViewModelAnalytics", "PostTimeToBook Response: ${response.body()?.data ?: "No data"}")
            } catch (e: Exception) {
                Log.e("ViewModelError", "Error in postTimeToBook: ${e.message ?: "unknown error"}")
            }
        }
    }

    fun bookingTime(){
        viewModelScope.launch {
            try {
                // Assuming bookingTime() is an analytics event or similar fire-and-forget call
                RetrofitClient.instance.bookingTime()
                Log.i("ViewModelAnalytics", "BookingTime event sent.")
            } catch (e: Exception) {
                Log.e("ViewModelError", "Error in bookingTime: ${e.message ?: "unknown error"}")
            }
        }
    }
}