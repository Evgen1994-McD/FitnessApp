package com.example.fitnessapp.exercises.domain.impl

import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.exercises.domain.DaysInteractor
import com.example.fitnessapp.exercises.domain.ExerciseRepository
import com.example.fitnessapp.exercises.domain.models.TrainingTopCardModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class DaysInteractorImpl @Inject constructor(
    private val exerciseRepository: ExerciseRepository
): DaysInteractor {
    override suspend fun getExerciseDaysByDifficulty(difficulty: String): Flow<List<DayModel>> {
      return exerciseRepository.getAllDaysByDifficulty(difficulty)
    }


    override fun resetSelectedDay(day: DayModel) {
   exerciseRepository.
    }
}