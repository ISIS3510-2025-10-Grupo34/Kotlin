package com.tutorapp.views

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.platform.LocalContext
import com.tutorapp.models.TutorsResponse
import org.json.JSONObject

class ShowTutorsActivity: ComponentActivity(){
    private val showTutorsViewModel: ShowTutorsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra("TOKEN_KEY") ?: ""
        setContent {
            TutorAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShowTutorsScreen(Modifier.padding(innerPadding), showTutorsViewModel,token)

                }
            }
        }
    }


}
@Composable
fun ShowTutorsScreen(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel,token: String){
    Column (modifier = modifier.fillMaxSize(1f)){
        TutorScreenHeader(modifier = Modifier.height(IntrinsicSize.Min),token)
        Spacer(modifier = Modifier.height(20.dp))
        FilterResultsButton(modifier = Modifier)
        ListOfTutorCards(modifier = modifier, showTutorsViewModel)
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
fun FilterResultsButton(modifier: Modifier){
    Row (modifier){
        Button(onClick = {}, modifier.weight(1f)
            .padding(horizontal = 35.dp)
            , colors = ButtonColors(containerColor = Color(0xFF192650), contentColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color(0xFF192650) )
        )

        {
            Text("Filter results")
        }
        Column(modifier.weight(1f)) {  }
    }

}

@Composable
fun ListOfTutorCards(modifier: Modifier, showTutorsViewModel: ShowTutorsViewModel){
    val exampleTutorResponse = TutorResponse(

        id = 0,
        name = "Example Tutor Name",
        email = "example@example.com",
        area_of_expertise = "Example course",
        major = "Example major",
        phone_number = "573333333333",
        university = "Example University"
    )
    val exampleTutorsResponse = TutorsResponse (listOf(exampleTutorResponse))
    showTutorsViewModel.onStart()
    val tutorsResponse: TutorsResponse by showTutorsViewModel.tutors.observeAsState(initial = exampleTutorsResponse)
    val scrollState = rememberScrollState()

    Column(modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)){
        tutorsResponse.tutors.forEach {
            tutor -> TutorCard(modifier = Modifier, tutor = tutor)
        }
    }
}


@Composable
fun TutorCard(modifier: Modifier, tutor: TutorResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
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
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Placeholder (Box)
        /*Box(
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

        Spacer(modifier = Modifier.height(16.dp))*/

        // Information Section (Column)
        Column {
            Text(
                text = tutor.university,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            /*Text(
                text = tutor.major,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))*/
            /*Text(
                text = tutor.description,
                fontSize = 14.sp
            )*/
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
