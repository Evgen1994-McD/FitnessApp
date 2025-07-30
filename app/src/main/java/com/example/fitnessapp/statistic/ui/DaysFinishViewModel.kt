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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltViewModel
class DaysFinishViewModel @Inject constructor(
    private val mainDb: MainDb,
    private val exerciseHelper: ExerciseHelper

)
    : ViewModel() {
    var tempId: Long? = null
    private var dayModel: DayModel? = null

    val statisticData = MutableLiveData<StatisticModel>()


    fun getStatisticByDate(date: String) = viewModelScope.launch {
        statisticData.value = mainDb.statisticDao
            .getStatisticByDate(date) ?: StatisticModel(
            null,
            date,
            0,
            "0"
        )
        /*

        Получить статистику по дате.
        Даже если статистики нет ( null) - мы создадим пустой и отправим (значит пользователь
        ещё не занимался)

         */

    }

    private fun addTrainingHarder(difficulty: String) = viewModelScope.launch {
        val exerciseList = mainDb.exerciseDao.getAllExercises()

        mainDb.daysDao.getDontDonesDayByDifficulty(difficulty).collect { list ->
            list.forEach {
                dayModel = it
                var exList = exerciseHelper.getExercisesOfTheDay(
                    it.exercises,
                    exerciseList
                ).forEach {
                    val newEx = addExerciseTime(it)
                    it.id = newEx.id

                }
                mainDb.daysDao.insertDay()
            }
        }


    }


    suspend fun addExerciseTime(exerciseModel: ExerciseModel): ExerciseModel {
        try {


            /*
        функция для настройки времени упражнений ( кастом)
         */


            var replacerWithoutX = ""
            var upX2 = ""
            var stringTime = ""
            if (exerciseModel.time.startsWith("x")) {
                replacerWithoutX = ((exerciseModel.time).split("x"))[1]
                upX2 = (replacerWithoutX.toInt() * 2).toString()
                stringTime = "x$upX2"
            } else {
                replacerWithoutX = exerciseModel.time
                upX2 = (replacerWithoutX.toInt() * 2).toString()
                stringTime = upX2

            }

            Log.d("MyLog", stringTime)
            val newEx = exerciseModel.copy(time = stringTime)
            tempId = mainDb.exerciseDao.insertExercise(newEx.copy(id = null))

            return newEx


        } catch (e: IndexOutOfBoundsException) {
            Log.d("MyLog", "Неверный Индекс")
//        } catch (e: NumberFormatException) {
//            Toast.makeText(context, "Ошибка: Невозможно преобразовать строку в число.", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            Toast.makeText(context, "Возникла неизвестная ошибка.", Toast.LENGTH_SHORT).show()
//        }
            return exerciseModel
        }


    }

}