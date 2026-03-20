package com.fit.fitnessapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fit.fitnessapp.db.ExerciseModel

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_table WHERE id BETWEEN :from AND :to")
    suspend fun getAllExercisesFromTo(from: Int, to: Int) : List<ExerciseModel> // это получение всех базовых упражнений ( не получаем дубли)

    @Query("SELECT * FROM exercise_table")
    suspend fun getAllExercises() : List<ExerciseModel> // Здесь мы сделали суспенд потому что нет необходимости в ФЛОУ
//Так же анностация - выбрать всё из таблицы эксерсайз тейбл. Берем все упражнения.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exerciseModel: ExerciseModel): Long // Запись сразу вернет id нового упражнения

    @Update
    suspend fun updateExercise(exerciseModel: ExerciseModel)

    @Query("SELECT * FROM exercise_table WHERE id LIKE :id")
    suspend fun findExerciseById(id: Int): ExerciseModel

    // Новые методы для AI функциональности
    @Query("SELECT * FROM exercise_table WHERE muscleZone LIKE :zone AND id BETWEEN :from AND :to")
    suspend fun getExercisesByZone(from: Int, to: Int, zone: String): List<ExerciseModel>

    @Query("SELECT * FROM exercise_table WHERE id IN (:ids)")
    suspend fun getExercisesByIds(ids: List<Int>): List<ExerciseModel>

    @Query("SELECT * FROM exercise_table WHERE id BETWEEN :from AND :to ORDER BY name")
    suspend fun getAllExercisesSorted(from: Int, to: Int): List<ExerciseModel>

    @Query("SELECT * FROM exercise_table WHERE muscleZone LIKE :zone ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomExercisesByZone(zone: String, limit: Int): List<ExerciseModel>
}