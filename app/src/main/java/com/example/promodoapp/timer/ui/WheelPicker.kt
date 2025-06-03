package com.example.promodoapp.timer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun WheelPicker(
    initialValue: Int,
    onValueChange: (Int) -> Unit
) {
    val items = (0..60).map { it.toString().padStart(2, '0') }
    val itemHeight = 40.dp
    val visibleItemCount = 3 // Hiển thị 3 items, trung tâm là item cần chọn

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialValue
    )
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .height(itemHeight * visibleItemCount)
            .fillMaxWidth()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItemCount / 2)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                // Tính toán item ở giữa dựa trên vị trí cuộn
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                val viewportCenter = listState.layoutInfo.viewportStartOffset +
                        (listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset) / 2

                val centerItem = visibleItems.minByOrNull {
                    val itemCenter = it.offset + it.size / 2
                    kotlin.math.abs(itemCenter - viewportCenter)
                }

                val isSelected = centerItem?.index == index

                Text(
                    text = item,
                    fontSize = if (isSelected) 24.sp else 16.sp,
                    color = if (isSelected) Color.Black else Color.Gray,
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Highlight vùng chọn trung tâm
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .height(itemHeight)
                .fillMaxWidth()
                .background(Color.LightGray.copy(alpha = 0.2f))
        )

        // Snap về item gần nhất sau khi scroll dừng
        LaunchedEffect(listState.isScrollInProgress.not()) {
            if (!listState.isScrollInProgress) {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                val viewportCenter = listState.layoutInfo.viewportStartOffset +
                        (listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset) / 2

                val centerItem = visibleItems.minByOrNull {
                    val itemCenter = it.offset + it.size / 2
                    kotlin.math.abs(itemCenter - viewportCenter)
                }

                centerItem?.let { item ->
                    val index = item.index
                    onValueChange(index)

                    // Snap nếu chưa đúng giữa
                    val itemCenter = item.offset + item.size / 2
                    if (itemCenter != viewportCenter) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    }
                }
            }
        }
    }
}

