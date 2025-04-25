package com.tutorapp.views
import android.content.Intent
import com.tutorapp.viewModels.LoginViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.tutorapp.models.LoginTokenDecoded
import com.tutorapp.remote.NetworkUtils
import com.tutorapp.ui.theme.Typography
import kotlinx.coroutines.launch

object Session {
    var userid: Int? = null
    var role: String? = null
}

class LoginActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(loginViewModel)
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current


    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        val welcome = Intent(context, WelcomeActivity::class.java)
        context.startActivity(welcome)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            text = "Login here",
            style= Typography.titleLarge
        )

        Text(
            text = "Welcome back! You have been missed.",
            style = Typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(0.9f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(0.9f),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if(true) {

                    viewModel.login(email, password) { success, message ->
                        if (true) {
                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                            Session.userid = message?.id
                            Session.role = message?.role

                            if (true) {
                                val intent =
                                    Intent(context, TutorProfileActivity::class.java).apply {
                                        putExtra("TOKEN_KEY", message)
                                    }
                                context.startActivity(intent)
                            } else {
                                val tokenAsString = Gson().toJson(message)
                                val intent = Intent(context, ShowTutorsActivity::class.java).apply {
                                    putExtra("TOKEN_KEY", tokenAsString)
                                }
                                context.startActivity(intent)
                            }

                        } else {
                            val error = message?.error
                            Toast.makeText(context, "Login Failed: $error", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
                else{

                    coroutineScope.launch {

                        Toast.makeText(context, "No internet connection, try later", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Login")
        }
    }
}



