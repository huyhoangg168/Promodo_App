package com.example.promodoapp.repository

import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.promodoapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth

class UserRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Lưu thông tin người dùng vào Firestore
    suspend fun saveUser(user: User) {
        try {
            Log.d("UserRepository", "Saving user to Firestore: ${user.email}")
            db.collection("users").document(user.uid).set(user).await()
            Log.d("UserRepository", "User saved successfully: ${user.email}")
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to save user: ${e.message}")
            throw e
        }
    }

    // Lấy thông tin người dùng từ Firestore
    suspend fun getUser(uid: String): User? {
        return try {
            Log.d("UserRepository", "Fetching user with UID: $uid")
            val userDoc = db.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java)
            if (user != null) {
                Log.d("UserRepository", "User fetched successfully: ${user.email}")
            } else {
                Log.w("UserRepository", "User not found for UID: $uid")
            }
            user
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to fetch user: ${e.message}")
            null
        }
    }

    // Cập nhật thông tin người dùng trên Firestore
    suspend fun updateUser(user: User) {
        try {
            Log.d("UserRepository", "Updating user: ${user.email}")
            db.collection("users").document(user.uid).set(user).await()
            Log.d("UserRepository", "User updated successfully: ${user.email}")
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to update user: ${e.message}")
            throw e
        }
    }

}