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

// Миграция с версии 5 на 7 для добавления всех полей версии 6
val MIGRATION_5_7 = object : Migration(5, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Добавляем completedDate в day_model_table (как в версии 6)
        database.execSQL("ALTER TABLE day_model_table ADD COLUMN completedDate TEXT")
        
        // Добавляем height в weight_table (как в версии 6)
        database.execSQL("ALTER TABLE weight_table ADD COLUMN height REAL")
        
        // Добавляем dayId в statistic_table (новое поле)
        database.execSQL("ALTER TABLE statistic_table ADD COLUMN dayId INTEGER")
        
        // Создаем индекс для быстрого поиска по dayId
        database.execSQL("CREATE INDEX IF NOT EXISTS index_statistic_dayId ON statistic_table(dayId)")
    }
}

// Миграция с версии 6 на 7 для добавления поля dayId
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Добавляем колонку dayId для связи с DayModel
        database.execSQL("ALTER TABLE statistic_table ADD COLUMN dayId INTEGER")
        // Создаем индекс для быстрого поиска по dayId
        database.execSQL("CREATE INDEX IF NOT EXISTS index_statistic_dayId ON statistic_table(dayId)")
    }
}



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
    version = 7, // Увеличена версия для добавления поля dayId в statistic_table
    exportSchema = true, // Экспортируем схему для миграций
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7)
    ]
)
abstract class MainDb: RoomDatabase()  {   // создаём базу данных она должна быть абстрактной
abstract val daysDao: DaysDao // инициализируем ДАО в БД
abstract val exerciseDao : ExerciseDao // инициализируем второе дао. Обязательно, т.к. доступ к функциям будем получать отсюда
abstract val weightDao : WeightDao // Инициализировали Дао с Весом
abstract val statisticDao : StatisticDao // Инициализировали Дао со статистикой
abstract val trainingPlanDao: TrainingPlanDao // Инициализировали Дао с AI планами
}