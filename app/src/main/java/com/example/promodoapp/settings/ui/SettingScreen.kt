package com.example.promodoapp.settings.ui

import android.annotation.SuppressLint
import android.net.Uri
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

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
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
    val user = shopViewModel.user.value
    val coroutineScope = rememberCoroutineScope()

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
                .padding(16.dp),
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
                    if (!user?.avatarUri.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(user?.avatarUri)),
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
                title = "Chế độ tối",
                switchState = viewModel.darkModeEnabled.value,
                onSwitchChange = { viewModel.toggleDarkMode(it) }
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
                    containerColor = Color(0xFF00BBF9),   // Màu nền
                    contentColor = Color.White            // Màu chữ
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
}

@Composable
fun SettingsItem(
    title: String,
    onClick: () -> Unit = {},
    switchState: Boolean? = null,
    onSwitchChange: ((Boolean) -> Unit)? = null
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
        if (switchState != null && onSwitchChange != null) {
            Switch(
                checked = switchState,
                onCheckedChange = onSwitchChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,       // Màu của nút tròn khi ON
                    checkedTrackColor = Color.Green   // Màu nền khi ON
                )
            )
        } else {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next",
                tint = Color.Gray
            )
        }
    }
}