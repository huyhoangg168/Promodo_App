package com.example.promodoapp.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")

}