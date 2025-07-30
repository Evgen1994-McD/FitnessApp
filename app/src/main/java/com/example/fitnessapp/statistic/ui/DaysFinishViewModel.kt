package com.example.fitnessapp.statistic.ui

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.exercises.utils.ExerciseHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltViewModel
class DaysFinishViewModel @Inject constructor(
    private val mainDb: MainDb,
    private val exerciseHelper: ExerciseHelper
) : ViewModel() {
    val statisticData = MutableLiveData<StatisticModel>()

    fun getStatisticByDate(date: String) = viewModelScope.launch {
        statisticData.value = mainDb.statisticDao
            .getStatisticByDate(date) ?: StatisticModel(null, date, 0, "0")
    }

    fun addTrainingHarder(difficulty: String) = viewModelScope.launch {

        mainDb.daysDao.getDontDonesDayByDifficulty(difficulty).collect { list ->
            list.forEach { day ->
                val exerciseList = mainDb.exerciseDao.getAllExercises()

                val exList = exerciseHelper.getExercisesOfTheDay(day.exercises, exerciseList)

                // Формируем новые идентификаторы упражнений
                val newExIds = exList.map { ex ->
                    val newEx = addExerciseTime(ex)
                    newEx.id.toString()
                }.joinToString(separator = ",")

                // Обновляем день с новыми идентификаторами упражнений
                mainDb.daysDao.insertDay(day.copy(exercises = newExIds))
            }
        }
    }

    suspend fun addExerciseTime(exerciseModel: ExerciseModel): ExerciseModel {
        try {
            var replacerWithoutX = ""
            var upX2 = ""
            var stringTime = ""
            if (exerciseModel.time.startsWith("x")) {
                replacerWithoutX = exerciseModel.time.split("x")[1]
                upX2 = (replacerWithoutX.toInt() * 2).toString()
                stringTime = "x$upX2"
            } else {
                replacerWithoutX = exerciseModel.time
                upX2 = (replacerWithoutX.toInt() * 2).toString()
                stringTime = upX2
            }

            val newEx = exerciseModel.copy(time = stringTime)
            val tempId = mainDb.exerciseDao.insertExercise(newEx.copy(id = null))
            return newEx.copy(id = tempId.toInt())
        } catch (e: IndexOutOfBoundsException) {
            Log.d("MyLog", "Неверный Индекс")
            return exerciseModel
        }
    }
}