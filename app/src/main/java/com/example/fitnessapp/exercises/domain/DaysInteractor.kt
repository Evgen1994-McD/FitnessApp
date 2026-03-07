package com.example.fitnessapp.exercises.domain

import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.exercises.domain.models.TrainingTopCardModel
import kotlinx.coroutines.flow.Flow

interface DaysInteractor {
    suspend fun getExerciseDaysByDifficulty (difficulty: String): Flow<List<DayModel>>

    suspend fun resetSelectedDay(day: DayModel)
    
    // Метод для принудительного обновления данных (после открытия всех тренировок)
    suspend fun refreshDays()
    suspend fun insertDay(day: DayModel)
}