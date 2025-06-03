//package com.example.promodoapp.timer.ui
//
//import android.view.ViewGroup
//import android.widget.Toast
//import android.widget.VideoView
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import com.example.promodoapp.R
//import com.example.promodoapp.timer.viewmodel.MainScreenViewModel
//
//
//@Composable
//fun AnimationSelectionDialog(
//    viewModel: MainScreenViewModel,
//    onDismiss: () -> Unit
//) {
//    val context = LocalContext.current
//    var selectedAnimation by remember { mutableStateOf<Animation?>(null) }
//    var currentIndex by remember { mutableStateOf(0) }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Text(
//                text = "Hoạt ảnh tập trung",
//                fontSize = 20.sp,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//        },
//        text = {
//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Hiển thị danh sách hoạt ảnh
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    IconButton(
//                        onClick = {
//                            if (currentIndex > 0) {
//                                currentIndex--
//                                selectedAnimation = viewModel.availableAnimations.value[currentIndex]
//                            }
//                        },
//                        enabled = currentIndex > 0
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_left_arrow),
//                            contentDescription = "Previous",
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                    AndroidView(
//                        factory = {
//                            VideoView(it).apply {
//                                layoutParams = ViewGroup.LayoutParams(
//                                    ViewGroup.LayoutParams.MATCH_PARENT,
//                                    ViewGroup.LayoutParams.WRAP_CONTENT
//                                )
//                                selectedAnimation?.let { anim ->
//                                    setVideoPath("android.resource://${context.packageName}/${anim.resourceId}")
//                                    setOnPreparedListener { mp ->
//                                        mp.isLooping = true
//                                        start()
//                                    }
//                                }
//                            }
//                        },
//                        modifier = Modifier
//                            .width(200.dp)
//                            .height(150.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                    )
//                    IconButton(
//                        onClick = {
//                            if (currentIndex < viewModel.availableAnimations.value.size - 1) {
//                                currentIndex++
//                                selectedAnimation = viewModel.availableAnimations.value[currentIndex]
//                            }
//                        },
//                        enabled = currentIndex < viewModel.availableAnimations.value.size - 1
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_right_arrow),
//                            contentDescription = "Next",
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//                // Nút mở khóa hoặc chọn
//                if (selectedAnimation != null) {
//                    if (selectedAnimation!!.isUnlocked) {
//                        Button(
//                            onClick = {
//                                viewModel.selectAnimation(selectedAnimation!!.id, viewModel.isWorkPhase.value)
//                                onDismiss()
//                            },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(48.dp)
//                        ) {
//                            Text("Chọn", fontSize = 16.sp)
//                        }
//                    } else {
//                        Button(
//                            onClick = {
//                                val success = viewModel.unlockAnimation(selectedAnimation!!.id)
//                                if (success) {
//                                    Toast.makeText(context, "Đã mở khóa hoạt ảnh!", Toast.LENGTH_SHORT).show()
//                                } else {
//                                    Toast.makeText(context, "Không đủ xu để mở khóa!", Toast.LENGTH_SHORT).show()
//                                }
//                            },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(48.dp)
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.Center
//                            ) {
//                                Text("${selectedAnimation!!.cost}", fontSize = 16.sp)
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Image(
//                                    painter = painterResource(id = R.drawable.ic_coin),
//                                    contentDescription = "coin",
//                                    modifier = Modifier.size(24.dp)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        },
//        confirmButton = {},
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Đóng")
//            }
//        }
//    )
//
//    // Cập nhật video đầu tiên khi dialog mở
//    LaunchedEffect(Unit) {
//        if (viewModel.availableAnimations.value.isNotEmpty()) {
//            currentIndex = 0
//            selectedAnimation = viewModel.availableAnimations.value[currentIndex]
//        }
//    }
//}