package com.example.fitnessapp.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ExerciseDao {
@Query("SELECT * FROM exercise_table")
suspend fun getAllExercises() : List<ExerciseModel> // Здесь мы сделали суспенд потому что нет необходимости в ФЛОУ
//Так же анностация - выбрать всё из таблицы эксерсайз тейбл. Берем все упражнения.
suspend fun insertExercise()
}