package com.example.promodoapp.timer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.promodoapp.timer.viewmodel.MainScreenViewModel

@Composable
fun CustomTimeDialog(
    viewModel: MainScreenViewModel,
    onDismiss: () -> Unit
) {
    var customWorkTime by remember { mutableStateOf(viewModel.workTime.value) }
    var customBreakTime by remember { mutableStateOf(viewModel.breakTime.value) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
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
                            onDismiss()
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
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}

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