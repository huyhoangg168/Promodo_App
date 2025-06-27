package com.example.promodoapp.timer.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.promodoapp.R
import com.example.promodoapp.model.ShopItem
import com.example.promodoapp.model.User
import com.example.promodoapp.repository.AuthRepository
import com.example.promodoapp.repository.UserRepository
import kotlinx.coroutines.launch

class ShopViewModel(
    private val mainScreenViewModel: MainScreenViewModel
) : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    // Danh sách các mục trong cửa hàng
    private val _shopItems: MutableState<List<ShopItem>> = mutableStateOf(emptyList())
    val shopItems: MutableState<List<ShopItem>> = _shopItems

    // Số xu hiện tại (lấy từ MainScreenViewModel)
    val coins: MutableState<Int>
        get() = mainScreenViewModel.coins

    // Lưu trữ thông tin người dùng
    private val _user: MutableState<User?> = mutableStateOf(null)
    val user: MutableState<User?> = _user

    // Trạng thái để thông báo khi hoạt ảnh được chọn thay đổi
    private val _animationSelectionChanged: MutableState<Long> = mutableStateOf(0L)
    val animationSelectionChanged: MutableState<Long> = _animationSelectionChanged

    init {
        loadShopItems()
        loadUserData()
    }

    private fun loadShopItems() {
        _shopItems.value = listOf(
            ShopItem(
                id = "default_work",
                name = "Default Work Animation",
                price = 0,
                resourceId = R.raw.vd_working,
                isPurchased = true,
                isSelected = true
            ),
            ShopItem(
                id = "default_break",
                name = "Default Break Animation",
                price = 0,
                resourceId = R.raw.vd_chilling2,
                isPurchased = true,
                isSelected = true
            ),
            ShopItem(
                id = "work_animation_1",
                name = "Work Animation 1",
                price = 50,
                resourceId = R.raw.vd_working2
            ),
            ShopItem(
                id = "break_animation_1",
                name = "Break Animation 1",
                price = 50,
                resourceId = R.raw.vd_chilling
            )
        )
        Log.d("ShopViewModel", "Loaded shop items: ${_shopItems.value.map { it.name }}")
    }

    fun loadUserData() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val userData = userRepository.getUser(currentUser.uid)
                    if (userData != null) {
                        _user.value = userData
                        mainScreenViewModel.coins.value = userData.coins
                        _shopItems.value = _shopItems.value.map { item ->
                            item.copy(
                                isPurchased = userData.purchasedAnimations.contains(item.id),
                                isSelected = if (item.id.contains("work")) {
                                    item.id == userData.selectedAnimationWork
                                } else {
                                    item.id == userData.selectedAnimationBreak
                                }
                            )
                        }
                        Log.d("ShopViewModel", "Loaded user data: coins=${userData.coins}, purchasedAnimations=${userData.purchasedAnimations}, selectedAnimationWork=${userData.selectedAnimationWork}, selectedAnimationBreak=${userData.selectedAnimationBreak}")
                        Log.d("ShopViewModel", "Updated shop items: ${_shopItems.value.map { "${it.name}: isPurchased=${it.isPurchased}, isSelected=${it.isSelected}" }}")
                    } else {
                        // Khởi tạo người dùng mới
                        val newUser = User(
                            uid = currentUser.uid,
                            email = currentUser.email ?: "",
                            coins = 0,
                            quote = "Stay focused and keep going!",
                            purchasedAnimations = mutableListOf("default_work", "default_break"),
                            selectedAnimationWork = "default_work",
                            selectedAnimationBreak = "default_break"
                        )
                        userRepository.saveUser(newUser)
                        _user.value = newUser
                        mainScreenViewModel.coins.value = newUser.coins
                        _shopItems.value = _shopItems.value.map { item ->
                            item.copy(
                                isPurchased = newUser.purchasedAnimations.contains(item.id),
                                isSelected = if (item.id.contains("work")) {
                                    item.id == newUser.selectedAnimationWork
                                } else {
                                    item.id == newUser.selectedAnimationBreak
                                }
                            )
                        }
                        Log.d("ShopViewModel", "Initialized new user: coins=${newUser.coins}, purchasedAnimations=${newUser.purchasedAnimations}")
                    }
                } catch (e: Exception) {
                    Log.e("ShopViewModel", "Failed to load user data: ${e.message}")
                }
            }
        } else {
            Log.w("ShopViewModel", "No user logged in")
        }
    }

    fun purchaseItem(item: ShopItem) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Log.w("ShopViewModel", "No user logged in")
            return
        }

        if (mainScreenViewModel.coins.value < item.price) {
            Log.w("ShopViewModel", "Not enough coins to purchase ${item.name}. Current coins: ${mainScreenViewModel.coins.value}, Price: ${item.price}")
            return
        }

        viewModelScope.launch {
            try {
                val user = userRepository.getUser(currentUser.uid)
                if (user != null) {
                    val updatedPurchasedAnimations = user.purchasedAnimations.toMutableList().apply {
                        if (!contains(item.id)) add(item.id)
                    }
                    val isWorkAnimation = item.id.contains("work")
                    val updatedUser = user.copy(
                        coins = user.coins - item.price,
                        purchasedAnimations = updatedPurchasedAnimations,
                        selectedAnimationWork = if (isWorkAnimation) item.id else user.selectedAnimationWork,
                        selectedAnimationBreak = if (!isWorkAnimation) item.id else user.selectedAnimationBreak
                    )
                    userRepository.updateUser(updatedUser)
                    _user.value = updatedUser
                    mainScreenViewModel.coins.value = updatedUser.coins
                    _shopItems.value = _shopItems.value.map {
                        if (it.id == item.id) {
                            it.copy(isPurchased = true, isSelected = true)
                        } else {
                            if (isWorkAnimation && it.id.contains("work")) {
                                it.copy(isSelected = false)
                            } else if (!isWorkAnimation && it.id.contains("break")) {
                                it.copy(isSelected = false)
                            } else {
                                it
                            }
                        }
                    }
                    _animationSelectionChanged.value = System.currentTimeMillis()
                    Log.d("ShopViewModel", "Purchased ${item.name}. New coin balance: ${mainScreenViewModel.coins.value}, Purchased animations: ${updatedUser.purchasedAnimations}")
                    Log.d("ShopViewModel", "Updated shop items after purchase: ${_shopItems.value.map { "${it.name}: isPurchased=${it.isPurchased}, isSelected=${it.isSelected}" }}")
                }
            } catch (e: Exception) {
                Log.e("ShopViewModel", "Failed to purchase item: ${e.message}")
            }
        }
    }

    fun selectItem(item: ShopItem) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Log.w("ShopViewModel", "No user logged in")
            return
        }

        if (!item.isPurchased) {
            Log.w("ShopViewModel", "Item ${item.name} not purchased yet")
            return
        }

        viewModelScope.launch {
            try {
                val user = userRepository.getUser(currentUser.uid)
                if (user != null) {
                    val isWorkAnimation = item.id.contains("work")
                    val updatedUser = user.copy(
                        selectedAnimationWork = if (isWorkAnimation) item.id else user.selectedAnimationWork,
                        selectedAnimationBreak = if (!isWorkAnimation) item.id else user.selectedAnimationBreak
                    )
                    userRepository.updateUser(updatedUser)
                    _user.value = updatedUser
                    _shopItems.value = _shopItems.value.map {
                        if (it.id == item.id) {
                            it.copy(isSelected = true)
                        } else {
                            if (isWorkAnimation && it.id.contains("work")) {
                                it.copy(isSelected = false)
                            } else if (!isWorkAnimation && it.id.contains("break")) {
                                it.copy(isSelected = false)
                            } else {
                                it
                            }
                        }
                    }
                    _animationSelectionChanged.value = System.currentTimeMillis()
                    Log.d("ShopViewModel", "Selected ${item.name} for ${if (isWorkAnimation) "work" else "break"} phase. Updated user: selectedAnimationWork=${updatedUser.selectedAnimationWork}, selectedAnimationBreak=${updatedUser.selectedAnimationBreak}")
                    Log.d("ShopViewModel", "Updated shop items after selection: ${_shopItems.value.map { "${it.name}: isPurchased=${it.isPurchased}, isSelected=${it.isSelected}" }}")
                }
            } catch (e: Exception) {
                Log.e("ShopViewModel", "Failed to select item: ${e.message}")
            }
        }
    }

    fun getCurrentAnimationResource(videoType: VideoType): Int {
        val selectedAnimationId = if (videoType == VideoType.Study) {
            _user.value?.selectedAnimationWork ?: "default_work"
        } else {
            _user.value?.selectedAnimationBreak ?: "default_break"
        }
        return _shopItems.value.find { it.id == selectedAnimationId }?.resourceId
            ?: if (videoType == VideoType.Study) R.raw.vd_working else R.raw.vd_chilling2
    }

    fun getFilteredShopItems(isWorkAnimation: Boolean): List<ShopItem> {
        val filteredItems = _shopItems.value.filter { item ->
            if (isWorkAnimation) item.id.contains("work") else item.id.contains("break")
        }
        Log.d("ShopViewModel", "Filtered items (isWorkAnimation=$isWorkAnimation): ${filteredItems.map { "${it.name}: isPurchased=${it.isPurchased}, isSelected=${it.isSelected}" }}")
        return filteredItems
    }
}