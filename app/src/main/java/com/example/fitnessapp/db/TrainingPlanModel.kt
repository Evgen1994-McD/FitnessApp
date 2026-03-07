package com.example.fitnessapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_plan_table")
data class TrainingPlanModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var name: String,              // название плана
    var description: String,       // описание плана
    var targetZones: String,       // целевые зоны (JSON array)
    var exercisesPerDay: Int,      // упражнений в день
    var aiGenerated: Boolean,      // сгенерировано AI
    var createdAt: Long,           // дата создания
    var isActive: Boolean          // активен ли план
)
