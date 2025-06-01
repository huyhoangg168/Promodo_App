package com.example.promodoapp.model

data class User(
    val uid: String = "",
    val email: String = "",
    val coins: Int = 0 // Số xu người dùng, mặc định là 0
)