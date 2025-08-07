package com.example.fitnessapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistic_table")
data class StatisticModel(
    @PrimaryKey(autoGenerate = true) //Аннотация которая говорит что ниже ключ и надо дать Id + он автодобавляется
    var id:Int? = null, // Room умеет создавать идентификаторы. Когда мы передаём в Энтити налл, то она знает что нужно дать ИД. А если мы сами потом передаём, то перезапишем файл под этим ИД
var date: String, //Будем показывать дату
    var kcal: Int, // Сколько всего сжег ккалорий
    var workoutTime: String, // Будем показывать сколько всего он занимался
    var completedExercise: Int // Будем показывать сколько всего он занимался
)
