@file:OptIn(ExperimentalMaterial3Api::class)

package com.tutorapp.views

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.viewModels.AddCourseViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerDefaults
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.saveable.rememberSaveable

// Observer for network connectivity
interface ConnectivityObserver {
    fun observe(): Flow<Status>
    enum class Status { Available, Unavailable }
}

class NetworkConnectivityObserver(context: Context) : ConnectivityObserver {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityObserver.Status> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(ConnectivityObserver.Status.Available)
            }
            override fun onLost(network: Network) {
                trySend(ConnectivityObserver.Status.Unavailable)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        // emit initial status
        val current = if (isCurrentlyConnected()) ConnectivityObserver.Status.Available else ConnectivityObserver.Status.Unavailable
        trySend(current)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

data class UniversitySimple(val name: String, val id: Int)
data class CourseSimple(val courseName: String, val id: Int)

class AddCourseActivity : ComponentActivity() {
    private val addCourseViewModel: AddCourseViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserInfo: LoginTokenDecoded? = intent.getParcelableExtra("TOKEN_KEY")
        val observer = NetworkConnectivityObserver(applicationContext)
        val connectivityFlow = observer.observe()

        addCourseViewModel.getSearchResults { _, data ->
            val universities = data?.data?.map { (uniName, uni) ->
                UniversitySimple(name = uniName, id = uni.id)
            } ?: emptyList()

            val coursesByUniversity = data?.data?.mapValues { (_, uni) ->
                uni.courses.map { (courseName, course) ->
                    CourseSimple(courseName = courseName, id = course.id)
                }
            } ?: emptyMap()

            val customColorScheme = lightColorScheme(
                primary = PrimaryAppColor,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE1E5F2),
                onPrimaryContainer = PrimaryAppColor,
                secondary = Color(0xFF4A90E2),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFD1E7FF),
                onSecondaryContainer = Color(0xFF4A90E2),
                tertiary = Color(0xFF51C3B3),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFE6F6F4),
                onTertiaryContainer = Color(0xFF51C3B3),
                background = Color(0xFFF5F5F5),
                onBackground = Color.Black,
                surface = Color.White,
                onSurface = Color.Black,
                error = Color.Red,
                onError = Color.White
            )

            setContent {
                MaterialTheme(
                    colorScheme = customColorScheme
                ) {
                    // Tu pantalla o Composable aquí
                    AddCourseScreen(
                        viewModel = addCourseViewModel,
                        universities = universities,
                        coursesByUniversity = coursesByUniversity,
                        currentUserInfo = currentUserInfo,
                        connectivityStatusFlow = connectivityFlow
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseScreen(
    viewModel: AddCourseViewModel,
    universities: List<UniversitySimple>,
    coursesByUniversity: Map<String, List<CourseSimple>>,
    currentUserInfo: LoginTokenDecoded?,
    connectivityStatusFlow: Flow<ConnectivityObserver.Status>
) {
    val context = LocalContext.current

    // Define Colombia timezone once
    val colombiaZone = ZoneId.of("America/Bogota")

    val connectivityStatus by connectivityStatusFlow.collectAsState(initial = ConnectivityObserver.Status.Available)
    val isConnected = connectivityStatus == ConnectivityObserver.Status.Available

    // --- Inicializar estados leyendo del cache ---
    // Use rememberSaveable for inputs that should survive process death AND configuration changes
    var selectedUniversityName by rememberSaveable {
        mutableStateOf(viewModel.getCachedInput("universityName") ?: "")
    }
    // Use mutableIntStateOf for primitive Int state
    var selectedUniversityId by rememberSaveable {
        mutableIntStateOf(viewModel.getCachedInput("universityId")?.toIntOrNull() ?: -1)
    }
    var selectedCourseName by rememberSaveable {
        mutableStateOf(viewModel.getCachedInput("courseName") ?: "")
    }
    var selectedCourseId by rememberSaveable {
        mutableIntStateOf(viewModel.getCachedInput("courseId")?.toIntOrNull() ?: -1)
    }
    var priceState by rememberSaveable { // Price is a string due to formatting/input
        mutableStateOf(viewModel.getCachedInput("price") ?: "")
    }

    // Fecha y hora
    var formattedDateTime by remember {
        mutableStateOf(viewModel.getCachedInput("dateTime") ?: "")
    }

    // Store selected date and time as ZonedDateTime to maintain timezone info
    // This state might not need to be rememberSaveable if it's always reconstructed
    // from formattedDateTimeForApi or re-picked.
    var selectedZonedDateTime by remember { mutableStateOf<ZonedDateTime?>(null) }

    var dateTimeError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Menús desplegables
    var expandedUniversity by remember { mutableStateOf(false) }
    var expandedCourse by remember { mutableStateOf(false) }


    // Initialize selectedZonedDateTime from cache if available
    LaunchedEffect(Unit) {
        if (formattedDateTime.isNotEmpty() && formattedDateTime.contains("-")) {
            try {
                val parts = formattedDateTime.split("-")
                val datePart = parts[0]
                val timePart = parts[1]
                val localDate = LocalDate.parse(datePart, dateFormatter)
                val localTime = LocalTime.parse(timePart, timeFormatter)
                selectedZonedDateTime = ZonedDateTime.of(localDate, localTime, colombiaZone)
            } catch (e: Exception) {
                // Handle parse error if needed
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("TutorApp", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Create a tutoring session",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))

        // --- University dropdown ---
        ExposedDropdownMenuBox(
            expanded = expandedUniversity,
            onExpandedChange = { expandedUniversity = !expandedUniversity }
        ) {
            OutlinedTextField(
                value = selectedUniversityName,
                onValueChange = {},
                label = { Text("University", color = PrimaryAppColor) },
                placeholder = { Text("Select university", color = Color.Gray) },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUniversity) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryAppColor,
                    unfocusedBorderColor = PrimaryAppColor
                )
            )

            ExposedDropdownMenu(
                expanded = expandedUniversity,
                onDismissRequest = { expandedUniversity = false }
            ) {
                universities.forEach { uni ->
                    DropdownMenuItem(
                        text = { Text(uni.name, color= PrimaryAppColor) },
                        onClick = {
                            selectedUniversityName= uni.name
                            selectedUniversityId= uni.id
                            viewModel.cacheInput("universityName", uni.name)
                            viewModel.cacheInput("universityId", uni.id.toString())
                            selectedCourseName= ""
                            selectedCourseId= -1
                            viewModel.cacheInput("courseName", "")
                            viewModel.cacheInput("courseId", "")
                            expandedUniversity= false
                        }
                    )
                }

            }
        }

        Spacer(Modifier.height(16.dp))
        // --- Course dropdown ---
        ExposedDropdownMenuBox(
            expanded = expandedCourse,
            onExpandedChange = { if (selectedUniversityId != -1) expandedCourse = !expandedCourse }
        ) {
            OutlinedTextField(
                value = selectedCourseName,
                onValueChange = {},
                label = { Text("Course") },
                enabled = selectedUniversityId != -1,
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourse) },
                modifier = Modifier
                    .menuAnchor()      // ← aquí
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedCourse,
                onDismissRequest = { expandedCourse = false }
            ) {
                coursesByUniversity[selectedUniversityName]?.forEach { crs ->
                    DropdownMenuItem(
                        text = { Text(crs.courseName) },
                        onClick = {
                            selectedCourseName = crs.courseName
                            selectedCourseId = crs.id
                            viewModel.cacheInput("courseName", crs.courseName)
                            viewModel.cacheInput("courseId", crs.id.toString())
                            expandedCourse = false
                        }
                    )
                }

            }
        }

        // Message if estimator is not available
        if (selectedUniversityId == -1 ) {
            Text(
                text = "A university must be selected first to see the available courses.",
                style=
                    MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(16.dp))
        // --- Precio ---
        OutlinedTextField(
            value = priceState,
            onValueChange = {
                val cleaned = it.replace(Regex("[^0-9]"), "")
                priceState = cleaned
                viewModel.cacheInput("price", cleaned)
            },
            label = { Text("Set the price (COP/hour)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Hint: Tutors that use our price estimator increased their students in 20%",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                currentUserInfo?.let {
                    viewModel.getPriceEstimation(it.id, selectedUniversityName) { ok, est ->
                        if (ok && est != null) {
                            priceState = est.toString()
                            viewModel.cacheInput("price", priceState)
                        } else if (ok && est == null) {
                            Toast.makeText(context, "Estimator couldn't find a price for this selection. Enter your price manually.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to get price estimation. Enter your price manually.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            enabled = selectedUniversityId != -1 && isConnected
        ) {
            Text("Use the estimator")
        }

        // Message if estimator is not available
        if (selectedUniversityId == -1  && isConnected) {
            Text(
                text = "A university must be selected first to use the estimator.",
                style=
                    MaterialTheme.typography.bodyMedium
            )
        }
        if (!isConnected) {
            Text(
                text = "The estimator is not available without internet, but you can enter price manually.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(16.dp))
        // --- Fecha y hora ---
        OutlinedTextField(
            value = formattedDateTime,
            onValueChange = {},
            label = { Text("Date and time of availability") },
            readOnly = true,
            isError = dateTimeError != null,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date and time"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        dateTimeError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // DatePickerDialog
        if (showDatePicker) {
            val picker = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        picker.selectedDateMillis?.let { ms ->
                            // 1. Creamos un Instant a partir del millis
                            val instant = Instant.ofEpochMilli(ms)
                            // 2. Extraemos el LocalDate en UTC (así no hay shift)
                            val localDateUtc = instant.atZone(ZoneOffset.UTC).toLocalDate()
                            // 3. Creamos el ZonedDateTime a la medianoche en Bogotá
                            val zonedStartOfDay = localDateUtc.atStartOfDay(colombiaZone)

                            // Almacenamos para luego ajustar la hora
                            selectedZonedDateTime = zonedStartOfDay

                            // Mostramos el time picker
                            showTimePicker = true
                        }
                        showDatePicker = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = picker)
            }
        }

        // TimePickerDialog
        if (showTimePicker) {
            val tp = rememberTimePickerState(is24Hour = true)
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        // Update the ZonedDateTime with selected time
                        selectedZonedDateTime = selectedZonedDateTime?.withHour(tp.hour)?.withMinute(tp.minute)

                        // Format for display and persistence
                        selectedZonedDateTime?.let { zdt ->
                            formattedDateTime = "${zdt.format(dateFormatter)}-${zdt.format(timeFormatter)}"

                            // Validation using ZonedDateTime for correct timezone comparison
                            val now = ZonedDateTime.now(colombiaZone)
                            dateTimeError = if (zdt.isBefore(now)) {
                                "Cannot select past date/time."
                            } else null

                            // Save to cache if valid
                            if (dateTimeError == null) {
                                viewModel.cacheInput("dateTime", formattedDateTime)
                            }
                        }

                        showTimePicker = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        // Clear formattedDateTime if time not selected
                        formattedDateTime = ""
                        selectedZonedDateTime = null
                        showTimePicker = false
                    }) {
                        Text("Cancel")
                    }
                }
            ) {
                // Usar TimePicker directamente con los colores personalizados
                TimePicker(
                    state = tp,
                    colors = TimePickerDefaults.colors(
                        // Colores principales para los números grandes
                        timeSelectorSelectedContainerColor = PrimaryAppColor,
                        timeSelectorUnselectedContainerColor = Color.White,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = PrimaryAppColor,

                        // Colores para el selector de hora
                        clockDialColor = Color(0xFFE1E5F2),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = PrimaryAppColor,
                        selectorColor = PrimaryAppColor,

                        // Colores para selector AM/PM (si lo usas)
                        periodSelectorSelectedContainerColor = PrimaryAppColor,
                        periodSelectorUnselectedContainerColor = Color(0xFFE1E5F2),
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = PrimaryAppColor,

                        // Color del contenedor general
                        containerColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                when {
                    selectedUniversityId == -1 -> Toast.makeText(context, "Select university", Toast.LENGTH_SHORT).show()
                    selectedCourseId == -1 -> Toast.makeText(context, "Select course", Toast.LENGTH_SHORT).show()
                    priceState.isEmpty() -> Toast.makeText(context, "Set price", Toast.LENGTH_SHORT).show()
                    formattedDateTime.isEmpty() || !formattedDateTime.contains("-") || formattedDateTime.endsWith("-") -> Toast.makeText(context, "Select date/time", Toast.LENGTH_SHORT).show()
                    dateTimeError != null -> Toast.makeText(context, dateTimeError, Toast.LENGTH_SHORT).show()
                    !isConnected -> {
                        currentUserInfo?.let { user ->
                            context.startActivity(
                                Intent(context, TutorProfileActivity::class.java)
                                    .putExtra("TOKEN_KEY", user)
                            )
                        }
                        return@Button // <-- Early return, skip postTutoringSession
                    }
                    else -> {
                        // Validate date/time is not in the past at the moment of saving
                        val now = ZonedDateTime.now(colombiaZone)
                        val selectedDateTime = selectedZonedDateTime
                        if (selectedDateTime == null || selectedDateTime.isBefore(now)) {
                            dateTimeError = "Cannot select past date/time."
                            Toast.makeText(context, "Cannot select past date/time.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        currentUserInfo?.let { user ->
                            viewModel.postTutoringSession(
                                user.id.toString(),
                                selectedCourseId.toString(),
                                priceState,
                                formattedDateTime
                            ) { ok, _ ->
                                if (ok) {
                                    Toast.makeText(context, "Tutoring created!", Toast.LENGTH_SHORT).show()
                                    viewModel.clearCache()
                                    context.startActivity(
                                        Intent(context, TutorProfileActivity::class.java)
                                            .putExtra("TOKEN_KEY", user)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAppColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
        if (!isConnected) {
            Text(
                text = "You are offline: the session will not be published but your input is will be saved as long as the application is open.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// Versión mejorada de tu TimePickerDialog con mejor contraste
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = {
            // Wrapping content in a custom theme to override time picker colors
            TimePickerTheme {
                content()
            }
        },
        containerColor = Color.White
    )
}

// Tema personalizado para el TimePicker usando MaterialTheme
@Composable
private fun TimePickerTheme(content: @Composable () -> Unit) {
    // En lugar de usar CompositionLocalProvider con LocalTimePickerColors,
    // usamos un MaterialTheme con colorScheme personalizado
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = PrimaryAppColor,
            onPrimary = Color.White,
            surface = Color.White,
            onSurface = PrimaryAppColor,
            surfaceVariant = Color(0xFFE1E5F2),
            onSurfaceVariant = PrimaryAppColor
        )
    ) {
        content()
    }
}
