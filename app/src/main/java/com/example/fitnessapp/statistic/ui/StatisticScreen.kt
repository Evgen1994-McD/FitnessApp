package com.example.fitnessapp.statistic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.ui.components.AddWeightDialogue
import com.example.fitnessapp.statistic.ui.components.CalendarView
import com.example.fitnessapp.statistic.ui.components.DateSelector
import com.example.fitnessapp.statistic.ui.components.MpAndroidChart
import com.example.fitnessapp.statistic.ui.components.HorizontalCalendar
import com.example.fitnessapp.statistic.ui.components.BMICard
import com.example.fitnessapp.statistic.ui.components.WeeklyCaloriesChart
import com.example.fitnessapp.statistic.ui.components.WorkoutHistoryCard
import com.example.fitnessapp.statistic.ui.components.UpdateBodyMetricsBottomSheet
import com.example.fitnessapp.statistic.ui.models.BMIModel
import com.example.fitnessapp.statistic.ui.models.WorkoutHistoryModel
import com.example.fitnessapp.statistic.ui.models.DayCalendarModel
import com.example.fitnessapp.statistic.ui.models.WeeklyCaloriesModel
import com.example.fitnessapp.statistic.ui.models.MonthlyCaloriesModel
import com.example.fitnessapp.statistic.ui.models.CalendarPeriod
import com.example.fitnessapp.statistic.ui.components.TopSheetCalendar
import com.example.fitnessapp.ui.theme.baseBlue
import com.example.fitnessapp.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewStatisticScreen(
    bmiData: BMIModel?,
    workoutHistory: List<WorkoutHistoryModel>,
    weeklyCalories: List<WeeklyCaloriesModel>,
    monthlyCalories: List<MonthlyCaloriesModel>,
    calendarPeriod: CalendarPeriod,
    calendarDays: List<DayCalendarModel>,
    showTopSheetCalendar: Boolean,
    eventList: List<com.applandeo.materialcalendarview.EventDay>,
    filterText: String,
    onCalendarDayClick: (DayCalendarModel) -> Unit,
    onWorkoutToggle: (Int) -> Unit,
    onCycleWorkoutFilter: () -> Unit,
    onAddWeight: () -> Unit,
    onUpdateBodyMetrics: (height: Double, weight: Double) -> Unit,
    onShowCalendar: () -> Unit,
    onCalendarDismiss: () -> Unit,
    onDateSelected: (java.time.LocalDate) -> Unit,
    onTogglePeriod: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val showBottomSheet = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {

                },
                actions = {
                    IconButton(
                        onClick = { onShowCalendar() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Календарь",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF10B981),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet.value = true },
                containerColor = Color(0xFF10B981), // green
                modifier = Modifier.padding(end = 16.dp, bottom = 24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_weight_24),
                    contentDescription = "Обновить данные",
                    tint = Color.White
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Горизонтальный календарь
            HorizontalCalendar(
                days = calendarDays,
                onDayClick = onCalendarDayClick
            )

            // Карточка ИМТ
            BMICard(bmiModel = bmiData, onAddWeight = { showBottomSheet.value = true })

            // График сожженных калорий
            WeeklyCaloriesChart(
                weeklyData = weeklyCalories,
                monthlyData = monthlyCalories,
                calendarPeriod = calendarPeriod,
                onPeriodClick = onTogglePeriod
            )

            // История тренировок
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "История тренировок",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = filterText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = baseBlue,
                        modifier = Modifier.clickable { onCycleWorkoutFilter() }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (workoutHistory.isEmpty()) {
                    // Показываем сообщение когда нет тренировок
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (filterText) {
                                "За день" -> "На выбранный день тренировок нет"
                                "За неделю" -> "На выбранную неделю тренировок нет"
                                else -> "Тренировок пока нет"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    workoutHistory.forEach { workout ->
                        WorkoutHistoryCard(
                            workout = workout,
                            onToggleExpand = { onWorkoutToggle(workout.id ?: 0) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }

    // TopSheet календарь
    TopSheetCalendar(
        isVisible = showTopSheetCalendar,
        onDismiss = onCalendarDismiss,
        onDateSelected = onDateSelected,
        eventList = eventList
    )

    // Bottom Sheet для обновления данных тела
    if (showBottomSheet.value) {
        UpdateBodyMetricsBottomSheet(
            currentBMI = bmiData,
            onDismiss = { showBottomSheet.value = false },
            onSave = { height, weight ->
                onUpdateBodyMetrics(height, weight)
            }
        )
    }
}