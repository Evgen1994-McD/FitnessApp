package com.example.fitnessapp.exercises.domain.impl

import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.exercises.domain.ExerciseInteractor
import com.example.fitnessapp.exercises.domain.ExerciseRepository
import jakarta.inject.Inject

class ExerciseInteractorImpl @Inject constructor(
    private val exerciseRepository: ExerciseRepository
):ExerciseInteractor {
    override suspend fun updateDay(dayModel: DayModel) {
        exerciseRepository.updateDay(dayModel)
    }

    override suspend fun getAndOpenNextDay(dayModel: DayModel) {
        exerciseRepository.getAndOpenNextDay(dayModel)

    }

    override suspend fun getStatisticByDate(date: String): StatisticModel? {
       return exerciseRepository.getStatisticByDate(date)
    }

    override suspend fun getCurrentDay(dayModel: DayModel): DayModel? {
        return exerciseRepository.getCurrentDay(dayModel)
    }

    override suspend fun insertStatistic(statisticModel: StatisticModel) {
       exerciseRepository.insertStatistic(statisticModel)
    }

    override suspend fun getAllExerciseList(): List<ExerciseModel> {
     return exerciseRepository.getAllExerciseList()
    }
}