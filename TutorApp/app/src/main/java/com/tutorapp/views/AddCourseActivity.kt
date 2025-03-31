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
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import com.google.gson.Gson
import com.tutorapp.viewModels.AddCourseViewModel

class AddCourseActivity : ComponentActivity() {
    private val addCourseViewModel: AddCourseViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addCourseViewModel.getSearchResults() { success, data ->
            if (success) {
                val universities: List<String> = data?.keys?.toList() ?: emptyList()
                val coursesByUniversity: Map<String, List<String>> = data?.mapValues { entry ->
                    entry.value.keys.toList()
                } ?: emptyMap()

                setContent {
                    AddCourseScreen(addCourseViewModel, universities, coursesByUniversity)
                }
            } else {
                setContent {
                    AddCourseScreen(addCourseViewModel, emptyList(), emptyMap())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseScreen(viewModel: AddCourseViewModel, universities: List<String>, coursesByUniversity: Map<String, List<String>>) {
    var expandedUniversity by remember { mutableStateOf(false) }
    var expandedCourse by remember { mutableStateOf(false) }
    var selectedUniversity by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("") }
    var priceState by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("TutorApp", style = MaterialTheme.typography.headlineLarge)

        Text(
            text = "Add a new course!",
            modifier = Modifier
                .fillMaxWidth() // Makes it take full width
                .padding(top = 32.dp), // Adds top margin
            textAlign = TextAlign.Center, // Centers the text
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expandedUniversity,
            onExpandedChange = { expandedUniversity = !expandedUniversity }
        ) {
            OutlinedTextField(
                value = selectedUniversity,
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
                universities.forEach { university ->
                    DropdownMenuItem(
                        text = { Text(university) },
                        onClick = {
                            selectedUniversity = university
                            selectedCourse = ""
                            expandedUniversity = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expandedCourse,
            onExpandedChange = { expandedCourse = !expandedCourse },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCourse,
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
                coursesByUniversity[selectedUniversity]?.forEach { course ->
                    DropdownMenuItem(
                        text = { Text(course) },
                        onClick = {
                            selectedCourse = course
                            expandedCourse = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = priceState,
            onValueChange = {
                priceState = it.replace(Regex("[^0-9]"), "") // Remove non-numeric characters
            },
            label = { Text("Set the price (COP/hour)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number // Restrict keyboard to numeric
            )
        )


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hint: Tutors that use our price estimator increased their students in 20%. ",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { /* TODO: Implement estimator logic */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))
        ) {
            Text("Use the estimator")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp), // Optional top padding
            horizontalAlignment = Alignment.CenterHorizontally // Centers items horizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp)) // Space between buttons

            Button(
                onClick = { /* TODO: Save course */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))
            ) {
                Text("Save")
            }
        }
    }
}