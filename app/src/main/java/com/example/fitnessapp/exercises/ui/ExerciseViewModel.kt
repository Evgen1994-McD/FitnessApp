package com.example.fitnessapp.exercises.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
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
        }

    }
}