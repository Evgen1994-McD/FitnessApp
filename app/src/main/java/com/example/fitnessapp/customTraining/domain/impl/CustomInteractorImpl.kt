package com.example.fitnessapp.customTraining.domain.impl

import com.example.fitnessapp.customTraining.domain.CustomInteractor
import com.example.fitnessapp.customTraining.domain.CustomRepository
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class CustomInteractorImpl @Inject constructor(
    private val customRepository: CustomRepository
):CustomInteractor {
    override suspend fun getAllExercisesFromTo(from: Int, to: Int): List<ExerciseModel> {
    return customRepository.getAllExercisesFromTo(from,to)
    }

    override suspend fun getDayById(id: Int): DayModel {
       return customRepository.getDayById(id)
    }

    override suspend fun insertDay(dayModel: DayModel) {
       customRepository.insertDay(dayModel)
    }

    override  fun getAllDaysByDifficulty(difficulty: String): Flow<List<DayModel>> {
       return customRepository.getAllDaysByDifficulty(difficulty)
    }

    override suspend fun deleteDay(dayModel: DayModel) {
        customRepository.deleteDay(dayModel)
    }

    override suspend fun insertExercise(newExercise: ExerciseModel): Long {
    return customRepository.insertExercise(newExercise)
    }

    override suspend fun getAllExercise(): List<ExerciseModel> {
        return customRepository.getAllExercise()
    }
}