package com.example.fitnessapp.training.utils

import androidx.collection.intListOf
import com.example.fitnessapp.R
import com.example.fitnessapp.training.data.TrainingTopCardModel

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

    val topCardList = listOf(
        TrainingTopCardModel(
            R.drawable.easy,
            "",
            0,
            0,
            "easy"  // эта диффикулти вместо id - чтобы отличать уровень сложности
        ),  // стандартная сложность не подходит потому чтотам может быть локазизация, и фильтр уже будет зависеть от языка на телефоне
                TrainingTopCardModel(
                R.drawable.middle,
        "",
        0,
        0,
        "middle"
    ),
    TrainingTopCardModel(
    R.drawable.hard,
    "",
    0,
    0,
    "hard"
    )
    )




}