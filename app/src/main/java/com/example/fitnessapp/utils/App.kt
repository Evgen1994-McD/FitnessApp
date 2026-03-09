package com.example.fitnessapp.utils

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.fitnessapp.ads.AppOpenAdManager
import com.example.fitnessapp.ads.InterstitialAdManager
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App:Application() {
    
    companion object {
        // Реальный ID для релиза
        private const val APP_OPEN_AD_UNIT_ID = "R-M-18846080-1"
//        private const val APP_OPEN_AD_UNIT_ID = "demo-appopenad-yandex"
        
        // ID для межстраничной рекламы
//        private const val INTERSTITIAL_AD_UNIT_ID = "demo-interstitial-yandex"

        private const val INTERSTITIAL_AD_UNIT_ID = "R-M-18846080-1"

        @Volatile
        private var appOpenAdManager: AppOpenAdManager? = null
        
        fun getAppOpenAdManager(application: Application): AppOpenAdManager {
            return appOpenAdManager ?: synchronized(this) {
                appOpenAdManager ?: AppOpenAdManager(application, APP_OPEN_AD_UNIT_ID).also {
                    appOpenAdManager = it
                }
            }
        }
        
        @Volatile
        private var interstitialAdManager: InterstitialAdManager? = null
        
        fun getInterstitialAdManager(application: Application): InterstitialAdManager {
            return interstitialAdManager ?: synchronized(this) {
                interstitialAdManager ?: InterstitialAdManager(application, INTERSTITIAL_AD_UNIT_ID).also {
                    interstitialAdManager = it
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Инициализируем Cactus AI (без автоматического скачивания)
        try {
            // Инициализация Cactus AI
            com.cactus.CactusContextInitializer.initialize(this)
        } catch (e: Exception) {
            // Логируем ошибку, но не падаем приложение
            e.printStackTrace()
        }
        
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
            val appOpenManager = getAppOpenAdManager(this)
            appOpenManager.incrementAppLaunches()
            // Предзагружаем рекламу
            appOpenManager.loadAppOpenAd()
            
            // Инициализируем менеджер межстраничной рекламы
            getInterstitialAdManager(this)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Освобождаем ресурсы менеджеров рекламы
        try {
            getAppOpenAdManager(this).destroy()
            getInterstitialAdManager(this).destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}