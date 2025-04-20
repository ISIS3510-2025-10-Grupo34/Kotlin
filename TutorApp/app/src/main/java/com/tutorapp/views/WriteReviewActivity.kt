package com.tutorapp.views

import android.content.Intent
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

class WriteReviewActivity : ComponentActivity() {
    private val viewModel: WriteReviewViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val studentId = intent.getIntExtra("TUTOR_ID", -1)
        val tutoringSessionId = intent.getIntExtra("TUTORING_SESSION_ID", -1)
        setContent {
            WriteReviewScreen(viewModel, studentId, tutoringSessionId)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(viewModel: WriteReviewViewModel, studentId: Int, tutoringSessionId: Int) {
    var rating by remember { mutableStateOf(0) }
    var review by remember { mutableStateOf(TextFieldValue()) }
    var isReviewError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Header2(modifier = Modifier.height(IntrinsicSize.Min))

        Text("Write a review", fontSize = 32.sp, color = Color(0xFF1A2340), fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tap to Rate:", fontSize = 16.sp, color = Color.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(-16.dp)) {
            for (i in 1..5) {
                IconButton(onClick = {
                    rating = i
                    isReviewError = false // Reset error state when user selects a rating
                }) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = Color(0xFF1A2340)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = review,
            onValueChange = {
                review = it
                isReviewError = false // Reset error state when user types
            },
            label = { Text("Review") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (rating > 0 && review.text.isNotBlank()) {
                // Both rating and review are filled, proceed with submission
                viewModel.postReview(tutoringSessionId, studentId, rating, review.text) { success, message ->
                    if (success) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
                val intent = Intent(context, StudentProfileActivity::class.java).apply {
                    putExtra("ID", studentId.toString())
                }
                context.startActivity(intent)
            } else {
                // Show error message if fields are not filled
                isReviewError = true
                val errorMessage = when {
                    rating == 0 && review.text.isBlank() -> "Please select a rating and write a review"
                    rating == 0 -> "Please select a rating"
                    else -> "Please write a review"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))) {
            Text("Submit", color = Color.White)
        }
    }
}