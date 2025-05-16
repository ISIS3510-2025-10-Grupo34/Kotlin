@file:OptIn(ExperimentalMaterial3Api::class)

package com.tutorapp.views

import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.viewModels.AddCourseViewModel
import java.time.*
import java.time.format.DateTimeFormatter

data class UniversitySimple(val name: String, val id: Int)
data class CourseSimple(val courseName: String, val id: Int)

class AddCourseActivity : ComponentActivity() {
    private val addCourseViewModel: AddCourseViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserInfo: LoginTokenDecoded? = intent.getParcelableExtra("TOKEN_KEY")

        addCourseViewModel.getSearchResults { success, data ->
            val universities = data?.data?.map { (uniName, uni) ->
                UniversitySimple(name = uniName, id = uni.id)
            } ?: emptyList()

            val coursesByUniversity = data?.data?.mapValues { (_, uni) ->
                uni.courses.map { (courseName, course) ->
                    CourseSimple(courseName = courseName, id = course.id)
                }
            } ?: emptyMap()

            setContent {
                AddCourseScreen(
                    viewModel = addCourseViewModel,
                    universities = universities,
                    coursesByUniversity = coursesByUniversity,
                    currentUserInfo = currentUserInfo
                )
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
    currentUserInfo: LoginTokenDecoded?
) {
    val context = LocalContext.current

    // --- Inicializar estados leyendo del cache ---
    var selectedUniversityName by remember {
        mutableStateOf(viewModel.getCachedInput("universityName") ?: "")
    }
    var selectedUniversityId by remember {
        mutableStateOf(viewModel.getCachedInput("universityId")?.toIntOrNull() ?: -1)
    }
    var selectedCourseName by remember {
        mutableStateOf(viewModel.getCachedInput("courseName") ?: "")
    }
    var selectedCourseId by remember {
        mutableStateOf(viewModel.getCachedInput("courseId")?.toIntOrNull() ?: -1)
    }
    var priceState by remember {
        mutableStateOf(viewModel.getCachedInput("price") ?: "")
    }

    // Fecha y hora
    var formattedDateTime by remember {
        mutableStateOf(viewModel.getCachedInput("dateTime") ?: "")
    }
    var dateTimeError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Menús desplegables
    var expandedUniversity by remember { mutableStateOf(false) }
    var expandedCourse by remember { mutableStateOf(false) }

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
        Spacer(Modifier.height(16.dp))

        // --- University dropdown ---
        ExposedDropdownMenuBox(
            expanded = expandedUniversity,
            onExpandedChange = { expandedUniversity = !expandedUniversity }
        ) {
            OutlinedTextField(
                value = selectedUniversityName,
                onValueChange = { /* readOnly */ },
                label = { Text("University") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUniversity) },
                modifier = Modifier
                    .menuAnchor()      // ← aquí
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedUniversity,
                onDismissRequest = { expandedUniversity = false }
            ) {
                universities.forEach { uni ->
                    DropdownMenuItem(
                        text = { Text(uni.name) },
                        onClick = {
                            selectedUniversityName = uni.name
                            selectedUniversityId = uni.id
                            viewModel.cacheInput("universityName", uni.name)
                            viewModel.cacheInput("universityId", uni.id.toString())
                            // reset curso
                            selectedCourseName = ""
                            selectedCourseId = -1
                            viewModel.cacheInput("courseName", "")
                            viewModel.cacheInput("courseId", "")
                            expandedUniversity = false
                        }
                    )
                }

            }
        }

        Spacer(Modifier.height(16.dp))
        // --- Course dropdown ---
        ExposedDropdownMenuBox(
            expanded = expandedCourse,
            onExpandedChange = { expandedCourse = !expandedCourse }
        ) {
            OutlinedTextField(
                value = selectedCourseName,
                onValueChange = {},
                label = { Text("Course") },
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
                        }
                    }
                }
            },
            enabled = selectedUniversityId != -1
        ) {
            Text("Use the estimator")
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
                        contentDescription = "Seleccionar fecha y hora"
                    )
                }
            }, // ← ¡aquí va la coma!
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
                            val ld = Instant.ofEpochMilli(ms)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            formattedDateTime = "${ld.format(dateFormatter)}-"
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
                        // 1) Armar el string
                        val parts = formattedDateTime.split("-")
                        val datePart = parts[0]
                        formattedDateTime = "$datePart-${String.format("%02d:%02d", tp.hour, tp.minute)}"

                        // 2) Validar (si quieres)
                        val selectedDT = LocalDateTime.parse(
                            formattedDateTime,
                            DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm")
                        )
                        val now = ZonedDateTime.now(ZoneId.of("GMT-5")).toLocalDateTime()
                        dateTimeError = if (selectedDT.isBefore(now)) {
                            "Cannot select past date/time."
                        } else null

                        // 3) **Guardar en el cache**
                        if (dateTimeError == null) {
                            viewModel.cacheInput("dateTime", formattedDateTime)
                        }

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
                TimePicker(state = tp)
            }
        }

        Spacer(Modifier.height(24.dp))
        // --- Guardar sesión ---
        Button(
            onClick = {
                when {
                    selectedUniversityId == -1 -> Toast.makeText(context, "Select university", Toast.LENGTH_SHORT).show()
                    selectedCourseId == -1 -> Toast.makeText(context, "Select course", Toast.LENGTH_SHORT).show()
                    priceState.isEmpty() -> Toast.makeText(context, "Set price", Toast.LENGTH_SHORT).show()
                    formattedDateTime.isEmpty() -> Toast.makeText(context, "Select date/time", Toast.LENGTH_SHORT).show()
                    dateTimeError != null -> Toast.makeText(context, dateTimeError, Toast.LENGTH_SHORT).show()
                    else -> {
                        currentUserInfo?.let { user ->
                            viewModel.postTutoringSession(
                                user.id.toString(),
                                selectedCourseId.toString(),
                                priceState,
                                formattedDateTime
                            ) { ok, _ ->
                                if (ok) {
                                    Toast.makeText(context, "Tutoring created!", Toast.LENGTH_SHORT).show()
                                    // limpiar cache
                                    viewModel.clearCache()
                                    // navegar
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

// Reusa tu TimePickerDialog
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
