package com.example.promodoapp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Session(
    val userId: String = "",
    val type: String = "", // "pomodoro" hoặc "custom"
    @ServerTimestamp val startTime: Date? = null,
    val duration: Int = 0, // Thời lượng phiên (phút)
    val completed: Boolean = true
)