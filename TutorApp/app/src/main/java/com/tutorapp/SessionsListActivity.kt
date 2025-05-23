package com.tutorapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tutorapp.models.BookedSession
import com.tutorapp.viewModels.CalendarViewModel
import com.tutorapp.viewModels.CalendarViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SessionsListActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedDateStr = intent.getStringExtra("selectedDate")
        val selectedDate = if (selectedDateStr != null) {
            LocalDate.parse(selectedDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } else {
            LocalDate.now()
        }

        val viewModel: CalendarViewModel by viewModels {
            CalendarViewModelFactory.provideFactory(application)
        }

        setContent {
            val context = LocalContext.current
            val sessions by viewModel.sessionsForSelectedDate.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val error by viewModel.error.collectAsState()
            var showDetail by remember { mutableStateOf<BookedSession?>(null) }

            LaunchedEffect(selectedDate) {
                viewModel.selectDateAndLoadSessions(selectedDate)
            }

            if (showDetail != null) {
                // Aquí podrías mostrar un detalle Compose, pero si quieres seguir usando la Activity clásica:
                LaunchedEffect(showDetail) {
                    val intent = Intent(context, SessionDetailActivity::class.java).apply {
                        putExtra("session", showDetail)
                    }
                    context.startActivity(intent)
                    showDetail = null
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Sessions for ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        error != null -> {
                            Text(
                                text = error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        sessions.isEmpty() -> {
                            Text(
                                text = "No sessions for this day.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            LazyColumn {
                                items(sessions) { session ->
                                    SessionItem(session) { showDetail = it }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItem(session: BookedSession, onClick: (BookedSession) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(session) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tutor: ${session.tutorName}")
            Text("Course: ${session.courseName}")
            Text("Date & Time: ${session.dateTime}")
            Text("Cost: $${session.cost}")
        }
    }
} 