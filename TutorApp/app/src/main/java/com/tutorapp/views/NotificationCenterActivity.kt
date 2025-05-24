package com.tutorapp.views

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.tutorapp.data.AppDatabase
import com.tutorapp.models.Notification
import com.tutorapp.ui.theme.TutorAppTheme
import com.tutorapp.util.LocalStorage
import com.tutorapp.viewModels.NotificationCenterViewModel
import com.tutorapp.viewModels.StudentProfileViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class NotificationCenterActivity: ComponentActivity(){
    private val notificationCenterViewModel: NotificationCenterViewModel by viewModels()
    private val studentProfileViewModel: StudentProfileViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra("ID")!!
        val db = AppDatabase.getDatabase(this)
        val savedUniversity = LocalStorage.getSavedUniversity(this)

        if (savedUniversity != null) {
            notificationCenterViewModel.loadNotifications(this, savedUniversity, db)
        } else {
            lifecycleScope.launch {
                val university = studentProfileViewModel.getStudentProfileBody(id)?.data?.university
                if (university != null) {
                    notificationCenterViewModel.loadNotifications(this@NotificationCenterActivity, university, db)
                }
            }
        }

        setContent {
            TutorAppTheme {
                NotificationCenterScreen(
                    notificationCenterViewModel = notificationCenterViewModel,
                    universityName = savedUniversity ?: ""
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(modifier: Modifier = Modifier, notificationCenterViewModel: NotificationCenterViewModel, universityName:String){

    val notifications = notificationCenterViewModel.notifications
    val scrollState = rememberScrollState()
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val networkError by notificationCenterViewModel.networkErrorOccurred.collectAsStateWithLifecycle()

    LaunchedEffect(networkError) {
        if (networkError) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "No internet connection. The notifications displayed may vary from those in real time."
                    // Puedes añadir actionLabel="OK" o duration si quieres, pero no es esencial
                )
            }
            notificationCenterViewModel.clearNetworkErrorFlag() // Esencial para resetear
        }
    }

    Scaffold( snackbarHost = { SnackbarHost(snackbarHostState) }, modifier = modifier,
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