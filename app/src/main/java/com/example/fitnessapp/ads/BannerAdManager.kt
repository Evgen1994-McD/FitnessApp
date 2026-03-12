package com.example.fitnessapp.ads

import android.app.Activity
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.banner.ClosableBannerAdEventListener

/**
 * Менеджер для управления баннером (Banner Ad)
 * 
 * Логика показа:
 * - Реклама предзагружается при инициализации приложения
 * - Показывается как полноэкранный баннер после завершения тренировки
 * - Имеет кнопку закрытия для пользователя
 */
class BannerAdManager(
    private val application: android.app.Application,
    private val adUnitId: String
) {

    private var bannerAdView: BannerAdView? = null
    private var isShowingAd = false
    private var isAdLoaded = false
    
    private val prefs: SharedPreferences = 
        application.getSharedPreferences("banner_ad_prefs", android.content.Context.MODE_PRIVATE)
    
    private val handler = Handler(Looper.getMainLooper())
    
    init {
        preloadAd()
    }

    /**
     * Предзагружает рекламу для будущего показа
     */
    fun preloadAd() {
        if (isAdLoaded) {
            Log.d("BannerAdManager", "Баннер уже загружен, пропускаем предзагрузку")
            return
        }

        Log.d("BannerAdManager", "Начинаем предзагрузку баннера с ID: $adUnitId")
        
        try {
            // Создаем BannerAdView
            bannerAdView = BannerAdView(application).apply {
                setAdUnitId(adUnitId)
                setAdSize(BannerAdSize.inlineSize(application, Int.MAX_VALUE, Int.MAX_VALUE))
                
                // Устанавливаем слушатель событий
                setBannerAdEventListener(createAdEventListener())
            }
            
            isAdLoaded = true
            Log.d("BannerAdManager", "Баннер успешно создан и готов к показу")
            
        } catch (e: Exception) {
            Log.e("BannerAdManager", "Ошибка при создании баннера: ${e.message}", e)
            isAdLoaded = false
            bannerAdView = null
        }
    }

    private fun createAdEventListener(): ClosableBannerAdEventListener {
        return object : ClosableBannerAdEventListener {
            override fun onAdLoaded() {
                Log.d("BannerAdManager", "Баннер успешно загружен")
                isAdLoaded = true
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                Log.e("BannerAdManager", "Ошибка загрузки баннера: ${error.code} - ${error.description}")
                isAdLoaded = false
                clearAd()
            }

            override fun onImpression(impressionData: ImpressionData?) {
                Log.d("BannerAdManager", "Зафиксирован показ баннера")
            }

            override fun closeBannerAd() {
                Log.d("BannerAdManager", "Баннер закрыт через closeBannerAd()")
                isShowingAd = false
                clearAd()
                preloadAd()
            }

            override fun onAdClicked() {
                Log.d("BannerAdManager", "Пользователь кликнул по баннеру")
            }

            override fun onLeftApplication() {
                Log.d("BannerAdManager", "Пользователь покинул приложение")
            }

            override fun onReturnedToApplication() {
                Log.d("BannerAdManager", "Пользователь вернулся в приложение")
            }
        }
    }

    /**
     * Показывает баннер с задержкой
     */
    fun showAdWithDelay(
        activity: Activity, 
        fragmentManager: FragmentManager,
        delayMs: Long = 1500, 
        onAdClosed: (() -> Unit)? = null
    ) {
        handler.postDelayed({
            showAd(activity, fragmentManager, onAdClosed)
        }, delayMs)
    }

    /**
     * Показывает баннер как полноэкранный
     */
    fun showAd(
        activity: Activity, 
        fragmentManager: FragmentManager,
        onAdClosed: (() -> Unit)? = null
    ) {
        if (isShowingAd) {
            Log.d("BannerAdManager", "Баннер уже показывается, пропускаем")
            onAdClosed?.invoke()
            return
        }

        if (!isAdLoaded || bannerAdView == null) {
            Log.w("BannerAdManager", "Баннер не загружен")
            onAdClosed?.invoke()
            preloadAd()
            return
        }

        Log.d("BannerAdManager", "Показываем баннер на активности: ${activity.javaClass.simpleName}")
        isShowingAd = true

        try {
            Log.d("BannerAdManager", "Создаем BannerAdFragment")
            Log.d("BannerAdManager", "FragmentManager: ${fragmentManager.javaClass.simpleName}")
            Log.d("BannerAdManager", "Activity: ${activity.javaClass.simpleName}")
            
            val bannerFragment = BannerAdFragment.newInstance(onAdClosed)
            bannerFragment.show(fragmentManager, "BannerAdFragment")
            Log.d("BannerAdManager", "✅ BannerAdFragment.show() вызван успешно")
        } catch (e: Exception) {
            Log.e("BannerAdManager", "❌ Исключение при показе баннера: ${e.message}", e)
            isShowingAd = false
            onAdClosed?.invoke()
            preloadAd()
        }
    }

    /**
     * Проверяет, загружена ли реклама
     */
    fun isAdReady(): Boolean {
        return isAdLoaded && bannerAdView != null
    }

    /**
     * Возвращает загруженный баннер для показа во фрагменте
     */
    fun getAdView(): BannerAdView? {
        return bannerAdView
    }

    /**
     * Освобождает ресурсы рекламы
     */
    private fun clearAd() {
        if (bannerAdView != null) {
            bannerAdView?.setBannerAdEventListener(null)
            bannerAdView = null
        }
        isAdLoaded = false
        isShowingAd = false
    }

    /**
     * Освобождает ресурсы при уничтожении менеджера
     */
    fun destroy() {
        clearAd()
        handler.removeCallbacksAndMessages(null)
    }
}
