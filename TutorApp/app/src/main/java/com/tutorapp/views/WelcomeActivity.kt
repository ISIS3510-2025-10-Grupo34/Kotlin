package com.tutorapp.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import com.tutorapp.ui.theme.Typography
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.Gson
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.SessionDataEntity
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.ui.theme.TutorAppTheme
import com.tutorapp.views.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            WelcomeScreen()
        }
    }

    @Composable
    fun WelcomeScreen() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            val db = AppDatabase.getDatabase(context)
            val session = withContext(Dispatchers.IO) {
                db.sessionDataDao().loadData()
            }

            if (session != null && session.token.isNotBlank()) {
                println("ppp")
                val tokenFormatted = Gson().fromJson(session.token, LoginTokenDecoded::class.java)
                Session.role = tokenFormatted.role
                Session.userid = tokenFormatted.id
                if (tokenFormatted.role == "tutor") {
                    val intent =
                        Intent(context, TutorProfileActivity::class.java).apply {
                            putExtra("TOKEN_KEY", tokenFormatted)
                        }
                    context.startActivity(intent)
                } else {
                    val tokenAsString = Gson().toJson(tokenFormatted)
                    val intent = Intent(context, ShowTutorsActivity::class.java).apply {
                        putExtra("TOKEN_KEY", tokenAsString)
                    }
                    context.startActivity(intent)
                }
                if (context is ComponentActivity) {
                    context.finish()
                }
            }
        }
        BackHandler(enabled = true) {

        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Text(
                text = "TutorApp",
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(150.dp))

            Text(
                text = "Welcome!",
                style = Typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Are you new? or do you have an account already",
                style = Typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
                    startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Login", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val timeToBookStartTime = System.currentTimeMillis()
                    val prefs = getSharedPreferences("timeToSignUpPrefs", MODE_PRIVATE)
                    prefs.edit().putLong("timeToSignUpStart", timeToBookStartTime).apply()
                    val intent = Intent(this@WelcomeActivity, RegisterActivity::class.java)
                    startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Register", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    @Preview(showBackground = true)
    @Composable
    fun WelcomePreview() {
        WelcomeScreen()
    }
}


