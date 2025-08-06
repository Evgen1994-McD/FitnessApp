package com.example.fitnessapp.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import androidx.compose.ui.text.intl.Locale
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")

object TimeUtils {
    val formatter = SimpleDateFormat("mm:ss")
    val workoutFormatter = SimpleDateFormat("HH'h':mm'm'", )
    val cvFormatter = SimpleDateFormat("dd/MM/yyyy")


    fun getTime(time:Long): String{
        val cv = Calendar.getInstance()
        cv.timeInMillis = time
        return formatter.format(cv.time)
    }  // Собственно, это функция для перевода времени в МС, урок 17.

    fun getWorkoutTime(time:Long): String{
        val cv = Calendar.getInstance()
        cv.timeInMillis = time
        return workoutFormatter.format(cv.time)
    }  // Собственно, это функция для перевода времени в МС, урок 17.



    fun getCurrentDate(): String{
        val cv = Calendar.getInstance()
        return cvFormatter.format(cv.time)
/*
"dd/MM/yyyy" - функция вернет нам время в таком формате
 */
    }

    fun getDateFromCalendar(c: java.util.Calendar) : String {
        return cvFormatter.format(c.time)

            /*
            Здесб передали дату в виде календарь,
            А вернули объект стринг
             */
        }



    fun getCalendarFromDate(date: String) : java.util.Calendar{
        return java.util.Calendar.getInstance().apply {
            time = cvFormatter.parse(date) as Date

            /*
            Здесб передали дату в String, и он выдал календарь на основе этого стринг
             */
        }
    }


}