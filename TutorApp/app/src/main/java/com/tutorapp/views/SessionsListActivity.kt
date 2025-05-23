package com.tutorapp.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tutorapp.models.BookedSession
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SessionsListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val date = intent.getStringExtra("DATE") ?: return
        val sessions = intent.getSerializableExtra("SESSIONS") as? ArrayList<BookedSession> ?: return
        
        setContent {
            MaterialTheme {
                SessionsListScreen(date = date, sessions = sessions)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsListScreen(date: String, sessions: List<BookedSession>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessions for $date") },

            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn {
                items(sessions) { session ->
                    SessionCard(session = session)
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: BookedSession) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = session.courseName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tutor: ${session.tutorName}",
                fontSize = 16.sp
            )
            Text(
                text = "Time: ${session.dateTime}",
                fontSize = 16.sp
            )
            Text(
                text = "Cost: $${session.cost}",
                fontSize = 16.sp
            )
        }
    }
} 