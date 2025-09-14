package com.example.fitnessapp.customTraining.chooseExercises.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseExercisesViewModel @Inject constructor(
    private val mainDb: MainDb
) : ViewModel() {

    val exerciseListData = MutableLiveData<List<ExerciseModel>>()
private var dayModel: DayModel? = null
    fun getAllExercises() = viewModelScope.launch {
        exerciseListData.value = mainDb.exerciseDao.getAllExercisesFromTo(4,53)
    }

    fun getDayById(id: Int)= viewModelScope.launch {
        dayModel = mainDb.daysDao.getDay(id)
    }


    fun updateDay(exercises: String) = viewModelScope.launch {
        val oldExercises = dayModel?.exercises ?: ""
        val tempExercises = if(oldExercises.isEmpty()){
            exercises.replaceFirst(",", "")
        } else {
            exercises
        }
        dayModel?.copy(
            exercises = oldExercises + tempExercises
        )?.let {
            mainDb.daysDao.insertDay(it)
        }
    }

}