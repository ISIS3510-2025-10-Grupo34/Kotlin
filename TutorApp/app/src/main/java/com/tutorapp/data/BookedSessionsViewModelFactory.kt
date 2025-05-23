package com.tutorapp.data // Or your preferred package for factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tutorapp.viewModels.BookedSesssionsViewModel

class BookedSessionsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookedSesssionsViewModel::class.java)) {
            val dao = AppDatabase.getDatabase(application).bookedSessionDao()
            return BookedSesssionsViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
