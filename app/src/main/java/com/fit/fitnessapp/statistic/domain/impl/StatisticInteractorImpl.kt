package com.fit.fitnessapp.statistic.domain.impl

import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.db.StatisticModel
import com.fit.fitnessapp.db.WeightModel
import com.fit.fitnessapp.statistic.domain.StatisticInteractor
import com.fit.fitnessapp.statistic.domain.StatisticRepository
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
    override suspend fun getStatisticByDayId(dayId: Int): StatisticModel? {
        return statisticRepository.getStatisticByDayId(dayId)
    }
    override suspend fun getYearWeightList(): List<WeightModel>{
        return statisticRepository.getYearWeightList()
    }
    override suspend fun getWeightByYearAndMonth(year: Int, month: Int): List<WeightModel>{
        return statisticRepository.getWeightByYearAndMonth(year,month)
    }
    override suspend fun getWeightToday(year: Int, month: Int, day: Int): WeightModel? {
        return statisticRepository.getWeightToday(year, month, day)
    }
    override suspend fun insertWeight(weightModel: WeightModel){
        statisticRepository.insertWeight(weightModel)
    }
    override suspend fun getDontDoesDaysByDifficulty(difficulty: String): List<DayModel>{
        return statisticRepository.getDontDoesDaysByDifficulty(difficulty)
    }
    override suspend fun getDontDoesDaysByDifficultyAndZone(difficulty: String, zone: String?): List<DayModel>{
        return statisticRepository.getDontDoesDaysByDifficultyAndZone(difficulty, zone)
    }
    override suspend fun getAllExercise(): List<ExerciseModel>{
        return statisticRepository.getAllExercise()
    }
    override suspend fun getAllDays(): List<DayModel>{
        return statisticRepository.getAllDays()
    }
    override suspend fun insertDay(dayModel: DayModel){
        statisticRepository.insertDay(dayModel)
    }
    override suspend fun insertExercise(exerciseModel: ExerciseModel): Long{
        return statisticRepository.insertExercise(exerciseModel)
    }
}