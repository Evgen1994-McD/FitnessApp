package com.example.fitnessapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.AutoMigration
import com.example.fitnessapp.db.dao.DaysDao
import com.example.fitnessapp.db.dao.ExerciseDao
import com.example.fitnessapp.db.dao.StatisticDao
import com.example.fitnessapp.db.dao.TrainingPlanDao
import com.example.fitnessapp.db.dao.WeightDao



@Database(
    entities = [
        DayModel::class,
        ExerciseModel::class,
        WeightModel::class,
        StatisticModel::class,
        TrainingPlanModel::class,
        PlannedDayModel::class
               // Таким образом при первом запуске приложения создадутся 6 таблиц и мы сможем в них записывать/считывать и ТД
               ],//Важно! Теперь используем AutoMigration для плавного обновления
    version = 7, // Текущая версия базы данных
    exportSchema = true, // Экспортируем схему для AutoMigration
    autoMigrations = [
        AutoMigration(from = 5, to = 6), // Добавляет completedDate и height
        AutoMigration(from = 6, to = 7)  // Добавляет dayId
    ]
)
abstract class MainDb: RoomDatabase()  {   // создаём базу данных она должна быть абстрактной
abstract val daysDao: DaysDao // инициализируем ДАО в БД
abstract val exerciseDao : ExerciseDao // инициализируем второе дао. Обязательно, т.к. доступ к функциям будем получать отсюда
abstract val weightDao : WeightDao // Инициализировали Дао с Весом
abstract val statisticDao : StatisticDao // Инициализировали Дао со статистикой
abstract val trainingPlanDao: TrainingPlanDao // Инициализировали Дао с AI планами
}