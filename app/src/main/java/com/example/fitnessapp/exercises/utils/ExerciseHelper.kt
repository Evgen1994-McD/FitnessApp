package com.example.fitnessapp.exercises.utils

import com.example.fitnessapp.db.ExerciseModel
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped  //Пометили анотацией чтобы привязать этот класс к жизни ViewModel
class ExerciseHelper @Inject constructor(

) {

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