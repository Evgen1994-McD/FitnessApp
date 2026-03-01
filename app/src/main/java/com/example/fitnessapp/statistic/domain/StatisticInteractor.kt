package com.example.fitnessapp.statistic.domain

import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.db.WeightModel

interface StatisticInteractor {
    suspend fun getStatistic(): List<StatisticModel>
    suspend fun getStatisticByDate(date: String): StatisticModel
    suspend fun getYearWeightList(): List<WeightModel>
    suspend fun getWeightByYearAndMonth(year: Int, month: Int): List<WeightModel>
    suspend fun getWeightToday(year: Int, month: Int, day: Int): WeightModel?
    suspend fun insertWeight(weightModel: WeightModel)
    suspend fun getDontDoesDaysByDifficulty(difficulty: String): List<DayModel>
    suspend fun getDontDoesDaysByDifficultyAndZone(difficulty: String, zone: String? = null): List<DayModel>
    suspend fun getAllExercise(): List<ExerciseModel>
    suspend fun insertDay(dayModel: DayModel)
    suspend fun insertExercise(exerciseModel: ExerciseModel): Long
}