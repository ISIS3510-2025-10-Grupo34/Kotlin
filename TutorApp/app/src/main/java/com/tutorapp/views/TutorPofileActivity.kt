@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.tutorapp.views

import android.content.Intent
import com.tutorapp.viewModels.LoginViewModel
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tutorapp.models.GetTutorProfileResponse
import com.tutorapp.models.Review
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.viewModels.TutorProfileViewModel

class TutorProfileActivity : ComponentActivity() {
    private val tutorProfileViewModel: TutorProfileViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserInfo: LoginTokenDecoded? = intent.getParcelableExtra("TOKEN_KEY")
        val tutorId = if (currentUserInfo?.role == "tutor") currentUserInfo.id else intent.getIntExtra("TUTOR_ID", -1)
        if (currentUserInfo != null) {
            tutorProfileViewModel.getTutorProfile(tutorId) { success, data ->
                if (success) {
                    setContent {
                        if (data != null) {
                            TutorProfileScreen(tutorProfileViewModel, currentUserInfo, data)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorProfileHeader(modifier: Modifier) {
    Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            "TutorApp",
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 15.dp),
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxWidth()
                .padding(horizontal = 25.dp, vertical = 35.dp),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ) {
            IconButton(
                onClick = {},
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
            IconButton(
                onClick = {},
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
fun TutorProfileScreen(
    viewModel: TutorProfileViewModel,
    currentUserInfo: LoginTokenDecoded?,
    tutorProfileInfo: GetTutorProfileResponse
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TutorProfileHeader(modifier = Modifier.height(IntrinsicSize.Min))

        // Profile Picture
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A2546)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tutorProfileInfo.data.name[0].toString(),
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
            repeat(tutorProfileInfo.data.ratings.toInt()) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Star",
                    tint = Color(0xFF1A2546)
                )
            }
            repeat(5-tutorProfileInfo.data.ratings.toInt()) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Star",
                    tint = Color(0xFF1A2546)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contact Info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Phone",
                tint = Color(0xFF1A2546)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = tutorProfileInfo.data.whatsappContact, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Specialty
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "Specialty",
                tint = Color(0xFF1A2546)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tutorProfileInfo.data.subjects,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (currentUserInfo?.role == "tutor") {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(context, AddCourseActivity::class.java).apply {
                        putExtra("TOKEN_KEY", currentUserInfo)
                    }
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add course",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add a subject", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar las reseñas traídas de la API
        tutorProfileInfo.data.reviews.forEach { review ->
            TutorReviewItem(review)
        }
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
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Star",
                        tint = Color(0xFF1A2546)
                    )
                }
                if (review.rating < 5) {
                    repeat(5 - review.rating) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Star",
                            tint = Color(0xFF1A2546)
                        )
                    }
                }
            }
            Text(text = review.comment)
        }
    }
}
