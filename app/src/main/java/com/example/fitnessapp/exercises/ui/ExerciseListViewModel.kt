package com.example.fitnessapp.exercises.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.ExerciseModel
import com.example.fitnessapp.db.MainDb
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

/* для каждого объекта будет свой вью модел */
@HiltViewModel //анннотация Хилт вьюмодел
class ExerciseListViewModel @Inject constructor( // инжект для того, чтобы передать конструктор с базой данных
    private val mainDb: MainDb  // Daggger подключил нам базу данных с помощью которой можем получать список
) : ViewModel() {
    val exerciseList =
        MutableLiveData<List<ExerciseModel>>() // сюда мы с базы данных будем передавать данные, а затем получать список с помощью обсервера уже на фрагменте

    fun getDayExerciseList(dayModel: DayModel?) =
        viewModelScope.launch { // запускаем в корутине, потому что сложная операция
            val day =
                dayModel?.id?.let { mainDb.daysDao.getDay(it) } // выбарается самый свежий день из базы данных по id

            val allExerciseList = mainDb.exerciseDao.getAllExercises() // получаем список со всеми упражнениями который будем потом перебирать
            val tempExerciseList = ArrayList<ExerciseModel>()
            day?.let { dayModel ->
dayModel.exercises.split(",").forEach{ index ->
tempExerciseList.add(allExerciseList[index.toInt()])  // сюда добавляем номера упражнений данного дня, а ЗАТЕМ по номеру достаём нужные из allExrciseList и помещать в новый список (exerciseList)
} // как в первой части мы разделяем по запятой, в результате получим массив в виде объекта String
            }
exerciseList.value = tempExerciseList // передали упражнения
        }
            /* теперь остаётся все передать в функцию и поставить обсервер чтобы следить за изменениями. Когда переберём списко - сразу выдаст этот список и передадим его уже в адаптер */

}