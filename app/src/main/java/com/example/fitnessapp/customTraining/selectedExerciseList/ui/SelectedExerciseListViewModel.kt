package com.example.fitnessapp.customTraining.selectedExerciseList.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.exercises.utils.ExerciseHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SelectedExerciseListViewModel @Inject constructor(
    private val mainDb: MainDb,
    private val exerciseHelper: ExerciseHelper
) : ViewModel() {
val exerciseData = MutableLiveData<List<ExerciseModel>>()

    fun getExercises(id: Int) = viewModelScope.launch {
       val day  = mainDb.daysDao.getDay(id)
        val exerciseList = mainDb.exerciseDao.getAllExercises()
        exerciseData.value = exerciseHelper.
        getExercisesOfTheDay(
            day.exercises,
            exerciseList
        )
    }
}