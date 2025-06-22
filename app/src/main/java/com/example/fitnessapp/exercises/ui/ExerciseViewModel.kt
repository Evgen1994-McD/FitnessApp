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

    private var timer: CountDownTimer? = null // переменная для таймера

    var currentDay: DayModel? = null
    private var exercisesStack : List<ExerciseModel> = emptyList() // изначально пустой список с упражнениями который мы заполним позже
    private var doneExerciseCounter = 0 // это счётчик для упражнений ( функция nextExercise() )

    private fun updateDay(dayModel: DayModel) = viewModelScope.launch {
        mainDb.daysDao.insertDay(dayModel)
    }

    private fun dayDone(){
        currentDay = currentDay?.copy(isDone = true)
        currentDay?.let {
            updateDay(it)
        }
/*
currentDay передаём тот же, но перезапишем параметр isDone чтобы поставить галочку
 */
    }

    fun getExercises(dayModel: DayModel) {
        currentDay = dayModel
        viewModelScope.launch {
            val exerciseList = mainDb.exerciseDao.getAllExercises()
            val exercisesOfTheDay = exerciseHelper.getExercisesOfTheDay(
                dayModel.exercises,
                exerciseList
                )
            exercisesStack = exerciseHelper.createExerciseStack(
                exercisesOfTheDay.subList(
                    dayModel.doneExerciseCounter,
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

    }


    fun startTimer(time : Long) {


        timer = object : CountDownTimer(
            (time+1) * 1000, 1
        ) { //мы сделали тут 100 мс для того чтобы прогресс бар шел плавно, вот и всё. Если бы было 1000, то были бы большие скачки.
            override fun onTick(restTime: Long) {
                updateTime.value = restTime


            }

            override fun onFinish() {
                nextExercise()
            } // тут мы переделали, он не вызывает фрагмент, а запускает следующее упражнение при завершении таймера
        }.start()  // обязательно указываем старт для нашего таймера
    }

    fun nextExercise(){
        timer?.cancel() // отключили таймер чтобы не запускался предыдущий на всякий случай

        val exercise = exercisesStack[doneExerciseCounter++]
        updateExercise.value = exercise

        /*
        будем запускать и передавать по обсерверу следующее упражнение на View через лайв дата.
        (Далее после проекта добавить не мутабл лайв дата)
         */
    }

    fun onPause(){
        timer?.cancel()
    }

}