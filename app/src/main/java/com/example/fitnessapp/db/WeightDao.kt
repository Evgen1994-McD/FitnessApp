package com.example.fitnessapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_table WHERE year = :year AND month =:month") // фильтруем по году и месяцу
   suspend fun getMonthWeightList(
        year: Int,
        month: Int
    ): List<WeightModel>
    // Тут мы фильтруем уже по месяцу и по году и когда есть совпадения нам выдаст результаты

    @Query("SELECT * FROM weight_table") // фильтруем по году и месяцу
    suspend fun getAllWeightList(
    ): List<WeightModel>


    @Query("SELECT * FROM weight_table WHERE year = :year AND month =:month AND day =:day") // Сдесь мы хотим получить вес дня еслли он есть, либо Налл если нет
    suspend fun getWeightToday(year: Int, month: Int, day: Int): WeightModel // чтобы пользователь не записывал много раз в один день

    @Insert(onConflict = OnConflictStrategy.REPLACE)  // опять же с помощью онконфликст стратегии можем с помощью одной функции и записывать и обновлять данные
    suspend fun insertWeight(weightModel: WeightModel)
}