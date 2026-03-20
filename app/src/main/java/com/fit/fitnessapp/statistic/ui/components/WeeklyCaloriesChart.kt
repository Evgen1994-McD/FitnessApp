package com.fit.fitnessapp.statistic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fit.fitnessapp.statistic.ui.models.WeeklyCaloriesModel
import com.fit.fitnessapp.statistic.ui.models.MonthlyCaloriesModel
import com.fit.fitnessapp.statistic.ui.models.CalendarPeriod
import com.fit.fitnessapp.ui.theme.baseGray

@Composable
fun WeeklyCaloriesChart(
    weeklyData: List<WeeklyCaloriesModel>,
    monthlyData: List<MonthlyCaloriesModel> = emptyList(),
    calendarPeriod: CalendarPeriod = CalendarPeriod.WEEK,
    onPeriodClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val maxCalories = when (calendarPeriod) {
        CalendarPeriod.WEEK -> weeklyData.maxOfOrNull { it.calories } ?: 1
        CalendarPeriod.MONTH -> monthlyData.maxOfOrNull { it.calories } ?: 1
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Сожжено калорий",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (calendarPeriod == CalendarPeriod.WEEK) "За неделю" else "За месяц",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.clickable { onPeriodClick() }
            )
        }
        
        // График
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                if (calendarPeriod == CalendarPeriod.WEEK) {
                    // Недельный вид - 7 столбцов
                    weeklyData.forEach { dayData ->
                        BarChartItem(
                            calories = dayData.calories,
                            maxCalories = maxCalories,
                            dayName = dayData.dayOfWeek,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Месячный вид - столбцы по неделям
                    monthlyData.forEach { weekData ->
                        MonthlyBarChartItem(
                            calories = weekData.calories,
                            maxCalories = maxCalories,
                            weekRange = weekData.weekRange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarChartItem(
    calories: Int,
    maxCalories: Int,
    dayName: String,
    modifier: Modifier = Modifier
) {
    val heightPercentage = if (maxCalories > 0) calories.toFloat() / maxCalories else 0f
    val hasWorkout = calories > 0 // Была тренировка если сожжены калории
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Значение калорий над столбцом (только если были тренировки)
        if (hasWorkout) {
            Text(
                text = "$calories",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B82F6), // синий цвет для цифр
                style = MaterialTheme.typography.labelSmall
            )
        } else {
            // Пустое место для сохранения высоты
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Столбик графика
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height((120.dp * heightPercentage).coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        if (hasWorkout) Color(0xFF3B82F6) // синий если были тренировки
                        else baseGray // обычный цвет если нет
                    )
            )
        }
        
        // День недели
        Text(
            text = dayName,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun MonthlyBarChartItem(
    calories: Int,
    maxCalories: Int,
    weekRange: String,
    modifier: Modifier = Modifier
) {
    val heightPercentage = if (maxCalories > 0) calories.toFloat() / maxCalories else 0f
    val hasWorkout = calories > 0
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Значение калорий над столбцом
        if (hasWorkout) {
            Text(
                text = "$calories",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B82F6),
                style = MaterialTheme.typography.labelSmall
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Столбик графика
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height((120.dp * heightPercentage).coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        if (hasWorkout) Color(0xFF3B82F6)
                        else baseGray
                    )
            )
        }
        
        // Диапазон недели
        Text(
            text = weekRange,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
