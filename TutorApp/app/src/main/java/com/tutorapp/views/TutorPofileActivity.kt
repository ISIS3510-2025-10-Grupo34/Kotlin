@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.tutorapp.views

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tutorapp.models.GetTimeToBookInsightResponse
import com.tutorapp.models.GetTutorProfileResponse
import com.tutorapp.models.Review
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.viewModels.TutorProfileViewModel
import android.net.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CalendarToday
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.TutorProfileViewModelFactory
import com.tutorapp.remote.NetworkUtils
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme

val PrimaryAppColor = Color(0xFF192650)

class TutorProfileActivity : ComponentActivity() {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkRequest: NetworkRequest

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            // Notify ViewModel that network is available
            viewModel.notifyNetworkStatusChanged(isConnected = true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            // Notify ViewModel that network is lost
            RetrofitClient.evictAllConnections()
            viewModel.notifyNetworkStatusChanged(isConnected = false)
        }
    }

    // usar nuestro ViewModelFactory
    private val viewModel: TutorProfileViewModel by viewModels {
        TutorProfileViewModelFactory(application)
    }

    private var currentTutorId: Int = 3 // Default or placeholder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserInfo: LoginTokenDecoded? = intent.getParcelableExtra("TOKEN_KEY")
        currentTutorId = if (currentUserInfo?.role == "tutor") currentUserInfo.id
        else intent.getIntExtra("TUTOR_ID", 3) // Ensure TUTOR_ID is passed for students

        // registrar callback red
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        // Note: registerDefaultNetworkCallback might be simpler if you only care about the default network
        // For more specific network handling, registerNetworkCallback is fine.

        // Initial load
        if (savedInstanceState == null) { // Load only on first creation, not on config change
            viewModel.loadTutorProfile(currentTutorId)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            TutorProfileContent(uiState, currentUserInfo)
        }
    }

    override fun onStart() {
        super.onStart()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Explicitly tell the ViewModel to re-evaluate the current network state
        // when the activity starts. This is crucial for when returning to the activity.
        // The ViewModel's notifyNetworkStatusChanged will handle the logic.
        // We can also directly ask it to check and refresh if stale.
        viewModel.checkForStaleDataAndRefreshIfNeeded()
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

data class TutorProfileUiState(
    val profile: GetTutorProfileResponse? = null,
    val insight: GetTimeToBookInsightResponse? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false, // This flag indicates if data is potentially outdated due to no internet
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileContent(
    uiState: TutorProfileUiState,
    currentUserInfo: LoginTokenDecoded?
) {
    val enableBackHandler = currentUserInfo?.role == "tutor"
    BackHandler(enabled = enableBackHandler) {
        // For tutors, disable back press or handle as needed (e.g., navigate to a dashboard)
        // For now, it does nothing, effectively disabling back press.
    }

    // Loading state
    if (uiState.isLoading) { // Show loading only if no profile data yet
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryAppColor)
        }
        return
    }

    // Error state (if profile is null and error is present)
    if (uiState.profile == null && uiState.error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = uiState.error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        return
    }

    // Display profile content (even if stale or with a non-critical error)
    uiState.profile?.let { perfil ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White) // Consider using MaterialTheme.colorScheme.background
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stale data message or general error message if profile data exists
            if (uiState.isStale && !uiState.isLoading) {
                Text(
                    text = if (currentUserInfo?.role == "student") "No internet connection: displayed information may be outdated. Will refresh when reconnected."
                    else "No internet connection: your profile information may be outdated. Will refresh when reconnected.",
                    color = MaterialTheme.colorScheme.error, // Or a less alarming color for stale data
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            } else if (uiState.error != null && !uiState.isStale && !uiState.isLoading) {
                // Show error if data is loaded but an update failed
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }


            TutorProfileScreen(
                currentUserInfo = currentUserInfo,
                tutorProfileInfo = perfil,
                timeToBookInsightData = uiState.insight,
                isStale = uiState.isStale // Pass stale state to conditionally show/hide UI elements
            )
        }
    } ?: run {
        if (!uiState.isLoading && uiState.error == null) { // Fallback if no data, not loading, no error
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile data not available.", textAlign = TextAlign.Center)
            }
        }
    }
}


@Composable
fun TutorProfileHeader(modifier: Modifier, currentUserInfo: LoginTokenDecoded?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val iconButtonModifier = remember {
        Modifier.size(30.dp).clip(CircleShape).background(PrimaryAppColor)
    }
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "TutorApp",
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 15.dp),
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryAppColor
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp) // Adjusted spacing
        ) {
            // Assuming Session.role and Session.userid are accessible Session management variables
            // For production, these should come from a ViewModel or a secure source.
            // For this example, let's assume they are passed or retrieved safely.
            // val currentRole = Session.role // Example
            // val currentUserId = Session.userid // Example

            // This check should ideally use currentUserInfo from TutorProfileContent
            // For simplicity in this snippet, I'll keep Session, but it's better to pass currentUserInfo down
            if (Session.role == "tutor") { // Replace Session.role with currentUserInfo.role if passed
                IconButton(
                    onClick = {
                        val intent = Intent(
                            context,
                            ConnectWithStudentsActivity::class.java
                        )
                        // intent.putExtra("USER_INFO", currentUserInfo) // Pass user info if needed
                        context.startActivity(intent)
                    },
                    modifier = iconButtonModifier
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        val intent =
                            Intent(context, BookedSessionsActivity::class.java).apply {
                                putExtra("TOKEN_KEY", currentUserInfo)
                            }
                        context.startActivity(intent)
                    },
                    modifier = iconButtonModifier
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Booked sessions",
                        tint = Color.White
                    )
                }
            }
            // Logout button visible for both roles if logged in
            // if (Session.role == "tutor" || Session.role == "student") { // Replace with currentUserInfo != null
            IconButton(
                onClick = {
                    // Clear local session data
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.sessionDataDao() // Assuming you have a SessionDataDao
                    coroutineScope.launch { dao.clearData() } // Implement clearData in your DAO

                    // Navigate to WelcomeActivity
                    val intent = Intent(context, WelcomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finishAffinity() // Finish all activities in the task
                },
                modifier = iconButtonModifier
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Log out",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileScreen(
    currentUserInfo: LoginTokenDecoded?,
    tutorProfileInfo: GetTutorProfileResponse,
    timeToBookInsightData: GetTimeToBookInsightResponse?,
    isStale: Boolean // Receive isStale state
) {
    val avgRatingFormatted = remember(tutorProfileInfo.data.ratings) {
        String.format("%.1f", tutorProfileInfo.data.ratings) // For display
    }
    val avgRatingForComparison = remember(tutorProfileInfo.data.ratings) {
        tutorProfileInfo.data.ratings.toFloat() // For logic
    }
    val tutorId = currentUserInfo?.id ?: 0 // Prefer currentUserInfo.id
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(avgRatingForComparison, tutorId, currentUserInfo?.role) {
        if (currentUserInfo?.role == "tutor" && avgRatingForComparison < 4.0 && avgRatingForComparison > 0 && NetworkUtils.isConnected(context)) {
            if (NetworkUtils.shouldShowRatingWarning(context, tutorId.toString())) {
                showDialog = true
                NetworkUtils.markRatingWarningAsShown(context, tutorId.toString())
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // Allow dismissing by clicking outside
            title = { Text(text = "We can help you improve!") },
            text = {
                Text(
                    "Your average review rating is $avgRatingFormatted, which is a bit low. We've found tutors similar to you. Check out their best reviews to get tips on how you can improve!"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        val intent = Intent(context, SimilarTutorsActivity::class.java)
                        // intent.putExtra("CURRENT_USER_INFO", currentUserInfo) // Pass necessary data
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAppColor,
                        contentColor = Color.White
                    )
                ) {
                    Text("See Similar Tutors")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Dismiss", color = PrimaryAppColor)
                }
            }
        )
    }

    TutorProfileHeader(modifier = Modifier.height(IntrinsicSize.Min), currentUserInfo) // Pass relevant info if needed
    Spacer(modifier = Modifier.height(16.dp))

    // Profile Picture & Info
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(PrimaryAppColor), // Your primary color
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tutorProfileInfo.data.name.firstOrNull()?.toString()?.uppercase() ?: "N",
            fontSize = 48.sp, // Adjusted for better visibility
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(12.dp)) // Adjusted spacing
    Text(text = tutorProfileInfo.data.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(text = tutorProfileInfo.data.university, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
    Spacer(modifier = Modifier.height(12.dp))

    // Rating Stars
    Row(verticalAlignment = Alignment.CenterVertically) {
        val ratingValue = remember(tutorProfileInfo.data.ratings) {
            tutorProfileInfo.data.ratings.toInt().coerceIn(0, 5)
        }
        repeat(ratingValue) {
            Icon(Icons.Filled.Favorite, contentDescription = "Filled Star", tint = PrimaryAppColor)
        }
        repeat(5 - ratingValue) {
            Icon(Icons.Filled.FavoriteBorder, contentDescription = "Empty Star", tint = PrimaryAppColor)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text("($avgRatingFormatted)", style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(modifier = Modifier.height(20.dp)) // Adjusted spacing

    // Contact & Specialty
    InfoRow(icon = Icons.Default.Call, text = tutorProfileInfo.data.whatsappContact)
    Spacer(modifier = Modifier.height(10.dp))
    InfoRow(icon = Icons.Default.Create, text = tutorProfileInfo.data.subjects, isBold = true)


    // Insight (even offline if cached)
    if (currentUserInfo?.role == "tutor") {
        timeToBookInsightData?.let { insight ->
            if (insight.time != -1) { // Assuming -1 means no data or error
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Insight: Time to Book",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Average time students take to book with you: ${insight.time} seconds.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (insight.message.isNotBlank()) {
                            Text(
                                text = insight.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    // "Announce a tutoring session" button
    // Show button only if the user is a tutor AND the data is not stale (i.e., internet is connected)
    if (currentUserInfo?.role == "tutor") {
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val intent = Intent(context, AddCourseActivity::class.java).apply {
                    putExtra("TOKEN_KEY", currentUserInfo) // Pass necessary data
                }
                context.startActivity(intent)
            },
            shape = RoundedCornerShape(50), // Fully rounded corners
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAppColor),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isStale
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Announce a Tutoring Session", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
    if (currentUserInfo?.role == "tutor" && isStale) {
        Text(
            text = "You cannot announce tutoring sessions when offline.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.error
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    Text("Reviews", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    // Reviews list
    if (tutorProfileInfo.data.reviews.isNotEmpty()) {
        tutorProfileInfo.data.reviews.forEach { review ->
            TutorReviewItem(review)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    } else {
        if (isStale) {
            Text("No reviews available due to internet connection.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else {
            Text("No reviews yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, isBold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PrimaryAppColor)
        Spacer(modifier = Modifier.width(12.dp)) // Increased spacing
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge, // Slightly larger text
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TutorReviewItem(review: Review) {
    Column( // Changed to Column for better structure
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val coercedRating = remember(review.rating) { review.rating.coerceIn(0, 5) }
            repeat(coercedRating) { Icon(Icons.Filled.Favorite, "Filled Star", tint = PrimaryAppColor) }
            repeat(5 - coercedRating) { Icon(Icons.Filled.FavoriteBorder, "Empty Star", tint = PrimaryAppColor) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
    }
}

// Dummy Session object for compilation. Replace with your actual session management.

