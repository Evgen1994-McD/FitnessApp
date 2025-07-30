package com.example.fitnessapp.customTraining.selectedExerciseList.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.exercises.utils.ExerciseHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class SelectedExerciseListViewModel @Inject constructor(
    private val mainDb: MainDb,
    private val exerciseHelper: ExerciseHelper
) : ViewModel() {
val exerciseData = MutableLiveData<List<ExerciseModel>>()
private var dayModel: DayModel? = null
    private var tempId: Long? = null
    private var tempListExercise:String =""

    fun getExercises(id: Int) = viewModelScope.launch {
        delay(100)
       dayModel  = mainDb.daysDao.getDay(id)
        val exerciseList = mainDb.exerciseDao.getAllExercises()
        exerciseData.value = exerciseHelper.
        getExercisesOfTheDay(
            dayModel?.exercises!!,
            exerciseList
        )
    }


    fun saveNewExerciseAndReplace(oldExercise: ExerciseModel, newExercise:ExerciseModel) = viewModelScope.launch {
        tempId = mainDb.exerciseDao.insertExercise(newExercise.copy(id = null))
       val input = dayModel?.exercises
        Log.d("MyLog", "input = $input")
        val numbers = input?.split(",")?.map { it.trim().toIntOrNull() ?: throw IllegalArgumentException("Invalid number format") }
            ?.toMutableList()
        Log.d("MyLog", "numbers = $numbers")
        // Проходим по списку и ищем заданное число
        for (i in numbers!!.indices) {
            if (numbers[i] == oldExercise.id) {
                numbers[i] = tempId!!.toInt()
            }
        }


        tempListExercise  = numbers.joinToString(separator = ",")
        Log.d("MyLog", "tempList = $tempListExercise")
        updateDay(tempListExercise)

    }






    fun updateDay(exercises: String) = viewModelScope.launch {
        val cleanedString = exercises.takeIf { it.startsWith(',') }?.removePrefix(",") ?: exercises
//        val tempExercises = exercises.replaceFirst(",", "")
        Log.d("MyLog", "updateDayExercise = $cleanedString")
        mainDb.daysDao.insertDay(
            dayModel?.copy(
                doneExerciseCounter = 0,
                isDone = false,
                exercises = cleanedString
            )!!)
    }
}