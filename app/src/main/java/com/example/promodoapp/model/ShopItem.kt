package com.example.promodoapp.model

data class ShopItem(
    val id: String,           // ID của hoạt ảnh (liên kết với tài nguyên trong res/raw)
    val name: String,      // Tên hoạt ảnh
    val price: Int,         // Giá xu để mua
    val resourceId: Int, // ID của tài nguyên hoạt ảnh trong thư mục res
    var isPurchased: Boolean = false,  // Trạng thái đã mua hay chưa
    var isSelected: Boolean = false    // Trạng thái đang được chọn để sử dụng
)