@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.tutorapp.views

import android.content.Intent
import com.tutorapp.viewModels.LoginViewModel
import android.os.Bundle
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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.viewModels.TutorProfileViewModel

class TutorProfileActivity : ComponentActivity() {
    private val tutorProfileViewModel: TutorProfileViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserInfo: LoginTokenDecoded? = intent.getParcelableExtra("TOKEN_KEY")
        setContent {
            TutorProfileScreen(tutorProfileViewModel, currentUserInfo)
        }
    }
}

@Composable
fun TutorProfileHeader(modifier: Modifier){
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
fun TutorProfileScreen(viewModel: TutorProfileViewModel, currentUserInfo: LoginTokenDecoded?) {
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
            Text(text = "A", fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Alejandro Hernandez", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = "Universidad de los Andes", fontSize = 16.sp, fontWeight = FontWeight.Thin)

        Spacer(modifier = Modifier.height(8.dp))

        // Rating Stars
        Row {
            repeat(4) {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = "Star", tint = Color(0xFF1A2546))
            }
            Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Star", tint = Color(0xFF1A2546))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contact Info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Call, contentDescription = "Phone", tint = Color(0xFF1A2546))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "3045748603", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Create, contentDescription = "Specialty", tint = Color(0xFF1A2546))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Mobile development, Business Intelligence", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        contentDescription = "Specialty",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add a subject", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reviews
        repeat(4) {
            TutorReviewItem()
        }

        if (currentUserInfo?.role == "student") {
            Spacer(modifier = Modifier.height(16.dp))

            // Write Review Button
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))
            ) {
                Text(text = "Write a review", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun TutorReviewItem() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A2546)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "A", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Row {
                repeat(4) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Star", tint = Color(0xFF1A2546))
                }
                Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Star", tint = Color(0xFF1A2546))
            }
            Text(text = "Supporting line text lorem ipsum dolor sit amet, consectetur.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTutorProfileScreen(viewModel: TutorProfileViewModel = viewModel()) {
    TutorProfileScreen(viewModel, null)
}