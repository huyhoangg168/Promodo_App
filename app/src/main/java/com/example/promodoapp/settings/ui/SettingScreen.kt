package com.example.promodoapp.settings.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.promodoapp.R
import com.example.promodoapp.navigation.Screen
import com.example.promodoapp.settings.viewmodel.SettingsViewModel
import com.example.promodoapp.timer.ui.ShopDialog
import com.example.promodoapp.timer.viewmodel.MainScreenViewModel
import com.example.promodoapp.timer.viewmodel.ShopViewModel


@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(),
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {

    val shopViewModel = ShopViewModel(mainScreenViewModel)
    var showShopDialog by remember { mutableStateOf(false) }

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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "User Icon",
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Nguyễn Huy Hoàng", // Có thể thay bằng userName từ ViewModel sau
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Thời gian tập trung", fontSize = 14.sp)
                            Text(text = "${viewModel.totalFocusTime.value} phút", fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Số lần tập trung", fontSize = 14.sp)
                            Text(text = "${viewModel.focusSessions.value}", fontSize = 14.sp)
                        }
                    }
                }
            }

            SettingsItem(
                title = "Quản lý quyền",
                onClick = { /* Xử lý click */ }
            )
            SettingsItem(
                title = "Chế độ tối",
                switchState = viewModel.darkModeEnabled.value,
                onSwitchChange = { viewModel.toggleDarkMode(it) }
            )
            SettingsItem(
                title = "Cài đặt âm thanh",
                onClick = { /* Xử lý click */ }
            )
            SettingsItem(
                title = "Hoạt ảnh tập trung",
                onClick = { showShopDialog = true }
            )
            SettingsItem(
                title = "Hoạt ảnh nghỉ ngơi",
                onClick = { /* Xử lý click */ }
            )

            Button(
                onClick = { navController.navigate(Screen.Login.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            ) {
                Text("Đăng xuất")
            }
        }
    }

    // Hiển thị ShopDialog khi bấm vào "Hoạt ảnh tập trung"
    if (showShopDialog) {
        ShopDialog(
            viewModel = shopViewModel,
            onDismiss = { showShopDialog = false }
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
                onCheckedChange = onSwitchChange
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