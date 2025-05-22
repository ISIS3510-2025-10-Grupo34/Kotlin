package com.tutorapp.views

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.LocalDate

// Data classes
data class Session2(
    val date: String,      // YYYY-MM-DD
    val count: Int         // number of sessions that day
)

data class SessionsResponse(
    val sessions: List<Session2>
)

// Retrofit API
interface ApiService {
    @GET("/users/{id}/sessions")
    suspend fun getUserSessions(
        @Path("id") userId: String
    ): SessionsResponse
}

class CalendarActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("ID") ?: return
        setContent {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalendarScreen(userId = userId) { date ->
                        // TODO: Navigate to SessionsDetailActivity
                        // startActivity(Intent(this, SessionsDetailActivity::class.java).apply {
                        //     putExtra("ID", userId)
                        //     putExtra("DATE", date)
                        // })
                    }

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    userId: String,
    onDateClick: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var sessions by remember { mutableStateOf<Map<LocalDate, Int>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    // Initialize Retrofit
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.tutorapp.com") // Ajusta tu base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ApiService::class.java)

    LaunchedEffect(userId) {
        try {
            val response = service.getUserSessions(userId)
            sessions = response.sessions.associate {
                LocalDate.parse(it.date) to it.count
            }
        } catch (e: Exception) {
            // Manejo de error
            sessions = emptyMap()
        } finally {
            loading = false
        }
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Mostrar calendario
        val today = LocalDate.now()
        val monthDays = today.withDayOfMonth(1).dayOfWeek.value // 1..7 pivot
        val daysInMonth = today.lengthOfMonth()

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Calendario de sesiones",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
                          )
            Spacer(modifier = Modifier.height(16.dp))
            // Simple grid of days
            val rows = ((daysInMonth + monthDays - 1) / 7) + 1
            for (week in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayOfWeek in 1..7) {
                        val dayIndex = week * 7 + dayOfWeek - monthDays
                        if (dayIndex in 1..daysInMonth) {
                            val date = today.withDayOfMonth(dayIndex)
                            val count = sessions[date] ?: 0
                            val enabled = count > 0
                            // Color scale: opacity relative to max sessions
                            val maxSessions = sessions.values.maxOrNull() ?: 1
                            val alpha = if (enabled) count.toFloat() / maxSessions else 0.2f
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color(0xFF192650).copy(alpha = alpha),
                                        shape = CircleShape
                                    )
                                    .clickable(enabled) {
                                        onDateClick(date.toString())
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayIndex.toString(),
                                    color = if (enabled) Color.White else Color.Gray
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
