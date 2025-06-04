package com.example.promodoapp.timer.ui

import android.Manifest
import android.media.MediaPlayer
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.promodoapp.timer.viewmodel.PhaseChangeEvent
import com.example.promodoapp.timer.viewmodel.VideoType
import com.example.promodoapp.utils.NotificationHelper
import com.example.promodoapp.utils.SoundManager

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
    val quote = viewModel.quote.value
    // Hiển thị dialog nhập quotes
    var showQuoteDialog by remember { mutableStateOf(false) }
    // Hiển thị dialog tùy chỉnh thời gian
    var showCustomDialog by remember { mutableStateOf(false) }
    // Hiển thị dialog xác nhận khi nhấn Replay
    var showReplayConfirmDialog by remember { mutableStateOf(false) }
    // Hiển thị dialog xác nhận khi nhấn Cancel
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    //Biến để lưu số coin trước khi được cộng
    var previousCoins by remember { mutableStateOf(viewModel.coins.value) }
    // Yêu cầu quyền POST_NOTIFICATIONS trên Android 13 trở lên
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainScreen", "POST_NOTIFICATIONS permission granted")
        } else {
            Log.w("MainScreen", "POST_NOTIFICATIONS permission denied")
        }
    }

    // Kiểm tra và yêu cầu quyền khi khởi động
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Cập nhật video khi chuyển giai đoạn
    LaunchedEffect(viewModel.currentVideo.value) {
        Log.d("MainScreen", "Current video changed to: ${viewModel.currentVideo.value}")
        videoViewInstance?.pause()
        videoViewInstance?.seekTo(0)

        val videoResId = if (viewModel.currentVideo.value == VideoType.Study) {
            R.raw.vd_working
        } else {
            R.raw.vd_chilling2
        }
        videoViewInstance?.setVideoPath("android.resource://${context.packageName}/$videoResId")

        if (viewModel.timerState.value == TimerState.Running) {
            videoViewInstance?.start()
        }
    }

    // Hiển thị thông báo khi chuyển giai đoạn
    LaunchedEffect(viewModel.phaseChangeEvent.value) {
        viewModel.phaseChangeEvent.value?.let { event ->
            when (event) {
                PhaseChangeEvent.WorkToBreak -> {
                    // Kết thúc phiên học, chuyển sang phiên nghỉ
                    NotificationHelper.showNotification(
                        context = context,
                        title = "Hết thời gian học!",
                        message = "Đã hoàn thành thời gian học (${viewModel.workTime.value} phút), đến thời gian nghỉ (${viewModel.breakTime.value} phút)!"
                    )
                }
                PhaseChangeEvent.BreakToWork -> {
                    // Kết thúc phiên nghỉ, chuyển sang phiên học
                    NotificationHelper.showNotification(
                        context = context,
                        title = "Hết thời gian nghỉ!",
                        message = "Đã hoàn thành thời gian nghỉ (${viewModel.breakTime.value} phút), đến thời gian học (${viewModel.workTime.value} phút)!"
                    )
                }
            }
            // Reset sự kiện sau khi xử lý
            viewModel.resetPhaseChangeEvent()
        }
    }

    //Phát âm thanh khi được cộng xu
    LaunchedEffect(viewModel.coins.value) {
        if (viewModel.coins.value > previousCoins) {
            try {
                SoundManager.playCoin()
            } catch (e: Exception) {
                Log.e("MainScreen", "Error playing sound: ${e.message}")
            }
        }
        previousCoins = viewModel.coins.value
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
                    onClick = {
                        Toast.makeText(context, "Coming soon", Toast.LENGTH_LONG).show()
                    }
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
                        text = if (viewModel.mode.value == Mode.Pomodoro) "Pomodoro" else "Custom",
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
                            showReplayConfirmDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Replay",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(onClick = {
                            showCancelConfirmDialog = true
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
                            showReplayConfirmDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Replay",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(onClick = {
                            showCancelConfirmDialog = true
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
        CustomTimeDialog(
            viewModel = viewModel,
            onDismiss = { showCustomDialog = false }
        )
    }

    // Dialog nhập quotes
    if (showQuoteDialog) {
        QuoteDialog(
            currentQuote = quote,
            onQuoteChange = { newQuote -> viewModel.updateQuote(newQuote) },
            onDismiss = { showQuoteDialog = false }
        )
    }

    // Dialog xác nhận khi nhấn Replay
    if (showReplayConfirmDialog) {
        ConfirmDialog(
            message = "Bạn có muốn khởi động lại bộ hẹn giờ? Dữ liệu sẽ không được lưu!",
            confirmButtonText = "Um",
            dismissButtonText = "Hủy",
            onConfirm = {
                viewModel.resetTimer()
                videoViewInstance?.seekTo(0)
            },
            onDismiss = {
                showReplayConfirmDialog = false
            }
        )
    }

    //Dialog xác nhận khi ấn Cancel
    if (showCancelConfirmDialog) {
        ConfirmDialog(
            message = "Bạn có muốn kết thúc phiên? Dữ liệu của bạn sẽ được lưu vào lịch sử!",
            confirmButtonText = "Um",
            dismissButtonText = "Hủy",
            onConfirm = {
                viewModel.cancelTimer()
                videoViewInstance?.seekTo(0)
            },
            onDismiss = {
                showCancelConfirmDialog = false
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
