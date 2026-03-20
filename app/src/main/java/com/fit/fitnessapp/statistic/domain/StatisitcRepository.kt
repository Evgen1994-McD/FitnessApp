package com.fit.fitnessapp.statistic.domain

import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.db.StatisticModel
import com.fit.fitnessapp.db.WeightModel

interface StatisticRepository {
    suspend fun getStatistic(): List<StatisticModel>
    suspend fun getStatisticByDate(date: String): StatisticModel
    suspend fun getStatisticByDayId(dayId: Int): StatisticModel?
    suspend fun getYearWeightList(): List<WeightModel>
    suspend fun getWeightByYearAndMonth(year: Int, month: Int): List<WeightModel>
    suspend fun getWeightToday(year: Int, month: Int, day: Int): WeightModel?
    suspend fun insertWeight(weightModel: WeightModel)
    suspend fun getDontDoesDaysByDifficulty(difficulty: String): List<DayModel>
    suspend fun getDontDoesDaysByDifficultyAndZone(difficulty: String, zone: String? = null): List<DayModel>
    suspend fun getAllExercise(): List<ExerciseModel>
    suspend fun getAllDays(): List<DayModel>
    suspend fun insertDay(dayModel: DayModel)
    suspend fun insertExercise(exerciseModel: ExerciseModel): Long
}