package com.tutorapp.data

import android.app.Application
import androidx.lifecycle.*
import com.tutorapp.data.AppDatabase
import com.tutorapp.viewModels.TutorProfileViewModel

class TutorProfileViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorProfileViewModel::class.java)) {
            val dao = AppDatabase.getDatabase(application).tutorProfileDao()
            return TutorProfileViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}