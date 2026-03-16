package com.example.fitnessapp.ads

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.fitnessapp.databinding.FragmentBannerAdBinding
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.AdRequest
import kotlin.math.roundToInt

/**
 * Диалоговый фрагмент для показа полноэкранного баннера
 */
class BannerAdFragment : DialogFragment() {

    private var _binding: FragmentBannerAdBinding? = null
    private val binding get() = _binding!!
    private var onAdClosed: (() -> Unit)? = null

    companion object {
        fun newInstance(onAdClosed: (() -> Unit)? = null): BannerAdFragment {
            val fragment = BannerAdFragment()
            fragment.onAdClosed = onAdClosed
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BannerAdFragment", "✅ onCreate вызван")
        // Устанавливаем стиль для полноэкранного показа
        setStyle(STYLE_NORMAL, android.R.style.Theme_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("BannerAdFragment", "✅ onCreateView вызван")
        _binding = FragmentBannerAdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("BannerAdFragment", "✅ onViewCreated вызван")
        
        // Устанавливаем полноэкранный режим
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }

        setupBanner()
        setupCloseButton()
    }

    private fun setupBanner() {
        try {
            Log.d("BannerAdFragment", "Начинаем setupBanner")
            
            val activity = requireActivity()
            val bannerManager = com.example.fitnessapp.utils.App.getBannerAdManager(activity.application)
            
            Log.d("BannerAdFragment", "BannerAdManager готов: ${bannerManager.isAdReady()}")
            
            // Скрываем кнопку закрытия изначально
            binding.closeButton.visibility = View.GONE
            
            // Рассчитываем размер баннера на весь экран
            val screenHeight = resources.displayMetrics.run { heightPixels / density }.roundToInt()
            val adWidthPixels = resources.displayMetrics.widthPixels
            val adWidth = (adWidthPixels / resources.displayMetrics.density).roundToInt()
            val maxAdHeight = screenHeight // Используем всю высоту экрана
            val adSize = BannerAdSize.inlineSize(requireContext(), adWidth, maxAdHeight)
            
            Log.d("BannerAdFragment", "Размер баннера: ${adWidth}x${maxAdHeight}")
            
            // Создаем BannerAdView как в документации
            val bannerAd = BannerAdView(requireContext()).apply {
                setAdSize(adSize)
                setAdUnitId(com.example.fitnessapp.utils.App.getBannerAdUnitId()) // Берем ID из App.kt
                
                setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        Log.d("BannerAdFragment", "✅ Баннер успешно загружен!")
                        // Показываем кнопку закрытия через 2 секунды после загрузки
                        showCloseButtonWithDelay()
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        Log.e("BannerAdFragment", "❌ Ошибка загрузки баннера: ${adRequestError.code} - ${adRequestError.description}")
                        // В случае ошибки все равно показываем кнопку закрытия через 2 секунды
                        showCloseButtonWithDelay()
                    }

                    override fun onAdClicked() {
                        Log.d("BannerAdFragment", "👆 Пользователь кликнул по баннеру")
                    }

                    override fun onLeftApplication() {
                        Log.d("BannerAdFragment", "📱 Пользователь покинул приложение")
                    }

                    override fun onReturnedToApplication() {
                        Log.d("BannerAdFragment", "📱 Пользователь вернулся в приложение")
                    }

                    override fun onImpression(impressionData: ImpressionData?) {
                        Log.d("BannerAdFragment", "📊 Зафиксирован показ баннера")
                    }
                })
                
                // Загружаем рекламу с контекстом для фитнес-приложения на русском языке
                loadAd(
                    AdRequest.Builder()
                        .setAge("25") // Возраст пользователя
                        .setContextQuery("Программа тренировок для фитнеса и здоровья") // Поисковый запрос пользователя
                        .setContextTags(listOf(
                            "фитнес",           // Фитнес тематика
                            "тренировки",       // Тренировки
                            "здоровье",          // Здоровье
                            "спорт",             // Спорт
                            "спортзал",          // Спортзал
                            "упражнения",        // Упражнения
                            "программа тренировок", // Программа тренировок
                            "силовые тренировки",  // Силовые тренировки
                            "кардио",            // Кардио
                            "набор массы",       // Набор массы
                            "похудение",         // Похудение
                            "здоровый образ жизни", // Здоровый образ жизни
                        "Купить спортивное питание",
                            "Купить квартиру в Москве",
                            "Купить абонемент в спортзал",
                            "Купить протеин и спортивное питание"
                            ))
                        .build()
                )
            }
            
            // Добавляем баннер в контейнер
            binding.bannerContainer.addView(bannerAd)
            Log.d("BannerAdFragment", "BannerAdView добавлен в контейнер")
            
            // Устанавливаем параметры
            bannerAd.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            
            Log.d("BannerAdFragment", "Параметры установлены, контейнер children: ${binding.bannerContainer.childCount}")
            
        } catch (e: Exception) {
            Log.e("BannerAdFragment", "Ошибка при установке баннера: ${e.message}", e)
            dismiss()
        }
    }

    private fun showCloseButtonWithDelay() {
        // Показываем кнопку закрытия через 2 секунды
        binding.closeButton.postDelayed({
            try {
                binding.closeButton.visibility = View.VISIBLE
                Log.d("BannerAdFragment", "✅ Кнопка закрытия показана через 2 секунды")
            } catch (e: Exception) {
                Log.e("BannerAdFragment", "Ошибка при показе кнопки закрытия: ${e.message}")
            }
        }, 2000) // 2 секунды
    }

    private fun setupCloseButton() {
        binding.closeButton.setOnClickListener {
            android.util.Log.d("BannerAdFragment", "Пользователь нажал кнопку закрытия баннера")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onAdClosed?.invoke()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        // Устанавливаем полноэкранный режим при старте
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
}
