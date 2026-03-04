package com.example.fitnessapp.statistic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.fitnessapp.statistic.ui.models.DayCalendarModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HorizontalCalendar(
    days: List<DayCalendarModel>,
    onDayClick: (DayCalendarModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days) { day ->
            DayCalendarItem(
                day = day,
                onClick = { onDayClick(day) }
            )
        }
    }
}

@Composable
private fun DayCalendarItem(
    day: DayCalendarModel,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        day.isSelected -> MaterialTheme.colorScheme.surfaceContainer
        day.isToday -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when {
        day.isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .width(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.dayName,
            fontSize = 10.sp,
            color = if (day.isSelected) 
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
            text = day.dayNumber.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            textAlign = TextAlign.Center
        )
        
        if (day.isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .padding(top = 4.dp)
            )
        }
    }
}

fun generateWeekDays(selectedDate: LocalDate = LocalDate.now()): List<DayCalendarModel> {
    val formatter = DateTimeFormatter.ofPattern("dd")
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale("ru"))
    
    // Получаем начало недели (понедельник)
    val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value - 1L)
    
    return (0..6).map { dayOffset ->
        val currentDate = startOfWeek.plusDays(dayOffset.toLong())
        val isToday = currentDate.isEqual(LocalDate.now())
        val isSelected = currentDate.isEqual(selectedDate)
        
        DayCalendarModel(
            dayNumber = currentDate.dayOfMonth,
            dayName = currentDate.format(dayFormatter).replaceFirstChar { it.uppercase() },
            isSelected = isSelected,
            isToday = isToday
        )
    }
}
