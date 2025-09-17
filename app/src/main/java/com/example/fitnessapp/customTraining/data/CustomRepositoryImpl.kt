package com.example.fitnessapp.customTraining.data

import com.example.fitnessapp.customTraining.domain.CustomRepository
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class CustomRepositoryImpl @Inject constructor(
    private val mainDb: MainDb
) :CustomRepository {
    override suspend fun getAllExercisesFromTo(from: Int, to: Int): List<ExerciseModel> {
        return mainDb.exerciseDao.getAllExercisesFromTo(from,to)
    }

    override suspend fun getDayById(id: Int): DayModel {
        return mainDb.daysDao.getDay(id)
    }

    override suspend fun insertDay(dayModel: DayModel) {
        mainDb.daysDao.insertDay(dayModel)
    }

    override  fun getAllDaysByDifficulty(difficulty: String): Flow<List<DayModel>> {
        return mainDb.daysDao.getAllDaysByDifficulty(difficulty)
    }

    override suspend fun deleteDay(dayModel: DayModel) {
        mainDb.daysDao.deleteDay(dayModel)
    }

    override suspend fun insertExercise(newExercise: ExerciseModel): Long {
        return mainDb.exerciseDao.insertExercise(newExercise)
    }

    override suspend fun getAllExercise(): List<ExerciseModel>{
        return mainDb.exerciseDao.getAllExercises()
    }
}