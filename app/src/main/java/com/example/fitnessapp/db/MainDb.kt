package com.example.fitnessapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(
    entities = [
        DayModel::class,
        ExerciseModel::class,
        WeightModel::class,
        StatisticModel::class // Таким образом при первом запуске приложения создадутся 4 таблицы и мы сможем в них записывать/считывать и ТД
               ],//Важно! Если выложить его, то пользователь не сможет увидеть обновления тк нет миграции
    version = 1 // Либо удалять каждый раз, либо делать миграцию!!
)
abstract class MainDb: RoomDatabase()  {   // создаём базу данных она должна быть абстрактной
abstract val daysDao: DaysDao // инициализируем ДАО в БД
abstract val exerciseDao : ExerciseDao // инициализируем второе дао. Обязательно, т.к. доступ к функциям будем получать отсюда
abstract val weightDao : WeightDao // Инициализировали Дао с Весом
}