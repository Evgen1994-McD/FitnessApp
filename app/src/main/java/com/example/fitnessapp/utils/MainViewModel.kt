package com.example.fitnessapp.utils

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fitnessapp.db.ExerciseModel

class MainViewModel : ViewModel() {  // Через класс ВьюМодел мы "Сохраняем" состояние. То есть, если повернется экран - список не пропадёт и т.д. Его надо подключить к активити и к фрагментам
    val mutableListExercise = MutableLiveData<ArrayList<ExerciseModel>> () // Этот Mutable List - это у нас ArrayList из ExerciseModel. Он поможет нам сохранить состояние, а так же с помощью него View как бы "Подписываются на обновления" и обновляют инфу если возможно
var pref : SharedPreferences? = null // будем сохранять количество выполненных упражнений
var currentDay = 0 // переменная для того чтобы через DayFragment записывать текущий день в ш.преф
     fun savePref(key : String, value : Int){
        pref?.edit()?.putInt(key, value)?.apply()   // будем сохрянть значение количества выполненных дней
    }


    fun getExerciseCount(): Int {
       return pref?.getInt(currentDay.toString(), 0) ?:0   // с помощью оператора элвиса мы выведем 0, в том случае, если преф не инициализирован. (?:) - оператор выдаёт то, что находится справа, если слева null

    }
}