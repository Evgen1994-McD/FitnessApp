package com.example.fitnessapp.exercises.utils

import com.example.fitnessapp.R
import com.example.fitnessapp.exercises.domain.models.TrainingTopCardModel

object TrainingUtils { //здесь будут константы которые будут определять уровень сложности. У нас будет 3 уровня сложности


    const val EASY = "easy"
    const val MIDDLE = "middle"
    const val HARD = "hard"
    const val CUSTOM = "custom"

    //руки
    const val HANDS = "hands"

    //спина
    const val BACK = "back"

    //тело
    const val BODY = "body"

    //ноги
    const val LEGS = "legs"

    //Грудь
    const val CHEST = "chest"

    //Пресс
    const val ABS = "abs"

    //Разминка
    const val WARM = "warm"

    //Растяжка
    const val STRETCH = "stretch"

    //Плечи
    const val SHOULDERS = "shoulders"


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

    fun getTrainingImage(difficulty: String, zone: String? = null): Int {
        return when (zone) {
            HANDS -> when (difficulty) {
                EASY -> R.drawable.hand_easy // Замените на hands_easy когда появятся
                MIDDLE -> R.drawable.hand_middle
                else -> R.drawable.hand_hard
            }
            BODY -> when (difficulty) {
                EASY -> R.drawable.body_easy
                MIDDLE -> R.drawable.body_middle
                else -> R.drawable.body_hard
            }
            BACK -> when (difficulty) {
                EASY -> R.drawable.back_easy
                MIDDLE -> R.drawable.back_middle
                else -> R.drawable.back_hard
            }
            LEGS -> when (difficulty) {
                EASY -> R.drawable.legs_easy
                MIDDLE -> R.drawable.legs_middle
                else -> R.drawable.legs_hard
            }
            CHEST -> when (difficulty) {
                EASY -> R.drawable.chest_easy
                MIDDLE -> R.drawable.chest_middle
                else -> R.drawable.chest_hard
            }
            ABS -> when (difficulty) {
                EASY -> R.drawable.abs_easy
                MIDDLE -> R.drawable.abs_middle
                else -> R.drawable.abs_hard
            }
            WARM -> when (difficulty) {
                EASY -> R.drawable.easy
                MIDDLE -> R.drawable.middle
                else -> R.drawable.hard
            }
            STRETCH -> when (difficulty) {
                EASY -> R.drawable.easy
                MIDDLE -> R.drawable.middle
                else -> R.drawable.hard
            }
            SHOULDERS -> when (difficulty) {
                EASY -> R.drawable.easy
                MIDDLE -> R.drawable.middle
                else -> R.drawable.hard
            }
            else -> when (difficulty) {
                EASY -> R.drawable.easy
                MIDDLE -> R.drawable.middle
                else -> R.drawable.hard
            }
        }
    }
}