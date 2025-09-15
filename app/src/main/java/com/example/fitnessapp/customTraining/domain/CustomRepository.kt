package com.example.fitnessapp.customTraining.domain

import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel

interface CustomRepository {
    /*
    Choose Exercise
     */

   suspend fun getAllExercisesFromTo(from:Int, to:Int): List<ExerciseModel>
    suspend fun getDayById(id: Int):DayModel
   suspend fun insertDay(dayModel:DayModel)

    /*
    CustomDaysList
     */
    suspend fun getAllDaysByDifficulty

}