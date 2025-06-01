package com.example.promodoapp.timer.ui

import android.util.Log
import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
//import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.promodoapp.R
import com.example.promodoapp.navigation.Screen
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.FlowRowScopeInstance.align
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.promodoapp.timer.viewmodel.MainScreenViewModel
import com.example.promodoapp.timer.viewmodel.VideoType

@Preview
@Composable
fun Demo(){
    MainScreen(navController = rememberNavController())
}

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenViewModel = viewModel()
) {
    // Biến để lưu trữ VideoView
    val context = LocalContext.current
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
    // Biến để lưu câu quotes
    var quote by remember { mutableStateOf("Stay focused and keep going!") }
    // Hiển thị dialog nhập quotes
    var showQuoteDialog by remember { mutableStateOf(false) }
    // Hiển thị dialog tùy chỉnh thời gian
    var showCustomDialog by remember { mutableStateOf(false) }

    // Cập nhật video khi chuyển giai đoạn
    LaunchedEffect(viewModel.currentVideo.value) {
        Log.d("MainScreen", "Current video changed to: ${viewModel.currentVideo.value}")
        videoViewInstance?.pause()
        videoViewInstance?.seekTo(0)

        val videoResId = if (viewModel.currentVideo.value == VideoType.Study) {
            R.raw.vd_working
        } else {
            R.raw.vd_chilling
        }
        videoViewInstance?.setVideoPath("android.resource://${context.packageName}/$videoResId")

        if (viewModel.timerState.value == TimerState.Running) {
            videoViewInstance?.start()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_timer), contentDescription = "Timer") },
                    label = { Text("Timer") },
                    selected = true,
                    onClick = { /* Đã ở màn Timer */ }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_calendar), contentDescription = "Statistics") },
                    label = { Text("Statistics") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Statistics.route) }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Phần trên: Số xu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.LightGray, shape = RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${viewModel.coins.value}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "coin",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            // Tiêu đề và bút chì
            Column(
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit mode",
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                showCustomDialog = true
                            }
                    )
                    Text(
                        text = "Pomodoro",
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            // Phần giữa: Đồng hồ, quotes, video và thời gian
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Quotes
                Text(
                    text = quote,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            showQuoteDialog = true
                        }
                )

                // Đồng hồ
                Text(
                    text = "${viewModel.currentTime.value / 60}:${String.format("%02d", viewModel.currentTime.value % 60)}",
                    fontSize = 75.sp,
                    modifier = Modifier.padding(16.dp)
                )

                //Trạng thái của app
                Text(
                    text = if (viewModel.isWorkPhase.value) "Studying ..." else "Chilling ...",
                    fontSize = 20.sp,
                    color = if (viewModel.isWorkPhase.value) Color(0xFF4CAF50) else Color(0xFF2196F3), // Xanh lá cho học, xanh dương cho nghỉ
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Video
                AndroidView(
                    factory = {
                        VideoView(it).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            val videoResId = if (viewModel.currentVideo.value == VideoType.Study) {
                                R.raw.vd_working
                            } else {
                                R.raw.vd_chilling
                            }
                            setVideoPath("android.resource://${context.packageName}/$videoResId")
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                seekTo(1)
                                Log.d("MainScreen", "Video prepared to: $videoResId")
                            }
                            videoViewInstance = this
                        }
                    },
                    modifier = Modifier
                        .width(270.dp)
                        .height(200.dp)
                        .clip(CircleShape)
                )

                // Thời gian học và nghỉ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Work: ${viewModel.workTime.value} min",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Break: ${viewModel.breakTime.value} min",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }

            // Phần dưới: Các nút điều khiển
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                when (viewModel.timerState.value) {
                    TimerState.Idle -> {
                        IconButton(onClick = {
                            viewModel.startTimer()
                            videoViewInstance?.start()
                        }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(onClick = {
                            viewModel.resetTimer()
                            videoViewInstance?.seekTo(0)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Replay",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(onClick = {
                            viewModel.cancelTimer()
                            videoViewInstance?.seekTo(0)
                            videoViewInstance?.pause()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    TimerState.Running -> {
                        IconButton(onClick = {
                            viewModel.pauseTimer()
                            videoViewInstance?.pause()
                        }) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_pause),
                                contentDescription = "Pause",
                                modifier = Modifier.size(33.dp)
                            )
                        }
                    }
                    TimerState.Paused -> {
                        IconButton(onClick = {
                            viewModel.startTimer()
                            videoViewInstance?.start()
                        }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(onClick = {
                            viewModel.resetTimer()
                            videoViewInstance?.seekTo(0)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Replay",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(onClick = {
                            viewModel.cancelTimer()
                            videoViewInstance?.seekTo(0)
                            videoViewInstance?.pause()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route)
                }
            ) {
                Text("Logout", fontSize = 16.sp)
            }
        }
    }

    // Dialog tùy chỉnh thời gian
    if (showCustomDialog) {
        var customWorkTime by remember { mutableStateOf(viewModel.workTime.value) }
        var customBreakTime by remember { mutableStateOf(viewModel.breakTime.value) }

        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Select Mode") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.setMode(Mode.Pomodoro)
                                showCustomDialog = false
                            }
                        ) {
                            Text("Pomodoro (25/5)")
                        }
                        Button(
                            onClick = {
                                viewModel.setMode(Mode.Custom, customWorkTime, customBreakTime)
                            }
                        ) {
                            Text("Custom")
                        }
                    }

                    if (viewModel.mode.value == Mode.Custom) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Work Time (minutes):")
                        TextField(
                            value = customWorkTime.toString(),
                            onValueChange = { customWorkTime = it.toIntOrNull() ?: customWorkTime },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Break Time (minutes):")
                        TextField(
                            value = customBreakTime.toString(),
                            onValueChange = { customBreakTime = it.toIntOrNull() ?: customBreakTime },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (viewModel.mode.value == Mode.Custom) {
                    Button(
                        onClick = {
                            viewModel.setMode(Mode.Custom, customWorkTime, customBreakTime)
                            showCustomDialog = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCustomDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog nhập quotes
    if (showQuoteDialog) {
        var newQuote by remember { mutableStateOf(quote) }
        AlertDialog(
            onDismissRequest = { showQuoteDialog = false },
            title = { Text("Enter Your Quote") },
            text = {
                TextField(
                    value = newQuote,
                    onValueChange = { newQuote = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your motivational quote") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newQuote.isNotBlank()) {
                            quote = newQuote
                        }
                        showQuoteDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showQuoteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
enum class TimerState {
    Idle, Running, Paused
}

enum class Mode {
    Pomodoro, Custom
}
