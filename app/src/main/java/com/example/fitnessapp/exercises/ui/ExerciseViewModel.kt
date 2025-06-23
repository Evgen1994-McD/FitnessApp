package com.example.fitnessapp.exercises.ui

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.exercises.utils.ExerciseHelper
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val mainDb: MainDb,
    private val exerciseHelper: ExerciseHelper
) : ViewModel() {
    var updateExercise = MutableLiveData<ExerciseModel>()
    var updateTime = MutableLiveData<Long>()
    var updateToolbar = MutableLiveData<String>()
    private var timer: CountDownTimer? = null // переменная для таймера
    var currentDay: DayModel? = null
    private var exercisesStack: List<ExerciseModel> =
        emptyList() // изначально пустой список с упражнениями который мы заполним позже
    private var doneExerciseCounter = 0 // это счётчик для упражнений ( функция nextExercise() )
    private var doneExerciseCounterToSave = 0
    private var totalExerciseNumber = 0

    private fun updateDay(dayModel: DayModel) = viewModelScope.launch {
        mainDb.daysDao.insertDay(dayModel)
    }

    private fun isDayDone() {
        if (totalExerciseNumber == doneExerciseCounterToSave - 1) {
            currentDay = currentDay?.copy(isDone = true)
            currentDay?.let {
                updateDay(it)
            }
        }
        /*
        currentDay передаём тот же, но перезапишем параметр isDone чтобы поставить галочку
         */
    }

    fun getExercises(dayModel: DayModel) = viewModelScope.launch {
        currentDay = dayModel.id?.let { mainDb.daysDao.getDay(it) }

        val exerciseList = mainDb.exerciseDao.getAllExercises()
        val exercisesOfTheDay = exerciseHelper.getExercisesOfTheDay(
            dayModel.exercises,
            exerciseList
        )
        doneExerciseCounterToSave = currentDay?.doneExerciseCounter ?: 0
        totalExerciseNumber = dayModel.exercises.split(",").size

        exercisesStack = exerciseHelper.createExerciseStack(
            exercisesOfTheDay.subList(
                currentDay?.doneExerciseCounter ?: 0,
                exercisesOfTheDay.size
                /*
                Нас интересуют только невыполненные упражнениня.
                Поэтому мы берем СУБ лист, в котором указываем с какой по какую позицию взять элементы из нашего массива.
                Для нас это от doneExerciseCounter - это наша созданная переменная специально для этого случая чтобы понять сколько выполнено упражнений.
                И до конца массива

                Если понадобится сбросить - можно в дальнейшем встроить вопрос ( желаете ли продолжить)
                 */
            )

        )
        nextExercise()


    }


    fun startTimer(time: Long) {


        timer = object : CountDownTimer(
            (time + 1) * 1000, 1
        ) { //мы сделали тут 100 мс для того чтобы прогресс бар шел плавно, вот и всё. Если бы было 1000, то были бы большие скачки.
            override fun onTick(restTime: Long) {
                updateTime.value = restTime


            }

            override fun onFinish() {
                nextExercise()
            } // тут мы переделали, он не вызывает фрагмент, а запускает следующее упражнение при завершении таймера
        }.start()  // обязательно указываем старт для нашего таймера
    }

    fun nextExercise() {
        timer?.cancel() // отключили таймер чтобы не запускался предыдущий на всякий случай
        updateToolbar()
        val exercise = exercisesStack[doneExerciseCounter++]
        updateExercise.value = exercise

        /*
        будем запускать и передавать по обсерверу следующее упражнение на View через лайв дата.
        (Далее после проекта добавить не мутабл лайв дата)
         */
    }

    private fun updateToolbar() {
        if (doneExerciseCounter % 2 == 0) { // если счётчик делится на 2 то считаем и обновляем, если нет то нет
            val text = "Выполнено: ${doneExerciseCounterToSave++} / $totalExerciseNumber"
            updateToolbar.value = text
        }
    }

    fun onPause() {
        timer?.cancel()
        isDayDone()
        updateDay(
            currentDay!!.copy(
                doneExerciseCounter = if (doneExerciseCounterToSave > 0) {
                    doneExerciseCounterToSave - 1
                } else {
                    0
                }
            )
        )
    }
    /*
    currentDay это экземпляр ДейМодел, и мы записываем готов день или нет
    Дело в том, что когда мы увеличиваем переменную инкрементом ++, то до того как покажет она увеличенное значение,
    Оно уже будет увеличено. Поэтому нам нужно отнять -1 если значение не 0.
    Если 0 - то не отнимать, показать 0 ( пользователь ничего не проходил)

    Теперь толчно будем начинать с упражнения на котором остановились
     */
}