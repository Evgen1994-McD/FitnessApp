package com.example.fitnessapp.exercises.utils

import com.example.fitnessapp.db.ExerciseModel
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped  //Пометили анотацией чтобы привязать этот класс к жизни ViewModel
class ExerciseHelper @Inject constructor(

) {

    fun createExerciseStack(list: List<ExerciseModel>) : List<ExerciseModel>{
val templist = ArrayList<ExerciseModel>()
        list.forEach { exercise ->
            templist.add(
                exercise.copy(
                    time = "11", // ОТДЫХ МЕЖДУ УПРАЖНЕНИЯМИ 10 СЕКУНД
                    subtitle = "Relax" // Это отдых
                )
            )
            templist.add(
                exercise.copy(
                    subtitle = "Start" // Это уже само упражнение
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
            "Day Finish",
            "Nice training",
            "", // это финиш тут заполнили просто так, тут не важно
            true, // это финиш тут заполнили просто так, тут не важно
            "congrats.gif", // из папки ассетс
            0
        )
    )
        /*
        Суть метода : мы перебираем лист с упражнениями и создаём с помощью цикла forEach Стек
        с упражнениями и отдыхом. Для этого каждое упражнение копируем 2 раза в лист, меняем только время ( время отдыха + субтайтл - это наш отдых)
        и повторно копируем упражнение - это уже само упражнение
         */
        return templist
    }


    fun getExercisesOfTheDay(exercisesIndexes : String,
                             list : List<ExerciseModel>) : List<ExerciseModel>{
val exercisesIndexesArray = exercisesIndexes.split(",") // Это массив который получаем разделителем
        val tempList = ArrayList<ExerciseModel>()
        for (i in exercisesIndexesArray.indices){
            tempList.add(
                list[exercisesIndexesArray[i].toInt()]
                /*
                В данном методе получаем упражнения нужного дня путём разделения переданной строки из БД
                по запятой.
                 */
            )
        }
        return tempList
    }

}