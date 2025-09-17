package com.example.fitnessapp.exercises.data

import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.exercises.domain.ExerciseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ExerciseRepositoryImpl @Inject constructor(
    private val mainDb: MainDb
) : ExerciseRepository {
    override suspend fun updateDay(dayModel: DayModel) {
        mainDb.daysDao.insertDay(dayModel)

    }

    override suspend fun getAndOpenNextDay(dayModel: DayModel) {
        var nextId = ((dayModel?.id)?.plus(1)) ?: 0
        if (nextId!=0) {
            try {
                var nextDay = mainDb.daysDao.getDay(nextId)
                nextDay = nextDay!!.copy(isOpen = true)
                updateDay(nextDay!!)
            }catch (e:Exception){

            }
        }
    }

    override  suspend fun getStatisticByDate(date: String): StatisticModel? {
       return mainDb.statisticDao.getStatisticByDate(date)
    }

    override suspend fun getCurrentDay(dayModel: DayModel): DayModel? {
       return dayModel.id?.let { mainDb.daysDao.getDay(it) }
    }

    override  suspend fun insertStatistic(statisticModel: StatisticModel) {
        mainDb.statisticDao.insertDayStatistic(statisticModel)
    }

    override suspend fun getAllExerciseList(): List<ExerciseModel> {
       return mainDb.exerciseDao.getAllExercises()
    }

    override suspend fun getDayById(dayId: Int): DayModel? {
        return mainDb.daysDao.getDay(dayId)
    }

    override suspend fun getAllDaysByDifficulty(diffculty:String): Flow<List<DayModel>> {
        return mainDb.daysDao.getAllDaysByDifficulty(diffculty)
    }

    override suspend fun insertDay(dayModel: DayModel){
mainDb.daysDao.insertDay(dayModel)
    }
}