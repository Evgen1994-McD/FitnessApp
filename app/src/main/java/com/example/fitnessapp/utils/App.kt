package com.example.fitnessapp.utils

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App:Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Инициализируем тему при запуске приложения
        val prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val themeMode = prefs.getString("theme_mode", "SYSTEM")
        
        when (themeMode) {
            "LIGHT" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "DARK" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "SYSTEM" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}