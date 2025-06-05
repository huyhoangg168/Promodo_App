package com.example.promodoapp.model

data class User(
    val uid: String = "",
    val email: String = "",
    val coins: Int = 0, // Số xu người dùng, mặc định là 0
    val quote: String = "Stay focused and keep going!",
    val purchasedAnimations: MutableList<String> = mutableListOf(), // Danh sách ID của các hoạt ảnh đã mua
    val selectedAnimationWork: String = "default_work", // ID của hoạt ảnh học đang chọn
    val selectedAnimationBreak: String = "default_break" // ID của hoạt ảnh nghỉ đang chọn
)