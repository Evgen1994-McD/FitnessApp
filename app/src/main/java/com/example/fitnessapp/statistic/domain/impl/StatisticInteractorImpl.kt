package com.example.fitnessapp.statistic.domain.impl

import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.domain.StatisticInteractor
import com.example.fitnessapp.statistic.domain.StatisticRepository
import jakarta.inject.Inject

class StatisticInteractorImpl @Inject constructor(
    private val statisticRepository: StatisticRepository
) : StatisticInteractor {
    override suspend fun getStatistic(): List<StatisticModel>{
        return statisticRepository.getStatistic()
    }
    override suspend fun getStatisticByDate(date: String): StatisticModel{
        return statisticRepository.getStatisticByDate(date)
    }
    override suspend fun getYearWeightList(): List<WeightModel>{
        return statisticRepository.getYearWeightList()
    }
    override suspend fun getWeightByYearAndMonth(year: Int, month: Int): List<WeightModel>{
        return statisticRepository.getWeightByYearAndMonth(year,month)
    }
    override suspend fun insertWeight(weightModel: WeightModel){
        statisticRepository.insertWeight(weightModel)
    }
    override suspend fun getDontDoesDaysByDifficulty(difficulty: String): List<DayModel>{
        return statisticRepository.getDontDoesDaysByDifficulty(difficulty)
    }
    override suspend fun getAllExercise(): List<ExerciseModel>{
        return statisticRepository.getAllExercise()
    }
    override suspend fun insertDay(dayModel: DayModel){
        statisticRepository.insertDay(dayModel)
    }
    override suspend fun insertExercise(exerciseModel: ExerciseModel): Long{
        return statisticRepository.insertExercise(exerciseModel)
    }

}