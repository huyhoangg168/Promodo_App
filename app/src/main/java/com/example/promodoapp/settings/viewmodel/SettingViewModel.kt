package com.example.promodoapp.settings.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.tasks.await


class SettingsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Trạng thái thời gian tập trung (phút)
    private val _totalFocusTime: MutableState<Long> = mutableStateOf(0L)
    val totalFocusTime: MutableState<Long> = _totalFocusTime

    // Trạng thái số lần tập trung
    private val _focusSessions: MutableState<Int> = mutableStateOf(0)
    val focusSessions: MutableState<Int> = _focusSessions

    init {
        loadUserStats()
    }

    // Tải thống kê người dùng
    private fun loadUserStats() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val sessions = db.collection("users").document(currentUser.uid)
                        .collection("sessions_new")
                        .whereEqualTo("completed", true)
                        .get()
                        .await()

                    _focusSessions.value = sessions.size()
                    _totalFocusTime.value = sessions.documents
                        .mapNotNull { it.getLong("duration") }
                        .sum()
                    Log.d("SettingsViewModel", "Loaded stats: sessions=${_focusSessions.value}, time=${_totalFocusTime.value}")
                } catch (e: Exception) {
                    Log.e("SettingsViewModel", "Failed to load user stats: ${e.message}")
                }
            }
        }
    }

    //Đổi mật khẩu
    fun reauthenticateAndChangePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        if (user != null && email != null) {
            viewModelScope.launch {
                try {
                    val credential = EmailAuthProvider.getCredential(email, oldPassword)
                    user.reauthenticate(credential).await()
                    user.updatePassword(newPassword).await()
                    onResult(Result.success(Unit))
                } catch (e: Exception) {
                    onResult(Result.failure(e))
                }
            }
        } else {
            onResult(Result.failure(Exception("Người dùng chưa đăng nhập")))
        }
    }

}