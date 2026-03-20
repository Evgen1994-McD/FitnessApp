package com.fit.fitnessapp.exercises.ui.exercise

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit.fitnessapp.db.DayModel
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.db.StatisticModel
import com.fit.fitnessapp.exercises.domain.ExerciseInteractor
import com.fit.fitnessapp.exercises.utils.ExerciseHelper
import com.fit.fitnessapp.settings.domain.SettingsInteractor
import com.fit.fitnessapp.utils.MySoundPool
import com.fit.fitnessapp.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val execiseInteractor: ExerciseInteractor,
    private val exerciseHelper: ExerciseHelper,
    private val tts: TextToSpeech,
    private val soundPool: MySoundPool,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {
    var updateExercise = MutableLiveData<ExerciseModel>()
    var updateTime = MutableLiveData<Long>()
    var updateToolbar = MutableLiveData<String>()
    private var timer: CountDownTimer? = null // переменная для таймера
    var currentTimerValue: Long? = null // текущее значение таймера
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
    private val handler = Handler(Looper.getMainLooper())
    private var currentAdviceRunnable: Runnable? = null
    private var usedAdvices = mutableSetOf<String>().toMutableSet() // Отслеживаем использованные советы

    private fun updateDay(dayModel: DayModel) = viewModelScope.launch {
       execiseInteractor.updateDay(dayModel)
    }

    fun getAndOpenNextDay()= viewModelScope.launch {
        currentDay?.let { day ->
            execiseInteractor.getAndOpenNextDay(day)
        }
    }



    private fun isDayDone() {
        Log.d("ExerciseViewModel", "DEBUG: totalExerciseNumber = $totalExerciseNumber, doneExerciseCounterToSave = $doneExerciseCounterToSave")
        if (totalExerciseNumber == doneExerciseCounterToSave - 1) {
            Log.d("ExerciseViewModel", "DEBUG: Условие выполнено, устанавливаем isDone = true")
            val todayDate = TimeUtils.getCurrentDate()
            currentDay = currentDay?.copy(isDone = true, completedDate = todayDate)
            currentDay?.let {
                updateDay(it)
            }
            getAndOpenNextDay()

        } else {
            Log.d("ExerciseViewModel", "DEBUG: Условие НЕ выполнено, isDone остается false")
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
        Log.d("ExerciseViewModel", "DEBUG: Создаем статистику для даты: currentDay?.completedDate = ${currentDay?.completedDate}")
        var kcal = 0.0
        var time = 0
exercisesOfTheDay.subList(0, doneExerciseCounterToSave-1).forEach { model ->
    val tempMultiplier = if (model.time.contains('x')) {

//если содержит x значит нужно умножть колличество повторов на количество минут за одно исполненеия


        model.time.substringAfter('x').toInt()
    }
    else model.time.toInt()/60
    kcal += (model.kcal) * tempMultiplier

    time += getTimeFromExercise(model)

}


        // Всегда создаем новую запись статистики для каждой тренировки
        return StatisticModel(
            null,  // если id null то запишется новый id в статистик модел ( мы указали стратегию)
            dayId = currentDay?.id, // Устанавливаем связь с тренировкой
            currentDay?.completedDate ?: TimeUtils.getCurrentDate(), // Используем дату тренировки
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
        doneExerciseCounterToSave = dayModel.doneExerciseCounter // Исправляю получение doneExerciseCounter - беру значение из переданного dayModel, а не из currentDay из базы
        totalExerciseNumber = dayModel.exercises.split(",").size

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
        getStatistic()
        nextExercise()


    }


    fun startTimer(time: Long) {
        timer = object : CountDownTimer(
            (time + 1) * 1000, 1000 // интервал запускается каждую секунду
        ) { //мы сделали тут 100 мс для того чтобы прогресс бар шел плавно, вот и всё. Если бы было 1000, то были бы большие скачки.
            override fun onTick(restTime: Long) {
                currentTimerValue = restTime // сохраняем текущее значение таймера
                updateTime.value = restTime
                speechLastDigits(restTime)


            }

            override fun onFinish() {
                nextExercise()
            } // тут мы переделали, он не вызывает фрагмент, а запускает следующее упражнение при завершении таймера
        }.start()  // обязательно указываем старт для нашего таймера
    }

    fun pauseTimer() {
        timer?.cancel()
    }

    fun pauseAdvice() {
        // Отменяем все отложенные советы
        currentAdviceRunnable?.let { handler.removeCallbacks(it) }
    }

    fun resumeAdvice() {
        // Возобновляем советы если они были активны
        currentAdviceRunnable?.let { runnable ->
            // Планируем следующий совет через 2-3 секунды после возобновления
            val nextDelay = Random.nextInt(2000, 3001)
            handler.postDelayed(runnable, nextDelay.toLong())
        }
    }

    fun updateTimerValue(newTime: Long) {
        currentTimerValue = newTime
        updateTime.value = newTime
        // Перезапускаем таймер с новым значением
        timer?.cancel()
        timer = object : CountDownTimer(
            (newTime + 1) * 1000, 1000
        ) {
            override fun onTick(restTime: Long) {
                currentTimerValue = restTime
                updateTime.value = restTime
                speechLastDigits(restTime)
            }

            override fun onFinish() {
                nextExercise()
            }
        }.start()
    }

    fun getNextExercise(): ExerciseModel? {
        return if (doneExerciseCounter < exercisesStack.size) {
            exercisesStack[doneExerciseCounter]
        } else {
            null
        }
    }

    fun nextExercise() {
        timer?.cancel() // отключили таймер чтобы не запускался предыдущий на всякий случай
        // Отменяем предыдущий отложенный совет если он есть
        currentAdviceRunnable?.let { handler.removeCallbacks(it) }
        currentAdviceRunnable = null
        usedAdvices.clear()
        updateToolbar()
        
        // Проверяем, что есть еще упражнения для показа
        if (doneExerciseCounter < exercisesStack.size) {
            val exercise = exercisesStack[doneExerciseCounter++]
            speechExercise(exercise)
            updateExercise.value = exercise
        } else {
            // Если упражнений больше нет, не делаем ничего
            // Фрагмент сам перейдет на экран статистики
        }

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
            // Добавляем небольшую задержку для первого упражнения, чтобы TTS успел инициализироваться
            val speechText = if (exerciseModel.subtitle.startsWith("Приготовьтесь")) {
                // Для "Приготовьтесь" не произносим время
                "${exerciseModel.subtitle}. ${exerciseModel.name}"
            } else {
                // Для обычных упражнений произносим время
                "${exerciseModel.subtitle}. ${exerciseModel.name} " + getTimeToSpeech(exerciseModel)
            }
            
            if (doneExerciseCounter == 1) {
                // Для первого упражнения добавляем задержку
                handler.postDelayed({
                    speechText(speechText)
                    // Добавляем случайный совет только для упражнений (не для отдыха и не для "Приготовьтесь")
                    if (!exerciseModel.subtitle.startsWith("Приготовьтесь")) {
                        speechRandomAdvice(exerciseModel)
                    }
                }, 500)
            } else {
                speechText(speechText)
                // Добавляем случайный совет только для упражнений (не для отдыха и не для "Приготовьтесь")
                if (!exerciseModel.subtitle.startsWith("Приготовьтесь")) {
                    speechRandomAdvice(exerciseModel)
                }
            }
        }
    }

    private fun getTimeToSpeech(exerciseModel: ExerciseModel): String {
        return if(exerciseModel.time.startsWith("x")) {
            val count = exerciseModel.time.replace("x", " ").trim().toInt()
            "${count} ${getRepsWord(count)}"
        } else {
            // Используем запятую вместо пробела, чтобы TTS не склонял число
            "${exerciseModel.time}, секунд"
        }
    }

    private fun getRepsWord(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "раз"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "раза"
            else -> "раз"
        }
    }

    private fun speechRandomAdvice(exerciseModel: ExerciseModel) {
        if (exerciseModel.advise.isBlank()) return
        
        // Проверяем включены ли голосовые советы
        viewModelScope.launch {
            val voiceTipsEnabled = settingsInteractor.getVoiceTipsEnabled().collect { enabled ->
                if (!enabled) return@collect // Если советы выключены, выходим
                
                val adviceList = exerciseModel.advise.split("||").map { it.trim() }.filter { it.isNotEmpty() }
                
                if (adviceList.isNotEmpty()) {
                    startAdviceRepeater(adviceList)
                }
            }
        }
    }

    private fun startAdviceRepeater(adviceList: List<String>) {
        // Отменяем предыдущий повторитель если есть
        currentAdviceRunnable?.let { handler.removeCallbacks(it) }
        
        // Создаем новый повторитель
        currentAdviceRunnable = object : Runnable {
            override fun run() {
                // Находим неиспользованные советы
                val availableAdvices = adviceList.filter { it !in usedAdvices }
                
                if (availableAdvices.isEmpty()) {
                    // Все советы уже использованы, прекращаем повторения
                    currentAdviceRunnable = null
                    return
                }
                
                val randomAdvice = availableAdvices[Random.nextInt(availableAdvices.size)]
                speechTextWithQueue(randomAdvice)
                
                // Добавляем совет в использованные
                usedAdvices.add(randomAdvice)
                
                // Планируем следующий совет через 5-10 секунд
                val nextDelay = Random.nextInt(12000, 20000)
                handler.postDelayed(this, nextDelay.toLong())
            }
        }
        
        // Начинаем первый совет через 5-10 секунд
        val firstDelay = Random.nextInt(5000, 10001)
        handler.postDelayed(currentAdviceRunnable!!, firstDelay.toLong())
    }

    private fun speechTextWithQueue(text: String) {
        viewModelScope.launch {
            settingsInteractor.getVoiceTipsEnabled().collect { enabled ->
                if (enabled) {
                    tts.speak(text, TextToSpeech.QUEUE_ADD, null, "advice_id")
                }
            }
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
        //queue mode у текст спит
        //QueueAdd - произносит по очереди
        //QueueFlash - заменяет прошлый текст
        //Мы делаем Флеш - потому что если пользователь перескочит на другое упражнение, нам не надо чтобы
        //произносило все подрялд из прошлого занятия

       //Параметры мы передали null - у нас нет параметнов
       //utteranceldId - нам не понадобится поэтому написали что в голову взброело
         //QueueAdd - произносит по очереди
         //QueueFlash - заменяет прошлый текст
         //Мы делаем Флеш - потому что если пользователь перескочит на другое упражнение, нам не надо чтобы
         //произносило все подрялд из прошлого занятия

       //Параметры мы передали null - у нас нет параметнов
       //utteranceldId - нам не понадобится поэтому написали что в голову взброело
    }

    private fun updateToolbar() {
        if (doneExerciseCounter % 2 == 0) { // если счётчик делится на 2 то считаем и обновляем, если нет то нет
            val text = "Выполнено: ${doneExerciseCounterToSave++} / $totalExerciseNumber"
            updateToolbar.value = text
        }
    }




    fun onPause() {
        timer?.cancel()
        tts.stop()
        handler.removeCallbacksAndMessages(null) // Очищаем все отложенные задачи

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