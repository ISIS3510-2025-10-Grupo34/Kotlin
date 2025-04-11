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
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.tutorapp.models.Notification
import com.tutorapp.remote.RetrofitClient
import com.tutorapp.viewModels.NotificationCenterViewModel
import kotlinx.coroutines.launch
import kotlin.math.*
import java.time.LocalDateTime
import kotlin.math.*




class ConnectWithStudentsActivity : ComponentActivity() {

    private val notificationCenterViewModel: NotificationCenterViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TutorAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    ConnectWithStudentsScreen(modifier = Modifier.padding(innerPadding), notificationCenterViewModel)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConnectWithStudentsScreen(modifier: Modifier, notificationCenterViewModel: NotificationCenterViewModel) {



    val context = LocalContext.current

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    RequestLocationPermission {
        getCurrentLocation(context, fusedLocationClient) { location ->
            Log.d("Location", "Lat: ${location.first}, Lng: ${location.second}")
        }
    }

    NearestUniversityFinder(modifier=modifier, context = context, notificationCenterViewModel)

}

@Composable
fun InputField(value: String, onValueChange: (String) -> Unit, label: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A1A3F),
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = Color(0xFFECE6F0),
                unfocusedContainerColor = Color(0xFFF3EDF8)
            )
        )
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NearestUniversityFinder(modifier: Modifier, context: Context, notificationCenterViewModel: NotificationCenterViewModel) {

    var nearestUniversity by remember { mutableStateOf("Searching...") }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val universities = mapOf(
        "Universidad de Los Andes" to Pair(4.6167997, -74.0999867),
        "Universidad del Rosario" to Pair(4.5883434, -74.1212905),
        "Universidad Nacional" to Pair(4.6363615, -74.0881756),
        "Universidad Javeriana" to Pair(4.6308434, -74.0816096),
    )


    val notificationId = 1
    val channelId = "message_channel"

    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermission = isGranted
        }

    LaunchedEffect(Unit) {
        getCurrentLocation(context, fusedLocationClient) { userLocation ->
            nearestUniversity = findNearestUniversity(userLocation, universities)
        }

        createNotificationChannel(context, channelId)
    }


    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top=100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Nearest University:", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = nearestUniversity, style = MaterialTheme.typography.bodyLarge)

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
            Spacer(modifier = Modifier.height(4.dp))

            Spacer(modifier = Modifier.height(16.dp))

            InputField(value = title, onValueChange = { title = it }, label = "Title")
            InputField(value = message, onValueChange = { message = it }, label = "Message")
            InputField(value = place, onValueChange = { place = it }, label = "Place")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { if (hasNotificationPermission) {

                    sendNotification(context, channelId, notificationId, title, message, place, nearestUniversity, notificationCenterViewModel)

                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A3F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(if (hasNotificationPermission) "Send" else "Request Permission", color = Color.White)
            }
        }
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




@Composable
fun RequestLocationPermission(onGranted: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onGranted()
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            onGranted()
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

@RequiresApi(Build.VERSION_CODES.O)
fun sendNotification(context: Context, channelId: String, notificationId: Int, title: String, message: String, place: String, nearestUnivesity:String, notificationCenterViewModel: NotificationCenterViewModel) {

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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notify(notificationId, notificationBuilder.build())
    }
}