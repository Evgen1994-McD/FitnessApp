package com.example.fitnessapp.utils

import android.os.Build
import androidx.fragment.app.Fragment
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import kotlin.math.roundToInt

fun Fragment.getDayFromArguments(): DayModel? {
    return arguments.let { bundle ->
        if (Build.VERSION.SDK_INT >= 33) {
            bundle?.getSerializable("day", DayModel::class.java)

        } else {
            bundle?.getSerializable("day") as DayModel
        }

    }
}

fun Fragment.correctExerciseTime(exerciseModel:ExerciseModel, multiplier:Double): ExerciseModel {
    try {

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

        val newExTemp = exerciseModel.copy(time = stringTime, kcal = exerciseModel.kcal*multiplier)
        return newExTemp.copy(id=null)


    } catch (e: IndexOutOfBoundsException) {
        return exerciseModel
    }
}




