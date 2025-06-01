package com.example.promodoapp.repository

import android.util.Log
import com.example.promodoapp.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Đăng ký người dùng mới
    suspend fun register(email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: ""
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed: User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Đăng nhập người dùng
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            Log.d("AuthRepository", "Logging in with email: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: ""
                )
                Log.d("AuthRepository", "Login successful: ${user.email}")
                Result.success(user)
            } else {
                Log.e("AuthRepository", "Login failed: User is null")
                Result.failure(Exception("Login failed: User is null"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    // Đăng xuất
    fun logout() {
        auth.signOut()
    }

    // Lấy người dùng hiện tại
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let {
            User(
                uid = it.uid,
                email = it.email ?: ""
            )
        }
    }
}