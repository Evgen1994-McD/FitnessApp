package com.example.fitnessapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.nio.charset.CodingErrorAction.REPLACE

@Dao  //это для Room - объект для доступа к данным . Это обязательно должен быть интерфейс
interface DaysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // стратегия при конфликте - чтобы не делать вторую функцию Апдейт, мы сразу будем обновлять
    suspend fun insertDay(dayModel: DayModel) // это функция для записи дня. Сюда мы передаём заполненный, но с идентификатором null, поэтому будет создан новый элемент

    @Query("SELECT * FROM day_model_table WHERE id =:dayId") // тут запрос в БД - выбрать всё ( * - всё) из таблицы деймоделтейбл где айди = ийди который передаём через функцию
suspend fun getDay(dayId: Int) : DayModel

    @Query("SELECT * FROM day_model_table WHERE difficulty =:difficulty") // тут мы выбираем и фильтруем себе дни по сложности
    fun getAllDaysByDifficulty(difficulty: String) : Flow<List<DayModel>>// выдасти нам лист с DayModel по сложности. Флоу обязательно из пакета корутин. Флоу сам следит за изменениями и обновляет при необходимости

}