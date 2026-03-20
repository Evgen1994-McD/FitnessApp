package com.fit.fitnessapp.exercises.domain.impl

import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.db.StatisticModel
import com.fit.fitnessapp.exercises.domain.ExerciseInteractor
import com.fit.fitnessapp.exercises.domain.ExerciseRepository
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

    override suspend fun getDayById(dayId: Int): DayModel? {
      return exerciseRepository.getDayById(dayId)
    }

    override suspend fun insertStatistic(statisticModel: StatisticModel) {
       exerciseRepository.insertStatistic(statisticModel)
    }

    override suspend fun getAllExerciseList(): List<ExerciseModel> {
     return exerciseRepository.getAllExerciseList()
    }

    override suspend fun getExerciseById(exerciseId: Int): ExerciseModel {
        return exerciseRepository.getExerciseById(exerciseId)
    }
}