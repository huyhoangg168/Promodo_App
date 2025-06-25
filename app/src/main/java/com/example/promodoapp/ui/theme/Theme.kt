package com.example.promodoapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00BBF9),      // Màu nút SIGN IN
    onPrimary = Color.White,          // Chữ trắng trên nút
    background = Color.White,         // Nền trắng
    onBackground = Color.Black,       // Chữ đen
    secondary = Color(0xFF00BBF9),    // Màu link SIGN UP
    onSecondary = Color.Black,
    tertiary = Color(0xFFBB86FC)
)

@Composable
fun PromodoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
