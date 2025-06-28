package com.example.promodoapp.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.promodoapp.repository.AuthRepository
import com.example.promodoapp.login_register.ui.LoginScreen
import com.example.promodoapp.timer.ui.MainScreen
import com.example.promodoapp.login_register.ui.RegisterScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import com.example.promodoapp.settings.ui.SettingsScreen
import com.example.promodoapp.statistics.ui.ReportScreen
import com.example.promodoapp.timer.viewmodel.MainScreenViewModel
import com.example.promodoapp.settings.ui.ChangePasswordScreen
import com.example.promodoapp.settings.ui.EditProfileScreen

@SuppressLint("UnrememberedGetBackStackEntry")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph() {
    val bottomBarScreens = listOf(
        Screen.Main.route,
        Screen.Statistics.route,
        Screen.Settings.route
    )

    val navController = rememberNavController()
    val authRepository = AuthRepository()

    val startDestination = if (authRepository.getCurrentUser() != null) {
        Screen.Main.route
    } else {
        Screen.Login.route
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            val (enter, _) = getSlideDirection(
                initialState.destination.route,
                targetState.destination.route,
                bottomBarScreens
            )
            enter
        },
        exitTransition = {
            val (_, exit) = getSlideDirection(
                initialState.destination.route,
                targetState.destination.route,
                bottomBarScreens
            )
            exit
        },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } }
    ) {
        composable(Screen.Login.route){ LoginScreen(navController) }
        composable(Screen.Register.route){ RegisterScreen(navController) }
        composable(Screen.Main.route){
            val currentUser = authRepository.getCurrentUser()
            val userId = currentUser?.uid ?: "guest"

            val navBackStackEntry = remember(navController) {
                navController.getBackStackEntry(Screen.Main.route)
            }

            val mainViewModel: MainScreenViewModel = viewModel(
                viewModelStoreOwner = navBackStackEntry,
                key = userId
            )
            MainScreen(navController, viewModel = mainViewModel)
        }
        composable(Screen.Statistics.route){ ReportScreen(navController) }
        composable(Screen.Settings.route){ SettingsScreen(navController) }
        composable(Screen.ChangePwd.route) { ChangePasswordScreen(navController) }
        composable(Screen.EditProfile.route) {
            val mainScreenViewModel : MainScreenViewModel = viewModel()
            EditProfileScreen(navController, shopViewModel = mainScreenViewModel.shopViewModel)
        }
    }
}

fun getSlideDirection(
    fromRoute: String?,
    toRoute: String?,
    screenOrder: List<String>
): Pair<EnterTransition, ExitTransition> {
    val fromIndex = screenOrder.indexOf(fromRoute)
    val toIndex = screenOrder.indexOf(toRoute)

    return if (fromIndex != -1 && toIndex != -1) {
        if (toIndex > fromIndex) {
            // Điều hướng sang phải (forward)
            Pair(
                slideInHorizontally(tween(300)) { it },
                slideOutHorizontally(tween(300)) { -it }
            )
        } else {
            // Điều hướng sang trái (backward)
            Pair(
                slideInHorizontally(tween(300)) { -it },
                slideOutHorizontally(tween(300)) { it }
            )
        }
    } else {
        // Mặc định
        Pair(
            slideInHorizontally(tween(300)) { it },
            slideOutHorizontally(tween(300)) { -it }
        )
    }
}

