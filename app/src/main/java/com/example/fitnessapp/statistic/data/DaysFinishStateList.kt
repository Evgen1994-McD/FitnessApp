package com.example.fitnessapp.statistic.data

data class DaysFinishStateList(
    var trainingCounter : Int = 0,
    var date: String, //Будем показывать дату
    var kcal: Int, // Сколько всего сжег ккалорий
    var workoutTime: String // Будем показывать сколько всего он занимался

)
