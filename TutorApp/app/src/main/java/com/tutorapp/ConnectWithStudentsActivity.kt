package com.tutorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.tutorapp.connectWithStudents.ConnectWithStudentsScreen
import com.tutorapp.ui.theme.TutorAppTheme

class ConnectWithStudentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TutorAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                 ConnectWithStudentsScreen(modifier = Modifier.padding(innerPadding))
                //ShowTutorsActivity(Modifier.padding(innerPadding), ShowTutorsViewModel())
                }
            }
        }
    }
}

