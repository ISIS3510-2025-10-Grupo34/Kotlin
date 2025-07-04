package com.tutorapp.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tutorapp.ui.theme.TutorAppTheme
import androidx.activity.ComponentActivity
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.models.TutoringSession
import com.tutorapp.viewModels.ShowTutorsViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.location.LocationServices
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.Converters
import com.tutorapp.data.StudentProfileEntity
import com.tutorapp.remote.NetworkUtils
import com.tutorapp.viewModels.StudentProfileViewModel
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.DisposableEffect 
import androidx.compose.runtime.LaunchedEffect 
import android.net.ConnectivityManager 
import android.net.Network 
import android.net.NetworkCapabilities 
import android.net.NetworkRequest
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import com.tutorapp.data.ShowTutorsViewModelFactory


class ShowTutorsActivity: ComponentActivity(){
    private val showTutorsViewModel: ShowTutorsViewModel by viewModels {
        ShowTutorsViewModelFactory(application) // <-- Usa la Factory aquí
    }
    val studentProfileViewModel = StudentProfileViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra("TOKEN_KEY") ?: ""
        setContent {
            TutorAppTheme {
                // Estados para los datos que vienen de getSearchResults
                var universities by remember { mutableStateOf<List<UniversitySimple>>(emptyList()) }
                var coursesByUniversity by remember { mutableStateOf<Map<String, List<CourseSimple>>?>(null) }
                var tutorsByCourse by remember { mutableStateOf<Map<String, List<String>>?>(null) }

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                // Obtén el ViewModel (ya usa la Factory correcta)
                val viewModel = showTutorsViewModel

                // Listener de conectividad (tu código existente)
                val isOffline = remember{ mutableStateOf(!viewModel.isNetworkAvailable()) }
                DisposableEffect(Unit){
                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkCallback = object: ConnectivityManager.NetworkCallback(){
                        override fun onAvailable(network: Network){ super.onAvailable(network); isOffline.value = false }
                        override fun onLost(network:Network){ super.onLost(network); isOffline.value = true }
                        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                            super.onCapabilitiesChanged(network, networkCapabilities)
                            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                            isOffline.value = !hasInternet
                        }
                    }
                    val networkRequest = NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
                    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
                    onDispose { connectivityManager.unregisterNetworkCallback(networkCallback) }
                }
                LaunchedEffect(isOffline.value) { // Efecto para el Snackbar de offline
                    if (isOffline.value) {
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = "No internet connection. Sessions shown may differ from those in real time.",
                                duration = SnackbarDuration.Indefinite
                            )
                        }
                    } else {
                        snackbarHostState.currentSnackbarData?.let {
                            if(it.visuals.message == "No internet connection. Sessions shown may differ from those in real time.") { it.dismiss() }
                        }
                    }
                }

                // --- Llamada a getSearchResults para los datos de filtros ---
                LaunchedEffect(Unit) {
                    Log.d("ActivityCompose", "Calling getSearchResults...")
                    viewModel.getSearchResults { success, data ->
                        Log.d("ActivityCompose", "getSearchResults Result: success=$success")
                        if (success && data != null) {
                            universities = data.data?.map { (uniName, uni) -> UniversitySimple(name = uniName, id = uni.id) } ?: emptyList()
                            coursesByUniversity = data.data?.mapValues { (_, uni) -> uni.courses.map { (cName, c) -> CourseSimple(cName, c.id) } }
                            tutorsByCourse = data.data?.flatMap { (_, uni) -> uni.courses.map { (cName, c) -> cName to c.tutors_names } }?.toMap()
                            Log.d("ActivityCompose", "Filter data updated.")
                        } else {
                            Log.e("ActivityCompose", "getSearchResults failed or returned null data.")
                            // Opcional: Mostrar error si falla carga de filtros
                            // scope.launch { snackbarHostState.showSnackbar("Failed to load filter options") }
                        }
                    }
                }

                // Log inicial de tiempo
                LaunchedEffect(Unit) {
                    val timeToBookStartTime = System.currentTimeMillis()
                    val prefs = getSharedPreferences("timeToBookPrefs", MODE_PRIVATE)
                    prefs.edit().putLong("timeToBookStart", timeToBookStartTime).apply()
                    Log.i("ShowTutorsTime", "Time to book started")
                }


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    // Llama a ShowTutorsScreen pasando los estados que se actualizan
                    ShowTutorsScreen(
                        Modifier.padding(innerPadding),
                        viewModel, // Pasa el ViewModel completo
                        token,
                        universities, // Pasa el estado
                        coursesByUniversity, // Pasa el estado
                        tutorsByCourse, // Pasa el estado
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        studentProfileViewModel
                    )
                }
            }
        } // --- Fin de setContent ---
    } // Fin de onCreate

}
@Composable
fun ShowTutorsScreen(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel, token: String, universities: List<UniversitySimple>,
                     coursesByUniversity: Map<String, List<CourseSimple>>?, tutorsByCourse : Map<String, List<String>>?, scope: CoroutineScope, snackbarHostState: SnackbarHostState, studentProfileViewModel: StudentProfileViewModel){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)
    val dao = db.studentProfileDao()
    val userid = Session.userid
    val role = Session.role


    if(NetworkUtils.isConnected(context)) {
        if (role == "student") {
            LaunchedEffect(userid) {

                coroutineScope.launch { val data = dao.loadData()
                    if(data==null){
                        studentProfileViewModel.studentProfile(userid.toString()) { profile ->
                            coroutineScope.launch {
                                if (profile != null) {
                                    dao.saveData(
                                        StudentProfileEntity(
                                            name = profile.name,
                                            university = profile.university,
                                            major = profile.major,
                                            learningStyles = Converters.fromStringList(profile.learning_styles)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }



            }
        }
    }





    BackHandler(enabled = true) {

    }
    Column (modifier = modifier.fillMaxSize(1f)){
        TutorScreenHeader(modifier = Modifier.height(IntrinsicSize.Min),token)
        Spacer(modifier = Modifier.height(20.dp))
        FilterResultsButton(modifier = Modifier, showTutorsViewModel, universities, coursesByUniversity, tutorsByCourse)
        ListOfTutorCards(modifier = modifier, showTutorsViewModel, token, scope, snackbarHostState)
    }

}


@Composable
fun TutorScreenHeader(modifier: Modifier, token: String) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TutorApp",
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(25.dp)) {
            // Calendario
            IconButton(
                onClick = {
                    val jsonToken = JSONObject(token)
                    val id = jsonToken.get("id").toString()

                    val intent = Intent(context, CalendarActivity::class.java).apply {
                        putExtra("ID", id)
                        putExtra("FORCE_REFRESH", true)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF192650))
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar",
                    tint = Color.White
                )
            }

            // Notificaciones
            IconButton(
                onClick = {
                    val jsonToken = JSONObject(token)
                    val role = jsonToken.get("role").toString()
                    val id = jsonToken.get("id").toString()

                    val intent = Intent(
                        context,
                        if (role == "tutor") ConnectWithStudentsActivity::class.java
                        else NotificationCenterActivity::class.java
                    ).apply {
                        putExtra("ID", id)
                    }

                    context.startActivity(intent)
                },
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF192650))
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }

            // Perfil
            IconButton(
                onClick = {
                    val jsonToken = JSONObject(token)
                    val role = jsonToken.get("role").toString()
                    val id = jsonToken.get("id").toString()

                    val intent = Intent(
                        context,
                        if (role == "tutor") TutorProfileActivity::class.java
                        else StudentProfileActivity::class.java
                    ).apply {
                        putExtra("ID", id)
                    }

                    context.startActivity(intent)
                },
                modifier = Modifier
                    .size(35.dp)
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




@Composable
fun FilterResultsButton(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel, universities: List<UniversitySimple>,
                        coursesByUniversity: Map<String, List<CourseSimple>>?, tutorsByCourse : Map<String, List<String>>?){

    var showBottomSheet by remember { mutableStateOf(false) }
    Row (modifier){
        Button(onClick = { showBottomSheet = true }, modifier.weight(1f)
            .padding(horizontal = 35.dp)
            , colors = ButtonColors(containerColor = Color(0xFF192650), contentColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color(0xFF192650) )
        )

        {
            Text("Filter results", color = Color.White)
        }
        Column(modifier.weight(1f)) {  }
    }

    if (showBottomSheet){
        FilterBottomSheet(modifier=modifier, showTutorsViewModel,
            onDismissRequest = { showBottomSheet = false }, universities, coursesByUniversity, tutorsByCourse
        )


    }

}

@Composable
fun ListOfTutorCards(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel, token: String, scope: CoroutineScope, snackbarHostState: SnackbarHostState){
    // Usando Column + verticalScroll como en tu versión
    val sessions by showTutorsViewModel.sessions.collectAsState()

    val emptyFilter = showTutorsViewModel.emptyFilter
    val isLoading = showTutorsViewModel.isLoading // Podrías usar isLoading para un indicador
    val scrollState = rememberScrollState()

    if (isLoading && sessions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp), // Padding aplicado aquí
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre tarjetas
        ) {
            if (emptyFilter) {
                Text("No tutoring sessions matched with the filter.", modifier = Modifier.fillMaxWidth().padding(vertical=16.dp), textAlign = TextAlign.Center)
            } else if (sessions.isEmpty() && !isLoading) {
                Text("No tutoring sessions available.", modifier = Modifier.fillMaxWidth().padding(vertical=16.dp), textAlign = TextAlign.Center)
            } else {
                sessions.forEach { tutoringSession ->
                    TutorCard(
                        modifier = Modifier.fillMaxWidth(), // Tarjeta ocupa ancho
                        tutoringSession = tutoringSession,
                        token = token,
                        showTutorsViewModel = showTutorsViewModel,
                        scope = scope,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
            // Indicador de carga al final si está refrescando y hay items
            if (isLoading && sessions.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical=16.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}


@Composable
fun TutorCard(modifier: Modifier, tutoringSession: TutoringSession, token: String, showTutorsViewModel: ShowTutorsViewModel, scope: CoroutineScope, snackbarHostState: SnackbarHostState) {
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
                val tokenFormatted = Gson().fromJson(token, LoginTokenDecoded::class.java)
                val intent = Intent(context, TutorProfileActivity::class.java).apply {
                    putExtra("TOKEN_KEY", tokenFormatted)
                    putExtra("TUTOR_ID", tutoringSession.tutor_id.toInt())
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
                    text = tutoringSession.tutor[0].uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tutoringSession.tutor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Information Section (Column)
        Column {
            Text(
                text = tutoringSession.course,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = tutoringSession.university,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Price: $"+tutoringSession.cost.toString(),
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date: "+tutoringSession.date_time,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button
        Button(
            onClick = onClick@{
                val userId = Session.userid ?: return@onClick  // Asegúrate de tener el ID

                showTutorsViewModel.bookTutoringSession(userId.toInt(), tutoringSession.id) { success, message ->
                    if (success) {
                        showTutorsViewModel.bookingTime()
                        val prefs = context.getSharedPreferences("timeToBookPrefs", Context.MODE_PRIVATE)
                        val startTime = prefs.getLong("timeToBookStart", 0L)
                        if (startTime != 0L) {
                            val timeToBook = System.currentTimeMillis() - startTime
                            showTutorsViewModel.postTimeToBook(timeToBook.toFloat(), tutoringSession.tutor_id.toInt())
                            prefs.edit().remove("timeToBookStart").apply()
                        }

                        showTutorsViewModel.fetchTutorPhoneNumber(tutoringSession.tutor_id.toInt(), sessionId = tutoringSession.id) { phoneNumber ->
                            if (phoneNumber != null) {
                                val url = "https://wa.me/57$phoneNumber"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                try {
                                    context.startActivity(intent)
                                    showTutorsViewModel.loadInitialSessions()
                                } catch (e: Exception) {
                                    scope.launch { snackbarHostState.showSnackbar("Could not open WhatsApp.") }
                                }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Could not fetch tutor's phone number.") }
                            }
                        }

                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Booking failed: $message") }
                    }
                }


            }
            ,
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))
        ) {
            Text(text = "Book", fontSize = 16.sp, color = Color.White)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel,
                      onDismissRequest: () -> Unit, universities: List<UniversitySimple>,
                      coursesByUniversity: Map<String, List<CourseSimple>>?, tutorsByCourse : Map<String, List<String>>?) {

    val coroutineScope = rememberCoroutineScope()
    var expandedUniversity by remember { mutableStateOf(false) }
    var selectedUniversity by remember {
        mutableStateOf(
            mapOf(
                "name" to "",
                "id" to -1,
            )
        )
    }
    var expandedCourse by remember { mutableStateOf(false) }
    var selectedCourse by remember {
        mutableStateOf(
            mapOf(
                "name" to "",
                "id" to -1,
            )
        )
    }

    var expandedTutor by remember { mutableStateOf(false) }
    var selectedTutor by remember {
        mutableStateOf(
            mapOf(
                "name" to ""
            )
        )
    }

    var isUniversitySelected = selectedUniversity["name"] != ""
    var isCourseSelected = selectedCourse["name"] != ""
    var isTutorSelected = selectedTutor["name"] != ""

    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Dropdown para University
            ExposedDropdownMenuBox(
                expanded = expandedUniversity,
                onExpandedChange = { expandedUniversity = !expandedUniversity }
            ) {
                OutlinedTextField(
                    value = selectedUniversity["name"].toString(),
                    onValueChange = {},
                    label = { Text("University") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUniversity) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedUniversity,
                    onDismissRequest = { expandedUniversity = false }
                ) {
                    if (isCourseSelected && isTutorSelected) {
                        val matchingUniversities = coursesByUniversity
                            ?.filter { (_, courses) ->
                                courses.any { course ->
                                    course.courseName == selectedCourse["name"] &&
                                            tutorsByCourse?.get(course.courseName)
                                                ?.contains(selectedTutor["name"]) == true
                                }
                            }
                            ?.keys

                        matchingUniversities?.forEach { university ->
                            DropdownMenuItem(
                                text = { Text(university) },
                                onClick = {
                                    selectedUniversity = mapOf(
                                        "name" to university
                                    )
                                    expandedUniversity = false
                                    isUniversitySelected = true
                                }
                            )
                        }


                    } else if (isTutorSelected) {
                        val universitiesWithTutor = coursesByUniversity
                            ?.filter { (_, courses) ->
                                courses.any { course ->
                                    val tutors = tutorsByCourse?.get(course.courseName).orEmpty()
                                    selectedTutor["name"] in tutors
                                }
                            }
                            ?.keys

                        universitiesWithTutor?.forEach { university ->
                            DropdownMenuItem(
                                text = { Text(university) },
                                onClick = {
                                    selectedUniversity = mapOf(
                                        "name" to university
                                    )
                                    expandedUniversity = false
                                    isUniversitySelected = true
                                }
                            )
                        }
                    } else if (isCourseSelected) {

                        val universitiesWithCourse = coursesByUniversity
                            ?.filter { (_, courses) ->
                                courses.any { it.courseName == selectedCourse["name"] }
                            }?.keys


                        universitiesWithCourse?.forEach { university ->
                            DropdownMenuItem(
                                text = { Text(university) },
                                onClick = {
                                    selectedUniversity = mapOf(
                                        "name" to university
                                    )
                                    expandedUniversity = false
                                    isUniversitySelected = true
                                }
                            )
                        }
                    } else {
                        universities.forEach { university ->
                            DropdownMenuItem(
                                text = { Text(university.name) },
                                onClick = {
                                    selectedUniversity = mapOf(
                                        "name" to university.name,
                                        "id" to university.id,
                                    )
                                    selectedCourse = mapOf(
                                        "name" to "",
                                        "id" to -1,
                                    )
                                    expandedUniversity = false
                                    isUniversitySelected = true
                                }
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown para Course
            ExposedDropdownMenuBox(
                expanded = expandedCourse,
                onExpandedChange = { expandedCourse = !expandedCourse },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCourse["name"].toString(),
                    onValueChange = {},
                    label = { Text("Course") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourse) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCourse,
                    onDismissRequest = { expandedCourse = false }
                ) {
                    if (isUniversitySelected && isTutorSelected) {
                        val matchingCourses = coursesByUniversity!![selectedUniversity["name"]]
                            ?.filter { course ->
                                tutorsByCourse!![course.courseName]?.contains(selectedTutor["name"]) == true
                            }

                        matchingCourses?.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.courseName) },
                                onClick = {
                                    selectedCourse = mapOf(
                                        "name" to course.courseName
                                    )
                                    expandedCourse = false
                                    isCourseSelected = true
                                }
                            )
                        }

                    } else if (isUniversitySelected) {

                        coursesByUniversity!![selectedUniversity["name"]]?.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.courseName) },
                                onClick = {
                                    selectedCourse = mapOf(
                                        "name" to course.courseName
                                    )
                                    expandedCourse = false
                                    isCourseSelected = true
                                }
                            )
                        }
                    } else if (isTutorSelected) {
                        val coursesByTutor = tutorsByCourse
                            ?.filter { (_, tutors) ->
                                selectedTutor["name"] in tutors
                            }
                            ?.keys

                        coursesByTutor?.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course) },
                                onClick = {
                                    selectedCourse = mapOf(
                                        "name" to course
                                    )
                                    expandedCourse = false
                                    isCourseSelected = true
                                }
                            )
                        }

                    } else {
                        val allCourseNames = coursesByUniversity
                            ?.values
                            ?.flatten()
                            ?.map { it.courseName }
                            ?.distinct()

                        allCourseNames?.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course) },
                                onClick = {
                                    selectedCourse = mapOf(
                                        "name" to course
                                    )
                                    expandedCourse = false
                                    isCourseSelected = true
                                }
                            )
                        }
                    }

                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown para Tutor
            ExposedDropdownMenuBox(
                expanded = expandedTutor,
                onExpandedChange = { expandedTutor = !expandedTutor },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTutor["name"].toString(),
                    onValueChange = {},
                    label = { Text("Tutor") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTutor) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedTutor,
                    onDismissRequest = { expandedTutor = false }
                ) {
                    if (isUniversitySelected && isCourseSelected) {
                        val tutorsForCourseInUniversity: List<String>? =
                            coursesByUniversity!![selectedUniversity["name"]]
                                ?.find { it.courseName == selectedCourse["name"] }
                                ?.let { course ->
                                    tutorsByCourse!![course.courseName]
                                }

                        tutorsForCourseInUniversity?.forEach { tutor ->
                            DropdownMenuItem(
                                text = { Text(tutor) },
                                onClick = {
                                    selectedTutor = mapOf(
                                        "name" to tutor
                                    )
                                    expandedTutor = false
                                }
                            )
                        }
                    } else if (isCourseSelected) {
                        tutorsByCourse!![selectedCourse["name"]]?.forEach { tutor ->
                            DropdownMenuItem(
                                text = { Text(tutor) },
                                onClick = {
                                    selectedTutor = mapOf(
                                        "name" to tutor
                                    )
                                    expandedTutor = false
                                }
                            )
                        }
                    } else if (isTutorSelected) {
                        val tutorsInUniversity: Set<String>? =
                            coursesByUniversity!![selectedUniversity["name"]]
                                ?.mapNotNull { course ->
                                    tutorsByCourse!![course.courseName]
                                }
                                ?.flatten()
                                ?.toSet()

                        tutorsInUniversity?.forEach { tutor ->
                            DropdownMenuItem(
                                text = { Text(tutor) },
                                onClick = {
                                    selectedTutor = mapOf(
                                        "name" to tutor
                                    )
                                    expandedTutor = false
                                }
                            )
                        }
                    } else {
                        val uniqueTutors = tutorsByCourse
                            ?.values
                            ?.flatten()
                            ?.toSet()

                        uniqueTutors?.forEach { tutor ->
                            DropdownMenuItem(
                                text = { Text(tutor) },
                                onClick = {
                                    selectedTutor = mapOf(
                                        "name" to tutor
                                    )
                                    expandedTutor = false
                                }
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                showTutorsViewModel.onFilterClick(
                                    selectedUniversity["name"].toString(),
                                    selectedCourse["name"].toString(),
                                    selectedTutor["name"].toString()
                                )
                            } catch (e: Exception) {
                                println(e)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))
                ) {
                    Text("Filter", color = Color.White)
                }

                Button(
                    onClick = {
                        selectedUniversity = mapOf("name" to "", "id" to -1)
                        selectedCourse = mapOf("name" to "", "id" to -1)
                        selectedTutor = mapOf("name" to "")
                        isUniversitySelected = false
                        isCourseSelected = false
                        isTutorSelected = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A1128))
                ) {
                    Text("Reset Filters", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(30.dp))

        }

    }
}