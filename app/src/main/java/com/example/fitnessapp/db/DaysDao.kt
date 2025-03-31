package com.example.fitnessapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update

@Dao  //это для Room - объект для доступа к данным . Это обязательно должен быть интерфейс
interface DaysDao {
    @Insert()
    suspend fun insertDay(dayModel: DayModel) // это функция для записи дня. Сюда мы передаём заполненный, но с идентификатором null, поэтому будет создан новый элемент
    @Update
    suspend fun updateDay(dayModel: DayModel) //это функция для апдейта. Сюда мы пеоедаём Daymodel с идентификатором который уже существует в базе данных. Поэтому данный элемент будет обновлен
}