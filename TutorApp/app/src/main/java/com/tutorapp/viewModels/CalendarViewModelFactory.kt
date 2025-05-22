package com.tutorapp.viewModels

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tutorapp.data.AppDatabase

object CalendarViewModelFactory {
    fun provideFactory(application: Application): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val dao = AppDatabase.getDatabase(application).bookedSessionDao()
            CalendarViewModel(application, dao)
        }
    }
} 