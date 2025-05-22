@file:OptIn(ExperimentalMaterial3Api::class)

package com.tutorapp.views

import android.content.Context // Required for ConnectivityManager
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
// import androidx.compose.material.icons.filled.Logout // If needed
// import androidx.compose.material.icons.filled.Notifications // If needed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tutorapp.data.BookedSessionsViewModelFactory // Import the factory
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.models.TutoringSession
import com.tutorapp.remote.RetrofitClient // For evictAllConnections
import com.tutorapp.viewModels.BookedSesssionsViewModel

class BookedSessionsActivity : ComponentActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkRequest: NetworkRequest

    private val viewModel: BookedSesssionsViewModel by viewModels {
        BookedSessionsViewModelFactory(application) // Use the factory
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("BookedSessionsActivity", "NetworkCallback: onAvailable")
            viewModel.notifyNetworkStatusChanged(isConnected = true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d("BookedSessionsActivity", "NetworkCallback: onLost")
            RetrofitClient.evictAllConnections()
            viewModel.notifyNetworkStatusChanged(isConnected = false)
        }
    }

    private var currentTutorId: Int = -1 // Initialize appropriately

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserInfo: LoginTokenDecoded? = intent.getParcelableExtra("TOKEN_KEY")
        if (currentUserInfo != null) {
            currentTutorId = currentUserInfo.id
        } else {
            // Handle case where tutorId is not available (e.g., finish activity or show error)
            Log.e("BookedSessionsActivity", "Tutor ID not found in intent.")
            finish() // Example: close if no tutor ID
            return
        }

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        if (savedInstanceState == null) {
            viewModel.loadBookedSessions(currentTutorId)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            BookedSessionsScreen(
                uiState = uiState,
                onBackClicked = { finish() } // Handle back navigation
            )
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("BookedSessionsActivity", "onStart called.")
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        viewModel.checkForStaleDataAndRefreshIfNeeded()
    }

    override fun onStop() {
        super.onStop()
        Log.d("BookedSessionsActivity", "onStop called.")
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            Log.w("BookedSessionsActivity", "Network callback already unregistered or never registered: ${e.message}")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookedSessionsScreen(
    uiState: BookedSessionsUiState,
    onBackClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Booked Sessions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF192650), // Your primary color
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.White // Background for the content area
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Stale data message
            if (uiState.isStale && !uiState.isLoading) { // Show only if not actively loading new data
                Text(
                    text = "No internet connection: displayed information may be outdated. Will refresh when reconnected.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Loading state (full screen if no data yet)
            if (uiState.isLoading && uiState.sessions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Error state (if sessions list is empty and error is present)
            else if (uiState.sessions.isEmpty() && uiState.error != null && !uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            // No sessions state (and not loading, no error)
            else if (uiState.sessions.isEmpty() && !uiState.isLoading && uiState.error == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (uiState.isStale) "No sessions found in cache." else "No sessions booked yet.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            // Display sessions list
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.sessions, key = { it.id }) { session ->
                        BookedSessionItem(session = session)
                    }
                }
            }
        }
    }
}

@Composable
fun BookedSessionItem(session: TutoringSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF192650)), // Your primary color
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = session.student?.firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = session.student ?: "Unknown Student",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = session.course,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = session.university,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date: ${session.date_time}", // Consider formatting this date/time
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
            // You could add cost or other details if needed
            // Text(text = "Cost: $${session.cost}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
