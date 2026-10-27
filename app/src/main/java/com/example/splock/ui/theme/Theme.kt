package com.example.splock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MotorsportRed,
    onPrimary = RacingWhite,
    primaryContainer = MotorsportRedDark,
    onPrimaryContainer = RacingWhite,
    secondary = TelemetryAccent,
    background = CarbonBackground,
    onBackground = RacingWhite,
    surface = CarbonSurface,
    onSurface = RacingWhite,
    surfaceVariant = CarbonSurfaceVariant,
    onSurfaceVariant = RacingSilver
)

private val LightColorScheme = lightColorScheme(
    primary = MotorsportRed,
    onPrimary = RacingWhite,
    primaryContainer = MotorsportRedDark,
    onPrimaryContainer = RacingWhite,
    secondary = Color(0xFF006B75),
    background = Color(0xFFF4F5F7),
    onBackground = Color(0xFF090A0E),
    surface = Color.White,
    onSurface = Color(0xFF090A0E),
    surfaceVariant = Color(0xFFE4E7ED),
    onSurfaceVariant = Color(0xFF5A6070)
)

@Composable
fun SplockTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
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
