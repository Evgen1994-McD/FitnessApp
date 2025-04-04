package com.example.fitnessapp.training.utilsfortraining

import com.example.fitnessapp.R

object TrainingUtils { //здесь будут константы которые будут определять уровень сложности. У нас будет 3 уровня сложности
    const val EASY = "easy"
    const val MIDDLE = "middle"
    const val HARD = "hard"


    val difListType = listOf( // список  из констант для передачи в Дейзфрагмент уровня сложности
        EASY, //будем передавать позицию из данного листа
        MIDDLE, // с помощью этого листа будем фильтровать данные в БД
        HARD
    )



    val tabTitles = listOf( //Список названий колонок ТабЛайоута
        R.string.easy,
        R.string.middle,
        R.string.hard
    )
}