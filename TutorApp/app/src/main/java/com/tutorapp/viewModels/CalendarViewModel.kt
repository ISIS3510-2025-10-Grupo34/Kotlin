package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.BookedSession
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CalendarViewModel : ViewModel() {
    private val _bookedSessions = MutableStateFlow<List<BookedSession>>(emptyList())
    val bookedSessions: StateFlow<List<BookedSession>> = _bookedSessions.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadBookedSessions(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.getBookedSessions(userId)
                if (response.isSuccessful) {
                    _bookedSessions.value = response.body()?.booked_sessions ?: emptyList()
                } else {
                    _error.value = "Error loading sessions: ${response.code()}"
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
} 