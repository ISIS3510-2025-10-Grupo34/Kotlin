package com.tutorapp.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.CachedNotificationEntity
import com.tutorapp.models.GetNotificationsRequest
import com.tutorapp.models.Notification
import com.tutorapp.remote.RetrofitClient
import com.tutorapp.util.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationCenterViewModel : ViewModel(){

    var notifications: List<Notification>  by mutableStateOf(emptyList())
    private set

    private val _networkErrorOccurred = MutableStateFlow(false)
    val networkErrorOccurred: StateFlow<Boolean> = _networkErrorOccurred.asStateFlow()

    fun getNotificationsByUniversityName(universityName:String, onComplete: (List<Notification>?) -> Unit){
        viewModelScope.launch{
            try{
                val response = RetrofitClient.instance.getNotifications(GetNotificationsRequest(universityName))
                if(response.isSuccessful){
                    notifications = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        onComplete(notifications)
                    }
                }
                else{
                    println("Error: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        onComplete(null)
                    }
                }
            }
            catch (e:Exception){
                println("Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }

        }

    }

    fun postNotification(notification: Notification){
        viewModelScope.launch {
            try{
                val response = RetrofitClient.instance.postNotification(notification)
            }
            catch (e:Exception) {
                println("Exception: ${e.message}")
            }
        }
    }

    fun loadNotifications(context: Context, universityName: String, database: AppDatabase) {
        viewModelScope.launch {
            _networkErrorOccurred.value = false
            try {
                val response = RetrofitClient.instance.getNotifications(GetNotificationsRequest(universityName))
                if (response.isSuccessful) {
                    val apiData = response.body() ?: emptyList()
                    notifications = apiData

                    val entities = apiData.map {
                        CachedNotificationEntity(
                            title = it.title,
                            message = it.message,
                            place = it.place,
                            date = it.date,
                            university = universityName
                        )
                    }
                    database.cachedNotificationDao().clearForUniversity(universityName)
                    database.cachedNotificationDao().insertAll(entities)

                    // Guardar universidad si no se había guardado antes
                    LocalStorage.saveUniversity(context, universityName)
                } else {
                    throw Exception("API error")
                }
            } catch (e: Exception) {
                _networkErrorOccurred.value = true
                val cached = database.cachedNotificationDao().getNotificationsByUniversity(universityName)
                notifications = cached.map {
                    Notification(title=it.title, message=it.message, place=it.place, date=it.date, university=universityName)
                }
            }
        }
    }
    fun clearNetworkErrorFlag(){
        _networkErrorOccurred.value = false
    }

}