package com.example.fitnessapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planned_day_table")
data class PlannedDayModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var planId: Int,              // ID плана
    var dayNumber: Int,           // номер дня в плане
    var exerciseIds: String,       // ID упражнений (JSON array)
    var restDay: Boolean,         // день отдыха
    var targetZone: String?,      // целевая зона дня
    var estimatedTime: Int,       // предполагаемое время в минутах
    var estimatedCalories: Double // предполагаемые калории
)
