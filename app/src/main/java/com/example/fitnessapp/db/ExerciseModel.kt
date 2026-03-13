package com.example.fitnessapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_table")
data class ExerciseModel(
    @PrimaryKey(autoGenerate = true) //Аннотация которая говорит что ниже ключ и надо дать Id + он автодобавляется
    var id:Int? = null, // Room умеет создавать идентификаторы. Когда мы передаём в Энтити налл, то она знает что нужно дать ИД. А если мы сами потом передаём, то перезапишем файл под этим ИД
    var name: String,
    var subtitle: String, // тут будем хранить доп запись на каждое упражнение
    var time: String,
    var isDone: Boolean, // выполнено упражнение или нет ( для чек боксов)
    var isFavorite: Boolean = false, // добавлено в избранное
    var image: String,
    var kcal: Double,  // килокалории
    var muscleZone: String,        // зона воздействия (hands/body/back/legs)
    var description: String,       // краткое описание упражнения
    var instruction: String,       // пошаговая инструкция выполнения
    var mistakes: String = "",      // распространенные ошибки выполнения (разделитель ||)
    var advise: String = ""         // советы по правильному выполнению (разделитель ||)
)
