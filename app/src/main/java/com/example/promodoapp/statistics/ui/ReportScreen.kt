package com.example.promodoapp.statistics.ui

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.promodoapp.R
import com.example.promodoapp.navigation.Screen
import com.example.promodoapp.statistics.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate
    val selectedMonth by viewModel.selectedMonth
    val focusSessions by viewModel.focusSessions
    val totalTime by viewModel.totalTime
    val monthlyFocusDays by viewModel.monthlyFocusDays
    val monthlyFocusSessions by viewModel.monthlyFocusSessions
    val monthlyTotalTime by viewModel.monthlyTotalTime
    val highlightedDays by viewModel.highlightedDays
    val context = LocalContext.current

    // Hàm điều hướng tháng
    fun updateMonth(offset: Int) {
        val sdf = SimpleDateFormat("yyyy/MM", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(selectedMonth)!!
        calendar.add(Calendar.MONTH, offset)
        viewModel.setSelectedMonth(sdf.format(calendar.time))
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_timer), contentDescription = "Timer") },
                    label = { Text("Timer") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Main.route) }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_calendar), contentDescription = "Statistics") },
                    label = { Text("Statistics") },
                    selected = true,
                    onClick = { /* Đã ở màn Statistics */ }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        //Toast.makeText(context,"Coming soon", Toast.LENGTH_LONG).show()
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
            horizontalAlignment = Alignment.Start
        ) {
            // Tiêu đề
            Text(
                text = "Báo cáo",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Điều hướng tháng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Trước",
                    modifier = Modifier.clickable {
                        updateMonth(-1)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedMonth,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Sau",
                    modifier = Modifier.clickable {
                        updateMonth(1)
                    }
                )
            }

            // Hiển thị tên các thứ trong tuần
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tính ngày đầu tiên của tháng
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy/MM", Locale.ENGLISH)
            calendar.time = sdf.parse(selectedMonth)!!
            val firstDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Điều chỉnh để CN là 0
            val offset = (firstDayOfWeek + 6) % 7

            // Vẽ lịch và tô vàng các ngày có phiên học
            Column {
                (0 until 6).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (0 until 7).forEach { dayOfWeek ->
                            val dayIndex = week * 7 + dayOfWeek - offset + 1
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (highlightedDays.contains(dayIndex) && dayIndex in 1..daysInMonth) Color.Yellow else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (dayIndex in 1..daysInMonth) {
                                            viewModel.setSelectedDate("${String.format("%02d", dayIndex)}/${selectedMonth.split("/")[1]}")
                                            Toast.makeText(
                                                context,
                                                "Số lần tập trung: $focusSessions\nThời gian: ${totalTime / 60000} phút",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayIndex in 1..daysInMonth) {
                                    Text(
                                        text = dayIndex.toString(),
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thông tin ngày
            Text(
                text = "Hôm nay $selectedDate",
                fontSize = 20.sp,
                color = Color.Black
            )
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = "Số lần tập trung: $focusSessions lần",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = "Thời gian tập trung: ${totalTime / 60000} phút",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tóm tắt tháng
            Text(
                text = "Tóm tắt tháng ${selectedMonth.split("/")[1]}",
                fontSize = 20.sp,
                color = Color.Black
            )
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = "Số ngày tập trung trong tháng: $monthlyFocusDays ngày",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = "Số lần tập trung hàng tháng: $monthlyFocusSessions lần",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = "Thời gian tập trung hàng tháng: ${monthlyTotalTime / 60000} phút",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}