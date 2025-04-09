package com.tutorapp.views

import android.content.Intent
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.tutorapp.models.TutorResponse
import com.tutorapp.ui.theme.TutorAppTheme
import com.tutorapp.viewModels.ShowTutorsViewModel
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.filled.Clear
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
import com.tutorapp.models.TutorsResponse
import com.tutorapp.viewModels.TutoringSessionViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject

class ShowTutorsActivity: ComponentActivity(){
    private val tutoringSessionViewModel: TutoringSessionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        tutoringSessionViewModel.getAllSessions {  }

        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra("TOKEN_KEY") ?: ""

        tutoringSessionViewModel.getSearchResults(){sucess, data ->
            if(sucess){
                val universities: List<UniversitySimple> = data?.data?.map { (uniName, uni) ->
                    UniversitySimple(name = uniName, id = uni.id)
                } ?: emptyList()

                val coursesByUniversity: Map<String, List<CourseSimple>>? = data?.data?.mapValues { (_, uni) ->
                    uni.courses.map { (courseName, course) ->
                        CourseSimple(courseName = courseName, id = course.id)
                    }
                }
                val tutorsByCourse: Map<String, List<String>>? = data?.data?.flatMap { (_, university) ->
                    university.courses.map { (courseName, course) ->
                    courseName to course.tutors_names
                }
                }?.toMap()



                Log.i("universities", universities.toString() )
                Log.i("courses", coursesByUniversity.toString())
                Log.i("tutors", tutorsByCourse.toString())
                setContent{
                    TutorAppTheme {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            ShowTutorsScreen(Modifier.padding(innerPadding), tutoringSessionViewModel,token, universities, coursesByUniversity, tutorsByCourse)

                        }
                    }
                }
            }else {
                setContent{
                    TutorAppTheme {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            ShowTutorsScreen(Modifier.padding(innerPadding), tutoringSessionViewModel,token, emptyList(), emptyMap(), emptyMap())

                        }
                    }
                }
            }
        }
    }


}
@Composable
fun ShowTutorsScreen(modifier: Modifier, tutoringSessionViewModel: TutoringSessionViewModel,token: String, universities: List<UniversitySimple>,
                     coursesByUniversity: Map<String, List<CourseSimple>>?, tutorsByCourse : Map<String, List<String>>?){
    Column (modifier = modifier.fillMaxSize(1f)){
        TutorScreenHeader(modifier = Modifier.height(IntrinsicSize.Min),token)
        Spacer(modifier = Modifier.height(20.dp))
        FilterResultsButton(modifier = Modifier, tutoringSessionViewModel, universities, coursesByUniversity, tutorsByCourse)
        ListOfTutorCards(modifier = modifier, tutoringSessionViewModel, token)
    }

}


@Composable
fun TutorScreenHeader(modifier: Modifier,token: String) {
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(25.dp) // Espacio uniforme entre iconos
        ) {
            IconButton(
                onClick = {
                    }
                ,
                modifier = Modifier
                    .size(35.dp) // Tamaño más visible
                    .clip(CircleShape)
                    .background(Color(0xFF192650))
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {val jsonToken = JSONObject(token)
                    val role = jsonToken.get("role").toString()
                    val id = jsonToken.get("id").toString()

                    val intent = Intent(
                        context,
                        if (role == "tutor") TutorProfileActivity::class.java else StudentProfileActivity::class.java
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
fun FilterResultsButton(modifier: Modifier, tutoringSessionViewModel: TutoringSessionViewModel, universities: List<UniversitySimple>,
                        coursesByUniversity: Map<String, List<CourseSimple>>?, tutorsByCourse : Map<String, List<String>>?){

    var showBottomSheet by remember { mutableStateOf(false) }
    Row (modifier){
        Button(onClick = { showBottomSheet = true }, modifier.weight(1f)
            .padding(horizontal = 35.dp)
            , colors = ButtonColors(containerColor = Color(0xFF192650), contentColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color(0xFF192650) )
        )

        {
            Text("Filter results")
        }
        Column(modifier.weight(1f)) {  }
    }

    if (showBottomSheet){
        FilterBottomSheet(modifier=modifier, tutoringSessionViewModel,
            onDismissRequest = { showBottomSheet = false }, universities, coursesByUniversity, tutorsByCourse
        )


    }

}

@Composable
fun ListOfTutorCards(modifier: Modifier, tutoringSessionViewModel: TutoringSessionViewModel, token: String){

    val sessions = tutoringSessionViewModel.sessions
    val scrollState = rememberScrollState()

    Column(modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)){
        sessions.forEach {
            tutoringSession -> TutorCard(modifier = Modifier, tutoringSession = tutoringSession, token = token)
        }
    }
}


@Composable
fun TutorCard(modifier: Modifier, tutoringSession: TutoringSession, token: String) {
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
            onClick = { /* Handle booking */ },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonColors(containerColor = Color(0xFF192650), contentColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color(0xFF192650) )
        ) {
            Text(text = "Book")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(modifier: Modifier, tutoringSessionViewModel: TutoringSessionViewModel,
                      onDismissRequest: () -> Unit, universities: List<UniversitySimple>,
                      coursesByUniversity: Map<String, List<CourseSimple>>?, tutorsByCourse : Map<String, List<String>>?){

    val coroutineScope = rememberCoroutineScope()
    var expandedUniversity by remember { mutableStateOf(false) }
    var selectedUniversity by remember { mutableStateOf(mapOf(
        "name" to "",
        "id" to -1,
    ))}
    var expandedCourse by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf(mapOf(
        "name" to "",
        "id" to -1,
    )) }

    var expandedTutor by remember { mutableStateOf(false) }
    var selectedTutor by remember { mutableStateOf(mapOf(
        "name" to ""
    ))}

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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick =
                { coroutineScope.launch {
                    try{
                        tutoringSessionViewModel.onFilterClick(selectedUniversity["name"].toString(), selectedCourse["name"].toString(), selectedTutor["name"].toString())
                    }catch (e:Exception){
                        println(e)
                    }
                }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Filter")
            }
        }
        Spacer(modifier = Modifier.height(30.dp))

    }

}