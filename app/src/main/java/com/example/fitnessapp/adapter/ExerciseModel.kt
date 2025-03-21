package com.example.fitnessapp.adapter

data class ExerciseModel(
    var name : String,
    var time : String,
    var isDone: Boolean, // выполнено упражнение или нет ( для чек боксов)
     var image : String
)
