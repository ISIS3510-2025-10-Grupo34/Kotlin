package com.tutorapp.views


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tutorapp.viewModels.StudentProfileViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import com.tutorapp.models.GetTutoringSessionsToReviewResponse
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.models.TutoringSessionToReview
import com.tutorapp.ui.theme.LightGrey
import com.tutorapp.ui.theme.Primary
import java.io.ByteArrayInputStream


class StudentProfileActivity : ComponentActivity() {
    private val studentProfileViewModel: StudentProfileViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val studentId = intent.getStringExtra("ID") ?: return
        studentProfileViewModel.getTutoringSessionsToReview(studentId.toInt()) { success, data ->
            if (success) {
                setContent {
                    if (data != null) {
                        StudentProfileScreen(studentProfileViewModel, studentId, data)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentProfileScreen(viewModel: StudentProfileViewModel, studentId: String, tutoringSessionsToReview: GetTutoringSessionsToReviewResponse) {
    var isLoading by remember { mutableStateOf(true) }
    val percentage by viewModel.percentage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.reviewPercentage(studentId)
    }
    LaunchedEffect(studentId) {
        viewModel.studentProfile(studentId) {
            isLoading = false
        }
    }
    LaunchedEffect(percentage) {
        if (50 > percentage) {
            showDialog = true
        }
    }


    if (isLoading) {
        CircularProgressIndicator()
    } else {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    // Evita que se cierre al tocar fuera del diálogo
                    // No hacemos nada aquí para forzar al usuario a usar el botón
                },
                title = {
                    Text(text = "Help tutorapp improve!")
                },
                text = {
                    Text("We notice you've only reviewed $percentage% of your booked tutorings, " +
                            "try to review more")
                },
                confirmButton = {
                    Button(onClick = { showDialog = false },
                        colors = ButtonColors(containerColor = Color(0xFF192650), contentColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color(0xFF192650) )) {
                        Text("Ok")
                    }
                }
            )
        }
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
                Text(
                    text = "Tutoring sessions pending to review",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.Start).padding(vertical = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                ListOfTutorCardsToReview(Modifier, studentId, tutoringSessionsToReview)
            }

        }
    }
}


@Composable
fun ListOfTutorCardsToReview(modifier: Modifier, token: String, tutoringSessionsToReview: GetTutoringSessionsToReviewResponse){

    val scrollState = rememberScrollState()

    Column(modifier = modifier
        .fillMaxSize()
        .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)){
        tutoringSessionsToReview.data.forEach {
                tutoringSession -> TutorCardToReview(modifier = Modifier, tutoringSession = tutoringSession, token = token)
        }
    }
}


@Composable
fun TutorCardToReview(modifier: Modifier, tutoringSession: TutoringSessionToReview, token: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Section (Row)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .clickable {
                val tokenFormatted = LoginTokenDecoded(
                    id = token.toInt(),
                    email = "",
                    role = "student",
                    exp = 0,
                    iat = 0
                )
                val intent = Intent(context, TutorProfileActivity::class.java).apply {
                    putExtra("TOKEN_KEY", tokenFormatted)
                    putExtra("TUTOR_ID", tutoringSession.tutorId)
                }
                context.startActivity(intent)
            }) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)), // Light Gray
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tutoringSession.tutorName[0].uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tutoringSession.tutorName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Information Section (Column)
        Column {
            Text(
                text = tutoringSession.courseName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date: "+tutoringSession.dateTime,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button
        Button(
            onClick = {
                val intent = Intent(context, WriteReviewActivity::class.java).apply {
                    putExtra("TUTOR_ID", token.toInt())
                    putExtra("TUTORING_SESSION_ID", tutoringSession.id)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonColors(containerColor = Color(0xFF192650), contentColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color(0xFF192650) )
        ) {
            Text(text = "Write a review")
        }
    }
    }
