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

    fun getExercises(id: Int) = viewModelScope.launch {
        delay(200L)
        dayModel = mainDb.daysDao.getDay(id)
        val exerciseList = mainDb.exerciseDao.getAllExercises()
        exerciseData.value = exerciseHelper.getExercisesOfTheDay(
            dayModel?.exercises!!,
            exerciseList
        )
    }


    fun saveNewExerciseAndReplace(
        oldExercise: ExerciseModel,
        newExercise: ExerciseModel,
        pos: Int
    ) = viewModelScope.launch {
        var tempId: Long? = null
         var tempListExercise = ""

        val input = dayModel?.exercises

        tempId = mainDb.exerciseDao.insertExercise(newExercise.copy(id = null))
        Log.d("MyLog", "input = $input")
        val numbers = input?.split(",")?.map {
            it.trim().toIntOrNull() ?: throw IllegalArgumentException("Invalid number format")
        }
            ?.toMutableList()
        Log.d("MyLog", "numbers = $numbers")
        // Проходим по списку и ищем заданное число
        numbers?.set(pos, tempId!!.toInt())





        tempListExercise  = numbers!!.joinToString(separator = ",")
        Log.d("MyLog", "tempList = $tempListExercise")
        updateDay(tempListExercise)
        delay(100L)
    }







    fun updateDay(exercises: String) = viewModelScope.launch {
        delay(200L)
        val cleanedString = exercises.takeIf { it.startsWith(',') }?.removePrefix(",") ?: exercises
        Log.d("MyLog", "updateDayExercise = $cleanedString")
        mainDb.daysDao.insertDay(
            dayModel?.copy(
                doneExerciseCounter = 0,
                isDone = false,
                exercises = cleanedString
            )!!)
    }
}