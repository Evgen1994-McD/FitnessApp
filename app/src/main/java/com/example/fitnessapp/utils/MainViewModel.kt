package com.example.fitnessapp.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fitnessapp.adapter.ExerciseModel

class MainViewModel : ViewModel() {  // Через класс ВьюМодел мы "Сохраняем" состояние. То есть, если повернется экран - список не пропадёт и т.д. Его надо подключить к активити и к фрагментам
    val mutableListExercise = MutableLiveData<ArrayList<ExerciseModel>> () // Этот Mutable List - это у нас ArrayList из ExerciseModel. Он поможет нам сохранить состояние, а так же с помощью него View как бы "Подписываются на обновления" и обновляют инфу если возможно


}