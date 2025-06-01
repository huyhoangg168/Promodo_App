package com.example.promodoapp.login_register.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.promodoapp.model.User
import com.example.promodoapp.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    var registerState = mutableStateOf<RegisterState>(RegisterState.Idle)
        private set

    fun register(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            registerState.value = RegisterState.Error("Passwords do not match")
            return
        }

        registerState.value = RegisterState.Loading
        viewModelScope.launch {
            val result = authRepository.register(email, password)
            registerState.value = when {
                result.isSuccess -> RegisterState.Success(result.getOrNull()!!)
                else -> RegisterState.Error(
                    result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}