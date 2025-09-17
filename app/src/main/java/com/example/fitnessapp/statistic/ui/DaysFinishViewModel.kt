package com.example.fitnessapp.statistic.ui


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.exercises.utils.ExerciseHelper
import com.example.fitnessapp.statistic.domain.StatisticInteractor
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

@HiltViewModel
class DaysFinishViewModel @Inject constructor(
    private val statisticInteractor: StatisticInteractor,
    private val exerciseHelper: ExerciseHelper
) : ViewModel() {
    private var multiplierAdd = 1.5
    private var multiplierDecrease = 0.7
    val statisticData = MutableLiveData<StatisticModel>()
    val statisticMonthData = MutableLiveData<DaysFinishStateList>()
    val eventListData = MutableLiveData<List<EventDay>>()
    private lateinit var  statisticList:List<StatisticModel>


    fun getStatisticByDate(date: String) = viewModelScope.launch {
        statisticData.value = statisticInteractor.getStatisticByDate(date)
    }

    private fun getWorkoutMonthStatistic(){
        statisticList.forEach { statisticModel ->
            val calendarFronStatistic = statisticModel.date//TimeUtils.getCalendarFromDate(statisticModel.date)
            val currentCalendar = TimeUtils.getCurrentDate()

            if(calendarFronStatistic.get(Calendar.MONTH).toChar() == currentCalendar.get(Calendar.MONTH)){
                if (statisticMonthData.value != null) {
                    val updatedValue = statisticMonthData.value!!.copy(
                        trainingCounter = statisticMonthData.value!!.trainingCounter + 1,
                        kcal = statisticMonthData.value!!.kcal + statisticModel.kcal
                    )
                    statisticMonthData.value = updatedValue
                } else {
                    // Если нет данных, то создаёшь новую запись
                    statisticMonthData.value = DaysFinishStateList(trainingCounter = 1, kcal = statisticModel.kcal, date = "0", workoutTime =  "0")
                }
            }


        }

    }


    fun getStatisticEvents() = viewModelScope.launch {
        val eventList = ArrayList<EventDay>()
        statisticList =  statisticInteractor.getStatistic()
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
        getWorkoutMonthStatistic()

    }

    fun addTrainingHarder(difficulty: String) = viewModelScope.launch {

        statisticInteractor.getDontDoesDaysByDifficulty(difficulty).forEach { day ->
                val exerciseList = statisticInteractor.getAllExercise()

                val exList = exerciseHelper.getExercisesOfTheDay(day.exercises, exerciseList)

                // Формируем новые идентификаторы упражнений
                val newExIds = exList.map { ex ->
                    val newEx = addExerciseTime(ex)

                    newEx.id.toString()
                }.joinToString(separator = ",")

                // Обновляем день с новыми идентификаторами упражнений
                statisticInteractor.insertDay(day.copy(exercises = newExIds))

        }
    }

    fun reduceTrainingComplexity(difficulty: String) = viewModelScope.launch {

        statisticInteractor.getDontDoesDaysByDifficulty(difficulty).forEach { day ->
            val exerciseList = statisticInteractor.getAllExercise()

            val exList = exerciseHelper.getExercisesOfTheDay(day.exercises, exerciseList)

            // Формируем новые идентификаторы упражнений
            val newExIds = exList.map { ex ->
                val newEx = reduceExerciseTime(ex)
                newEx.id.toString()
            }.joinToString(separator = ",")

            // Обновляем день с новыми идентификаторами упражнений
            statisticInteractor.insertDay(day.copy(exercises = newExIds))

        }
    }



    private suspend fun addExerciseTime(exerciseModel: ExerciseModel): ExerciseModel {
        try {

            var replacerWithoutX = ""
            var upX2 = ""
            var stringTime = ""
            if (exerciseModel.time.startsWith("x")) {
                replacerWithoutX = exerciseModel.time.split("x")[1]
                upX2 = ((replacerWithoutX.toInt() * multiplierAdd).roundToInt()).toString()
                stringTime = "x$upX2"

            } else {
                replacerWithoutX = exerciseModel.time
                upX2 = ((replacerWithoutX.toInt() * multiplierAdd).roundToInt()).toString()
                stringTime = upX2

            }

            val newEx = exerciseModel.copy(time = stringTime, kcal = exerciseModel.kcal*multiplierAdd)
            val tempId = statisticInteractor.insertExercise(newEx.copy(id = null))
            return newEx.copy(id = tempId.toInt())
        } catch (e: IndexOutOfBoundsException) {
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
                upX2 = ((replacerWithoutX.toInt() / multiplierAdd).roundToInt()).toString()
                stringTime = "x$upX2"

            } else {
                replacerWithoutX = exerciseModel.time
                upX2 = ((replacerWithoutX.toInt() / multiplierAdd).roundToInt()).toString()
                stringTime = upX2

            }

            val newEx = exerciseModel.copy(time = stringTime, kcal = exerciseModel.kcal/multiplierAdd)
            val tempId = statisticInteractor.insertExercise(newEx.copy(id = null))
            return newEx.copy(id = tempId.toInt())
        } catch (e: IndexOutOfBoundsException) {
            return exerciseModel
        }
    }




}