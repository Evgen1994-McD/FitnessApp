package com.example.fitnessapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao

interface CustomExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // стратегия при конфликте - чтобы не делать вторую функцию Апдейт, мы сразу будем обновлять
    suspend fun insertExercise(customExerciseModel: CustomExerciseModel) // это функция для записи дня. Сюда мы передаём заполненный, но с идентификатором null, поэтому будет создан новый элемент

}