package com.example.fitnessapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_table")
data class WeightModel( // Это мы делаем график с весом на каждый месяц
    @PrimaryKey(autoGenerate = true) //Аннотация которая говорит что ниже ключ и надо дать Id + он автодобавляется
    var id:Int? = null, // Room умеет создавать идентификаторы. Когда мы передаём в Энтити налл, то она знает что нужно дать ИД. А если мы сами потом передаём, то перезапишем файл под этим ИД
 var weight: Int, // Какой вес
    var day: Int, // В какой день записал
    var month : Int, // В какой месяц записал вес.
    var year : Int // в какой год записал. Это для фильтрации веса по месяцу или году например

)
