package com.fit.fitnessapp.ads

import android.app.Activity
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

/**
 * Менеджер для управления межстраничной рекламой (Interstitial Ad)
 * 
 * Логика показа:
 * - Реклама предзагружается при инициализации приложения
 * - Показывается сразу при переходе на экран завершения тренировки
 */
class InterstitialAdManager(
    private val application: android.app.Application,
    private val adUnitId: String
) {

    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdLoader: InterstitialAdLoader? = null
    private var isShowingAd = false
    private var isAdLoaded = false
    
    private val prefs: SharedPreferences = 
        application.getSharedPreferences("interstitial_ad_prefs", android.content.Context.MODE_PRIVATE)
    
    private val handler = Handler(Looper.getMainLooper())
    
    init {
        initializeAdLoader()
        preloadAd()
    }

    private fun initializeAdLoader() {
        interstitialAdLoader = InterstitialAdLoader(application).apply {
            setAdLoadListener(createAdLoadListener())
        }
    }

    private fun createAdLoadListener(): com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener {
        return object : com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener {
            override fun onAdLoaded(loadedAd: InterstitialAd) {
                Log.d("InterstitialAdManager", "Межстраничная реклама успешно загружена")
                interstitialAd?.setAdEventListener(null)
                interstitialAd = loadedAd
                isAdLoaded = true
            }

            override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                Log.e("InterstitialAdManager", "Ошибка загрузки межстраничной рекламы: ${adRequestError.code} - ${adRequestError.description}")
                isAdLoaded = false
                interstitialAd = null
            }
        }
    }

    private fun createAdEventListener(): InterstitialAdEventListener {
        return object : InterstitialAdEventListener {
            override fun onAdShown() {
                Log.d("InterstitialAdManager", "Межстраничная реклама показана успешно")
                
                // Уведомляем App Open Ad Manager что показывали межстраничную рекламу
                try {
                    val appOpenManager = com.fit.fitnessapp.utils.App.getAppOpenAdManager(application)
                    appOpenManager.setInterstitialShown()
                } catch (e: Exception) {
                    Log.w("InterstitialAdManager", "Не удалось уведомить App Open Ad Manager", e)
                }
                
                // После показа рекламы сбрасываем её и загружаем новую
                clearAd()
                preloadAd()
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.e("InterstitialAdManager", "Ошибка показа межстраничной рекламы: ${adError.description}")
                isShowingAd = false
                // При ошибке также сбрасываем рекламу и загружаем новую
                clearAd()
                preloadAd()
            }

            override fun onAdDismissed() {
                Log.d("InterstitialAdManager", "Межстраничная реклама закрыта пользователем")
                isShowingAd = false
                // После закрытия загружаем новую рекламу
                clearAd()
                preloadAd()
            }

            override fun onAdClicked() {
                Log.d("InterstitialAdManager", "Пользователь кликнул по межстраничной рекламе")
            }

            override fun onAdImpression(impressionData: ImpressionData?) {
                Log.d("InterstitialAdManager", "Зафиксирован показ межстраничной рекламы")
            }
        }
    }

    /**
     * Предзагружает рекламу для будущего показа
     */
    fun preloadAd() {
        if (isAdLoaded) {
            Log.d("InterstitialAdManager", "Реклама уже загружена, пропускаем предзагрузку")
            return
        }

        Log.d("InterstitialAdManager", "Начинаем предзагрузку межстраничной рекламы с ID: $adUnitId")
        val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()
        interstitialAdLoader?.loadAd(adRequestConfiguration)
    }

    /**
     * Показывает рекламу с задержкой
     */
    fun showAdWithDelay(activity: Activity, delayMs: Long = 1500, onAdClosed: (() -> Unit)? = null) {
        handler.postDelayed({
            showAd(activity, onAdClosed)
        }, delayMs)
    }

    /**
     * Показывает межстраничную рекламу
     */
    fun showAd(activity: Activity, onAdClosed: (() -> Unit)? = null) {
        if (isShowingAd) {
            Log.d("InterstitialAdManager", "Реклама уже показывается, пропускаем")
            onAdClosed?.invoke()
            return
        }

        // Проверяем, не показывается ли уже App Open Ad
        try {
            val appOpenManager = com.fit.fitnessapp.utils.App.getAppOpenAdManager(activity.application)
            if (appOpenManager.isShowingAd()) {
                Log.d("InterstitialAdManager", "App Open Ad уже показывается, пропускаем Interstitial Ad")
                onAdClosed?.invoke()
                return
            }
        } catch (e: Exception) {
            Log.w("InterstitialAdManager", "Не удалось проверить состояние App Open Ad", e)
        }

        val ad = interstitialAd ?: run {
            Log.w("InterstitialAdManager", "Реклама не загружена")
            onAdClosed?.invoke()
            preloadAd() // Пробуем загрузить для следующего раза
            return
        }

        Log.d("InterstitialAdManager", "Показываем межстраничную рекламу на активности: ${activity.javaClass.simpleName}")

        ad.setAdEventListener(createAdEventListener())
        isShowingAd = true

        try {
            ad.show(activity)
            Log.d("InterstitialAdManager", "Вызван метод show() для межстраничной рекламы")
        } catch (e: Exception) {
            Log.e("InterstitialAdManager", "Исключение при показе межстраничной рекламы: ${e.message}", e)
            isShowingAd = false // Сбрасываем флаг при ошибке!
            onAdClosed?.invoke()
            preloadAd()
        }
    }

    /**
     * Проверяет, загружена ли реклама
     */
    fun isAdReady(): Boolean {
        return isAdLoaded && interstitialAd != null
    }

    /**
     * Освобождает ресурсы рекламы
     */
    private fun clearAd() {
        interstitialAd?.setAdEventListener(null)
        interstitialAd = null
        isAdLoaded = false
        isShowingAd = false
    }

    /**
     * Освобождает ресурсы при уничтожении менеджера
     */
    fun destroy() {
        clearAd()
        interstitialAdLoader = null
        handler.removeCallbacksAndMessages(null)
    }
}
