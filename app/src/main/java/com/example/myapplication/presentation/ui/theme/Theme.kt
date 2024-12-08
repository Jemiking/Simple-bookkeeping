package com.example.myapplication.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 浅色主题配色
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = Color(0xFF003258),
    
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = Color(0xFF410002),
    
    tertiary = ChartColor1,
    onTertiary = Color.White,
    tertiaryContainer = ChartColor2,
    onTertiaryContainer = Color(0xFF002022),
    
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    outline = TextHint
)

// 深色主题配色
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF003258),
    primaryContainer = Primary,
    onPrimaryContainer = Color(0xFFD1E4FF),
    
    secondary = SecondaryLight,
    onSecondary = Color(0xFF680003),
    secondaryContainer = Secondary,
    onSecondaryContainer = Color(0xFFFFDAD6),
    
    tertiary = ChartColor2,
    onTertiary = Color(0xFF003738),
    tertiaryContainer = ChartColor1,
    onTertiaryContainer = Color(0xFFBDEBEE),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7CF),
    
    outline = Color(0xFF8D9199)
)

// 主题数据类
data class MoneyManagerTheme(
    val darkTheme: Boolean,
    val dynamicColor: Boolean
)

// 主题设置
object ThemeSettings {
    val current = mutableStateOf(
        MoneyManagerTheme(
            darkTheme = false,
            dynamicColor = true
        )
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val current = ThemeSettings.current.value
    
    val darkTheme = when {
        current.darkTheme -> true
        !current.darkTheme -> false
        else -> isSystemInDarkTheme()
    }
    
    val colorScheme = when {
        current.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 