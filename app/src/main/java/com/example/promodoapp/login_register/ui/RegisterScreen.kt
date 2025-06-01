package com.example.promodoapp.login_register.ui

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.promodoapp.R
import com.example.promodoapp.navigation.Screen
import com.example.promodoapp.login_register.viewmodel.RegisterState
import com.example.promodoapp.login_register.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel() // sd viewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerState by viewModel.registerState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
//        GifLogo()
        Image(
            painter = painterResource(id = R.drawable.img_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(220.dp)
                .padding(bottom = 24.dp)
                .clip(CircleShape)
        )
        // email field
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Username icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (email.isEmpty()) {
                            Text("Email", color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
            }
        }

        // Password field
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Password icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    decorationBox = { innerTextField ->
                        if (password.isEmpty()) {
                            Text("Password", color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
            }
        }

        // Confirm Password field
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Confirm Password icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    decorationBox = { innerTextField ->
                        if (confirmPassword.isEmpty()) {
                            Text("Confirm Password", color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
            }
        }

        // Sign Up button
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    viewModel.registerState.value = RegisterState.Error("All fields must be filled")
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.registerState.value = RegisterState.Error("Invalid email format")
                } else if (password != confirmPassword) {
                    viewModel.registerState.value = RegisterState.Error("Passwords do not match")
                } else {
                    viewModel.register(email, password, confirmPassword)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BBF9),
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("SIGN UP", fontSize = 16.sp)
        }

        // Hiển thị trạng thái
        val context = LocalContext.current

        when (registerState) {
            is RegisterState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
            is RegisterState.Success -> {
                Text(
                    text = "Registration successful! Welcome",
                    color = Color.Green,
                    modifier = Modifier.padding(top = 16.dp)
                )
                // TODO: Chuyển hướng đến màn hình chính sau khi đăng ký thành công

                LaunchedEffect(Unit) {
                    // Trì hoãn điều hướng để thông báo hiển thị
                    kotlinx.coroutines.delay(2000) // Hiển thị thông báo trong 2 giây
                    Toast.makeText(context, "Sign up successfully", Toast.LENGTH_LONG).show()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }

            }
            is RegisterState.Error -> {
                Text(
                    text = (registerState as RegisterState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Toast.makeText(context, "Đăng ký thất bại", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }

        // Sign In link
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", color = Color.Gray)
            Text(
                text = "SIGN IN",
                color = Color.Blue,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Terms and Conditions
        Text(
            text = "Terms and Conditions",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}