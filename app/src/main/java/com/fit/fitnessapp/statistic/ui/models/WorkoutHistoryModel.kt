package com.fit.fitnessapp.statistic.ui.models

import com.fit.fitnessapp.db.ExerciseModel

data class WorkoutHistoryModel(
    val id: Int?,
    val date: String,
    val zone: String?,
    val difficulty: String,
    val caloriesBurned: Double,
    val duration: Int, // в минутах
    val exercises: List<ExerciseModel>,
    val isExpanded: Boolean = false
)

data class WeeklyCaloriesModel(
    val dayOfWeek: String,
    val calories: Int,
    val dayNumber: Int // для сортировки
)

data class DayCalendarModel(
    val dayNumber: Int,
    val dayName: String,
    val isSelected: Boolean,
    val isToday: Boolean
)
