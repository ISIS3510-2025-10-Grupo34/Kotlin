package com.tutorapp.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.text.SimpleDateFormat
import java.util.*

object NetworkUtils {
    fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false // Safe cast and early return

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }


    fun shouldShowRatingWarning(context: Context, tutorId: String): Boolean {
        val prefs = context.getSharedPreferences("rating_warnings", Context.MODE_PRIVATE)
        val lastShownKey = "lastShown_$tutorId"
        val lastShown = prefs.getLong(lastShownKey, 0L)

        if (lastShown == 0L) return true

        val now = System.currentTimeMillis()
        val calendarNow = Calendar.getInstance().apply { timeInMillis = now }
        val calendarLastShown = Calendar.getInstance().apply { timeInMillis = lastShown }

        // Compara si es otro día
        val isSameDay = calendarNow.get(Calendar.YEAR) == calendarLastShown.get(Calendar.YEAR) &&
                calendarNow.get(Calendar.DAY_OF_YEAR) == calendarLastShown.get(Calendar.DAY_OF_YEAR)

        return !isSameDay
    }

    fun markRatingWarningAsShown(context: Context, tutorId: String) {
        val prefs = context.getSharedPreferences("rating_warnings", Context.MODE_PRIVATE)
        prefs.edit().putLong("lastShown_$tutorId", System.currentTimeMillis()).apply()
    }



}
