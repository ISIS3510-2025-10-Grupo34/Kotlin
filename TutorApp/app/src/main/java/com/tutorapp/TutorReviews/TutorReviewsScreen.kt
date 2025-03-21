package com.tutorapp.TutorReviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import com.tutorapp.TutorReviews.data.network.response.ReviewResponse
import com.tutorapp.ui.theme.Primary
import com.tutorapp.ui.theme.White

@Composable
fun TutorReviewsScreen(modifier: Modifier, navController: NavHostController, id:Int, name:String, course:String, phone:String, reviews_score: Number){

    Column (modifier = modifier.fillMaxSize(1f)){
        Header(modifier = Modifier.height(IntrinsicSize.Min))
        UserProfileCard(name, course, phone, reviews_score)
        Column (modifier = Modifier.fillMaxHeight()){
            ReviewsList(modifier.weight(1f) , TutorReviewsViewModel())
            WriteReviewButton(modifier = modifier.weight(1f))
        }

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
                    .background(Primary)

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
fun UserProfileCard(name:String, course:String, phone:String, reviews_score: Number) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Circle Image with Initial
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = Primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = name[0].uppercase(),
                    color = White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // User Name
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall
        )

        // Rating Stars
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(reviews_score.toInt()) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Primary)
            }
            Icon(Icons.Outlined.Star, contentDescription = null, tint = White)
        }

        // Phone Number
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Phone, contentDescription = "Phone")
            Spacer(modifier = Modifier.width(4.dp))
            Text(phone)
        }

        // Course
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.DateRange, contentDescription = "Course")
            Spacer(modifier = Modifier.width(4.dp))
            Text(course)
        }
    }
}

@Composable
fun ReviewsList(modifier: Modifier, tutorReviewsViewModel: TutorReviewsViewModel){
    tutorReviewsViewModel.onStart()
    val exampleReview = ReviewResponse(
        name = "Example name",
        score = 3,
        message = "Example message"
    )
    val exampleList : List<ReviewResponse> = listOf(exampleReview)

    val reviews : List<ReviewResponse> by tutorReviewsViewModel.reviews.observeAsState(initial = exampleList)

    Column(modifier = Modifier.padding(30.dp).height(300.dp).verticalScroll(rememberScrollState())) {
        reviews.forEach{
            review -> ReviewItem(modifier = Modifier, review = review)
        }
    }

}


@Composable
fun ReviewItem(modifier: Modifier, review:ReviewResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = review.name[0].uppercase(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row {
                repeat(review.score.toInt()) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = Primary
                    )
                }

               /* Icon(
                    imageVector = Icons.Outlined.Star, // Última estrella vacía
                    contentDescription = "Star",
                    tint = Primary

                )*/
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Texto del review
            Text(
                text = review.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WriteReviewButton(modifier: Modifier){
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Button(onClick = {}, modifier
            .padding(horizontal = 35.dp)
            , colors = ButtonColors(containerColor = Color(0xFF192650), contentColor = Color.White, disabledContentColor = Color.White, disabledContainerColor = Color(0xFF192650) )
        )

        {
            Text("Write a review")
        }

    }

}