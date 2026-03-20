package com.fit.fitnessapp

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fit.fitnessapp.db.ExerciseModel
import com.fit.fitnessapp.db.MainDb
import com.fit.fitnessapp.exercises.utils.ExerciseHelper
import com.fit.fitnessapp.utils.FirstLaunchChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mainDb: MainDb,
    private val exerciseHelper: ExerciseHelper
) : ViewModel() {  // Через класс ВьюМодел мы "Сохраняем" состояние. То есть, если повернется экран - список не пропадёт и т.д. Его надо подключить к активити и к фрагментам
    companion object{
        const val PREFS_NAME = "AppPrefs"
        const val FIRST_LAUNCH_KEY = "first_launch_completed"

        const val EASY = "easy"
        const val MIDDLE = "middle"
        const val HARD = "hard"
        const val CUSTOM = "custom"
    }

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress

    private val _progressText = MutableLiveData<String>()
    val progressText: LiveData<String> = _progressText

    suspend fun controlFirstCheck(){
        val isFirst = FirstLaunchChecker.isFirstLaunch(context)
        Log.d("SplashViewModel", " isFirstLaunch: $isFirst")
        
        if (isFirst) {
            _progress.value = 10
            _progressText.value = "Настройка упражнений..."
            Log.d("SplashViewModel", " Начинаем addTrainingHarder")
            addTrainingHarder()
            FirstLaunchChecker.markAsLaunched(context)
            _progress.value = 100
            _progressText.value = "Загрузка завершена!"
            Log.d("SplashViewModel", " addTrainingHarder завершен")
        } else {
            Log.d("SplashViewModel", " Запускаем simulateProgress")
            simulateProgress()
        }
    }

    private suspend fun simulateProgress() {
        for (i in 0..100 step 20) {
            _progress.value = i
            _progressText.value = "Загрузка... $i%"
            delay(100)
        }
    }

    suspend fun addTrainingHarder() {
        Log.d("SplashViewModel", " Начинаем addTrainingHarder")
        
        val days = mainDb.daysDao.getAllDays()
        Log.d("SplashViewModel", " Найдено дней: ${days.size}")
        
        // Получаем все упражнения один раз вместо каждого раза
        val exerciseList = mainDb.exerciseDao.getAllExercises()
        Log.d("SplashViewModel", " Всего упражнений в базе: ${exerciseList.size}")
        
        // Группируем дни по сложности для обработки пачками
        val daysByDifficulty = days.groupBy { it.difficulty }
        
        daysByDifficulty.forEach { (difficulty, difficultyDays) ->
            Log.d("SplashViewModel", " Обрабатываем $difficulty: ${difficultyDays.size} дней")
            
            difficultyDays.forEachIndexed { index, day ->
                val globalIndex = days.indexOf(day)
                val exList = exerciseHelper.getExercisesOfTheDay(day.exercises, exerciseList)
                
                // Создаем новые упражнения параллельно
                val newExercises = exList.map { ex ->
                    addExerciseTime(ex, difficulty)
                }
                
                val newExIds = newExercises.joinToString(",") { it.id.toString() }
                mainDb.daysDao.insertDay(day.copy(exercises = newExIds))
                
                // Обновляем прогресс
                val progress = 10 + (globalIndex * 80 / days.size)
                _progress.value = progress
                _progressText.value = "Обновлено ${globalIndex + 1} из ${days.size} дней..."
            }
        }
        
        Log.d("SplashViewModel", " addTrainingHarder завершен")
    }

    private suspend fun addExerciseTime(exerciseModel: ExerciseModel, difficulty: String): ExerciseModel {
        try {
            var multiplier = 1.0
            when(difficulty){
                EASY -> multiplier = 1.25
                MIDDLE -> multiplier = 1.6
                HARD -> multiplier = 2.1
            }
            var replacerWithoutX = ""
            var upX2 = ""
            var stringTime = ""
            if (exerciseModel.time.startsWith("x")) {
                replacerWithoutX = exerciseModel.time.split("x")[1]
                upX2 = ((replacerWithoutX.toInt() * multiplier).roundToInt()).toString()
                stringTime = "x$upX2"

            } else {
                replacerWithoutX = exerciseModel.time
                upX2 = ((replacerWithoutX.toInt() * multiplier).roundToInt()).toString()
                stringTime = upX2

            }

            val newEx = exerciseModel.copy(time = stringTime)
            val tempId = mainDb.exerciseDao.insertExercise(newEx.copy(id = null, kcal = (newEx.kcal)*multiplier))
            return newEx.copy(id = tempId.toInt())
        } catch (e: IndexOutOfBoundsException) {
            return exerciseModel
        }
    }





}