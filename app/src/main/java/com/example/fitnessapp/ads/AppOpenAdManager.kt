package com.example.fitnessapp.ads

import android.app.Activity
import android.content.SharedPreferences
import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

/**
 * Менеджер для управления рекламой при открытии приложения (App Open Ad)
 * 
 * Рекомендации по использованию:
 * - Не показывать рекламу новым пользователям (первые несколько запусков)
 * - Показывать рекламу только после определенного времени в фоне (например, 30 секунд)
 * - Регулировать частоту показов
 */
class AppOpenAdManager(
    private val application: android.app.Application,
    private val adUnitId: String
) : DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var appOpenAdLoader: AppOpenAdLoader? = null
    private var isShowingAd = false
    private var currentActivity: Activity? = null
    
    // Время, когда приложение ушло в фон (в миллисекундах)
    private var backgroundTime: Long = 0
    
    // Минимальное время в фоне перед показом рекламы (30 секунд)
    private val MIN_BACKGROUND_TIME_MS = 30_000L
    
    // Минимальное количество запусков перед показом рекламы
    private val MIN_APP_LAUNCHES = 0
    
    private val prefs: SharedPreferences = 
        application.getSharedPreferences("app_open_ad_prefs", android.content.Context.MODE_PRIVATE)
    
    init {
        initializeAdLoader()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun initializeAdLoader() {
        appOpenAdLoader = AppOpenAdLoader(application).apply {
            setAdLoadListener(createAdLoadListener())
        }
    }

    private fun createAdLoadListener(): AppOpenAdLoadListener {
        return object : AppOpenAdLoadListener {
            override fun onAdLoaded(loadedAd: AppOpenAd) {
                // Реклама успешно загружена
                appOpenAd?.setAdEventListener(null)
                appOpenAd = loadedAd
            }

            override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                // Ошибка загрузки рекламы
                // Не рекомендуется загружать новую рекламу сразу после ошибки
                appOpenAd = null
            }
        }
    }

    private fun createAdEventListener(): AppOpenAdEventListener {
        return object : AppOpenAdEventListener {
            override fun onAdShown() {
                // Реклама показана
            }

            override fun onAdFailedToShow(adError: AdError) {
                // Ошибка показа рекламы
                clearAppOpenAd()
                loadAppOpenAd()
            }

            override fun onAdDismissed() {
                // Реклама закрыта пользователем
                clearAppOpenAd()
                // Предзагружаем следующую рекламу
                loadAppOpenAd()
            }

            override fun onAdClicked() {
                // Пользователь кликнул по рекламе
            }

            override fun onAdImpression(impressionData: ImpressionData?) {
                // Зафиксирован показ рекламы
            }
        }
    }

    /**
     * Загружает рекламу при открытии приложения
     */
    fun loadAppOpenAd() {
        if (appOpenAd != null || isShowingAd) {
            return
        }

        val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()
        appOpenAdLoader?.loadAd(adRequestConfiguration)
    }

    /**
     * Показывает рекламу при открытии приложения
     */
    private fun showAppOpenAd(activity: Activity) {
        if (isShowingAd) {
            return
        }

        // Проверяем условия показа рекламы
        if (!shouldShowAd()) {
            return
        }

        val ad = appOpenAd ?: return
        currentActivity = activity

        ad.setAdEventListener(createAdEventListener())
        isShowingAd = true

        try {
            ad.show(activity)
        } catch (e: Exception) {
            isShowingAd = false
            clearAppOpenAd()
            loadAppOpenAd()
        }
    }

    /**
     * Проверяет, нужно ли показывать рекламу
     * Согласно рекомендациям:
     * - Не показывать новым пользователям
     * - Показывать только после определенного времени в фоне
     */
    private fun shouldShowAd(): Boolean {
        // Проверяем минимальное количество запусков
        val appLaunches = prefs.getInt("app_launches", 0)
        if (appLaunches < MIN_APP_LAUNCHES) {
            return false
        }

        // Проверяем время в фоне
        if (backgroundTime > 0) {
            val timeInBackground = SystemClock.elapsedRealtime() - backgroundTime
            if (timeInBackground < MIN_BACKGROUND_TIME_MS) {
                return false
            }
        }

        return true
    }

    /**
     * Освобождает ресурсы рекламы
     */
    private fun clearAppOpenAd() {
        appOpenAd?.setAdEventListener(null)
        appOpenAd = null
        isShowingAd = false
        currentActivity = null
    }

    /**
     * Увеличивает счетчик запусков приложения
     */
    fun incrementAppLaunches() {
        val currentLaunches = prefs.getInt("app_launches", 0)
        prefs.edit().putInt("app_launches", currentLaunches + 1).apply()
    }

    /**
     * Вызывается, когда приложение переходит в foreground
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        
        // Если приложение было в фоне достаточно долго, показываем рекламу
        if (backgroundTime > 0) {
            val timeInBackground = SystemClock.elapsedRealtime() - backgroundTime
            if (timeInBackground >= MIN_BACKGROUND_TIME_MS) {
                currentActivity?.let { activity ->
                    if (!isShowingAd && appOpenAd != null) {
                        showAppOpenAd(activity)
                    }
                }
            }
        }
        
        // Предзагружаем рекламу для следующего раза
        loadAppOpenAd()
    }

    /**
     * Вызывается, когда приложение переходит в background
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        backgroundTime = SystemClock.elapsedRealtime()
    }

    /**
     * Устанавливает текущую активность для показа рекламы
     */
    fun setCurrentActivity(activity: Activity?) {
        currentActivity = activity
    }

    /**
     * Освобождает ресурсы при уничтожении менеджера
     */
    fun destroy() {
        clearAppOpenAd()
        appOpenAdLoader = null
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
}
