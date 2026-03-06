package com.example.fitnessapp.statistic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
                    val workoutHistory by viewModel.filteredWorkoutHistory.collectAsState()
                    val weeklyCalories by viewModel.weeklyCalories.collectAsState()
                    val monthlyCalories by viewModel.monthlyCalories.collectAsState()
                    val calendarPeriod by viewModel.calendarPeriod.collectAsState()
                    val calendarDays by viewModel.calendarDays.collectAsState()
                    val showTopSheetCalendar by viewModel.showTopSheetCalendar.collectAsState()
                    val eventList by viewModel.eventListData.collectAsState()
                    val workoutFilterType by viewModel.workoutFilterType.collectAsState()
                    val filterText = when (workoutFilterType) {
                        WorkoutFilterType.ALL -> "Все"
                        WorkoutFilterType.WEEK -> "За неделю"
                        WorkoutFilterType.DAY -> "За день"
                    }

                    // Загружаем новые данные при первом запуске
                    LaunchedEffect(Unit) {
                        viewModel.loadNewStatisticsData()
                    }

                    // Используем только новый дизайн экрана статистики
                    NewStatisticScreen(
                        bmiData = bmiData,
                        workoutHistory = workoutHistory,
                        weeklyCalories = weeklyCalories,
                        monthlyCalories = monthlyCalories,
                        calendarPeriod = calendarPeriod,
                        calendarDays = calendarDays,
                        showTopSheetCalendar = showTopSheetCalendar,
                        eventList = eventList,
                        filterText = filterText,
                        onCalendarDayClick = { day ->
                            viewModel.onCalendarDayClick(day)
                        },
                        onWorkoutToggle = { workoutId ->
                            viewModel.toggleWorkoutExpanded(workoutId)
                        },
                        onCycleWorkoutFilter = {
                            viewModel.cycleWorkoutFilterSafe()
                        },
                        onAddWeight = {
                            showAddWeightDialog(context, viewModel)
                        },
                        onUpdateBodyMetrics = { height, weight ->
                            viewModel.updateBodyMetrics(height, weight)
                        },
                        onShowCalendar = {
                            viewModel.showTopSheetCalendar()
                        },
                        onCalendarDismiss = {
                            viewModel.hideTopSheetCalendar()
                        },
                        onDateSelected = { selectedDate ->
                            viewModel.onDateSelected(selectedDate)
                        },
                        onTogglePeriod = {
                            viewModel.toggleCalendarPeriod()
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
    
    private fun showAddWeightDialog(context: android.content.Context, viewModel: StatisticViewModel) {
        // Здесь можно показать диалог для добавления веса
        // Реализация зависит от ваших требований
    }
}
