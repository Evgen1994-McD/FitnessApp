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
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.fitnessapp.exercises.domain.models.ThemeMode
import com.example.fitnessapp.settings.domain.SettingsInteractor
import dagger.hilt.EntryPoints





private val DarkColorScheme = darkColorScheme(
    primary = mainDarkBgColor,
    onPrimary = mainLightBgColor,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    onPrimaryContainer = Color(0xFF1A1A1A),
    onBackground = Color(0xFFE6E1E5),
    background = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFFE6E1E5),
    surfaceContainer = baseBlue
)

private val LightColorScheme = lightColorScheme(
    primary = mainLightBgColor,
    onPrimary = mainDarkBgColor,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    onBackground = Color(0xFF1C1B1F),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceContainer = baseBlue

)


@Composable
private fun getThemeModeFromHilt(
    context: android.content.Context,
    defaultDarkTheme: Boolean
): Boolean {
    // Проверяем доступность Hilt до вызова composable функций
    val hiltAvailable = remember {
        try {
            EntryPoints.get(context.applicationContext, ThemeEntryPoint::class.java)
            true
        } catch (e: IllegalStateException) {
            false
        }
    }
    
    return if (hiltAvailable) {
        val themeUseCase = EntryPoints.get(
            context.applicationContext,
            ThemeEntryPoint::class.java
        ).themeInteractor()
        
        val themeMode by themeUseCase.getThemeMode().collectAsState(initial = ThemeMode.SYSTEM)
        Log.d("theme", "theme in mode $themeMode")

        when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> defaultDarkTheme
        }
    } else {
        Log.d("theme", "Hilt not available, using darkTheme parameter: $defaultDarkTheme")
        defaultDarkTheme
    }
}

@Composable
fun FitnessAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val mainScreenBgColor = if (darkTheme){
        mainDarkBgColor
    }else mainLightBgColor

    Log.d("theme", "theme in THEME $darkTheme")
    
    val context = LocalContext.current
    val shouldUseDarkTheme = getThemeModeFromHilt(context, darkTheme)

    Log.d("theme", "shouldUseDarkTheme: $shouldUseDarkTheme")

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


@Composable
fun AllBodyCardBgColor(): Color{
    val isDark = MaterialTheme.colorScheme.background == DarkColorScheme.background
    return if (isDark) {
        allBodyDarkBgColor
    } else {
        allBodyLightBgColor
    }
}

@Composable
fun AllBodyCardTextColor(): Color{
    val isDark = MaterialTheme.colorScheme.background == DarkColorScheme.background
    return if (isDark) {
        Color.White
    } else {
        Color.Black
    }
}

@Composable
fun AllBodyCardProgressTrackColor(): Color{
    val isDark = MaterialTheme.colorScheme.background == DarkColorScheme.background
    return if (isDark) {
        allBodyLightBgColor // светлый цвет для темной темы
    } else {
        allBodyDarkBgColor // темный цвет для светлой темы
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ThemeEntryPoint {
    fun themeInteractor(): SettingsInteractor
}