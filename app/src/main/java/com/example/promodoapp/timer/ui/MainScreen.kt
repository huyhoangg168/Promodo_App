package com.example.promodoapp.timer.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.promodoapp.R
import com.example.promodoapp.navigation.Screen
import com.example.promodoapp.service.TimerService
import com.example.promodoapp.timer.viewmodel.MainScreenViewModel
import com.example.promodoapp.timer.viewmodel.PhaseChangeEvent
import com.example.promodoapp.utils.NotificationHelper
import com.example.promodoapp.utils.SoundManager

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenViewModel,
) {
    val shopViewModel = viewModel.shopViewModel
    val context = LocalContext.current
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
    val quote = viewModel.quote.value
    var showQuoteDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var showReplayConfirmDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showShopDialog by remember { mutableStateOf(false) }

    // Yêu cầu quyền POST_NOTIFICATIONS
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainScreen", "POST_NOTIFICATIONS permission granted")
        } else {
            Log.w("MainScreen", "POST_NOTIFICATIONS permission denied")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(viewModel.currentVideo.value, shopViewModel.animationSelectionChanged.value) {
        Log.d("MainScreen", "Current video changed to: ${viewModel.currentVideo.value}")
        videoViewInstance?.pause()
        videoViewInstance?.seekTo(0)
        val videoResId = viewModel.getCurrentAnimationResource()
        videoViewInstance?.setVideoPath("android.resource://${context.packageName}/$videoResId")
        if (viewModel.timerState.value == TimerState.Running) {
            videoViewInstance?.start()
        }
    }

    LaunchedEffect(viewModel.phaseChangeEvent.value) {
        viewModel.phaseChangeEvent.value?.let { event ->
            when (event) {
                PhaseChangeEvent.WorkToBreak -> {
                    NotificationHelper.showNotification(
                        context = context,
                        title = "Hết thời gian học!",
                        message = "Đã hoàn thành thời gian học (${viewModel.workTime.value} phút), đến thời gian nghỉ (${viewModel.breakTime.value} phút)!"
                    )
                }

                PhaseChangeEvent.BreakToWork -> {
                    NotificationHelper.showNotification(
                        context = context,
                        title = "Hết thời gian nghỉ!",
                        message = "Đã hoàn thành thời gian nghỉ (${viewModel.breakTime.value} phút), đến thời gian học (${viewModel.workTime.value} phút)!"
                    )
                }
            }
            viewModel.resetPhaseChangeEvent()
        }
    }

    LaunchedEffect(shopViewModel.user.value, shopViewModel.coins.value) {
        val videoResId = viewModel.getCurrentAnimationResource()
        videoViewInstance?.pause()
        videoViewInstance?.setVideoPath("android.resource://${context.packageName}/$videoResId")
        videoViewInstance?.seekTo(1)
    }

    LaunchedEffect(videoViewInstance) {
        if (viewModel.timerState.value == TimerState.Running) {
            videoViewInstance?.start()
        }
    }

    LaunchedEffect(viewModel.coins.value) {
        try {
            SoundManager.playCoin()
        } catch (e: Exception) {
            Log.e("MainScreen", "Error playing sound: ${e.message}")
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_timer),
                            contentDescription = "Timer"
                        )
                    },
                    label = { Text("Timer") },
                    selected = true,
                    onClick = { /* Đã ở màn Timer */ }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Statistics"
                        )
                    },
                    label = { Text("Statistics") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Statistics.route) }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings"
                        )
                    },
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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

                Text(
                    text = "${viewModel.currentTime.value / 60}:${
                        String.format(
                            "%02d",
                            viewModel.currentTime.value % 60
                        )
                    }",
                    fontSize = 75.sp,
                    modifier = Modifier.padding(16.dp)
                )

                Text(
                    text = if (viewModel.isWorkPhase.value) "Studying ..." else "Chilling ...",
                    fontSize = 20.sp,
                    color = if (viewModel.isWorkPhase.value) Color(0xFF4CAF50) else Color(0xFF2196F3),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AndroidView(
                    factory = {
                        VideoView(it).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            val videoResId = viewModel.getCurrentAnimationResource()
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
                        .clip(RoundedCornerShape(20))
                )

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
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_START
                                putExtra(TimerService.EXTRA_WORK_TIME, viewModel.workTime.value)
                                putExtra(TimerService.EXTRA_BREAK_TIME, viewModel.breakTime.value)
                                putExtra(
                                    TimerService.EXTRA_CURRENT_TIME,
                                    viewModel.currentTime.value
                                )
                                putExtra(
                                    TimerService.EXTRA_IS_WORK_PHASE,
                                    viewModel.isWorkPhase.value
                                )
                                putExtra(
                                    TimerService.EXTRA_MODE,
                                    if (viewModel.mode.value == Mode.Pomodoro) "pomodoro" else "custom"
                                )
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
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
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_PAUSE
                            }
                            context.startService(intent)
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
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_START
                                putExtra(TimerService.EXTRA_WORK_TIME, viewModel.workTime.value)
                                putExtra(TimerService.EXTRA_BREAK_TIME, viewModel.breakTime.value)
                                putExtra(
                                    TimerService.EXTRA_CURRENT_TIME,
                                    viewModel.currentTime.value
                                )
                                putExtra(
                                    TimerService.EXTRA_IS_WORK_PHASE,
                                    viewModel.isWorkPhase.value
                                )
                                putExtra(
                                    TimerService.EXTRA_MODE,
                                    if (viewModel.mode.value == Mode.Pomodoro) "pomodoro" else "custom"
                                )
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
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
        }
    }

    if (showCustomDialog) {
        CustomTimeDialog(
            viewModel = viewModel,
            onDismiss = { showCustomDialog = false }
        )
    }

    if (showQuoteDialog) {
        QuoteDialog(
            currentQuote = quote,
            onQuoteChange = { newQuote -> viewModel.updateQuote(newQuote) },
            onDismiss = { showQuoteDialog = false }
        )
    }

    if (showReplayConfirmDialog) {
        ConfirmDialog(
            message = "Bạn có muốn khởi động lại bộ hẹn giờ? Dữ liệu sẽ không được lưu!",
            confirmButtonText = "Um",
            dismissButtonText = "Hủy",
            onConfirm = {
                viewModel.resetTimer()
                videoViewInstance?.seekTo(0)
                val intent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP
                }
                context.startService(intent)
            },
            onDismiss = {
                showReplayConfirmDialog = false
            }
        )
    }

    if (showCancelConfirmDialog) {
        ConfirmDialog(
            message = "Bạn có muốn kết thúc phiên? Dữ liệu của bạn sẽ được lưu vào lịch sử!",
            confirmButtonText = "Um",
            dismissButtonText = "Hủy",
            onConfirm = {
                viewModel.cancelTimer()
                videoViewInstance?.seekTo(0)
                val intent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP
                }
                context.startService(intent)
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