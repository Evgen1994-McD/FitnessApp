package com.fit.fitnessapp.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fit.fitnessapp.db.StatisticModel

@Dao
interface StatisticDao {

    @Query("SELECT * FROM statistic_table") // получить всё из статистик тейбл
    suspend fun getStatistic(): List<StatisticModel>   //Опять же, мы будем получать один раз, поэтому это суспенд функция а не Флоу

    @Query("SELECT * FROM statistic_table WHERE date=:date") //Выбрать один конкретный день по дате которую передали
    suspend fun getStatisticByDate(date: String) : StatisticModel? // передаем дату получаем статистик модел по дате
    
    @Query("SELECT * FROM statistic_table WHERE dayId=:dayId LIMIT 1") //Выбрать статистику по ID тренировки
    suspend fun getStatisticByDayId(dayId: Int): StatisticModel? // получить статистику по ID тренировки

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayStatistic(statisticModel: StatisticModel)

    @Query("DELETE  FROM statistic_table") // получить всё из статистик тейбл
    suspend fun clearStatistic()   //Опять же, мы будем получать один раз, поэтому это суспенд функция а не Флоу

}