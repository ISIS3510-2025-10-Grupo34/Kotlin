package com.tutorapp.views

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tutorapp.viewModels.SimilarTutorsViewModel

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.tutorapp.models.BestReview
import com.tutorapp.models.SimilarTutorReview
import com.tutorapp.remote.NetworkUtils
import com.tutorapp.views.HomeActivity
import com.tutorapp.views.Session
import kotlinx.coroutines.launch

class SimilarTutorsActivity : ComponentActivity() {
    private val viewModel: SimilarTutorsViewModel by viewModels()
    val tutorid = Session.userid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


            if (tutorid != null) {
                viewModel.fetchSimilarTutors(tutorid)
            }



        setContent {

                SimilarTutorsScreen(viewModel) { reviews ->
                    val intent = Intent(this, SimilarTutorReviewActivity::class.java).apply {
                        putExtra("reviews", ArrayList(reviews))
                    }
                    startActivity(intent)
                }

        }
    }
}
@Composable
fun SimilarTutorsScreen(viewModel: SimilarTutorsViewModel, onSeeReviews: (List<BestReview>) -> Unit) {
   val context = LocalContext.current
    if(!NetworkUtils.isConnected(context)){
    AlertDialog(
        onDismissRequest = { /* Forzar usar el botón */ },
        title = { Text(text = "We are sorry") },
        text = {
            Text(
                "You've lost connection, try again later"
            )
        },
        confirmButton = {

                Button(
                    onClick = { (context as? Activity)?.finish() },
                    colors = ButtonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black,
                        disabledContentColor = Color.White,
                        disabledContainerColor = Color(0xFF192650)
                    )
                ) {
                    Text("Ok")
                }


            }
    )}
    else{

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (viewModel.errorMessage != null) {
        Text("Error: ${viewModel.errorMessage}")
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TutorApp",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                var counter = 0
                items(viewModel.similarTutors) { tutor ->
                    counter++
                    TutorCard(tutor, counter,onSeeReviews)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }}
}
@Composable
fun TutorCard(tutor: SimilarTutorReview, counter: Int, onSeeReviews: (List<BestReview>) -> Unit) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.LightGray, shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text("#$counter", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Anonymous Tutor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(tutor.similarity_basis.joinToString(", "), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onSeeReviews(tutor.best_reviews) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonColors(
                    containerColor = Color(0xFF192650),
                    contentColor = Color.White,
                    disabledContentColor = Color.White,
                    disabledContainerColor = Color(0xFF192650)
                )
            ) {
                Text("See their best reviews")
            }
        }
    }
}

