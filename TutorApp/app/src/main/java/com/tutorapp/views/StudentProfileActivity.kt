package com.tutorapp.views


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tutorapp.viewModels.StudentProfileViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tutorapp.ui.theme.Black
import com.tutorapp.ui.theme.LightGrey
import com.tutorapp.ui.theme.Primary
import com.tutorapp.ui.theme.Typography
import java.io.ByteArrayInputStream


class StudentProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val studentId = intent.getStringExtra("ID") ?: return

        setContent {
            val viewModel: StudentProfileViewModel = viewModel()

            StudentProfileScreen(viewModel, studentId)
        }
    }
}




@Composable
fun StudentProfileScreen(viewModel: StudentProfileViewModel, studentId: String) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(studentId) {
        viewModel.studentProfile(studentId) {
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        viewModel.studentProfile?.let { profile ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(
                    text = "TutorApp",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))


                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (profile.profile_picture.isNullOrEmpty()) {
                        val initial = profile.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        val decodedBytes = Base64.decode(profile.profile_picture, Base64.DEFAULT)
                        val profilePicture = BitmapFactory.decodeStream(ByteArrayInputStream(decodedBytes))

                        Image(
                            bitmap = profilePicture.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = profile.name,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center)
                    Text(text = profile.university,
                        fontSize = 12.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(400),
                        color = LightGrey,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center)
                    Text(text = profile.major,
                        fontSize = 12.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(400),
                        color = LightGrey,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(16.dp))
                }


                Text(
                    text = "Learning Styles",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start),
                    color = Primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    profile.learning_styles.forEach { style ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = style, color = Color.Black)
                        }
                    }
                }
            }

        }
    }
}
