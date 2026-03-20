package com.fit.fitnessapp.exercises.domain

import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.db.StatisticModel

interface ExerciseInteractor {
    suspend fun updateDay(dayModel: DayModel)
    suspend fun getAndOpenNextDay(dayModel: DayModel)
    suspend fun getStatisticByDate(date:String): StatisticModel?
    suspend fun getCurrentDay(dayModel: DayModel): DayModel?
    suspend fun getDayById(dayId:Int): DayModel?
    suspend  fun insertStatistic(statisticModel: StatisticModel)
    suspend fun getAllExerciseList():List<ExerciseModel>
    suspend fun getExerciseById(exerciseId: Int): ExerciseModel
}