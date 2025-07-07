package com.example.fitnessapp.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")

object TimeUtils {
    val formatter = SimpleDateFormat("mm:ss")
    val cvFormatter = SimpleDateFormat("dd/MM/yyyy")
    fun getTime(time:Long): String{
        val cv = Calendar.getInstance()
        cv.timeInMillis = time
        return formatter.format(cv.time)
    }  // Собственно, это функция для перевода времени в МС, урок 17.


    fun getCalendarFromDate(date: String) : java.util.Calendar{
        return java.util.Calendar.getInstance().apply {
            time = cvFormatter.parse(date) as Date

            /*
            Здесб передали дату в String, и он выдал календарь на основе этого стринг
             */
        }
    }


}