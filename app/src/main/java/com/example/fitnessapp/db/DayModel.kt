package com.example.fitnessapp.db

import androidx.navigation.NavType
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "day_model_table") // Это сущность данных в базе данных, в скобках указали номер таблицы
data class DayModel(
    @PrimaryKey(autoGenerate = true) //Аннотация которая говорит что ниже ключ и надо дать Id + он автодобавляется
    var id:Int? = null, // Room умеет создавать идентификаторы. Когда мы передаём в Энтити налл, то она знает что нужно дать ИД. А если мы сами потом передаём, то перезапишем файл под этим ИД
    var exercises : String,
    var difficulty: String,  // Это мы добавляем сложность упражнениям. Сделаю 3 группы по сложности
    var isDone : Boolean,
    var dayNumber : Int, // будем передавать день по счетчику
    var doneExerciseCounter:Int,
    var isOpen:Boolean
) : Serializable // чтобы сделать бандл нужно подключить интерфейс сериализации
