package com.example.fitnessapp.ads

import android.app.Activity
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log
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
 * Логика показа:
 * - При первом запуске реклама показывается сразу после загрузки
 * - При возврате из фона реклама показывается только если приложение было в фоне >= 30 секунд
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
    
    // Флаг что межстраничная реклама показывалась недавно
    private var interstitialShownRecently = false
    
    // Время когда реклама была закрыта (чтобы не показывать сразу снова)
    private var adDismissedTime: Long = 0
    
    // Время последней попытки показа (чтобы не показывать несколько раз подряд)
    private var lastShowAttemptTime: Long = 0
    
    // Минимальное время в фоне перед показом рекламы (30 секунд)
    // При первом запуске реклама показывается сразу
    private val MIN_BACKGROUND_TIME_MS = 25_000L
    
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
                Log.d("AppOpenAdManager", "Реклама успешно загружена")
                // Реклама успешно загружена
                appOpenAd?.setAdEventListener(null)
                appOpenAd = loadedAd
                
                // Показываем рекламу сразу после загрузки, если активность готова
                if (currentActivity != null) {
                    Log.d("AppOpenAdManager", "Активность установлена, проверяем условия показа")
                    if (!isShowingAd && shouldShowAd()) {
                        Log.d("AppOpenAdManager", "Условия выполнены, показываем рекламу")
                        showAppOpenAd(currentActivity!!)
                    } else {
                        Log.d("AppOpenAdManager", "Условия не выполнены: isShowingAd=$isShowingAd, shouldShowAd=${shouldShowAd()}")
                    }
                } else {
                    Log.d("AppOpenAdManager", "Активность не установлена, реклама будет показана позже")
                }
            }

            override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                Log.e("AppOpenAdManager", "Ошибка загрузки рекламы: ${adRequestError.code} - ${adRequestError.description}")
                // Ошибка загрузки рекламы
                // Не рекомендуется загружать новую рекламу сразу после ошибки
                appOpenAd = null
            }
        }
    }

    private fun createAdEventListener(): AppOpenAdEventListener {
        return object : AppOpenAdEventListener {
            override fun onAdShown() {
                Log.d("AppOpenAdManager", "Реклама показана успешно")
                // Сбрасываем флаг после успешного показа
                isShowingAd = false
                // Предзагружаем следующую рекламу для будущего использования
                loadAppOpenAd()
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.e("AppOpenAdManager", "Ошибка показа рекламы: ${adError.description} - ${adError.description}")
                // Ошибка показа рекламы
                isShowingAd = false // Сбрасываем флаг при ошибке!
                clearAppOpenAd()
                loadAppOpenAd()
            }

            override fun onAdDismissed() {
                Log.d("AppOpenAdManager", "Реклама закрыта пользователем")
                // Реклама закрыта пользователем
                adDismissedTime = SystemClock.elapsedRealtime()
                // НЕ сбрасываем активность - она остаётся той же
                // Предзагружаем следующую рекламу
                loadAppOpenAd()
            }

            override fun onAdClicked() {
                Log.d("AppOpenAdManager", "Пользователь кликнул по рекламе")
            }

            override fun onAdImpression(impressionData: ImpressionData?) {
                Log.d("AppOpenAdManager", "Зафиксирован показ рекламы")
            }
        }
    }

    /**
     * Загружает рекламу при открытии приложения
     */
    fun loadAppOpenAd() {
        if (appOpenAd != null || isShowingAd) {
            Log.d("AppOpenAdManager", "Пропускаем загрузку: appOpenAd=${appOpenAd != null}, isShowingAd=$isShowingAd")
            return
        }

        Log.d("AppOpenAdManager", "Начинаем загрузку рекламы с ID: $adUnitId")
        val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()
        appOpenAdLoader?.loadAd(adRequestConfiguration)
    }

    /**
     * Показывает рекламу при открытии приложения
     */
    private fun showAppOpenAd(activity: Activity) {
        if (isShowingAd) {
            Log.d("AppOpenAdManager", "Реклама уже показывается, пропускаем")
            return
        }

        // Проверяем, не пытались ли показать только что
        val currentTime = SystemClock.elapsedRealtime()
        if (lastShowAttemptTime > 0) {
            val timeSinceLastAttempt = currentTime - lastShowAttemptTime
            if (timeSinceLastAttempt < 20000) { // 10 секунд
                Log.d("AppOpenAdManager", "Пытались показать рекламу только что, пропускаем")
                return
            }
        }

        // Проверяем, не закрылась ли реклама только что (защита от повторного показа)
        if (adDismissedTime > 0) {
            val timeSinceDismissed = currentTime - adDismissedTime
            if (timeSinceDismissed < 30000) { // 10 секунд
                Log.d("AppOpenAdManager", "Реклама закрыта только что, пропускаем показ")
                return
            }
        }

        val ad = appOpenAd ?: run {
            Log.w("AppOpenAdManager", "Реклама не загружена")
            loadAppOpenAd()
            return
        }

        Log.d("AppOpenAdManager", "Показываем рекламу при открытии приложения")
        lastShowAttemptTime = currentTime // Запоминаем время попытки
        ad.setAdEventListener(createAdEventListener())
        isShowingAd = true
        
        try {
            ad.show(activity)
            Log.d("AppOpenAdManager", "Вызван метод show() для рекламы")
            
            // Сбрасываем флаг по таймауту если реклама не показалась
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.postDelayed({
                if (isShowingAd) {
                    Log.d("AppOpenAdManager", "Реклама не показалась за 5 секунд, сбрасываем флаг")
                    isShowingAd = false
                }
            }, 5000) // 5 секунд
            
        } catch (e: Exception) {
            Log.e("AppOpenAdManager", "Исключение при показе рекламы: ${e.message}", e)
            isShowingAd = false // Сбрасываем флаг при ошибке!
            clearAppOpenAd()
            loadAppOpenAd()
        }
    }



    /**
     * Проверяет, нужно ли показывать рекламу
     * Показываем рекламу:
     * - При первом запуске приложения
     * - После возврата из фона (через 50 секунд)
     */
    private fun shouldShowAd(): Boolean {
        // Если недавно показывали межстраничную рекламу, пропускаем
        if (interstitialShownRecently) {
            Log.d("AppOpenAdManager", "Недавно показывали межстраничную рекламу, пропускаем App Open Ad")
            return false
        }
        
        // Если приложение было в фоне, проверяем время
        if (backgroundTime > 10_000L) {
            val timeInBackground = SystemClock.elapsedRealtime() - backgroundTime
            // Показываем только если было достаточно времени в фоне (30 секунд)
            return timeInBackground >= 30_000L
        }
        
        // При первом запуске (backgroundTime = 0) показываем сразу
        return true
    }

    /**
     * Устанавливает флаг что межстраничная реклама показана
     */
    fun setInterstitialShown() {
        interstitialShownRecently = true
        Log.d("AppOpenAdManager", "Установлен флаг interstitialShownRecently")
        
        // Сбрасываем флаг через 5 минут
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed({
            interstitialShownRecently = false
            Log.d("AppOpenAdManager", "Сброшен флаг interstitialShownRecently")
        }, 5 * 60 * 1000) // 5 минут
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
        Log.d("AppOpenAdManager", "onStart вызван, backgroundTime=$backgroundTime")
        
        // Показываем рекламу, если она загружена и условия выполнены
        currentActivity?.let { activity ->
            Log.d("AppOpenAdManager", "Проверяем условия показа: isShowingAd=$isShowingAd, appOpenAd=${appOpenAd != null}, shouldShowAd=${shouldShowAd()}")
            if (!isShowingAd && appOpenAd != null && shouldShowAd()) {
                showAppOpenAd(activity)
            }
        } ?: Log.d("AppOpenAdManager", "Активность не установлена")
        
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
        Log.d("AppOpenAdManager", "Установлена активность: ${activity?.javaClass?.simpleName ?: "null"}")
        
        // НЕ сбрасываем флаг isShowingAd - активность не меняется
        // НЕ сбрасываем активность - она остаётся той же
        
        // Проверяем, не закрылась ли реклама только что
        if (adDismissedTime > 0) {
            val timeSinceDismissed = SystemClock.elapsedRealtime() - adDismissedTime
            if (timeSinceDismissed < 15000) { // 15 секунд
                Log.d("AppOpenAdManager", "Реклама закрыта только что, пропускаем показ при установке активности")
                return
            }
        }
        
        // Если реклама уже загружена и активность установлена, пытаемся показать
        if (activity != null && appOpenAd != null && !isShowingAd && shouldShowAd()) {
            Log.d("AppOpenAdManager", "Реклама загружена, активность установлена, показываем")
            showAppOpenAd(activity)
        }
    }

    /**
     * Проверяет, показывается ли реклама сейчас
     */
    fun isShowingAd(): Boolean {
        return isShowingAd
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
