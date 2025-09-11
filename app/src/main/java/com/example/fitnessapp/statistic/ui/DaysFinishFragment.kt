package com.example.fitnessapp.statistic.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitnessapp.databinding.FinishBinding
import androidx.appcompat.app.ActionBar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.utils.DialogManager
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DaysFinishFragment(
) : Fragment() {
    private lateinit var binding: FinishBinding
    private var ab: ActionBar? =
        null // добавили переменную для ActionBar, будем показывать счетчик упражнений
    private val model: DaysFinishViewModel by viewModels()
    private  var difficulty = ""
    private var likeCounter = 0

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
        ab?.title = getString(R.string.Done)
        observerCurrentDayStatisitcs()
difficulty = arguments?.getString("difficulty").toString()
        model.getStatisticByDate(TimeUtils.getCurrentDate())
        model.getStatisticEvents()

        calendarDateObserver()
        workoutMonthStatisticObserver()
//        model.getWorkoutMonthStatistic()


        binding.bBack.setOnClickListener {
            findNavController()
                .popBackStack(
                    R.id.trainingFragment,
                    inclusive = false
                )

        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Ничего не делаем или можем закрыть activity
                findNavController()
                    .popBackStack(
                        R.id.trainingFragment,
                        inclusive = false
                    )
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
                    override fun onDifficultySelected(difficultyLevel: Int) {
                        if(difficultyLevel == 1){
                            model.addTrainingHarder(difficulty)
                        } else if (difficultyLevel==2){
                            model.reduceTrainingComplexity(difficulty)
                        }
                    }
                })
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



    }

