package com.example.promodoapp.timer.ui

import android.icu.text.CaseMap.Title
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.promodoapp.timer.viewmodel.MainScreenViewModel

@Composable
fun QuoteDialog(
    currentQuote: String,
    onQuoteChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newQuote by remember { mutableStateOf(currentQuote) }
    AlertDialog(
        onDismissRequest = { onDismiss() },
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
                        onQuoteChange(newQuote)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Hủy")
            }
        }
    )
}


@Composable
fun ConfirmDialog(
    message: String,
    confirmButtonText: String = "",
    dismissButtonText: String = "",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.padding(16.dp),
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss() // Đóng dialog sau khi xác nhận
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BBF9), // Màu xanh  cho nút "Có"
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = confirmButtonText,
                        fontSize = 16.sp
                    )
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BBF9), // Màu xanh cho nút "Không"
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = dismissButtonText,
                        fontSize = 16.sp
                    )
                }
            }
        },
        dismissButton = {} // Không sử dụng dismissButton mặc định vì đã tùy chỉnh
    )
}

@Composable
fun CustomTimeDialog(
    viewModel: MainScreenViewModel,
    onDismiss: () -> Unit
) {
    var customWorkTime by remember { mutableStateOf(viewModel.workTime.value) }
    var customBreakTime by remember { mutableStateOf(viewModel.breakTime.value) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Mode", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            viewModel.setMode(Mode.Pomodoro)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.mode.value == Mode.Pomodoro) Color(0xFF3B82F6) else Color.LightGray
                        )
                    ) {
                        Text("Pomodoro (25/5)", color = Color.White)
                    }
                    Button(
                        onClick = {
                            viewModel.setMode(Mode.Custom, customWorkTime, customBreakTime)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.mode.value == Mode.Custom) Color(0xFF3B82F6) else Color.LightGray
                        )
                    ) {
                        Text("Custom", color = Color.White)
                    }
                }

                if (viewModel.mode.value == Mode.Custom) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Work Time (minutes):")
                    WheelPicker(
                        initialValue = customWorkTime,
                        onValueChange = { customWorkTime = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Break Time (minutes):")
                    WheelPicker(
                        initialValue = customBreakTime,
                        onValueChange = { customBreakTime = it }
                    )
                }
            }
        },
        confirmButton = {
            if (viewModel.mode.value == Mode.Custom) {
                Button(
                    onClick = {
                        viewModel.setMode(Mode.Custom, customWorkTime, customBreakTime)
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = Color.White
    )
}
