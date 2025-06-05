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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.promodoapp.R
import com.example.promodoapp.model.ShopItem
import com.example.promodoapp.timer.viewmodel.ShopViewModel

@Composable
fun ShopDialog(
    viewModel: ShopViewModel,
    onDismiss: () -> Unit
) {
    // Chỉ số của mục hiện tại trong danh sách
    var currentIndex by remember { mutableStateOf(0) }
    val items = viewModel.shopItems.value

    // Đảm bảo không truy cập ngoài danh sách
    if (items.isEmpty()) return

    val currentItem = items[currentIndex]
    val context = LocalContext.current
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }

    // Cập nhật video khi mục hiện tại thay đổi
    LaunchedEffect(currentIndex) {
        videoViewInstance?.pause()
        videoViewInstance?.seekTo(0)
        val videoResId = currentItem.resourceId
        videoViewInstance?.setVideoPath("android.resource://${context.packageName}/$videoResId")
        videoViewInstance?.start()
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tiêu đề
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hoạt Ảnh Tâp Trung",
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    if (!currentItem.isPurchased) { // Chỉ hiển thị biểu tượng khóa nếu chưa mua
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Phần preview hoạt ảnh
                AndroidView(
                    factory = {
                        VideoView(it).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            val videoResId = currentItem.resourceId
                            setVideoPath("android.resource://${context.packageName}/$videoResId")
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                seekTo(1)
                                Log.d("ShopDialog", "Video prepared to: $videoResId")
                            }
                            videoViewInstance = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                // Các mũi tên điều hướng
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_left_arrow),
                        contentDescription = "Previous",
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                if (currentIndex > 0) {
                                    currentIndex--
                                }
                            },
                        tint = if (currentIndex > 0) Color.Black else Color.Gray
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_right_arrow),
                        contentDescription = "Next",
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                if (currentIndex < items.size - 1) {
                                    currentIndex++
                                }
                            },
                        tint = if (currentIndex < items.size - 1) Color.Black else Color.Gray
                    )
                }

                // Nút mua hoặc chọn
                if (!currentItem.isPurchased) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF2196F3), CircleShape)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${currentItem.price}",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "Coin",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.purchaseItem(currentItem) },
                        enabled = viewModel.coins.value >= currentItem.price,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mua")
                    }
                } else {
                    Button(
                        onClick = { viewModel.selectItem(currentItem) },
                        enabled = !currentItem.isSelected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentItem.isSelected) Color.Gray else Color.Green
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (currentItem.isSelected) "Đang Chọn" else "Chọn")
                    }
                }
            }
        }
    }
}