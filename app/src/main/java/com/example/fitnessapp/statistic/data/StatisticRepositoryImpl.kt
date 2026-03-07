package com.example.fitnessapp.statistic.data

import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.domain.StatisticRepository
import jakarta.inject.Inject

class StatisticRepositoryImpl @Inject constructor(
    private val mainDb: MainDb
) : StatisticRepository {
    override suspend fun getStatistic(): List<StatisticModel>{
        return mainDb.statisticDao.getStatistic()
    }

    override suspend fun getStatisticByDate(date:String):StatisticModel{
        return mainDb.statisticDao.getStatisticByDate(date) ?: StatisticModel(
            null,
            null,
            date,
            0.0,
            "0",
            0
        )

    }

    override suspend fun getStatisticByDayId(dayId: Int): StatisticModel? {
        return mainDb.statisticDao.getStatisticByDayId(dayId)
    }

    override suspend fun getYearWeightList():List<WeightModel> {
        return mainDb.weightDao.getAllWeightList()
    }

    override suspend fun getWeightByYearAndMonth(year : Int, month:Int):List<WeightModel>{
        return mainDb.weightDao.getMonthWeightList(year,month)
    }

    override suspend fun getWeightToday(year: Int, month: Int, day: Int): WeightModel? {
        return mainDb.weightDao.getWeightToday(year, month, day)
    }

    override suspend fun insertWeight(weightModel: WeightModel){
        mainDb.weightDao.insertWeight(weightModel)
    }


    override suspend fun getDontDoesDaysByDifficulty(difficulty:String):List<DayModel>{
        return mainDb.daysDao.getDontDonesDayByDifficulty(difficulty)
    }

    override suspend fun getDontDoesDaysByDifficultyAndZone(difficulty: String, zone: String?): List<DayModel>{
        return if (zone != null) {
            mainDb.daysDao.getDontDonesDayByDifficultyAndZone(difficulty, zone)
        } else {
            mainDb.daysDao.getDontDonesDayByDifficultyAndNoZone(difficulty)
        }
    }

    override suspend fun getAllExercise():List<ExerciseModel>{
     return mainDb.exerciseDao.getAllExercises()
    }

    override suspend fun getAllDays():List<DayModel>{
        return mainDb.daysDao.getAllDays()
    }

    override suspend fun insertDay(dayModel: DayModel){
        mainDb.daysDao.insertDay(dayModel)
    }

    override suspend fun insertExercise(exerciseModel: ExerciseModel):Long{
        return mainDb.exerciseDao.insertExercise(exerciseModel)
    }
}