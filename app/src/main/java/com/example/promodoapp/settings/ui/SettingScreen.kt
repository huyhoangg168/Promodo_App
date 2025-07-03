package com.example.promodoapp.settings.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.promodoapp.R
import com.example.promodoapp.navigation.Screen
import com.example.promodoapp.repository.UserRepository
import com.example.promodoapp.settings.viewmodel.SettingsViewModel
import com.example.promodoapp.timer.ui.ConfirmDialog
import com.example.promodoapp.timer.viewmodel.MainScreenViewModel
import com.example.promodoapp.timer.viewmodel.ShopViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.promodoapp.utils.SharedPrefHelper


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(),
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val shopViewModel = mainScreenViewModel.shopViewModel
    var showWorkAnimationDialog by remember { mutableStateOf(false) }
    var showBreakAnimationDialog by remember { mutableStateOf(false) }
    var showDeleteDBDialog by remember { mutableStateOf(false) }
    val showPinDialog = remember { mutableStateOf(false) }
    val user = shopViewModel.user.value
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Tải lại dữ liệu người dùng khi mở màn hình
    LaunchedEffect(Unit) {
        shopViewModel.loadUserData()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_timer), contentDescription = "Timer") },
                    label = { Text("Timer") },
                    selected = false,
                    onClick = { navController.popBackStack("main", inclusive = false) }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_calendar), contentDescription = "Statistics") },
                    label = { Text("Statistics") },
                    selected = false,
                    onClick = { navController.navigate("statistics") }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = true,
                    onClick = { /* Đã ở màn Settings */ }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Trang cá nhân",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Avt
                    val savedAvatarUri = SharedPrefHelper.getAvatarUri(context)
                    if (!savedAvatarUri.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(savedAvatarUri)),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "User Icon",
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val user = shopViewModel.user.value
                    Text(
                        text = user?.username ?: "Chưa đặt tên",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "Thời gian tập trung", fontSize = 14.sp)
                            Text(text = "( phút )", fontSize = 14.sp)
                            Text(text = "${viewModel.totalFocusTime.value} phút", fontSize = 14.sp)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "Số lần tập trung", fontSize = 14.sp)
                            Text(text = "( lần )", fontSize = 14.sp)
                            Text(text = "${viewModel.focusSessions.value}", fontSize = 14.sp)
                        }
                    }
                }
            }

            SettingsItem(
                title = "Quản lý thông tin ",
                onClick = { navController.navigate(Screen.EditProfile.route) }
            )
            SettingsItem(
                title = "Chế độ nghiêm khắc ",
                onClick = {
                    showPinDialog.value = true
                }
            )
            SettingsItem(
                title = "Đổi mật khẩu",
                onClick = { navController.navigate(Screen.ChangePwd.route) }
            )
            SettingsItem(
                title = "Hoạt ảnh tập trung",
                onClick = { showWorkAnimationDialog = true }
            )
            SettingsItem(
                title = "Hoạt ảnh nghỉ ngơi",
                onClick = { showBreakAnimationDialog = true }
            )
            SettingsItem(
                title = "Xóa dữ liệu người dùng",
                onClick = { showDeleteDBDialog = true }
            )

            Button(
                onClick = { navController.navigate(Screen.Login.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BBF9),
                    contentColor = Color.Black
                )
            ){
                Text("Đăng xuất")
            }
        }
    }

    // Dialog cho hoạt ảnh tập trung
    if (showWorkAnimationDialog) {
        AnimationShopDialog(
            viewModel = shopViewModel,
            animationType = "work",
            onDismiss = { showWorkAnimationDialog = false }
        )
    }

    // Dialog cho hoạt ảnh nghỉ ngơi
    if (showBreakAnimationDialog) {
        AnimationShopDialog(
            viewModel = shopViewModel,
            animationType = "break",
            onDismiss = { showBreakAnimationDialog = false }
        )
    }

    //Dialog xóa db user
    if (showDeleteDBDialog) {
        ConfirmDialog(
            message = "Bạn có chắc chắn muốn xóa toàn bộ dữ liệu người dùng? Hành động này không thể hoàn tác.",
            confirmButtonText = "Xóa",
            dismissButtonText = "Hủy",
            onConfirm = {
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.let {
                    val uid = it.uid
                    // Gọi hàm xóa dữ liệu
                    coroutineScope.launch {
                        try {
                            UserRepository().deleteUserData(uid)
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Settings.route) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            Log.e("DeleteUser", "Failed: ${e.message}")
                        }
                    }
                }
            },
            onDismiss = { showDeleteDBDialog = false }
        )
    }

    if (showPinDialog.value) {
        AlertDialog(
            onDismissRequest = { showPinDialog.value = false },
            text = {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.img_tutorial_screen_pinning),
                        contentDescription = "Hướng dẫn ghim ứng dụng",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .padding(bottom = 12.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        "Hướng dẫn thực hiện bật ghim ứng dụng để thực hiện chế độ nghiêm khắc:\n" +
                                "1. Cài đặt\n" +
                                "2. Bảo mật\n" +
                                "3. Cài đặt bảo mật khác\n" +
                                "4. Ghim ứng dụng/ Ghim cửa sổ/ Screen pinning/App pinning"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPinDialog.value = false
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    context.startActivity(intent)
                }) {
                    Text("Đi đến Cài đặt")
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp)
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Next",
            tint = Color.Gray
        )
    }
}
