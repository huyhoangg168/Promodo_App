package com.example.promodoapp.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.promodoapp.R
import com.example.promodoapp.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("Đổi mật khẩu") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Old Password
        OutlinedTextField(
            value = oldPassword,
            onValueChange = {
                oldPassword = it
                errorMessage = null
                successMessage = null
            },
            label = { Text("Mật khẩu cũ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                    Icon(
                        painter = painterResource(id = if (oldPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_close),
                        contentDescription = "Toggle Password",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // New Password
        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                errorMessage = null
                successMessage = null
            },
            label = { Text("Mật khẩu mới") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                    Icon(
                        painter = painterResource(id = if (newPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_close),
                        contentDescription = "Toggle Password",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
                successMessage = null
            },
            label = { Text("Xác nhận mật khẩu mới") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(id = if (confirmPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_close),
                        contentDescription = "Toggle Password",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                        errorMessage = "Vui lòng nhập đầy đủ các trường"
                    }
                    newPassword != confirmPassword -> {
                        errorMessage = "Mật khẩu mới và xác nhận không khớp"
                    }
                    newPassword.length < 6 -> {
                        errorMessage = "Mật khẩu mới phải từ 6 ký tự trở lên"
                    }
                    else -> {
                        settingsViewModel.reauthenticateAndChangePassword(oldPassword, newPassword) { result ->
                            if (result.isSuccess) {
                                successMessage = "Đổi mật khẩu thành công"
                                oldPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            } else {
                                errorMessage = "Mật khẩu cũ không đúng"
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BBF9),
                contentColor = Color.Black
            )
        ) {
            Text("Đổi mật khẩu")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = Color.Red)
        }

        successMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = Color.Blue)
        }
    }
}
