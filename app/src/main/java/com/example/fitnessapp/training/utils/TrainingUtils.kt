package com.example.fitnessapp.training.utils

import androidx.collection.intListOf
import com.example.fitnessapp.R
import com.example.fitnessapp.training.data.TrainingTopCardModel

object TrainingUtils { //здесь будут константы которые будут определять уровень сложности. У нас будет 3 уровня сложности


    const val EASY = "easy"
    const val MIDDLE = "middle"
    const val HARD = "hard"
    const val CUSTOM = "custom"



    val difListType = listOf( // список  из констант для передачи в Дейзфрагмент уровня сложности
        R.string.easy, //будем передавать позицию из данного листа
        R.string.middle, // с помощью этого листа будем фильтровать данные в БД
        R.string.hard,
        R.string.custom

    )



    val tabTitles = listOf( //Список названий колонок ТабЛайоута
        R.string.easy,
        R.string.middle,
        R.string.hard,
        R.string.custom
    )

    val topCardList = listOf(
        TrainingTopCardModel(
            R.drawable.easy,
            R.string.easy,
            0,
            0,
            EASY // эта диффикулти вместо id - чтобы отличать уровень сложности
        ),  // стандартная сложность не подходит потому чтотам может быть локазизация, и фильтр уже будет зависеть от языка на телефоне
                TrainingTopCardModel(
                R.drawable.middle,
        R.string.middle,
        0,
        0,
                    MIDDLE
    ),
    TrainingTopCardModel(
    R.drawable.hard,
    R.string.hard,
    0,
    0,
        HARD
    ),
        TrainingTopCardModel(
            R.drawable.hard,
            R.string.custom,
            0,
            0,
            CUSTOM
        )
    )




}