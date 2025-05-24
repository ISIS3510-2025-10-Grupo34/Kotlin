package com.tutorapp.views


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.collection.LruCache
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.Converters
import com.tutorapp.data.StudentProfileEntity
import com.tutorapp.models.GetTutoringSessionsToReviewResponse
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.models.TutoringSessionToReview
import com.tutorapp.models.dataSP
import com.tutorapp.remote.NetworkUtils
import com.tutorapp.ui.theme.LightGrey
import com.tutorapp.ui.theme.Primary
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers

object ProfileImageCache {
    private val cacheSize = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt() // Usamos 1/8 de la memoria
    private val lruCache = object : LruCache<String, Bitmap>(cacheSize) {}

    fun put(key: String, bitmap: Bitmap) {
        lruCache.put(key, bitmap)
    }

    fun get(key: String): Bitmap? {
        return lruCache.get(key)
    }
}


class StudentProfileActivity : ComponentActivity() {
    private val studentProfileViewModel: StudentProfileViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this


        val studentId = intent.getStringExtra("ID") ?: return
        if(NetworkUtils.isConnected(context)) {
            studentProfileViewModel.getTutoringSessionsToReview(studentId.toInt()) { success, data ->
                if (success) {
                    setContent {
                        if (data != null) {
                            StudentProfileScreen(studentProfileViewModel, studentId, data)
                        }
                    }
                } else {
                    setContent {
                        StudentProfileScreen(studentProfileViewModel, studentId, null)
                    }
                }
            }
        }
        else{
            setContent {
                StudentProfileScreen(studentProfileViewModel, studentId, null)
            }
        }
    }
}

@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel,
    studentId: String,
    tutoringSessionsToReview: GetTutoringSessionsToReviewResponse?
) {
    var isLoading by remember { mutableStateOf(true) }
    val percentage by viewModel.percentage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Inicializar la base de datos y DAO
    val db = AppDatabase.getDatabase(context)
    val studentProfileDao = db.studentProfileDao()

    // Estado local para cargar perfil offline
    var localProfile by remember { mutableStateOf<StudentProfileEntity?>(null) }

    // Estado para la Uri temporal de la imagen
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    // Cargar porcentaje de reseñas
    LaunchedEffect(Unit) {
        if(NetworkUtils.isConnected(context)){
        viewModel.reviewPercentage(studentId)}
    }

    // Cargar perfil (online o local)
    val hasConnection = NetworkUtils.isConnected(context)
    if (hasConnection) {
    LaunchedEffect(studentId) {
        viewModel.studentProfile(studentId) {
            isLoading = false
            viewModel.studentProfile?.let { profile ->
                // Guardar imagen si viene
                profile.profile_picture?.let { base64Img ->
                    coroutineScope.launch(Dispatchers.IO) {
                        val decodedBytes = Base64.decode(base64Img, Base64.DEFAULT)
                        val tempFile = File.createTempFile("profile_${studentId}", ".jpg", context.cacheDir)
                        tempFile.writeBytes(decodedBytes)
                        profileImageUri = Uri.fromFile(tempFile)
                    }
                }
                val entity = StudentProfileEntity(
                    name = profile.name,
                    university = profile.university,
                    major = profile.major,
                    learningStyles = Converters.fromStringList(profile.learning_styles)
                )
                coroutineScope.launch {
                    studentProfileDao.saveData(entity)
                }
            }
        }
    }
    }
        else {
            // Cargar de Room si no hay conexión
            LaunchedEffect(Unit) {
                println("ddd")
                localProfile = studentProfileDao.loadData()
                println(localProfile.toString()+"acaaa")
                isLoading = false
            }
        }

    // Lanzar alerta si porcentaje es bajo
    LaunchedEffect(percentage) {
        if (percentage < 50) {
            showDialog = true
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { /* Forzar usar el botón */ },
                title = { Text(text = "Help tutorapp improve!") },
                text = {
                    Text(
                        "We notice you've only reviewed $percentage% of your booked tutorings, " +
                                "try to review more"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonColors(
                            containerColor = Color(0xFF192650),
                            contentColor = Color.White,
                            disabledContentColor = Color.White,
                            disabledContainerColor = Color(0xFF192650)
                        )
                    ) {
                        Text("Ok")
                    }
                }
            )
        }
        fun StudentProfileEntity.toStudentProfile(): dataSP {
            return dataSP(
                profile_picture = null, // no guardamos foto local
                name = this.name,
                university = this.university,
                major = this.major,
                learning_styles = Converters.toStringList(this.learningStyles)
            )
        }

        println("vvv")
        // Elegir perfil: online o local
        val profile = viewModel.studentProfile ?: localProfile?.toStudentProfile()
        println(profile)
        profile?.let { profileData ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TutorApp",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    IconButton(
                        onClick = {
                            val dao = db.sessionDataDao()
                            val bsdao = db.bookedSessionCalendarDao()
                            coroutineScope.launch { dao.clearData()
                                bsdao.clearAll()
                            studentProfileDao.clearData()}
                            val intent = Intent(context, WelcomeActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log Out",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (profileImageUri != null) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!profileData.profile_picture.isNullOrEmpty()) {
                        // Mientras se decodifica la imagen, muestra un placeholder
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profileData.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = profileData.name,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = profileData.university,
                        fontSize = 12.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(400),
                        color = LightGrey,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = profileData.major,
                        fontSize = 12.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(400),
                        color = LightGrey,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )

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
                    profileData.learning_styles.forEach { style ->
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
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
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
fun ListOfTutorCardsToReview(modifier: Modifier, token: String, tutoringSessionsToReview: GetTutoringSessionsToReviewResponse?){
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    if(tutoringSessionsToReview!=null || NetworkUtils.isConnected(context)) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (tutoringSessionsToReview != null) {
                tutoringSessionsToReview.data.forEach { tutoringSession ->
                    TutorCardToReview(
                        modifier = Modifier,
                        tutoringSession = tutoringSession,
                        token = token
                    )
                }
            }
        }
    }
    else{
        Text(
            text = "There isn't internet connection try later",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontWeight = FontWeight.Normal
        )
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
