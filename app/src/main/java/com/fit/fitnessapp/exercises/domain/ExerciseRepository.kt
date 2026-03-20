package com.fit.fitnessapp.exercises.domain

import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.db.StatisticModel
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository{
   suspend fun updateDay(dayModel: DayModel)
    suspend fun getAndOpenNextDay(dayModel: DayModel)
    suspend fun getStatisticByDate(date:String): StatisticModel?
    suspend fun getCurrentDay(dayModel: DayModel):DayModel?
    suspend  fun insertStatistic(statisticModel: StatisticModel)
    suspend fun getAllExerciseList():List<ExerciseModel>
    suspend fun getDayById(dayId:Int): DayModel?
    suspend fun getExerciseById(exerciseId: Int): ExerciseModel

 suspend fun getAllDaysByDifficulty(diffculty: String): Flow<List<DayModel>>
 suspend fun insertDay(dayModel: DayModel)
}
