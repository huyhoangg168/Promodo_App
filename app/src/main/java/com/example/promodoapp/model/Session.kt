package com.example.promodoapp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Session(
    val userId: String = "",
    val type: String = "",
    val duration: Int = 0,
    val completed: Boolean = false,
    val date: String? = null, // Thêm trường date dạng "dd/MM/yyyy"
    val startTime: Long? = null, // Loại bỏ @ServerTimestamp, dùng clientStartTime
    val clientStartTime: Long? = null, // Thời gian từ client
    var formattedTime: String = "N/A"
)