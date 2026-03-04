package com.example.fitnessapp.statistic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticFragment : Fragment() {
    private val viewModel: StatisticViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(
                    lifecycleOwner = this@StatisticFragment
                )
            )
            setContent {
                FitnessAppTheme {
                    // Получаем данные из StateFlow
                    val bmiData by viewModel.bmiData.collectAsState()
                    val workoutHistory by viewModel.workoutHistory.collectAsState()
                    val weeklyCalories by viewModel.weeklyCalories.collectAsState()
                    val calendarDays by viewModel.calendarDays.collectAsState()

                    // Загружаем новые данные при первом запуске
                    LaunchedEffect(Unit) {
                        viewModel.loadNewStatisticsData()
                    }

                    // Используем только новый дизайн экрана статистики
                    NewStatisticScreen(
                        bmiData = bmiData,
                        workoutHistory = workoutHistory,
                        weeklyCalories = weeklyCalories,
                        calendarDays = calendarDays,
                        onCalendarDayClick = { day ->
                            viewModel.onCalendarDayClick(day)
                        },
                        onWorkoutToggle = { workoutId ->
                            viewModel.toggleWorkoutExpanded(workoutId)
                        },
                        onAddWeight = {
//                            viewModel.showAddWeightDialog()
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Устанавливаем заголовок
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Статистика"
        
        // Загружаем данные при создании фрагмента
        viewModel.getStatisticEvents()
        viewModel.getStatisticByDate(TimeUtils.getCurrentDate())
    }
}
