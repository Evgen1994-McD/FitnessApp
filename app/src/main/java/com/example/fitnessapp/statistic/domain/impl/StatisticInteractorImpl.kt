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

    override suspend fun getCurrentStreak(): Int {
        val workoutDates = statisticRepository.getAllWorkoutDates()
        if (workoutDates.isEmpty()) return 0

        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        
        // Parse all dates and sort them descending
        val dates = workoutDates.map { dateFormat.parse(it)!! }.sortedDescending()
        
        // Reset calendar to yesterday (start of streak calculation)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        
        // Check if yesterday was the last workout day
        val lastWorkout = dates.first()
        val yesterdayStr = dateFormat.format(yesterday)
        val todayStr = dateFormat.format(java.util.Date())
        
        // If no workout yesterday and none today, streak is 0
        if (dateFormat.format(lastWorkout) != yesterdayStr && dateFormat.format(lastWorkout) != todayStr) {
            return 0
        }
        
        // Count consecutive days
        var streak = 1
        for (i in 1 until dates.size) {
            calendar.time = dates[i - 1]
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val expectedPrevDay = calendar.time
            
            if (dateFormat.format(dates[i]) == dateFormat.format(expectedPrevDay)) {
                streak++
            } else {
                break
            }
        }
        
        return streak
    }
}