package com.tutorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tutorapp.TutorReviews.TutorReviewsScreen
import com.tutorapp.showTutors.Routes
import com.tutorapp.showTutors.ShowTutorsActivity
import com.tutorapp.showTutors.ShowTutorsViewModel
import com.tutorapp.ui.theme.TutorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navigationController = rememberNavController()
            TutorAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = navigationController, startDestination = Routes.ShowTutors.route){
                        composable(Routes.ShowTutors.route){ ShowTutorsActivity(modifier = Modifier.padding(innerPadding), ShowTutorsViewModel(), navigationController )}
                        composable(route=Routes.TutorProfile.route, arguments = listOf(
                            navArgument("id"){type = NavType.IntType},
                            navArgument("name"){type = NavType.StringType },
                            navArgument("course"){type = NavType.StringType },
                            navArgument("phone"){type = NavType.StringType },
                            navArgument("reviews_score"){type = NavType.FloatType}
                        )){ backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("id") ?: 0
                            val name = backStackEntry.arguments?.getString("name") ?: "Nombre Ejemplo"
                            val course = backStackEntry.arguments?.getString("course") ?: "Curso Ejemplo"
                            val phone = backStackEntry.arguments?.getString("phone") ?: "00000000000"
                            val reviews_score = backStackEntry.arguments?.getFloat("reviews_score") ?: 0
                            TutorReviewsScreen(modifier = Modifier.padding(innerPadding), navigationController, id, name, course, phone, reviews_score) }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TutorAppTheme {
        Greeting("Android")
    }
}