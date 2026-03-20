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
 * - При возврате из фона реклама показывается только если приложение было в фоне >= 15 секунд
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
    
    // Минимальное время в фоне перед показом рекламы (15 секунд)
    // При первом запуске реклама показывается сразу
    private val MIN_BACKGROUND_TIME_MS = 15_000L
    
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

        appOpenAd?.let { ad ->
            ad.setAdEventListener(createAdEventListener())
            currentActivity = activity
            isShowingAd = true
            Log.d("AppOpenAdManager", "Показываем App Open рекламу")
            ad.show(activity)
        }
    }

    /**
     * Публичный метод для показа рекламы с колбэком
     */
    fun showAppOpenAdWithCallback(activity: Activity, onAdClosed: () -> Unit) {
        Log.d("AppOpenAdManager", "showAppOpenAdWithCallback вызван")
        Log.d("AppOpenAdManager", "isShowingAd=$isShowingAd, appOpenAd=${appOpenAd != null}")
        
        if (isShowingAd) {
            Log.d("AppOpenAdManager", "Реклама уже показывается, сбрасываем флаг и показываем новую")
            // Сбрасываем флаг и показываем новую рекламу
            isShowingAd = false
        }

        if (appOpenAd == null) {
            Log.w("AppOpenAdManager", "Реклама не готова, загружаем новую")
            loadAppOpenAd()
            // Добавляем небольшую задержку и повторяем попытку
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                showAppOpenAdWithCallback(activity, onAdClosed)
            }, 1000)
            return
        }

        Log.d("AppOpenAdManager", "Показываем App Open рекламу с колбэком")
        appOpenAd?.let { ad ->
            ad.setAdEventListener(object : AppOpenAdEventListener {
                override fun onAdShown() {
                    Log.d("AppOpenAdManager", "Реклама показана успешно")
                    isShowingAd = false
                    loadAppOpenAd()
                }

                override fun onAdFailedToShow(adError: AdError) {
                    Log.e("AppOpenAdManager", "Ошибка показа рекламы: ${adError.description}")
                    isShowingAd = false
                    onAdClosed()
                }

                override fun onAdDismissed() {
                    Log.d("AppOpenAdManager", "Реклама закрыта пользователем")
                    isShowingAd = false
                    onAdClosed()
                    loadAppOpenAd()
                }

                override fun onAdClicked() {
                    Log.d("AppOpenAdManager", "Пользователь кликнул по рекламе")
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                    Log.d("AppOpenAdManager", "Зафиксирован показ рекламы")
                }
            })
            currentActivity = activity
            isShowingAd = true
            Log.d("AppOpenAdManager", "Показываем App Open рекламу с колбэком")
            ad.show(activity)
        } ?: run {
            Log.w("AppOpenAdManager", "Реклама не готова, загружаем новую")
            loadAppOpenAd()
            onAdClosed()
        }
    }

    /**
     * Проверяет, нужно ли показывать рекламу
     * Показываем рекламу:
     * - При первом запуске приложения
     * - После возврата из фона (через 15+ секунд)
     */
    private fun shouldShowAd(): Boolean {
        // Если недавно показывали межстраничную рекламу, пропускаем
        if (interstitialShownRecently) {
            Log.d("AppOpenAdManager", "Недавно показывали межстраничную рекламу, пропускаем App Open Ad")
            return false
        }
        
        // Если приложение было в фоне, проверяем время
        if (backgroundTime > 0) {
            val timeInBackground = SystemClock.elapsedRealtime() - backgroundTime
            Log.d("AppOpenAdManager", "Время в фоне: ${timeInBackground}ms")
            // Показываем только если было достаточно времени в фоне (15+ секунд)
            return timeInBackground >= 15_000L
        }
        
        // При первом запуске (backgroundTime = 0) показываем сразу
        Log.d("AppOpenAdManager", "Первый запуск, показываем рекламу")
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
