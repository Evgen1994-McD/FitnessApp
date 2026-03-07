package com.example.fitnessapp.statistic.ui.models

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

fun generateWeekDays(selectedDate: LocalDate = LocalDate.now()): List<DayCalendarModel> {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd")
    val dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE", Locale("ru"))
    
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
