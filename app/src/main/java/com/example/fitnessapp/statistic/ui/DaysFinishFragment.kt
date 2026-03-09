package com.example.fitnessapp.statistic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitnessapp.databinding.FinishBinding
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.utils.DialogManager
import com.example.fitnessapp.utils.TimeUtils
import com.example.fitnessapp.utils.App
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DaysFinishFragment(
) : Fragment() {
    private lateinit var binding: FinishBinding
    private var ab: ActionBar? =
        null // добавили переменную для ActionBar, будем показывать счетчик упражнений
    private val model: DaysFinishViewModel by viewModels()
    private  var difficulty = ""
    private var zone: String? = null
    private var likeCounter = 0
    private var adShown = false // Флаг для предотвращения повторных показов рекламы

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FinishBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ab = (activity as AppCompatActivity).supportActionBar
        ab?.title = getString(R.string.statistic)
        ab?.setDisplayHomeAsUpEnabled(false) // Скрываем иконку "назад"
        observerCurrentDayStatisitcs()
difficulty = arguments?.getString("difficulty").toString()
        zone = arguments?.getString("zone")
        android.util.Log.d("DaysFinishFragment", "Received difficulty: $difficulty, zone: $zone")
        model.getStatisticByDate(TimeUtils.getCurrentDate())
        model.getStatisticEvents()

        calendarDateObserver()
        workoutMonthStatisticObserver()
//        model.getWorkoutMonthStatistic()

        // Показываем межстраничную рекламу с задержкой
        showInterstitialAd()


        binding.bBack.setOnClickListener {
            navigateBack()
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // При нажатии системной кнопки "назад" возвращаем на главный экран тренировок
                navigateBack()
            }
        })



        binding.happy.setOnClickListener  { with(binding) {
            if (likeCounter==0) {


                happy.setAnimation(R.raw.like_button)
                happy.repeatCount = 0
                happy.playAnimation()
                likeCounter++
            }
        }
        }

        binding.btIsBad.setOnClickListener{
            DialogManager.showAfterTrainingDialog(requireContext(),
                object : DialogManager.OnDifficultySelectedListener {
                    override fun onDifficultySelected(difficultyLevel: Int, zone: String?) {
                        if(difficultyLevel == 1){
                            model.addTrainingHarder(difficulty, zone)
                        } else if (difficultyLevel==2){
                            model.reduceTrainingComplexity(difficulty, zone)
                        }
                    }
                }, zone)
        }




    }

    private fun workoutMonthStatisticObserver(){
        model.statisticMonthData.observe(viewLifecycleOwner){ statistic->
            with(binding){
                tvTrainingSumm.text = statistic.trainingCounter.toString()
                kcalSumm2.text=(statistic.kcal.toInt().toString())+" кКал"
                tvFatSumm.text = (statistic.kcal/7).toInt().toString()+" грамм"
            }

        }
    }



    private fun calendarDateObserver() {
        model.eventListData.observe(viewLifecycleOwner) { list ->
            binding.calendarView.setEvents(list)
            /*
            Подписались на вью модел и получение событий для календаря
             */
        }
    }

    private fun observerCurrentDayStatisitcs(){
        model.statisticData.observe(viewLifecycleOwner){ statisitc->
            binding.tvKcal.text = statisitc.kcal.toInt().toString()+" кКал"
            binding.tvTime.text = (statisitc.workoutTime.toLong()/60).toString()
            binding.tvExCounter.text = statisitc.completedExercise.toString()
            binding.tvFatDay.text=(statisitc.kcal/7).toInt().toString()+" грамм"

//                arguments?.getString("tec") ?: "0"
        }
    }

    /**
     * Показывает межстраничную рекламу
     */
    private fun showInterstitialAd() {
        if (adShown) return // Не показывать рекламу повторно
        
        activity?.let { activity ->
            val interstitialManager = App.getInterstitialAdManager(activity.application)
            
            if (interstitialManager.isAdReady()) {
                interstitialManager.showAdWithDelay(activity, 1500) {
                    // Реклама закрыта, можно продолжать работу
                }
                adShown = true
            } else {
                // Если реклама не готова, предзагружаем для следующего раза
                interstitialManager.preloadAd()
            }
        }
    }

    /**
     * Обрабатывает навигацию назад
     */
    private fun navigateBack() {
        activity?.let { activity ->
            val interstitialManager = App.getInterstitialAdManager(activity.application)
            
            if (interstitialManager.isAdReady() && !adShown) {
                // Показываем рекламу перед выходом
                interstitialManager.showAd(activity) {
                    // Реклама закрыта, выполняем навигацию
                    performNavigation()
                }
                adShown = true
            } else {
                // Реклама не готова или уже показана, выполняем навигацию сразу
                performNavigation()
            }
        }
    }

    /**
     * Выполняет фактическую навигацию
     */
    private fun performNavigation() {
        findNavController()
            .popBackStack(
                R.id.trainingFragment,
                inclusive = false
            )
    }

}
