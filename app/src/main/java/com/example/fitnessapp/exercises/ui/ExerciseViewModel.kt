package com.example.fitnessapp.exercises.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.exercises.utils.ExerciseHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
private val mainDb: MainDb,
    private val exerciseHelper: ExerciseHelper
) : ViewModel() {
    var currentDay: DayModel? = null
    private var exercisesStack : List<ExerciseModel> = emptyList() // изначально пустой список с упражнениями который мы заполним позже
    private fun updateDay(dayModel: DayModel) = viewModelScope.launch {
        mainDb.daysDao.insertDay(dayModel)
    }

    private fun dayDone(){
        currentDay = currentDay?.copy(isDone = true)
        currentDay?.let {
            updateDay(it)
        }
/*
currentDay передаём тот же, но перезапишем параметр isDone чтобы поставить галочку
 */
    }

    fun getExercises(dayModel: DayModel) {
        currentDay = dayModel
        viewModelScope.launch {
            val exerciseList = mainDb.exerciseDao.getAllExercises()
            val exercisesOfTheDay = exerciseHelper.getExercisesOfTheDay(
                dayModel.exercises,
                exerciseList
                )
            exercisesStack = exerciseHelper.createExerciseStack(
                exercisesOfTheDay.subList(
                    dayModel.doneExerciseCounter, exercisesOfTheDay.size
                    /*
                    Нас интересуют только невыполненные упражнениня.
                    Поэтому мы берем СУБ лист, в котором указываем с какой по какую позицию взять элементы из нашего массива.
                    Для нас это от doneExerciseCounter - это наша созданная переменная специально для этого случая чтобы понять сколько выполнено упражнений.
                    И до конца массива

                    Если понадобится сбросить - можно в дальнейшем встроить вопрос ( желаете ли продолжить)
                     */
                )
            )
        }

    }
}