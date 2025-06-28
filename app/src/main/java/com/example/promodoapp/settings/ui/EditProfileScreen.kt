package com.example.promodoapp.settings.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.promodoapp.R
import com.example.promodoapp.repository.UserRepository
import com.example.promodoapp.timer.viewmodel.ShopViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    shopViewModel: ShopViewModel = viewModel()
) {
    val user = shopViewModel.user.value
    val coroutineScope = rememberCoroutineScope()
    var username by remember { mutableStateOf(user?.username ?: "") }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri.value = uri }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(
            title = { Text("Chỉnh sửa thông tin") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp).clip(CircleShape).align(Alignment.CenterHorizontally).clickable {
                launcher.launch("image/*")
            }
        ) {
            val avatarPainter = when {
                imageUri.value != null -> rememberAsyncImagePainter(imageUri.value)
                !user?.avatarUri.isNullOrEmpty() -> rememberAsyncImagePainter(user?.avatarUri)
                else -> null
            }
            if (avatarPainter != null) {
                Image(
                    painter = avatarPainter,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(painter = painterResource(id = R.drawable.ic_user), contentDescription = "Default Avatar", modifier = Modifier.size(80.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null
                successMessage = null
            },
            label = { Text("Tên người dùng") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = username.isBlank()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClick@{
                if (username.trim().isEmpty()) {
                    errorMessage = "Tên người dùng không được để trống"
                    return@onClick
                }

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && user != null) {
                    coroutineScope.launch {
                        try {
                            val finalAvatarUri = if (imageUri.value != null) {
                                val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                                val avatarRef = storageRef.child("avatars/${currentUser.uid}.jpg")
                                avatarRef.putFile(imageUri.value!!).await()
                                avatarRef.downloadUrl.await().toString()
                            } else user.avatarUri

                            val updatedUser = user.copy(username = username, avatarUri = finalAvatarUri)
                            UserRepository().updateUser(updatedUser)
                            shopViewModel.loadUserData()
                            successMessage = "Cập nhật thành công!"
                        } catch (e: Exception) {
                            errorMessage = "Lỗi: ${e.message}"
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
            Text("Lưu thay đổi")
        }

        successMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = Color.Blue)
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = Color.Red)
        }
    }
}