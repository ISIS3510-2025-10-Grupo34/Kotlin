package com.tutorapp.connectWithStudents

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import android.util.Log
import kotlin.math.*




@Composable
fun ConnectWithStudentsScreen(modifier: Modifier) {


    val context = LocalContext.current

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    RequestLocationPermission {
        getCurrentLocation(context, fusedLocationClient) { location ->
            Log.d("Location", "Lat: ${location.first}, Lng: ${location.second}")
        }
    }


    NearestUniversityFinder(modifier=modifier, context = context)

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



@Composable
fun NearestUniversityFinder(modifier: Modifier, context: Context) {
    var nearestUniversity by remember { mutableStateOf("Searching...") }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Diccionario de universidades con sus coordenadas (latitud, longitud)
    val universities = mapOf(
        "Universidad de Los Andes" to Pair(4.6167997, -74.0999867),
        "Universidad del Rosario" to Pair(4.5883434, -74.1212905),
        "Universidad Nacional" to Pair(4.6363615, -74.0881756),
        "Universidad Javeriana" to Pair(4.6308434, -74.0816096),
    )

    LaunchedEffect(Unit) {
        getCurrentLocation(context, fusedLocationClient) { userLocation ->
            nearestUniversity = findNearestUniversity(userLocation, universities)
        }
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
                text = "Connect with students",
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
                onClick = {  },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A3F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Send", color = Color.White)
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