package com.example.fitnessapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.AutoMigration
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
               // Таким образом при первом запуске приложения создадутся 5 таблиц и мы сможем в них записывать/считывать и ТД
               ],//Важно! Если выложить его, то пользователь не сможет увидеть обновления тк нет миграции
    version = 6, // Увеличена версия для добавления полей height в weight_table и completedDate в day_model_table
    exportSchema = true, // Экспортируем схему для миграций
    autoMigrations = [
        AutoMigration(from = 5, to = 6)
    ]
)
abstract class MainDb: RoomDatabase()  {   // создаём базу данных она должна быть абстрактной
abstract val daysDao: DaysDao // инициализируем ДАО в БД
abstract val exerciseDao : ExerciseDao // инициализируем второе дао. Обязательно, т.к. доступ к функциям будем получать отсюда
abstract val weightDao : WeightDao // Инициализировали Дао с Весом
abstract val statisticDao : StatisticDao // Инициализировали Дао со статистикой
abstract val trainingPlanDao: TrainingPlanDao // Инициализировали Дао с AI планами
}