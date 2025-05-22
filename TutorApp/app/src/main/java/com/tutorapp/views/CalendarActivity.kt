package com.tutorapp.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tutorapp.models.BookedSession
import com.tutorapp.viewModels.CalendarViewModel
import com.tutorapp.viewModels.CalendarViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val viewModel: CalendarViewModel = viewModel(
                    factory = CalendarViewModelFactory.provideFactory(application)
                )
                CalendarScreen(
                    viewModel = viewModel,
                    onDateSelected = { date, sessions ->
                        val intent = Intent(this, SessionsListActivity::class.java).apply {
                            putExtra("DATE", date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")))
                            putExtra("SESSIONS", ArrayList(sessions))
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onDateSelected: (LocalDate, List<BookedSession>) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val selectedDate by viewModel.selectedDate.collectAsState()
    val bookedSessions by viewModel.bookedSessions.collectAsState()
    val sessionsForSelectedDate by viewModel.sessionsForSelectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()

    LaunchedEffect(Unit) {
        Session.userid?.let { userId ->
            viewModel.loadBookedSessions(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My sessions") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle back navigation */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOffline) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = "Offline Mode",
                            tint = Color.Gray
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Text("<", fontSize = 20.sp)
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${currentMonth.year}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Text(">", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days of week header
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            val firstDayOfMonth = currentMonth.atDay(1)
            val lastDayOfMonth = currentMonth.atEndOfMonth()
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
            val daysInMonth = lastDayOfMonth.dayOfMonth

            val maxSessions = viewModel.getMaxSessionsInMonth()

            Column {
                var currentDay = 1
                for (week in 0..5) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (dayOfWeek in 0..6) {
                            if (week == 0 && dayOfWeek < firstDayOfWeek) {
                                Box(modifier = Modifier.weight(1f))
                            } else if (currentDay <= daysInMonth) {
                                val date = currentMonth.atDay(currentDay)
                                val sessionCount = viewModel.getSessionCountForDate(date)
                                val color = if (sessionCount > 0) {
                                    val intensity = (sessionCount.toFloat() / maxSessions).coerceIn(0f, 1f)
                                    Color(0xFF2196F3).copy(alpha = 0.2f + (intensity * 0.8f))
                                } else {
                                    Color.LightGray.copy(alpha = 0.2f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable(enabled = sessionCount > 0) {
                                            viewModel.selectDate(date)
                                            onDateSelected(date, sessionsForSelectedDate)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentDay.toString(),
                                        color = if (sessionCount > 0) Color.Black else Color.Gray
                                    )
                                }
                                currentDay++
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected date sessions
            selectedDate?.let { date ->
                if (sessionsForSelectedDate.isNotEmpty()) {
                    Text(
                        text = "Sessions for ${date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(sessionsForSelectedDate) { session ->
                            SessionCard(session = session)
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

