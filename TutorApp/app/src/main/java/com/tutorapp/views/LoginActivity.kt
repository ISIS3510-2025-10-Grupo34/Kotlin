package com.tutorapp.views
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
import androidx.compose.ui.platform.LocalContext
import com.tutorapp.ui.theme.Typography

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título "TutorApp" alineado arriba a la izquierda
        Text(
            text = "TutorApp",
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(150.dp))

        // Texto de bienvenida
        Text(
            text = "Login here",
            style= Typography.titleLarge
        )

        Text(
            text = "Welcome back! You have been missed.",
            style = Typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Campo de Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(0.9f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(0.9f),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Botón de Login
        Button(
            onClick = {
                viewModel.login(email, password) { success, message ->
                    if (success) {
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Login Failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2247)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Login")
        }
    }
}

