package com.tutorapp.viewModels

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.tutorapp.data.AppDatabase

class CalendarViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            val dao = AppDatabase.getDatabase(application).bookedSessionDao()
            return CalendarViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 