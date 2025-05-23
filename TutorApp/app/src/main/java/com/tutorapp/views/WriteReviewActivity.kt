package com.tutorapp.views

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.tutorapp.viewModels.LoginViewModel
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tutorapp.viewModels.WriteReviewViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign


class WriteReviewActivity : ComponentActivity() {
    private val viewModel: WriteReviewViewModel by viewModels()
    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            viewModel.sendPendingReviews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val studentId = intent.getIntExtra("TUTOR_ID", -1)
        val tutoringSessionId = intent.getIntExtra("TUTORING_SESSION_ID", -1)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
            networkCallback
        )

        viewModel.sendPendingReviews()

        setContent {
            WriteReviewScreen(
                viewModel = viewModel,
                studentId = studentId,
                tutoringSessionId = tutoringSessionId
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    viewModel: WriteReviewViewModel,
    studentId: Int,
    tutoringSessionId: Int
) {
    val context = LocalContext.current
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val maxCommentLength = 40

    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf(TextFieldValue()) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sentCount.collectLatest { count ->
            if (count > 0) { // Only show toast if there were pending reviews sent
                Toast.makeText(
                    context,
                    "All $count pending review(s) have just been sent.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header2(modifier = Modifier.height(IntrinsicSize.Min))
        Spacer(Modifier.height(24.dp))

        Text("Write a review", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Text("Tap to Rate:", fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { i ->
                IconButton(onClick = {
                    rating = i
                    isError = false
                }) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = Color(0xFF1A2340),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = comment,
            onValueChange = { newTextFieldValue ->
                // Prevent line breaks and limit length
                val newText = newTextFieldValue.text.replace("\n", "")
                if (newText.length <= maxCommentLength) {
                    comment = newTextFieldValue.copy(text = newText)
                }
                isError = false // Reset error when comment changes
            },
            label = { Text("Review") },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true, // This helps prevent line breaks from keyboard actions
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done, // Or ImeAction.Next if there's another field
                keyboardType = KeyboardType.Text
            ),
            supportingText = { // Display character count here
                Text(
                    text = "${comment.text.length}/$maxCommentLength",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End, // Align to the end (right)
                    color = if (comment.text.length > maxCommentLength) MaterialTheme.colorScheme.error else LocalContentColor.current.copy(alpha = LocalContentColor.current.alpha * 0.6f) // Dimmer or error color
                )
            }
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                // val nw = connectivityManager.activeNetwork // Not directly used for validation
                // val caps = connectivityManager.getNetworkCapabilities(nw)
                // val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

                // Trim comment before validation to ensure spaces don't count as content
                val trimmedComment = comment.text.trim()

                if (rating == 0 || trimmedComment.isBlank()) {
                    isError = true // Set general error flag
                    val msg = when {
                        rating == 0 && trimmedComment.isBlank() -> "Please select a rating and write a review."
                        rating == 0 -> "Please select a rating."
                        else -> "Please write a review." // This will be shown if comment is blank
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                } else {
                    isError = false // Clear error if validation passes
                    viewModel.postReviewOrSaveDraft(
                        tutoringSessionId,
                        studentId,
                        rating,
                        trimmedComment // Use trimmed comment
                    ) { success, message -> // Assuming your callback provides a success boolean
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        if (success) {
                            // Optionally navigate or clear fields
                            // context.startActivity(Intent(context, StudentProfileActivity::class.java) ...)
                            // rating = 0
                            // comment = TextFieldValue("")
                        }
                    }
                }
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)), // Consider MaterialTheme.colorScheme.primary
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Submit", color = Color.White, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun Header2(modifier: Modifier){
    Row (modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween){
        Text("TutorApp", modifier = Modifier
            .weight(1f)
            .padding(vertical = 15.dp),
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
        )
        Row (modifier = Modifier
            .weight(0.5f)
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 35.dp)

            , horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ){
            IconButton(onClick = {},
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
            IconButton(onClick = {},
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF192650))


            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White
                )
            }

        }
    }
}