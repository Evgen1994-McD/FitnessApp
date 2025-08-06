package com.example.fitnessapp.statistic.ui

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.exercises.utils.ExerciseHelper
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

@HiltViewModel
class DaysFinishViewModel @Inject constructor(
    private val mainDb: MainDb,
    private val exerciseHelper: ExerciseHelper
) : ViewModel() {
    val statisticData = MutableLiveData<StatisticModel>()
    val eventListData = MutableLiveData<List<EventDay>>()

    fun getStatisticByDate(date: String) = viewModelScope.launch {
        statisticData.value = mainDb.statisticDao
            .getStatisticByDate(date) ?: StatisticModel(null, date, 0, "0")
    }


    fun getStatisticEvents() = viewModelScope.launch {
        val eventList = ArrayList<EventDay>()
        val statisticList = mainDb.statisticDao.getStatistic()
        statisticList.forEach { statisticModel ->
            eventList.add(
                EventDay(
                    TimeUtils.getCalendarFromDate(statisticModel.date),
                    R.drawable.star
                )
            )
            /*
            Здесь получаем статистику и она уходит по обсерверу на фрагмент
             */
        }
        eventListData.value = eventList

    }

    fun addTrainingHarder(difficulty: String) = viewModelScope.launch {

        mainDb.daysDao.getDontDonesDayByDifficulty(difficulty).forEach { day ->
                val exerciseList = mainDb.exerciseDao.getAllExercises()

                val exList = exerciseHelper.getExercisesOfTheDay(day.exercises, exerciseList)

                // Формируем новые идентификаторы упражнений
                val newExIds = exList.map { ex ->
                    val newEx = addExerciseTime(ex)
                    newEx.id.toString()
                }.joinToString(separator = ",")
                Log.d("finish", " Новые АйДи = $newExIds")

                // Обновляем день с новыми идентификаторами упражнений
                mainDb.daysDao.insertDay(day.copy(exercises = newExIds))

        }
    }

    fun reduceTrainingComplexity(difficulty: String) = viewModelScope.launch {

        mainDb.daysDao.getDontDonesDayByDifficulty(difficulty).forEach { day ->
            val exerciseList = mainDb.exerciseDao.getAllExercises()

            val exList = exerciseHelper.getExercisesOfTheDay(day.exercises, exerciseList)

            // Формируем новые идентификаторы упражнений
            val newExIds = exList.map { ex ->
                val newEx = reduceExerciseTime(ex)
                newEx.id.toString()
            }.joinToString(separator = ",")
            Log.d("finish", " Новые АйДи = $newExIds")

            // Обновляем день с новыми идентификаторами упражнений
            mainDb.daysDao.insertDay(day.copy(exercises = newExIds))

        }
    }



    private suspend fun addExerciseTime(exerciseModel: ExerciseModel): ExerciseModel {
        try {
            var replacerWithoutX = ""
            var upX2 = ""
            var stringTime = ""
            if (exerciseModel.time.startsWith("x")) {
                replacerWithoutX = exerciseModel.time.split("x")[1]
                upX2 = ((replacerWithoutX.toInt() * 1.5).roundToInt()).toString()
                stringTime = "x$upX2"
                Log.d("finish", " Новое время = $stringTime")

            } else {
                replacerWithoutX = exerciseModel.time
                upX2 = ((replacerWithoutX.toInt() * 1.5).roundToInt()).toString()
                stringTime = upX2
                Log.d("finish", " Новое время = $stringTime")

            }

            val newEx = exerciseModel.copy(time = stringTime)
            val tempId = mainDb.exerciseDao.insertExercise(newEx.copy(id = null))
            return newEx.copy(id = tempId.toInt())
        } catch (e: IndexOutOfBoundsException) {
            Log.d("MyLog", "Неверный Индекс")
            return exerciseModel
        }
    }

    private suspend fun reduceExerciseTime(exerciseModel: ExerciseModel): ExerciseModel {
        try {
            var replacerWithoutX = ""
            var upX2 = ""
            var stringTime = ""
            if (exerciseModel.time.startsWith("x")) {
                replacerWithoutX = exerciseModel.time.split("x")[1]
                upX2 = ((replacerWithoutX.toInt() / 1.5).roundToInt()).toString()
                stringTime = "x$upX2"
                Log.d("finish", " Новое время = $stringTime")

            } else {
                replacerWithoutX = exerciseModel.time
                upX2 = ((replacerWithoutX.toInt() / 1.5).roundToInt()).toString()
                stringTime = upX2
                Log.d("finish", " Новое время = $stringTime")

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