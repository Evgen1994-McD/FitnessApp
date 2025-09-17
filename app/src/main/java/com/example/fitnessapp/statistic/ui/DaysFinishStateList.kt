package com.example.fitnessapp.statistic.ui

data class DaysFinishStateList(
    var trainingCounter : Int = 0,
    var date: String, //Будем показывать дату
    var kcal: Double, // Сколько всего сжег ккалорий
    var workoutTime: String // Будем показывать сколько всего он занимался

)
