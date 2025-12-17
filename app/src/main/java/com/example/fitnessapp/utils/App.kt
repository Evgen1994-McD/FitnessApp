package com.example.fitnessapp.utils

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitnessapp.ads.AppOpenAdManager
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App:Application() {
    
    companion object {
        // Демонстрационный ID для тестирования
        // Замените на реальный ID из Рекламной сети Яндекса перед релизом
        private const val APP_OPEN_AD_UNIT_ID = "R-M-18059682-1"
        
        @Volatile
        private var appOpenAdManager: AppOpenAdManager? = null
        
        fun getAppOpenAdManager(application: Application): AppOpenAdManager {
            return appOpenAdManager ?: synchronized(this) {
                appOpenAdManager ?: AppOpenAdManager(application, APP_OPEN_AD_UNIT_ID).also {
                    appOpenAdManager = it
                }
            }
        }
    }
    
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
        
        // Инициализируем Yandex Mobile Ads SDK
        // SDK также может инициализироваться автоматически, но явная инициализация дает больше контроля
        MobileAds.initialize(this) {
            // SDK успешно инициализирован, теперь можно использовать рекламу
            // Инициализируем менеджер рекламы при открытии приложения
            val manager = getAppOpenAdManager(this)
            manager.incrementAppLaunches()
            // Предзагружаем рекламу
            manager.loadAppOpenAd()
        }
    }
}