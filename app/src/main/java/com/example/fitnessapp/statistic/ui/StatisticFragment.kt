package com.example.fitnessapp.statistic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.utils.DialogManager
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
                    val weigthDialogueState by remember { mutableStateOf(false) }
                    // Получаем данные из StateFlow
                    val eventList by viewModel.eventListData.collectAsState()
                    val weightList by viewModel.weightListData.collectAsState()
                    val statisticData by viewModel.statisticData.collectAsState()
                    val selectedYear by viewModel.selectedYear.collectAsState()
                    val selectedMonth by viewModel.selectedMonth.collectAsState()

                    // Передача данных в экран статистики
                    StatisticScreen(
                        date = statisticData?.date ?: TimeUtils.getCurrentDate(),
                        eventList = eventList,
                        weightList = weightList,
                        statisticData = statisticData,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        onDayClick = { selectedDate -> 
                            viewModel.getStatisticByDate(selectedDate)
                        },
                        onWeightClick = { weightModel ->
                            DialogManager.showWeightDialog(
                                requireContext(),
                                object : DialogManager.WeightListener {
                                    override fun onClick(weight: String) {
                                        if (weight.isNotEmpty()) {
                                            try {
                                                viewModel.updateWeight(weightModel.copy(
                                                    weight = weight.toDouble()
                                                ))
                                            } catch (e: NumberFormatException) {
                                                // Обработка ошибки формата
                                            }
                                        }
                                    }
                                },
                                String.format("%.1f", weightModel.weight)
                            )
                        },
                        addWeightClick = { weight -> viewModel.saveWeight(weight) },
                        onYearChange = { year -> viewModel.updateYear(year) },
                        onMonthChange = { month -> viewModel.updateMonth(month) }
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
        viewModel.getWeightByYearAndMonth()
    }

}