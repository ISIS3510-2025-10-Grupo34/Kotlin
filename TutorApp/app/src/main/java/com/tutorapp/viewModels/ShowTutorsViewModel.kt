package com.tutorapp.viewModels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.format.DateTimeFormatter
import androidx.lifecycle.*
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.CachedAchievementEntity
import com.tutorapp.data.GamificationCacheDao
import com.tutorapp.data.TutoringSessionDao
import com.tutorapp.data.TutoringSessionEntity
import com.tutorapp.models.BookTutoringSessionRequest
import com.tutorapp.models.TutoringSession
import com.tutorapp.models.PostFilterCounterIncreaseRequest
import com.tutorapp.models.PostTimeToBookRequest
import com.tutorapp.models.SearchResultFilterResponse
import com.tutorapp.models.UpdateGamificationRequest
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit


class ShowTutorsViewModel(
    application: Application,
    private val tutoringSessionDao: TutoringSessionDao,
    private val gamificationCacheDao: GamificationCacheDao
) : AndroidViewModel(application) {

    private val _sessions = MutableStateFlow<List<TutoringSession>>(emptyList())
    val sessions: StateFlow<List<TutoringSession>> = _sessions

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
                _sessions.value = cachedDomainSessions

                Log.i("ViewModel", "Cache Load: Loaded ${cachedDomainSessions.size} sessions from Room. Data is initially STALE.")

                // 👉 Actualiza números faltantes si hay internet
                if (isNetworkAvailable()) {
                    cachedDomainSessions.forEach { session ->
                        if (session.tutor_phone.isBlank()) {
                            fetchTutorPhoneNumber(session.tutor_id.toInt(), session.id) { phone ->
                                // Nada más que hacer, ya se guarda en Room dentro de fetchTutorPhoneNumber
                                Log.d("ViewModel", "Phone updated for session ${session.id}: $phone")
                            }
                        }
                    }

                    // También refresca desde backend
                    try {
                        val response = RetrofitClient.instance.tutoringSessions()
                        if (response.isSuccessful) {
                            val networkSessions = response.body()?.filter { it.student == null } ?: emptyList()
                            val newEntities = mapDomainToEntities(networkSessions)

                            withContext(Dispatchers.IO) {
                                tutoringSessionDao.clearAll()
                                tutoringSessionDao.insertAll(newEntities)
                            }


                            _sessions.value = networkSessions
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
                _sessions.value = emptyList()
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
        val sourceList = sessions.value
        val filteredList = sourceList.filter { session ->
            (university.isEmpty() || session.university.contains(university, ignoreCase = true)) &&
                    (course.isEmpty() || session.course.contains(course, ignoreCase = true)) &&
                    (professor.isEmpty() || session.tutor.contains(professor, ignoreCase = true))
        }
        _sessions.value = filteredList
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
                tutor_phone = entity.tutor_phone, university = entity.university,
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
                tutor_phone = defaultPhoneNumber, university = session.university,
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun bookTutoringSession(userId: Int, tutoringSessionId: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = BookTutoringSessionRequest(userId, tutoringSessionId)
                val response = RetrofitClient.instance.bookTutoringSession(request)
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Booking successful"
                    onResult(true, message)
                    Log.i("Booking", message)
                    checkAndAssignAchievements(userId.toString())
                    checkCommittedStudentAchievement(userId.toString()) { achievement ->
                        Log.i("Achievement", "Awarded: $achievement")
                    }
                    checkThreeBookingsInAWeekAchievement(userId.toString()){ achievement ->
                        Log.i("Achievement", "Awarded: $achievement")
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    onResult(false, errorMsg)
                    Log.e("Booking", "Failed: $errorMsg")
                }
            } catch (e: Exception) {
                onResult(false, "Network error: ${e.message}")
                Log.e("Booking", "Exception: ${e.message}")
            }
        }
    }


    fun fetchTutorPhoneNumber(tutorId: Int, sessionId: Int, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getTutorProfile(tutorId)
                if (response.isSuccessful) {
                    val number = response.body()?.data?.whatsappContact
                    if (number != null) {
                        withContext(Dispatchers.IO) {
                            tutoringSessionDao.updateTutorPhoneNumber(sessionId, number)
                        }
                    }
                    onResult(number)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    private fun checkAndAssignAchievements(studentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val previousBookings = tutoringSessionDao.getBookingsByStudent(studentId)
            if (previousBookings.isEmpty()) {
                // Award "First Booking" achievement
                Log.i("Achievement", "Awarding First Booking achievement to $studentId")
                // Aquí podrías guardar en cache o usar tu DAO para persistir este logro
                gamificationCacheDao.insertAchievement(
                    CachedAchievementEntity(
                        studentId = studentId,
                        achievementName = "First Booking",
                        timestamp = System.currentTimeMillis()
                    )
                )
                syncAchievementWithBackend(studentId, "First Booking")
            }
        }
    }


    fun checkCommittedStudentAchievement(studentId: String, onAchievementUnlocked: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dao = AppDatabase.getDatabase(getApplication()).tutoringSessionDao()
                val bookings = dao.getBookingsByStudent(studentId)

                // Map of tutorId to number of bookings
                val bookingCountByTutor = bookings.groupingBy { it.tutor_id }.eachCount()

                // Find if there's any tutor with 3 or more bookings
                val qualifiedTutor = bookingCountByTutor.entries.firstOrNull { it.value >= 3 }

                if (qualifiedTutor != null) {
                    Log.i("Achievement", "Awarding Committed Student achievement to $studentId with tutor ${qualifiedTutor.key}")

                    // Save achievement in Room or trigger logic
                    val achievement = "Committed Student"
                    val timestamp = System.currentTimeMillis()
                    val gamificationCacheDao = AppDatabase.getDatabase(getApplication()).gamificationCacheDao()

                    gamificationCacheDao.insertAchievement(
                        CachedAchievementEntity(
                            studentId = studentId,
                            achievementName = achievement,
                            timestamp = timestamp
                        )
                    )

                    withContext(Dispatchers.Main) {
                        onAchievementUnlocked(achievement)
                    }
                }
            } catch (e: Exception) {
                Log.e("AchievementCheck", "Error checking Committed Student: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkThreeBookingsInAWeekAchievement(studentId: String, onAchievementUnlocked: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getBookedSessionsV2(studentId)
                if (response.isSuccessful) {
                    val bookings = response.body() ?: emptyList()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    val bookingDates = bookings.mapNotNull {
                        try {
                            LocalDate.parse(it.dateTime.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE)
                        } catch (e: Exception) {
                            null
                        }
                    }.sorted()

                    // Sliding window to find 3 bookings in a 7-day period
                    for (i in 0 until bookingDates.size - 2) {
                        val first = bookingDates[i]
                        val third = bookingDates[i + 2]
                        if (ChronoUnit.DAYS.between(first, third) <= 6) {
                            withContext(Dispatchers.Main) {
                                onAchievementUnlocked("3 Bookings in a Week")
                            }

                            // Persist locally (opcional)
                            gamificationCacheDao.insertAchievement(
                                CachedAchievementEntity(
                                    studentId = studentId,
                                    achievementName = "3 Bookings in a Week",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            break
                        }
                    }
                } else {
                    Log.e("Achievement", "API error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Achievement", "Error checking 3 bookings in a week: ${e.message}")
            }
        }
    }

    private fun syncAchievementWithBackend(studentId: String, achievement: String, points: Int = 50) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = UpdateGamificationRequest(
                    user_id= studentId.toInt(),
                    achievement = achievement,
                    points = points
                )
                val response = RetrofitClient.instance.updateGamification(payload)
                if (response.isSuccessful) {
                    Log.i("GamificationSync", "Achievement synced: $achievement")
                } else {
                    Log.e("GamificationSync", "Failed to sync: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("GamificationSync", "Error syncing achievement: ${e.message}")
            }
        }
    }






}