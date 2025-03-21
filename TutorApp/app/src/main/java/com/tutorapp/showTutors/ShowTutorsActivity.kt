package com.tutorapp.showTutors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.tutorapp.showTutors.data.network.response.TutorResponse
import com.tutorapp.ui.theme.Primary
import com.tutorapp.ui.theme.White
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTutorsActivity(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel, navHostController: NavHostController) {
    showTutorsViewModel.onStart()

    var sheetState = rememberModalBottomSheetState()

    Column(modifier = modifier.fillMaxSize(1f)) {
        Header(modifier = Modifier.height(IntrinsicSize.Min))
        FilterResultsButton(modifier = Modifier, showTutorsViewModel)
        ListOfTutorCards(modifier = modifier.weight(1f), showTutorsViewModel, navHostController)
    }
}


@Composable
fun Header(modifier: Modifier){
    Row (modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween){
        Text("TutorApp", modifier = Modifier
            .weight(1f)
            .padding(horizontal = 15.dp, vertical = 15.dp),
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
        )
        Row (modifier = Modifier
            .weight(0.5f)
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 35.dp)

            , horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ){
            IconButton(onClick = {},
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF192650))

            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }
            IconButton(onClick = {},
                modifier = Modifier
                    .size(25.dp)
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
fun FilterResultsButton(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel){

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
        FilterBottomSheet(modifier=modifier, showTutorsViewModel,
            onDismissRequest = { showBottomSheet = false }
        )


    }

}

@Composable
fun ListOfTutorCards(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel, navHostController: NavHostController ){
    val exampleTutor = TutorResponse(
        id = 0,
        name = "Example Tutor Name",
        email = "example@example.com",
        course = "Example suibject",
        university = "Example University",
        title = "Example title",
        description = "Example description.",
        reviews_score = 0.0f,
        image_url = "",
        phone = "000000000000"
    )

    val exampleList : List<TutorResponse> = listOf(exampleTutor)
    val tutors: List<TutorResponse> by showTutorsViewModel.tutors.observeAsState(initial = exampleList)
    val scrollState = rememberScrollState()

    Column(modifier = modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)){
        tutors.forEach {
                tutor -> TutorCard(modifier = Modifier, tutor = tutor, navHostController)
        }
    }
}


@Composable
fun TutorCard(modifier: Modifier, tutor: TutorResponse, navHostController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White)
            .padding(16.dp)
    ) {
        // Top Section (Row)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)), // Light Gray
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tutor.name[0].uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tutor.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable{navHostController.navigate("TutorProfile/${tutor.id}/${tutor.name}/${tutor.course}/${tutor.phone}/${tutor.reviews_score}")}
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Placeholder (Box)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
            , // Lighter Gray
            contentAlignment = Alignment.Center
        ) {

            AsyncImage(
                model = tutor.image_url,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Information Section (Column)
        Column {
            Text(
                text = tutor.course,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = tutor.title,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tutor.description,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button
        Button(
            onClick = { /* Handle booking */ },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonColors(containerColor = Primary, contentColor = White, disabledContentColor = White, disabledContainerColor = Primary )
        ) {
            Text(text = "Book")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel,
                      onDismissRequest: () -> Unit){

    val coroutineScope = rememberCoroutineScope()
    var universityName by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }
    var professorName by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = universityName,
                onValueChange = { universityName = it },
                label = { Text("University") },
                trailingIcon = {
                    if (universityName.isNotEmpty()) {
                        IconButton(onClick = { universityName = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Course") },
                trailingIcon = {
                    if (courseName.isNotEmpty()) {
                        IconButton(onClick = { courseName = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = professorName,
                onValueChange = {
                    professorName = it
                    showSuggestions = it.isNotEmpty()
                },
                label = { Text("Professor") },
                trailingIcon = {
                    if (professorName.isNotEmpty()) {
                        IconButton(onClick = {
                            professorName = ""
                            showSuggestions = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            /*if (showSuggestions) {
                SuggestionDropdown(
                    suggestions = listOf("Juan Hernandez", "Julián Rodriguez"),
                    onSuggestionClick = { selectedSuggestion ->
                        professorName = selectedSuggestion
                        showSuggestions = false
                    }
                )
            }*/

            Button(
                onClick =
                { coroutineScope.launch {
                    try{
                        showTutorsViewModel.onFilterClick(universityName, courseName, professorName)
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
    }
}



/*
@Composable
fun SuggestionDropdown(suggestions: List<String>, onSuggestionClick: (String) -> Unit) {
Card(
modifier = Modifier
.fillMaxWidth()
.padding(top = 4.dp)
.clip(RoundedCornerShape(8.dp)),
elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
) {
Column(modifier = Modifier.padding(8.dp)) {
suggestions.forEach { suggestion ->
Text(
text = suggestion,
modifier = Modifier
.fillMaxWidth()
.clickable { onSuggestionClick(suggestion) }
.padding(vertical = 4.dp)
)
}
}
}
}*/