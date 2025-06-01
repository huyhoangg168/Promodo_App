package com.example.promodoapp.login_register.ui

import android.util.Log
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
import com.example.promodoapp.login_register.viewmodel.LoginState
import com.example.promodoapp.login_register.viewmodel.LoginViewModel


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel() // Khởi tạo thủ công
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo (Thay thế bằng Text tạm thời vì không có hình ảnh)
        //GifLogo()
        Image(
            painter = painterResource(id = R.drawable.img_logo),
            contentDescription = "Logo",

            modifier = Modifier
                .size(220.dp)
                .padding(bottom = 24.dp)
                .clip(CircleShape)
        )
        // Email field
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
                    contentDescription = "Email icon",
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

        // Sign In button
        Button(
            onClick = {
                Log.d("LoginScreen", "Sign In button clicked")
                if (email.isBlank() || password.isBlank()) {
                    viewModel.loginState.value = LoginState.Error("Email and password cannot be empty")
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.loginState.value = LoginState.Error("Invalid email format")
                } else {
                    viewModel.login(email, password)
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
            Text("SIGN IN", fontSize = 16.sp)
        }

        // Hiển thị trạng thái
        val context = LocalContext.current
        LaunchedEffect(loginState) {
            Log.d("LoginScreen", "Inside LaunchedEffect with state: $loginState")
            when (loginState) {
                is LoginState.Success -> {
                    Log.d("LoginScreen", "Inside LaunchedEffect with state: $loginState")
                    Toast.makeText(context, "Sign in successfully!", Toast.LENGTH_LONG).show()
                    navController.navigate(Screen.Main.route)
                }
                is LoginState.Error -> {}
                else -> {} // Idle, Loading không làm gì
            }
        }

        when (loginState) {
            is LoginState.Loading -> {
                Log.d("LoginScreen", "Showing loading state")
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
            is LoginState.Success -> {
                Log.d("LoginScreen", "Showing success state: ${(loginState as LoginState.Success).user.email}")
                Text(
                    text = "Login successful! Welcome ${(loginState as LoginState.Success).user.email}",
                    color = Color.Green,
                    modifier = Modifier.padding(top = 16.dp)
                )
                // TODO: Chuyển hướng đến màn hình chính sau khi đăng nhập thành công

            }
            is LoginState.Error -> {
                Log.d("LoginScreen", "Showing error state: ${(loginState as LoginState.Error).message}")
                Text(
                    text = (loginState as LoginState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            else -> {
                Log.d("LoginScreen", "Showing idle state")
            }
        }

        // Sign Up link
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ", color = Color.Gray)
            Text(
                text = "SIGN UP NOW",
                color = Color.Blue,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Register.route)
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


