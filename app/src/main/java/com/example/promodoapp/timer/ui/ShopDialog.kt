package com.example.promodoapp.timer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.promodoapp.R
import com.example.promodoapp.timer.viewmodel.MainScreenViewModel
import com.example.promodoapp.timer.viewmodel.VideoType

data class AnimationItem(
    val videoResId: Int,
    val videoType: VideoType,
    val price: Int,
    val name: String
)

@Composable
fun ShopDialog(
    viewModel: MainScreenViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userCoins by viewModel.coins.collectAsState()
    val purchasedAnimations by viewModel.purchasedAnimations.collectAsState()
    var selectedAnimation by remember { mutableStateOf<AnimationItem?>(null) }

    // Danh sách các hoạt ảnh có sẵn trong shop
    val animationItems = listOf(
        AnimationItem(R.raw.vd_working, VideoType.Study, 50, "Chó & Mèo Học Tập"),
        AnimationItem(R.raw.vd_chilling2, VideoType.Break, 50, "Chó & Mèo Nghỉ Ngơi"),
        AnimationItem(R.raw.vd_working2, VideoType.Study, 100, "Thỏ Học Tập"),
        AnimationItem(R.raw.vd_chilling3, VideoType.Break, 100, "Thỏ Nghỉ Ngơi")
    )

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Tiêu đề
                Text(
                    text = "Cửa Hàng Hoạt Ảnh",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                // Số xu của người dùng
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Số Xu: $userCoins",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_coin),
                        contentDescription = "xu",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Danh sách hoạt ảnh
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(animationItems) { _, item ->
                        val isPurchased = purchasedAnimations.contains(item.videoResId)
                        val isSelected = viewModel.currentVideo.value == item.videoType && isPurchased

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(
                                    if (isSelected) Color(0xFFE0E0E0) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isPurchased) {
                                        // Chọn hoạt ảnh nếu đã mua
                                        viewModel.selectAnimation(item.videoResId, item.videoType)
                                    } else {
                                        // Mua nếu chưa sở hữu
                                        if (userCoins >= item.price) {
                                            viewModel.purchaseAnimation(item.videoResId)
                                            viewModel.deductCoins(item.price)
                                            viewModel.selectAnimation(item.videoResId, item.videoType)
                                        } else {
                                            // Hiển thị thông báo không đủ xu
                                            android.widget.Toast.makeText(
                                                context,
                                                "Không đủ xu để mua hoạt ảnh này!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 16.sp,
                                color = Color.Black
                            )

                            if (isPurchased) {
                                Text(
                                    text = if (isSelected) "Đang Sử Dụng" else "Chọn",
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color(0xFF4CAF50) else Color(0xFF2196F3)
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.price}",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_coin),
                                        contentDescription = "xu",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Nút đóng
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}