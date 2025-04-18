package com.tutorapp.views
import com.tutorapp.viewModels.RegisterViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import com.tutorapp.ui.theme.Typography
import androidx.compose.foundation.Image
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowDropDown
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import java.io.ByteArrayOutputStream
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import com.tutorapp.viewModels.LoginViewModel
import java.io.File

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import kotlinx.coroutines.launch


class RegisterActivity : ComponentActivity() {
    private val registerViewModel: RegisterViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen(registerViewModel)
        }
    }
}

@Composable
fun RegisterScreen(viewModel: RegisterViewModel) {

    var role by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var university by rememberSaveable { mutableStateOf("") }
    var major by rememberSaveable { mutableStateOf("") }
    var expertise by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var learningStyles by rememberSaveable { mutableStateOf(listOf<String>()) }
    var profilePictureUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var idPictureUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var screenStack by rememberSaveable { mutableStateOf(listOf("roleSelection")) }
    val currentScreen = screenStack.last()
    fun goTo(screen: String) {
        screenStack = screenStack + screen
    }

    fun goBack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        }
    }
    BackHandler(enabled = screenStack.size > 1) {
        goBack()
    }


    when (currentScreen) {
        "roleSelection" -> RoleSelectionScreen { roleselected ->
            role = roleselected
            goTo("registerDetails")
        }
        "registerDetails" -> {
            if (role == "student") {
                StudentRegisterScreen(name, university, major, email, password,viewModel) { n, u, m, e, p ->
                    name = n
                    university = u
                    major = m
                    email = e
                    password = p
                    goTo("learningStyles")
                }
            } else if (role=="tutor") {
                TutorRegisterScreen(name, university, expertise, email, password, phoneNumber,viewModel) { n, u, exp, e, p, ph ->
                    name = n
                    university = u
                    expertise = exp
                    email = e
                    password = p
                    phoneNumber = ph
                    goTo("uploadProfile")
                }
            }
        }
        "learningStyles" -> LearningStylesScreen(learningStyles) { selectedStyles ->
            learningStyles = selectedStyles
            goTo("uploadProfile")
        }
        "uploadProfile" -> UploadProfileScreen { uri ->
            profilePictureUri = uri
            goTo("uploadID")
        }
        "uploadID" -> UploadIDScreen(
            name, email, password, phoneNumber, profilePictureUri, university, major, expertise,
            role, learningStyles,

            onRegisterFail = { errorMessage -> println("Error: $errorMessage") },
            viewModel
        )
    }
}

@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TutorApp", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(80.dp))
        Text("Create Account", style = Typography.titleLarge)
        Text("In order to continue we must know if you are a tutor or a student", style = Typography.bodyLarge, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
        Button(onClick = { onRoleSelected("tutor") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)), shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)) { Text("Tutor", color = Color.White) }
        Button(onClick = { onRoleSelected("student") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)), shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)) { Text("Student", color = Color.White) }
    }
}

@Composable
fun StudentRegisterScreen(
    name: String,
    university: String,
    major: String,
    email: String,
    password: String,
    viewModel: RegisterViewModel,
    onDetailsEntered: (String, String, String, String, String) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.universities()
        viewModel.majors()
    }

    var nameState by rememberSaveable { mutableStateOf(name) }
    var universityState by rememberSaveable { mutableStateOf(university) }
    var majorState by rememberSaveable { mutableStateOf(major) }
    var emailState by rememberSaveable { mutableStateOf(email) }
    var passwordState by rememberSaveable { mutableStateOf(password) }

    var nameError by remember { mutableStateOf(false) }
    var universityError by remember { mutableStateOf(false) }
    var majorError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val fieldModifier = Modifier.fillMaxWidth(0.9f)
    var expanded by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    val universities by viewModel.universities.collectAsState()
    val majors by viewModel.majors.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("TutorApp", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

            Spacer(modifier = Modifier.height(80.dp))

            Text("Student", style = Typography.titleLarge)
            Text(
                "We would like to know more about you",
                style = Typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = nameState,
                onValueChange = {
                    nameState = it
                    nameError = false
                },
                label = { Text("First and last name") },
                isError = nameError,
                modifier = fieldModifier,
                singleLine = true
            )

            Box {
                OutlinedTextField(
                    value = majorState,
                    onValueChange = {},
                    label = { Text("Major") },
                    isError = majorError,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    },
                    modifier = fieldModifier
                )

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    majors.forEach { major ->
                        DropdownMenuItem(
                            text = { Text(major) },
                            onClick = {
                                majorState = major
                                majorError = false
                                expanded = false
                            }
                        )
                    }
                }
            }

            Box {
                OutlinedTextField(
                    value = universityState,
                    onValueChange = {},
                    label = { Text("University") },
                    isError = universityError,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded2 = !expanded2 }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    },
                    modifier = fieldModifier
                )

                DropdownMenu(expanded = expanded2, onDismissRequest = { expanded2 = false }) {
                    universities.forEach { university ->
                        DropdownMenuItem(
                            text = { Text(university) },
                            onClick = {
                                universityState = university
                                universityError = false
                                expanded2 = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = emailState,
                onValueChange = {
                    emailState = it
                    emailError = false
                },
                label = { Text("Email") },
                isError = emailError,
                modifier = fieldModifier,
                singleLine = true
            )

            OutlinedTextField(
                value = passwordState,
                onValueChange = {
                    passwordState = it
                    passwordError = false
                },
                label = { Text("Password") },
                isError = passwordError,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = fieldModifier,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    nameError = nameState.isBlank()
                    universityError = universityState.isBlank()
                    majorError = majorState.isBlank()
                    emailError = emailState.isBlank()
                    passwordError = passwordState.isBlank()

                    if (nameError || universityError || majorError || emailError || passwordError) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please complete all fields")
                        }
                    } else {
                        viewModel.email(emailState) { success, errorMessage ->
                            if (success) {
                                onDetailsEntered(
                                    nameState,
                                    universityState,
                                    majorState,
                                    emailState,
                                    passwordState
                                )
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(errorMessage)
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 8.dp)
            ) {
                Text("Continue", color = Color.White)
            }
        }
    }
}



@Composable
fun TutorRegisterScreen(
    name: String,
    university: String,
    expertise: String,
    email: String,
    password: String,
    phoneNumber: String,
    viewModel: RegisterViewModel,
    onDetailsEntered: (String, String, String, String, String, String) -> Unit
) {
    val fieldModifier = Modifier.fillMaxWidth(0.9f)

    var nameState by rememberSaveable { mutableStateOf(name) }
    var universityState by rememberSaveable { mutableStateOf(university) }
    var expertiseState by rememberSaveable { mutableStateOf(expertise) }
    var emailState by rememberSaveable { mutableStateOf(email) }
    var passwordState by rememberSaveable { mutableStateOf(password) }
    var phoneNumberState by rememberSaveable { mutableStateOf(phoneNumber) }

    var expanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var showErrors by remember { mutableStateOf(false) }
    val universities by viewModel.universities.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.universities()
    }

    fun hasEmptyFields(): Boolean {
        return nameState.isBlank() ||
                universityState.isBlank() ||
                expertiseState.isBlank() ||
                emailState.isBlank() ||
                passwordState.isBlank() ||
                phoneNumberState.isBlank()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "TutorApp",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(60.dp))

            Text("Tutor", style = Typography.titleLarge)
            Text(
                "We would like to know more about you",
                style = Typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = nameState,
                onValueChange = { nameState = it },
                label = { Text("First and last name") },
                modifier = fieldModifier,
                isError = showErrors && nameState.isBlank()
            )

            // Dropdown para seleccionar Universidad
            Box {
                OutlinedTextField(
                    value = universityState,
                    onValueChange = {},
                    label = { Text("University") },
                    modifier = fieldModifier,
                    readOnly = true,
                    isError = showErrors && universityState.isBlank(),
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        }
                    }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    universities.forEach { university ->
                        DropdownMenuItem(
                            text = { Text(university) },
                            onClick = {
                                universityState = university
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = expertiseState,
                onValueChange = { expertiseState = it },
                label = { Text("Area of expertise") },
                modifier = fieldModifier,
                isError = showErrors && expertiseState.isBlank()
            )

            OutlinedTextField(
                value = emailState,
                onValueChange = { emailState = it },
                label = { Text("Email") },
                modifier = fieldModifier,
                isError = showErrors && emailState.isBlank()
            )

            OutlinedTextField(
                value = passwordState,
                onValueChange = { passwordState = it },
                label = { Text("Password") },
                modifier = fieldModifier,
                singleLine = true,
                minLines = 1,
                isError = showErrors && passwordState.isBlank(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Default.Visibility
                    else Icons.Default.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                }
            )

            OutlinedTextField(
                value = phoneNumberState,
                onValueChange = { phoneNumberState = it },
                label = { Text("Phone number") },
                modifier = fieldModifier,
                isError = showErrors && phoneNumberState.isBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            val coroutineScope = rememberCoroutineScope()

            Button(
                onClick = {
                    showErrors = true
                    if (hasEmptyFields()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please complete all fields")
                        }
                    } else {
                        viewModel.email(emailState) { success, errorMessage ->
                            if (success) {
                                onDetailsEntered(
                                    nameState,
                                    universityState,
                                    expertiseState,
                                    emailState,
                                    passwordState,
                                    phoneNumberState
                                )
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(errorMessage)
                                }
                            }
                        }
                    }
                },

                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 8.dp)
            ) {
                Text("Continue", color = Color.White)
            }
        }

        // SnackBar para mostrar errores
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


@Composable
fun LearningStylesScreen(selectedStyles: List<String>, onContinue: (List<String>) -> Unit) {
    var learningStyles by rememberSaveable { mutableStateOf(selectedStyles) }
    val styles = listOf("Visual", "Auditory", "Reading", "Kinesthetic")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TutorApp",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Select your preferred learning styles (Optional)",
            style = Typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            styles.forEach { style ->
                val isSelected = learningStyles.contains(style)
                Text(
                    text = style,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF1A2247) else Color.Black,
                    modifier = Modifier
                        .clickable {
                            learningStyles = if (isSelected) {
                                learningStyles - style
                            } else {
                                learningStyles + style
                            }
                        }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { onContinue(learningStyles) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp)
        ) {
            Text("Continue", color = Color.White)
        }
    }
}
@Composable
fun UploadProfileScreen(onImageUploaded: (Uri?) -> Unit) {
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profilePictureUri = uri
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TutorApp", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(16.dp))

        Text("You can upload a profile picture if you want", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            profilePictureUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(120.dp)
                )
            } ?: Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile Icon",
                modifier = Modifier.size(100.dp),
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { launcher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)),
            shape = RoundedCornerShape(50)
        ) {
            Text("Upload", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onImageUploaded(profilePictureUri) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)
        ) {
            Text("Continue", color = Color.White)
        }
    }
}





@Composable
fun UploadIDScreen(
    name: String,
    email: String,
    password: String,
    phoneNumber: String?,
    profilePicture: Uri?,
    university: String,
    major: String?,
    expertise: String?,
    role:String,
    learningStyles: List<String>?,
    onRegisterFail: (String) -> Unit,
    viewModel: RegisterViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var idPictureUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val contentResolver = context.contentResolver

    // Crear un Uri temporal para la foto de la cámara
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    // Selector de imagen desde la galería
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        idPictureUri = uri
    }

    // Tomar foto con la cámara
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            idPictureUri = cameraImageUri.value
        }
    }

    fun captureImage() {
        val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        cameraImageUri.value = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
        cameraImageUri.value?.let { uri ->
            cameraLauncher.launch(uri)
        }
    }



    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        idPictureUri = uri
    }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TutorApp", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "You have to upload a picture of your university ID so we can verify your identity and university.",

            style = Typography.bodyLarge,
            textAlign = TextAlign.Center

        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.Gray.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(8.dp))

                .clickable { galleryLauncher.launch("image/*") },

            contentAlignment = Alignment.Center
        ) {
            idPictureUri?.let {
                Image(painter = rememberAsyncImagePainter(it), contentDescription = "ID Picture")

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { galleryLauncher.launch("image/*") },colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))) {
                Text("Choose from Gallery")

            }

            Button(onClick = { captureImage() },colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))) {
                Text("Take a Photo")
            }


        } ?: Text("Tap to upload picture", color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))
        val coroutineScope = rememberCoroutineScope()

        Button(
            onClick = {
                if (idPictureUri != null) {
                    viewModel.register(
                        name = name,
                        email = email,
                        password = password,
                        phoneNumber = phoneNumber,
                        profilePictureUri = profilePicture,
                        university = university,
                        major = major,
                        expertise = expertise,
                        role = role,
                        learningStyles = learningStyles,
                        idPictureUri = idPictureUri,
                        context = context

                    ) { success, message ->
                        if (success) {
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                            Toast.makeText(context, "Register Successful", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(context, "Register Failed: $message", Toast.LENGTH_SHORT).show()
                            onRegisterFail("")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Please upload your university ID picture")
                    }
                    onRegisterFail("Please upload your university ID picture")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))
        ) {
            Text("Create my account", color = Color.White)
        }
        SnackbarHost(
            hostState = snackbarHostState,
        )
    }


}
