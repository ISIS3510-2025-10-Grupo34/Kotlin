package com.tutorapp.viewModels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.BookedSessionCalendarDao
import com.tutorapp.data.BookedSessionCalendarEntity
import com.tutorapp.models.BookedSession
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CalendarViewModel(
    application: Application,
    private val bookedSessionCalendarDao: BookedSessionCalendarDao
) : AndroidViewModel(application) {
    private val _bookedSessions = MutableStateFlow<List<BookedSession>>(emptyList())
    val bookedSessions: StateFlow<List<BookedSession>> = _bookedSessions.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isStale = MutableStateFlow(false)
    val isStale: StateFlow<Boolean> = _isStale.asStateFlow()

    fun loadBookedSessions(userId: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // First try to load from Room cache
                val cachedSessions = bookedSessionCalendarDao.getAllSessions()
                if (cachedSessions.isNotEmpty()) {
                    _bookedSessions.value = cachedSessions.map { it.toDomain() }
                    _isStale.value = true
                }

                // If we have internet and either force refresh or no cache, fetch from API
                if (isNetworkAvailable() && (forceRefresh || cachedSessions.isEmpty())) {
                    val response = RetrofitClient.instance.getBookedSessions(userId)
                    if (response.isSuccessful) {
                        val sessions = response.body()?.booked_sessions ?: emptyList()
                        
                        // Save to Room
                        bookedSessionCalendarDao.clearAll()
                        bookedSessionCalendarDao.insertAll(sessions.map { it.toEntity() })
                        
                        _bookedSessions.value = sessions
                        _isStale.value = false
                    } else {
                        _error.value = "Error loading sessions: ${response.code()}"
                    }
                } else if (!isNetworkAvailable() && cachedSessions.isEmpty()) {
                    _error.value = "No internet connection and no cached data available"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun getSessionsForDate(date: LocalDate): List<BookedSession> {
        return _bookedSessions.value.filter { session ->
            val sessionDate = LocalDateTime.parse(
                session.dateTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            ).toLocalDate()
            sessionDate == date
        }
    }

    fun getSessionCountForDate(date: LocalDate): Int {
        return getSessionsForDate(date).size
    }

    fun getMaxSessionsInMonth(): Int {
        val currentMonth = _selectedDate.value?.month ?: return 0
        return _bookedSessions.value
            .map { LocalDateTime.parse(it.dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate() }
            .filter { it.month == currentMonth }
            .groupingBy { it }
            .eachCount()
            .values
            .maxOrNull() ?: 0
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun BookedSessionCalendarEntity.toDomain(): BookedSession {
        return BookedSession(
            id = id,
            tutorName = tutorName,
            courseName = courseName,
            cost = cost,
            dateTime = dateTime,
            student = student
        )
    }

    private fun BookedSession.toEntity(): BookedSessionCalendarEntity {
        return BookedSessionCalendarEntity(
            id = id,
            tutorName = tutorName,
            courseName = courseName,
            cost = cost,
            dateTime = dateTime,
            student = student
        )
    }
} 