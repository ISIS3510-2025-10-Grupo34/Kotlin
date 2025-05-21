package com.tutorapp.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.tutorapp.ui.theme.TutorAppTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.CachedNotificationEntity
import com.tutorapp.models.Notification
import com.tutorapp.remote.NetworkUtils
import com.tutorapp.viewModels.NotificationCenterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*
import java.time.LocalDateTime
import java.time.LocalTime
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState


class ConnectWithStudentsActivity : ComponentActivity() {

    private val notificationCenterViewModel: NotificationCenterViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TutorAppTheme {

                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    ConnectWithStudentsScreen(
                        modifier = Modifier.padding(innerPadding),
                        notificationCenterViewModel = notificationCenterViewModel,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConnectWithStudentsScreen(
    modifier: Modifier,
    notificationCenterViewModel: NotificationCenterViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var permissionGranted by remember { mutableStateOf<Boolean?>(null) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val permission = Manifest.permission.ACCESS_FINE_LOCATION
                val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                permissionGranted = granted
                permanentlyDenied = !granted && !ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)
            }


        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }





    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (!isGranted) {
            permanentlyDenied = isPermissionPermanentlyDenied(context)
        }
    }

    LaunchedEffect(Unit) {

        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val locationGranted = ContextCompat.checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED
        permissionGranted = locationGranted

        if (!locationGranted) {
            permissionLauncher.launch(locationPermission)
        }
    }


    when (permissionGranted) {
        true -> {
            getCurrentLocation(context, fusedLocationClient) { location ->
                Log.d("Location", "Lat: ${location.first}, Lng: ${location.second}")
            }
            NearestUniversityFinder(
                modifier = Modifier,
                context = context,
                notificationCenterViewModel = notificationCenterViewModel,
                snackbarHostState = snackbarHostState
            )

        }

        false -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Location access is necessary to find nearest universities.")
                Spacer(modifier = Modifier.height(16.dp))

                if (permanentlyDenied) {
                    Button(onClick = { showSettingsDialog = true },
                        colors = ButtonColors(containerColor = Color(0xFF192650), contentColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color(0xFF192650) )) {
                        Text("Go to settings", color = Color.White)
                    }
                } else {
                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                        colors = ButtonColors(containerColor = Color(0xFF192650), contentColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color(0xFF192650) )) {
                        Text("Request permission again", color = Color.White)
                    }
                }
            }


            if (showSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    title = { Text("Location access is required") },
                    text = {
                        Text("Go to settings and manually enable the location permission to continue.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showSettingsDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Go to settings")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showSettingsDialog = false
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        null -> {
            CircularProgressIndicator(modifier = modifier)
        }
    }



}



@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false // Nuevo parámetro para indicar si hay un error
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = if (isError) Color.Red else Color.Gray) // Color rojo si hay error
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) Color.Red else Color(0xFF1A1A3F), // Borde rojo si hay error
                unfocusedBorderColor = if (isError) Color.Red.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.5f), // Borde rojo si hay error
                focusedContainerColor = Color(0xFFECE6F0),
                unfocusedContainerColor = Color(0xFFF3EDF8)
            )
        )
        if (isError) {
            Text(text = "This field is required", color = Color.Red, fontSize = 10.sp) // Mensaje de error
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NearestUniversityFinder(
    modifier: Modifier,
    context: Context,
    notificationCenterViewModel: NotificationCenterViewModel,
    snackbarHostState: SnackbarHostState
) {
    var notificationPermissionGranted by remember { mutableStateOf(checkNotificationPermission(context)) }
    var notificationPermanentlyDenied by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }


    // Estado del TimePicker
    val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }




    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
        permissionRequested = true
        if (!isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
            notificationPermanentlyDenied = !shouldShowRationale
        } else {
            notificationPermanentlyDenied = false
        }
    }

    var nearestUniversity by remember { mutableStateOf("Searching...") }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val universities = mapOf(
        "Universidad de Los Andes" to Pair(4.6026819, -74.0675411),
        "Universidad del Rosario" to Pair(4.5883434, -74.1212905),
        "Universidad Nacional" to Pair(4.6363615, -74.0881756),
        "Universidad Javeriana" to Pair(4.6308434, -74.0816096),
        "University of Chicago" to Pair(41.7919066, -87.6076938)

    )

    val notificationId = 1
    val channelId = "message_channel"

    LaunchedEffect(Unit) {
        getCurrentLocation(context, fusedLocationClient) { userLocation ->
            nearestUniversity = findNearestUniversity(userLocation, universities)
        }
        createNotificationChannel(context, channelId)

        if (!notificationPermissionGranted && !permissionRequested) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        launch {
            val db = AppDatabase.getDatabase(context)
            val dao = db.cachedNotificationDao()

            while (true) {
                delay(60_000)

                if (NetworkUtils.isConnected(context)) {
                    val now = LocalDateTime.now()
                    val retryables = dao.getPendingToRetry().filter {
                        val deadlineTime = LocalTime.parse(it.deadline)
                        now.toLocalTime().isBefore(deadlineTime)
                    }

                    retryables.forEach { notif ->
                        try {
                            notificationCenterViewModel.postNotification(
                                Notification(
                                    title = notif.title,
                                    message = notif.message,
                                    place = notif.place,
                                    university = notif.university,
                                    date = notif.date
                                )
                            )
                            dao.delete(notif)
                            Log.d("Retry", "Notification sent after retry: ${notif.title}")
                        } catch (e: Exception) {
                            Log.e("Retry", "Retry failed", e)
                        }
                    }
                }
            }
        }
    }

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var timeLimit by remember { mutableStateOf("") }
    var isTimeEmpty by remember { mutableStateOf(false) }



    var isTitleEmpty by remember { mutableStateOf(false) }
    var isMessageEmpty by remember { mutableStateOf(false) }
    var isPlaceEmpty by remember { mutableStateOf(false) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Nearest University:", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = nearestUniversity, style = MaterialTheme.typography.bodyLarge)


        if (notificationPermissionGranted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Connect with the students!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A3F),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    value = title,
                    onValueChange = { title = it; isTitleEmpty = it.trim().isEmpty() },
                    label = "Title",
                    isError = isTitleEmpty
                )
                InputField(
                    value = message,
                    onValueChange = { message = it; isMessageEmpty = it.trim().isEmpty() },
                    label = "Message",
                    isError = isMessageEmpty
                )
                InputField(
                    value = place,
                    onValueChange = { place = it; isPlaceEmpty = it.trim().isEmpty() },
                    label = "Place",
                    isError = isPlaceEmpty
                )


                Text(text = "Retry deadline (HH:mm)", fontSize = 12.sp, color = Color.Gray)

                Button(
                    onClick = { showTimePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECE6F0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (timeLimit.isEmpty()) "Pick Time" else "Selected: $timeLimit",
                        color = Color(0xFF1A1A3F)
                    )
                }



                Spacer(modifier = Modifier.height(16.dp))





                Button(
                    onClick = {
                        isTitleEmpty = title.trim().isEmpty()
                        isMessageEmpty = message.trim().isEmpty()
                        isPlaceEmpty = place.trim().isEmpty()
                        isTimeEmpty = timeLimit.isBlank()

                        if (notificationPermissionGranted && !isTitleEmpty && !isMessageEmpty && !isPlaceEmpty && !isTimeEmpty) {
                            sendNotification(
                                context,
                                channelId,
                                notificationId,
                                title,
                                message,
                                place,
                                nearestUniversity,
                                notificationCenterViewModel,
                                timeLimit = timeLimit,
                                snackbarHostState = snackbarHostState
                            )
                        } else if (notificationPermissionGranted) {
                            // Los campos vacíos se resaltarán automáticamente
                        } else {
                            if (notificationPermanentlyDenied) {
                                showNotificationSettingsDialog = true
                            } else {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A3F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        if (notificationPermissionGranted) "Send" else "Enable notifications permission",
                        color = Color.White
                    )
                }


                if (showTimePicker) {
                    TimePickerDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val hour = timePickerState.hour
                                val minute = timePickerState.minute

                                if (isTimeInFuture(hour, minute)) {
                                    val formatted = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                                    timeLimit = formatted
                                    showTimePicker = false
                                } else {
                                    // Muestra un snackbar o Toast
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("Please pick a time at least 1 minute in the future.")
                                    }
                                }
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        TimePicker(state = timePickerState)
                    }
                }

            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Notification permission is required to send messages.",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (notificationPermanentlyDenied) {
                    Button(onClick = { showNotificationSettingsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A3F), contentColor = Color.White)
                    ) {
                        Text("Go to Settings", color = Color.White)
                    }
                } else {
                    Button(onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A3F), contentColor = Color.White)
                    ) {
                        Text("Request Permission", color = Color.White)
                    }
                }
            }
        }
    }

    if (showNotificationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationSettingsDialog = false },
            title = { Text("Push notifications are required") },
            text = {
                Text("Go to Settings and manually enable the notification permission to continue.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Go to settings")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNotificationSettingsDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentPermissionGranted = checkNotificationPermission(context)
                notificationPermissionGranted = currentPermissionGranted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !currentPermissionGranted && permissionRequested) {
                    val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    notificationPermanentlyDenied = !shouldShowRationale
                } else {
                    notificationPermanentlyDenied = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
        onDispose {}
    }
}


@SuppressLint("MissingPermission")
fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Pair<Double, Double>) -> Unit
) {
    val locationTask = fusedLocationClient.lastLocation
    locationTask.addOnSuccessListener { location: Location? ->
        location?.let {
            onLocationReceived(Pair(it.latitude, it.longitude))
        }
    }
}

fun findNearestUniversity(userLocation: Pair<Double, Double>, universities: Map<String, Pair<Double, Double>>): String {
    return universities.minByOrNull { (_, uniLocation) ->
        calculateDistance(userLocation, uniLocation)
    }?.key ?: "No universities found"
}

// Función para calcular la distancia entre dos coordenadas usando la fórmula de Haversine
fun calculateDistance(loc1: Pair<Double, Double>, loc2: Pair<Double, Double>): Double {
    val (lat1, lon1) = loc1
    val (lat2, lon2) = loc2
    val radius = 6371.0 // Radio de la Tierra en km

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return radius * c
}

fun isPermissionPermanentlyDenied(context: Context): Boolean {
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED &&
            !ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)
}

@Composable
fun RequestLocationPermissionLauncher(onPermissionResult: (Boolean) -> Unit): ManagedActivityResultLauncher<String, Boolean> {
    val context = LocalContext.current

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }
}


@Composable
fun RequestLocationPermission(onResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            onResult(true)
        } else {
            permissionLauncher.launch(permission)
        }
    }
}


fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}


fun createNotificationChannel(context: Context, channelId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Message Channel"
        val descriptionText = "Channel for form messages"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun isTimeInFuture(hour: Int, minute: Int): Boolean {
    val now = LocalTime.now()
    val pickedTime = LocalTime.of(hour, minute)
    return pickedTime.isAfter(now.plusMinutes(1))
}

@RequiresApi(Build.VERSION_CODES.O)
fun sendNotification(context: Context, channelId: String, notificationId: Int, title: String, message: String, place: String, nearestUnivesity:String, notificationCenterViewModel: NotificationCenterViewModel,  timeLimit: String,
                     snackbarHostState: SnackbarHostState ) {

    val db = AppDatabase.getDatabase(context)
    val cachedDao = db.cachedNotificationDao()

    if (!NetworkUtils.isConnected(context)) {
        val pending = CachedNotificationEntity(
            title = title,
            message = message,
            place = place,
            university = nearestUnivesity,
            date = LocalDateTime.now().toString(),
            scheduledTime = LocalDateTime.now().toString(),
            deadline = timeLimit
        )

        CoroutineScope(Dispatchers.IO).launch {
            cachedDao.insertAll(listOf(pending))
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("No internet. Notification saved for retry.")
            }
        }
        return
    }


    notificationCenterViewModel.postNotification(Notification(title, message, place, university = nearestUnivesity.toString(), date = LocalDateTime.now().toString() ))


    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title.ifEmpty { "New Message" })
        .setContentText("$message (Place: $place)")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notify(notificationId, notificationBuilder.build())
    }
}