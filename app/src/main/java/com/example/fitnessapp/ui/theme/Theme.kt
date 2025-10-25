package com.example.fitnessapp.ui.theme

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.fitnessapp.exercises.domain.models.ThemeMode
import com.example.fitnessapp.settings.domain.SettingsInteractor
import dagger.hilt.EntryPoints

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF1A1A1A),
    secondary = PurpleGrey80,
    tertiary = Pink80,
    onPrimaryContainer = Color(0xFF1A1A1A),
    onBackground = Color(0xFFE6E1E5),
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2A2A2A),
    onSurface = Color(0xFFE6E1E5)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    onBackground = Color(0xFF1C1B1F),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun FitnessAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    Log.d("theme", "theme in THEME $darkTheme")
    val context = LocalContext.current
    val themeUseCase = EntryPoints.get(
        context.applicationContext,
        ThemeEntryPoint::class.java
    ).themeInteractor()

    val themeMode by themeUseCase.getThemeMode().collectAsState(initial = ThemeMode.SYSTEM)
    Log.d("theme", "theme in mode $themeMode")

    val shouldUseDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> darkTheme
    }
    Log.d("theme", "shouldUseDarkTheme: $shouldUseDarkTheme, themeMode: $themeMode")

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (shouldUseDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        shouldUseDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    Log.d("theme", "shouldUseDarkTheme: $shouldUseDarkTheme")

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ThemeEntryPoint {
    fun themeInteractor(): SettingsInteractor
}