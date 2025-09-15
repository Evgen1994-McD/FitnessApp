package com.example.fitnessapp.exercises.domain

import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.StatisticModel

interface ExerciseRepository{
   suspend fun updateDay(dayModel: DayModel)
    suspend fun getAndOpenNextDay(dayModel: DayModel)
    suspend fun getStatisticByDate(date:String): StatisticModel?
    suspend fun getCurrentDay(dayModel: DayModel):DayModel?
    suspend  fun insertStatistic(statisticModel: StatisticModel)
    suspend fun getAllExerciseList():List<ExerciseModel>
}
