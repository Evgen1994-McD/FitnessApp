package com.example.fitnessapp.exercises.utils

import android.content.Context
import com.example.fitnessapp.R
import com.example.fitnessapp.db.ExerciseModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped  //Пометили анотацией чтобы привязать этот класс к жизни ViewModel
class ExerciseHelper @Inject constructor(@ApplicationContext private val context: Context

) {

    fun createExerciseStack(list: List<ExerciseModel>) : List<ExerciseModel>{
val templist = ArrayList<ExerciseModel>()
        list.forEachIndexed {index, exercise ->
            templist.add(
                exercise.copy(
                    time = context.getString(R.string.recovery), // ОТДЫХ МЕЖДУ УПРАЖНЕНИЯМИ 10 СЕКУНД
                    subtitle = if (index == 0) {
                        context.getString(R.string.subtitle_start)
                    } else {

                        context.getString(R.string.subtitle) // Это отдых
                    }
                )
            )
            templist.add(
                exercise.copy(
                    subtitle = context.getString(R.string.start) // Это уже само упражнение
                )
            )
        }
    templist.add(
        ExerciseModel(
            /*
            суть - выше мы записали все упражнения в стек.
            А ниже это после цикла forEach формируем последний экран ( с поздравлениями)
            Таким образом получаем стек с упражнениями.
            Некоторые поля не нужны, но их тоже надо заполнить для проформы.
             */
            null,
            context.getString(R.string.day_finish_name),
            context.getString(R.string.day_finish_subtitle),
            "", // это финиш тут заполнили просто так, тут не важно
            true, // это финиш тут заполнили просто так, тут не важно
            context.getString(R.string.day_finish_fire), // из папки ассетс
            0.0
        )
    )
        /*
        Суть метода : мы перебираем лист с упражнениями и создаём с помощью цикла forEach Стек
        с упражнениями и отдыхом. Для этого каждое упражнение копируем 2 раза в лист, меняем только время ( время отдыха + субтайтл - это наш отдых)
        и повторно копируем упражнение - это уже само упражнение
         */
        return templist
    }


    fun getExercisesOfTheDay(exercisesIds : String,
                             list : List<ExerciseModel>) : List<ExerciseModel>{
val exercisesIdsArray = exercisesIds.split(",") // Это массив который получаем разделителем
        val tempList = ArrayList<ExerciseModel>()
        for (i in exercisesIdsArray.indices){
            if (exercisesIdsArray[i].isNotEmpty()) {
                val exerciseId = exercisesIdsArray[i].toInt()
                val exercise = list.filter {
                    it.id == exerciseId
                } [0]
                tempList.add(
                    exercise
                    /*
                В данном методе получаем упражнения нужного дня путём разделения переданной строки из БД
                по запятой.
                 */
                )
            }
        }
        return tempList
    }

}