package com.tutorapp.views

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tutorapp.ui.theme.TutorAppTheme
import com.tutorapp.viewModels.GamificationViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class GamificationActivity : ComponentActivity() {
    private val gamificationViewModel: GamificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = Session.userid ?: return  // usa tu Session para obtener el userId
        setContent {
            TutorAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GamificationProgressScreen(
                        viewModel = gamificationViewModel,
                        userId = userId
                    )
                }
            }
        }
    }
}

@Composable
fun GamificationProgressScreen(viewModel: GamificationViewModel, userId: Int) {
    val profile = viewModel.profile
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(Unit) {
        Log.i("userIdd", userId.toString())
        viewModel.fetchProfile(userId)
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("My Progress", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator()
            errorMessage != null -> Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            profile != null -> {
                Text("Points: ${profile.points}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                Text("Achievements:", style = MaterialTheme.typography.titleMedium)
                profile.achievements.forEach {
                    Text("• $it")
                }
            }
        }
    }
}
