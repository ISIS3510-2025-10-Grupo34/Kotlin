@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.tutorapp.views

import android.content.Intent
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.viewModels.AddCourseViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class UniversitySimple(
    val name: String,
    val id: Int
)
data class CourseSimple(
    val courseName: String,
    val id: Int
)


class AddCourseActivity : ComponentActivity() {
    private val addCourseViewModel: AddCourseViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserInfo: LoginTokenDecoded? = intent.getParcelableExtra("TOKEN_KEY")
        addCourseViewModel.getSearchResults() { success, data ->
            if (success) {
                val universities: List<UniversitySimple> = data?.data?.map { (uniName, uni) ->
                    UniversitySimple(name = uniName, id = uni.id)
                } ?: emptyList()

                val coursesByUniversity: Map<String, List<CourseSimple>>? = data?.data?.mapValues { (_, uni) ->
                    uni.courses.map { (courseName, course) ->
                        CourseSimple(courseName = courseName, id = course.id)
                    }
                }

                setContent {
                    AddCourseScreen(addCourseViewModel, universities, coursesByUniversity, currentUserInfo)
                }
            } else {
                setContent {
                    AddCourseScreen(addCourseViewModel, emptyList(), emptyMap(), currentUserInfo)
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
    coursesByUniversity: Map<String, List<CourseSimple>>?,
    currentUserInfo: LoginTokenDecoded?
) {
    var expandedUniversity by remember { mutableStateOf(false) }
    var expandedCourse by remember { mutableStateOf(false) }
    var selectedUniversity by remember { mutableStateOf(mapOf(
        "name" to "",
        "id" to -1,
    ))}
    var selectedCourse by remember { mutableStateOf(mapOf(
        "name" to "",
        "id" to -1,
    )) }
    var priceState by remember { mutableStateOf("") }

    // Date and time state variables
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var formattedDateTime by remember { mutableStateOf("") }

    // Date and time picker dialog states
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Update formatted date time when either date or time changes
    LaunchedEffect(selectedDate, selectedTime) {
        formattedDateTime = "${selectedDate.format(dateFormatter)}-${selectedTime.format(timeFormatter)}"
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("TutorApp", style = MaterialTheme.typography.headlineLarge)

        Text(
            text = "Create a tutoring session",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

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
                        }
                    )
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
                coursesByUniversity!![selectedUniversity["name"]]?.forEach { course ->
                    DropdownMenuItem(
                        text = { Text(course.courseName) },
                        onClick = {
                            selectedCourse = mapOf(
                                "name" to course.courseName,
                                "id" to course.id,
                            )
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
                priceState = it.replace(Regex("[^0-9]"), "")
            },
            label = { Text("Set the price (COP/hour)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            )
        )
        Text(
            text = "Hint: Tutors that use our price estimator increased their students in 20%",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                if (currentUserInfo != null) {
                    viewModel.getPriceEstimation(currentUserInfo.id,
                        selectedUniversity["name"].toString()
                    ) { success, estimatedPrice ->
                        if (success) {
                            priceState = estimatedPrice.toString()
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))
        ) {
            Text("Use the estimator")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date and Time selection using Material3 pickers
        OutlinedTextField(
            value = formattedDateTime,
            onValueChange = { /* Read-only field, we'll use dialogs to select */ },
            label = { Text("Date and time of availability") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.DateRange,
                        contentDescription = "Select date and time"
                    )
                }
            }
        )
        Text(
            text = "Format: DD/MM/YYYY-HH:MM",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convert millis to LocalDate
                            selectedDate = java.time.Instant
                                .ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                        showTimePicker = true // Show time picker after date is selected
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
                DatePicker(state = datePickerState)
            }
        }

        // Time Picker Dialog
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = selectedTime.hour,
                initialMinute = selectedTime.minute,
                is24Hour = true
            )

            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedTime = LocalTime.of(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        showTimePicker = false
                    }) {
                        Text("Confirm")
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    if (formattedDateTime.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Please select date and time of availability",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (currentUserInfo != null) {
                            viewModel.postTutoringSession(
                                currentUserInfo.id.toString(),
                                selectedCourse["id"].toString(),
                                priceState,
                                formattedDateTime
                            ) { success, _ ->
                                if (success) {
                                    Toast.makeText(context, "The course has been created successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        val intent = Intent(context, TutorProfileActivity::class.java).apply {
                            putExtra("TOKEN_KEY", currentUserInfo)
                        }
                        context.startActivity(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247))
            ) {
                Text("Save", color = Color.White)
            }
        }
    }
}

// Custom TimePickerDialog component
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
        text = { content() }
    )
}