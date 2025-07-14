package com.example.fitnessapp.customTraining.chooseExercises.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun getAllExercises() = viewModelScope.launch {
        exerciseListData.value = mainDb.exerciseDao.getAllExercises()
    }
}