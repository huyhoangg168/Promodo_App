package com.example.promodoapp.login_register.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.promodoapp.model.User
import com.example.promodoapp.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _loginState: MutableState<LoginState> = mutableStateOf(LoginState.Idle)
    val loginState: MutableState<LoginState> = _loginState

    fun login(email: String, password: String) {
        Log.d("LoginViewModel", "Login called with email: $email")
        _loginState.value = LoginState.Loading
        Log.d("LoginViewModel", "Before launching coroutine")
        viewModelScope.launch {
            Log.d("LoginViewModel", "Inside coroutine")
            try {
                val result = authRepository.login(email, password)
                Log.d("LoginViewModel", "Result received: $result")
                _loginState.value = when {
                    result.isSuccess -> {
                        val user = result.getOrNull()!!
                        Log.d("LoginViewModel", "Login success: ${user.email}")
                        LoginState.Success(user)
                    }
                    else -> {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                        Log.e("LoginViewModel", "Login failed: $errorMessage")
                        when(errorMessage){
                        }
                        LoginState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Exception in login: ${e.message}")
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
        Log.d("LoginViewModel", "After launching coroutine")
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}