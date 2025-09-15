package com.example.fitnessapp.exercises.ui

import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.exercises.domain.ExerciseInteractor
import com.example.fitnessapp.exercises.utils.ExerciseHelper
import com.example.fitnessapp.utils.MySoundPool
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val execiseInteractor: ExerciseInteractor,
    private val exerciseHelper: ExerciseHelper,
    private val tts: TextToSpeech,
    private val soundPool: MySoundPool
) : ViewModel() {
    var updateExercise = MutableLiveData<ExerciseModel>()
    var updateTime = MutableLiveData<Long>()
    var updateToolbar = MutableLiveData<String>()
    private var timer: CountDownTimer? = null // переменная для таймера
    var currentDay: DayModel? = null
    var nextDay: DayModel? = null
    var statisticModel: StatisticModel? = null // глобал переменная для получения статистики
    private var exercisesOfTheDay: List<ExerciseModel> = emptyList()
    /*
    открывая переменную на уровне класа, тем самым мы делаем её глобальной
     */
    private var exercisesStack: List<ExerciseModel> =
        emptyList() // изначально пустой список с упражнениями который мы заполним позже
    private var doneExerciseCounter = 0 // это счётчик для упражнений ( функция nextExercise() )
    private var doneExerciseCounterToSave = 0
    private var totalExerciseNumber = 0

    private fun updateDay(dayModel: DayModel) = viewModelScope.launch {
       execiseInteractor.updateDay(dayModel)
    }

    fun getAndOpenNextDay()= viewModelScope.launch {
        var nextId = ((currentDay?.id)?.plus(1)) ?: 0
        if (nextId!=0) {
            try {
                nextDay = execiseInteractor.getDayById(nextId)
                    nextDay = nextDay!!.copy(isOpen = true)
                    updateDay(nextDay!!)
            }catch (e:Exception){

            }
        }
    }



    private fun isDayDone() {
        if (totalExerciseNumber == doneExerciseCounterToSave - 1) {
            currentDay = currentDay?.copy(isDone = true)
            currentDay?.let {
                updateDay(it)
            }
            getAndOpenNextDay()

        }
        /*
        currentDay передаём тот же, но перезапишем параметр isDone чтобы поставить галочку
         */
    }

    private fun getStatistic() = viewModelScope.launch {
        val currentDate = TimeUtils.getCurrentDate()
        statisticModel = execiseInteractor.getStatisticByDate(currentDate) // Если статистика есть выдаст, если нет - то выдаст null

    }

    private fun createStatistic() : StatisticModel {
        var kcal = 0.0
        var time = 0
exercisesOfTheDay.subList(0, doneExerciseCounterToSave-1).forEach { model ->
    kcal += (model.kcal)/2
    /*
    поделю статистику на 3( слишком много калорий считает)
     */
    time += getTimeFromExercise(model)

}
        /*
        exercisesOfTheDay суб лист от 0 до элемента doneExerciseCounter мы перебираем с помощью цикла forEach
         */

        return statisticModel?.copy(
            kcal = statisticModel!!.kcal+kcal,
            workoutTime = (statisticModel!!.workoutTime.toInt()+ time).toString(),
            completedExercise = statisticModel!!.completedExercise + doneExerciseCounterToSave -1

        ) ?: StatisticModel(
            null,  // если id null то запишется новый id в статистик модел ( мы указали стратегию)
            TimeUtils.getCurrentDate(),
            kcal = kcal,
            workoutTime = time.toString(),
            completedExercise = doneExerciseCounterToSave-1
            )

        /*
        таким образом мы либо перезаписываем статистику ( если уже получили её до этого и она
        не null, либо создаём новую статистику ( оператор элвиса) и передаём её
         */
    }

    private fun getTimeFromExercise(exerciseModel: ExerciseModel): Int{
        return if (exerciseModel.time.startsWith("x")){
            exerciseModel.time.replace("x", "").toInt()*4
        }
        else exerciseModel.time.toInt()
    }

    fun getExercises(dayModel: DayModel) = viewModelScope.launch {
        currentDay = execiseInteractor.getCurrentDay(dayModel)

        val exerciseList = execiseInteractor.getAllExerciseList()
        exercisesOfTheDay = exerciseHelper.getExercisesOfTheDay(
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
        getStatistic()
        nextExercise()


    }


    fun startTimer(time: Long) {
        timer = object : CountDownTimer(
            (time + 1) * 1000, 1000 // интервал запускается каждую секунду
        ) { //мы сделали тут 100 мс для того чтобы прогресс бар шел плавно, вот и всё. Если бы было 1000, то были бы большие скачки.
            override fun onTick(restTime: Long) {
                updateTime.value = restTime
                speechLastDigits(restTime)


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
        speechExercise(exercise)
        updateExercise.value = exercise

        /*
        будем запускать и передавать по обсерверу следующее упражнение на View через лайв дата.
        (Далее после проекта добавить не мутабл лайв дата)
         */
    }

    private fun speechExercise(exerciseModel: ExerciseModel){
        if (exerciseModel.subtitle.startsWith("Отдохните")) {
        speechText(
            "${exerciseModel.subtitle}. ${exerciseModel.name}"
        )
        }else {
            speechText(
                "${exerciseModel.subtitle}. ${exerciseModel.name} " +
                getTimeToSpeech(exerciseModel)
            )
        }
    }

    private fun getTimeToSpeech(exerciseModel: ExerciseModel): String {
        return if(exerciseModel.time.startsWith("x")) {
            "${exerciseModel.time.replace("x", " ")} раз"
        } else {
            "${exerciseModel.time} секунд "
        }
    }


    private fun speechLastDigits(time: Long){
        if (time<= 0) return
        val timeInSeconds = (time/1000).toInt() // время в секундах превратили его в Интеджер, чтобы округлить
        if (timeInSeconds==0){
            soundPool.playSound()
            return
        }
        if (timeInSeconds<4) {
            speechText(timeInSeconds.toString())
        }
    }

    private fun speechText(text:String){
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ut_id" )

        /*
        queue mode у текст спит
        QueueAdd - произносит по очереди
        QueueFlash - заменяет прошлый текст
        Мы делаем Флеш - потому что если пользователь перескочит на другое упражнение, нам не надо чтобы
        произносило все подрялд из прошлого занятия

       Параметры мы передали null - у нас нет параметнов
       utteranceldId - нам не понадобится поэтому написали что в голову взброело
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
        tts.stop() // если пользователь вышел с фрагмента, то перестанет воспроизводить текст

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

        viewModelScope.launch {
          execiseInteractor.insertStatistic(createStatistic())
        }
    }
    /*
    currentDay это экземпляр ДейМодел, и мы записываем готов день или нет
    Дело в том, что когда мы увеличиваем переменную инкрементом ++, то до того как покажет она увеличенное значение,
    Оно уже будет увеличено. Поэтому нам нужно отнять -1 если значение не 0.
    Если 0 - то не отнимать, показать 0 ( пользователь ничего не проходил)

    Теперь толчно будем начинать с упражнения на котором остановились
     */
}