package com.example.fitnessapp.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import androidx.compose.ui.text.intl.Locale
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

@SuppressLint("SimpleDateFormat")

object TimeUtils {
    val formatter = SimpleDateFormat("mm:ss")
    val workoutFormatter = SimpleDateFormat("HH'h':mm'm'")

    val cvFormatter = SimpleDateFormat("dd/MM/yyyy")


    fun getTime(time:Long): String{
        val seconds = (time / 1000) % 60
        val minutes = (time / (1000 * 60)) % 60
        val hours = (time / (1000 * 60 * 60)) % 24
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }  // Форматируем интервал времени в ММ:СС или ЧЧ:ММ:СС

    fun getWorkoutTime(time: Long): String {
        val cv = GregorianCalendar(TimeZone.getTimeZone("UTC"))
        cv.timeInMillis = time
        workoutFormatter.timeZone = TimeZone.getTimeZone("UTC")
        return workoutFormatter.format(cv.time)
    }


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

    // Новый метод для форматирования LocalDate в тот же формат что и остальные
    fun formatLocalDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }


}