package com.example.promodoapp.navigation

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
import androidx.navigation.compose.composable
import com.example.promodoapp.statistics.ui.ReportScreen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
/*fun NavGraph() {
    val navController = rememberNavController()
    val authRepository = AuthRepository() // Khởi tạo thủ công

    // Kiểm tra trạng thái đăng nhập
    val startDestination = if (authRepository.getCurrentUser() != null) {
        // TODO: Thay "main" bằng route của màn hình chính sau khi đăng nhập
        Screen.Login.route // Tạm thời để là Login, bạn sẽ cần thêm màn hình chính
    } else {
        Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        AnimatedNavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = { slideInHorizontally { it } },) {
            composable(Screen.Login.route) {
                LoginScreen(navController = navController)
            }
            composable(Screen.Register.route) {
                RegisterScreen(navController = navController)
            }
            // TODO: Thêm các màn hình khác như MainScreen sau khi đăng nhập thành công
            composable(Screen.Main.route){
                MainScreen(navController = navController)
            }
        }
    }
}*/

fun NavGraph() {
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
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } }
//        enterTransition = {
//            scaleIn(
//                initialScale = 0.9f,
//                animationSpec = tween(300, easing = FastOutSlowInEasing)
//            ) + fadeIn(animationSpec = tween(300))
//        },
//        exitTransition = {
//            scaleOut(
//                targetScale = 1.1f,
//                animationSpec = tween(200, easing = FastOutSlowInEasing)
//            ) + fadeOut(animationSpec = tween(200))
//        },
//        popEnterTransition = {
//            scaleIn(
//                initialScale = 0.9f,
//                animationSpec = tween(300, easing = FastOutSlowInEasing)
//            ) + fadeIn(animationSpec = tween(300))
//        },
//        popExitTransition = {
//            scaleOut(
//                targetScale = 1.1f,
//                animationSpec = tween(200, easing = FastOutSlowInEasing)
//            ) + fadeOut(animationSpec = tween(200))
//        }
    ) {
        composable(Screen.Login.route){ LoginScreen(navController) }
        composable(Screen.Register.route){ RegisterScreen(navController) }
        composable(Screen.Main.route){ MainScreen(navController) }
        composable(Screen.Statistics.route){ ReportScreen(navController) }

    }
}
