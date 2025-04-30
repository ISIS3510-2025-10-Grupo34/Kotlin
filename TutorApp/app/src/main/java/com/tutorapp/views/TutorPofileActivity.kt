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
import android.widget.Toast
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

import android.content.*
import android.net.*
import android.os.*
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.TutorProfileViewModelFactory
import kotlinx.coroutines.launch

class TutorProfileActivity : ComponentActivity() {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkRequest: NetworkRequest

    private val networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            viewModel.loadTutorProfile(currentTutorId)
        }
    }

    // usar nuestro ViewModelFactory
    private val viewModel: TutorProfileViewModel by viewModels {
        TutorProfileViewModelFactory(application)
    }

    private var currentTutorId: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserInfo: LoginTokenDecoded? = intent.getParcelableExtra("TOKEN_KEY")
        currentTutorId = if (currentUserInfo?.role == "tutor") currentUserInfo.id
        else intent.getIntExtra("TUTOR_ID", 3)

        // registrar callback red
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // inicializar carga
        viewModel.loadTutorProfile(currentTutorId)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            TutorProfileContent(uiState, currentUserInfo)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

data class TutorProfileUiState(
    val profile: GetTutorProfileResponse? = null,
    val insight: GetTimeToBookInsightResponse? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileContent(
    uiState: TutorProfileUiState,
    currentUserInfo: LoginTokenDecoded?
) {
    // Loading state
    if (uiState.isLoading && uiState.profile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Error state
    uiState.error?.let { msg ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = msg, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    uiState.profile?.let { perfil ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isStale) {
                Text(
                    text = if (currentUserInfo?.role == "student") "No internet connection: displayed information may not be the desired. Will refresh when reconnected." else "No internet connection: your profile information may be outdated. Will refresh when reconnected.",
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
                isStale = uiState.isStale
            )
        }
    }
}

@Composable
fun TutorProfileHeader(modifier: Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            if (Session.role == "tutor") {
                IconButton(
                    onClick = {
                        val intent = Intent(
                            context,
                            ConnectWithStudentsActivity::class.java
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF192650))
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }
                if (Session.role == "tutor" || Session.role == "student") {
                    IconButton(
                        onClick = {
                            val db = AppDatabase.getDatabase(context)
                            val dao = db.sessionDataDao()
                            coroutineScope.launch { dao.clearData() }
                            val intent = Intent(
                                context,
                                WelcomeActivity::class.java
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(25.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF192650))
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileScreen(
    currentUserInfo: LoginTokenDecoded?,
    tutorProfileInfo: GetTutorProfileResponse,
    timeToBookInsightData: GetTimeToBookInsightResponse?,
    isStale: Boolean
) {
    val context = LocalContext.current

    TutorProfileHeader(modifier = Modifier.height(IntrinsicSize.Min))
    Spacer(modifier = Modifier.height(16.dp))

    // Profile Picture & Info
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color(0xFF192650)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tutorProfileInfo.data.name.first().toString(),
            fontSize = 36.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = tutorProfileInfo.data.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text(text = tutorProfileInfo.data.university, fontSize = 16.sp, fontWeight = FontWeight.Thin)
    Spacer(modifier = Modifier.height(8.dp))

    // Rating Stars
    Row {
        val r = tutorProfileInfo.data.ratings.toInt().coerceIn(0,5)
        repeat(r) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF192650))
        }
        repeat(5 - r) {
            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFF192650))
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    // Contact & Specialty
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF192650))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = tutorProfileInfo.data.whatsappContact, fontSize = 16.sp)
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Create, contentDescription = null, tint = Color(0xFF192650))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = tutorProfileInfo.data.subjects, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }

    // Insight (even offline if cached)
    if (currentUserInfo?.role == "tutor") {
        timeToBookInsightData?.let { insight ->
            if (insight.time != -1) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Time it takes a student to book with you:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${insight.time} seconds. ${insight.message}",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Announce button always visible
    if (currentUserInfo?.role == "tutor" && !isStale) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* announce logic */ },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF192650))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Announce a tutoring session", color = Color.White)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    // Reviews list
    tutorProfileInfo.data.reviews.forEach { review ->
        TutorReviewItem(review)
    }
}

@Composable
fun TutorReviewItem(review: Review) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row {
                repeat(review.rating) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF192650))
                }
                repeat(5 - review.rating) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFF192650))
                }
            }
            Text(text = review.comment)
        }
    }
}
