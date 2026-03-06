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
import com.example.fitnessapp.statistic.ui.models.MonthlyCaloriesModel
import com.example.fitnessapp.statistic.ui.models.CalendarPeriod
import com.example.fitnessapp.statistic.ui.models.BMIModel
import com.example.fitnessapp.statistic.ui.models.WorkoutHistoryModel
import com.example.fitnessapp.statistic.ui.models.WeeklyCaloriesModel
import com.example.fitnessapp.statistic.ui.models.DayCalendarModel
import com.applandeo.materialcalendarview.EventDay
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
                    val bmiData by viewModel.bmiData.collectAsState<BMIModel?>()
                    val workoutHistory by viewModel.workoutHistory.collectAsState<List<WorkoutHistoryModel>>()
                    val weeklyCalories by viewModel.weeklyCalories.collectAsState<List<WeeklyCaloriesModel>>()
                    val monthlyCalories by viewModel.monthlyCalories.collectAsState<List<MonthlyCaloriesModel>>()
                    val calendarDays by viewModel.calendarDays.collectAsState<List<DayCalendarModel>>()
                    val showTopSheetCalendar by viewModel.showTopSheetCalendar.collectAsState<Boolean>()
                    val eventList by viewModel.eventListData.collectAsState<List<EventDay>>()
                    val calendarPeriod by viewModel.calendarPeriod.collectAsState<CalendarPeriod>()

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
                        onCalendarDayClick = { day ->
                            viewModel.onCalendarDayClick(day)
                        },
                        onWorkoutToggle = { workoutId ->
                            viewModel.toggleWorkoutExpanded(workoutId)
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
