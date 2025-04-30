package com.tutorapp.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tutorapp.models.BestReview

class SimilarTutorReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val reviews = intent.getSerializableExtra("reviews") as? ArrayList<BestReview> ?: arrayListOf()

        setContent {
            MaterialTheme {
                ReviewsScreen(reviews)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(reviews: List<BestReview>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Reviews") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF192650),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(reviews) { review ->
                ReviewCard(
                    initial = "A",
                    rating = review.rating ?: 0,
                    comment = review.comment ?: "No comment provided."
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ReviewCard(initial: String, rating: Int, comment: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF192650), shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row {
                    repeat(rating) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF192650))
                    }
                    repeat(5 - rating) {
                        Icon(Icons.Default.StarBorder, contentDescription = null, tint = Color(0xFF192650))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = comment, fontSize = 14.sp)
            }
        }
    }
}
