package com.fit.fitnessapp.exercises.domain.impl

import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.exercises.domain.DaysInteractor
import com.fit.fitnessapp.exercises.domain.ExerciseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class DaysInteractorImpl @Inject constructor(
    private val exerciseRepository: ExerciseRepository
): DaysInteractor {
    override suspend fun getExerciseDaysByDifficulty(difficulty: String): Flow<List<DayModel>> {
      return exerciseRepository.getAllDaysByDifficulty(difficulty)
    }


    override suspend fun resetSelectedDay(day: DayModel) {
   exerciseRepository.insertDay(day)
    }

    override suspend fun insertDay(day: DayModel) {
       exerciseRepository.insertDay(day)
    }
    
    override suspend fun refreshDays() {
        // Получаем все дни по всем сложностям чтобы Room обновил Flow observers
        val difficulties = listOf("Легкая", "Средняя", "Сложная")
        difficulties.forEach { difficulty ->
            exerciseRepository.getAllDaysByDifficulty(difficulty)
        }
    }
}