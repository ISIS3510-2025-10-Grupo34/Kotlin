package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.GetNotificationsRequest
import com.tutorapp.models.Notification
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationCenterViewModel : ViewModel(){

    var notifications: List<Notification>  by mutableStateOf(emptyList())
    private set

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
}