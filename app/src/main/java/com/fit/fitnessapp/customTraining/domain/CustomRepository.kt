package com.fit.fitnessapp.customTraining.domain

import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.db.ExerciseModel
import kotlinx.coroutines.flow.Flow

interface CustomRepository {
    /*
    Choose Exercise
     */

   suspend fun getAllExercisesFromTo(from:Int, to:Int): List<ExerciseModel>
    suspend fun getDayById(id: Int):DayModel?
   suspend fun insertDay(dayModel:DayModel)

    /*
    CustomDaysList
     */
     fun getAllDaysByDifficulty(difficulty:String): Flow<List<DayModel>>
    suspend fun deleteDay(dayModel: DayModel)

    /*
    SelectedExerciseList
     */

    suspend fun insertExercise(newExercise:ExerciseModel):Long

    suspend fun updateExercise(exercise: ExerciseModel)

    suspend fun getAllExercise(): List<ExerciseModel>

    /*
    Фильтрация по зонам
     */
    suspend fun getExercisesByZone(from: Int, to: Int, zone: String): List<ExerciseModel>

    /*
    Сортировка упражнений
     */
    suspend fun getAllExercisesSorted(from: Int, to: Int): List<ExerciseModel>
}