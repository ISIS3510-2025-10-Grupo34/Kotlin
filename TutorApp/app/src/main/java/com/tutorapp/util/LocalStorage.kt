package com.tutorapp.util

import android.content.Context

object LocalStorage {
    fun saveUniversity(context: Context, university: String) {
        val prefs = context.getSharedPreferences("tutor_app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("university_name", university).apply()
    }

    fun getSavedUniversity(context: Context): String? {
        val prefs = context.getSharedPreferences("tutor_app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("university_name", null)
    }
}