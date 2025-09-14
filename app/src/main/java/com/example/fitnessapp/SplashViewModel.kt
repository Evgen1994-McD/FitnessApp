package com.example.fitnessapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.exercises.utils.ExerciseHelper
import com.example.fitnessapp.utils.FirstLaunchChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
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

    suspend fun controlFirstCheck(){
        if (FirstLaunchChecker.isFirstLaunch(context)) {
            addTrainingHarder()
            FirstLaunchChecker.markAsLaunched(context)
        }
    }



    suspend fun addTrainingHarder() {

        mainDb.daysDao.getAllDays().forEach { day ->
            val exerciseList = mainDb.exerciseDao.getAllExercises()

            val exList = exerciseHelper.getExercisesOfTheDay(day.exercises, exerciseList)

            // Формируем новые идентификаторы упражнений
            val newExIds = exList.map { ex ->
                val newEx = addExerciseTime(ex, day.difficulty)
                newEx.id.toString()
            }.joinToString(separator = ",")

            // Обновляем день с новыми идентификаторами упражнений
            mainDb.daysDao.insertDay(day.copy(exercises = newExIds))

        }
    }





    private suspend fun addExerciseTime(exerciseModel: ExerciseModel, difficulty: String): ExerciseModel {
        try {
            var multiplier = 1.0
            when(difficulty){
                EASY -> multiplier = 1.125
                MIDDLE -> multiplier = 1.4
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