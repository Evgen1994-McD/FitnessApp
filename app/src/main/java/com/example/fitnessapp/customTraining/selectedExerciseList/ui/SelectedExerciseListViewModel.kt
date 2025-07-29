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
private var dayModel: DayModel? = null
    private var tempId: Long? = null

    fun getExercises(id: Int) = viewModelScope.launch {
       dayModel  = mainDb.daysDao.getDay(id)
        val exerciseList = mainDb.exerciseDao.getAllExercises()
        exerciseData.value = exerciseHelper.
        getExercisesOfTheDay(
            dayModel?.exercises!!,
            exerciseList
        )
    }


    fun saveNewExercise(exerciseModel: ExerciseModel) = viewModelScope.launch {

    tempId = mainDb.exerciseDao.insertExercise(exerciseModel.copy(id = null))
    }


    fun updateDay(exercises: String) = viewModelScope.launch {
        val tempExercises = exercises.replaceFirst(",", "")
        mainDb.daysDao.insertDay(
            dayModel?.copy(
                doneExerciseCounter = 0,
                isDone = false,
                exercises = tempExercises
            )!!)
    }
}