package com.tutorapp.views

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.tutorapp.models.Notification
import com.tutorapp.ui.theme.TutorAppTheme
import com.tutorapp.viewModels.NotificationCenterViewModel
import com.tutorapp.viewModels.StudentProfileViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationCenterActivity: ComponentActivity(){
    private val notificationCenterViewModel: NotificationCenterViewModel by viewModels()
    private val studentProfileViewModel: StudentProfileViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra("ID") !!
        lifecycleScope.launch {
            val universityName = studentProfileViewModel.getStudentProfileBody(id)!!.data.university
            notificationCenterViewModel.getNotificationsByUniversityName(universityName){}

            setContent{
                TutorAppTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                        NotificationCenterScreen(modifier = Modifier.padding(innerPadding), notificationCenterViewModel, universityName)
                    }
                }
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(modifier: Modifier, notificationCenterViewModel: NotificationCenterViewModel, universityName:String){
    notificationCenterViewModel.getNotificationsByUniversityName(universityName){}
    val notifications = notificationCenterViewModel.notifications
    val scrollState = rememberScrollState()
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            notifications
                .sortedByDescending { LocalDateTime.parse(it.date, formatter) }
                .forEach{ notification ->
                NotificationCard(notification)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Place: ${notification.place}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Date: ${notification.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}