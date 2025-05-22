package com.tutorapp.viewModels

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.data.BookedSessionCache
import com.tutorapp.data.BookedSessionDao
import com.tutorapp.data.BookedSessionEntity
import com.tutorapp.models.BookedSession
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CalendarViewModel(
    application: Application,
    private val bookedSessionDao: BookedSessionDao
) : AndroidViewModel(application) {

    private val _bookedSessions = MutableStateFlow<List<BookedSession>>(emptyList())
    val bookedSessions: StateFlow<List<BookedSession>> = _bookedSessions.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _sessionsForSelectedDate = MutableStateFlow<List<BookedSession>>(emptyList())
    val sessionsForSelectedDate: StateFlow<List<BookedSession>> = _sessionsForSelectedDate.asStateFlow()

    fun loadBookedSessions(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // First try to load from cache
                val cachedSessions = withContext(Dispatchers.IO) {
                    bookedSessionDao.getAllSessions()
                }
                if (cachedSessions.isNotEmpty()) {
                    _bookedSessions.value = mapEntitiesToDomain(cachedSessions)
                }

                // Then try to load from network if available
                if (isNetworkAvailable()) {
                    _isOffline.value = false
                    try {
                        val response = RetrofitClient.instance.getBookedSessions(userId)
                        if (response.isSuccessful) {
                            val sessions = response.body()?.booked_sessions ?: emptyList()
                            _bookedSessions.value = sessions

                            // Save to Room database
                            withContext(Dispatchers.IO) {
                                bookedSessionDao.clearAll()
                                bookedSessionDao.insertAll(mapDomainToEntities(sessions))
                            }

                            // Update cache
                            sessions.groupBy { 
                                LocalDateTime.parse(it.dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate() 
                            }.forEach { (date, dateSessions) ->
                                BookedSessionCache.put(date, dateSessions)
                            }
                        } else {
                            _error.value = "Error loading sessions: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        _error.value = "Network error: ${e.message}"
                        _isOffline.value = true
                    }
                } else {
                    _isOffline.value = true
                    if (cachedSessions.isEmpty()) {
                        _error.value = "No internet connection and no cached data available"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun selectDateAndLoadSessions(date: LocalDate): List<BookedSession> {
        _selectedDate.value = date
        return loadSessionsForDate(date)
    }

    private suspend fun loadSessionsForDate(date: LocalDate): List<BookedSession> {
        // First try to get from cache
        BookedSessionCache.get(date)?.let {
            _sessionsForSelectedDate.value = it
            return it
        }

        // If not in cache, get from Room database
        val datePattern = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val sessions = withContext(Dispatchers.IO) {
            bookedSessionDao.getSessionsForDate(datePattern)
        }
        val domainSessions = mapEntitiesToDomain(sessions)
        _sessionsForSelectedDate.value = domainSessions
        return domainSessions
    }

    fun getSessionCountForDate(date: LocalDate): Int {
        return _bookedSessions.value.count { session ->
            val sessionDate = LocalDateTime.parse(
                session.dateTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            ).toLocalDate()
            sessionDate == date
        }
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
        val connectivityManager = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun mapEntitiesToDomain(entities: List<BookedSessionEntity>): List<BookedSession> {
        return entities.map { entity ->
            BookedSession(
                id = entity.id,
                tutorName = entity.tutorName,
                courseName = entity.courseName,
                cost = entity.cost,
                dateTime = entity.dateTime,
                student = entity.student
            )
        }
    }

    private fun mapDomainToEntities(sessions: List<BookedSession>): List<BookedSessionEntity> {
        return sessions.map { session ->
            BookedSessionEntity(
                id = session.id,
                tutorName = session.tutorName,
                courseName = session.courseName,
                cost = session.cost,
                dateTime = session.dateTime,
                student = session.student
            )
        }
    }
} 