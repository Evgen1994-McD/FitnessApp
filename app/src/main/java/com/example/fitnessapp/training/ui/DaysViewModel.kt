package com.example.fitnessapp.training.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.MainDb
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel //пометили аннотацией что это модел Хилт
// будем вызывать БД из МейнМодел
class DaysViewModel @Inject constructor(
    private val mainDb: MainDb, // получили доступ к базе данных
) : ViewModel() { //если БД инициализирована мы её найдём  в МейнМодуль
val daysList = MutableLiveData<List<DayModel>>() // список дней с тренировками

    fun getExerciseDaysByDifficulty ( difficulty : String) {
        viewModelScope.launch {  /* это трудоёмкая операция, поэтому делаем
        в корутинах */
            mainDb.daysDao.getAllDaysByDifficulty(difficulty).collect { /* collect -
            получить то что найдём в БД */
                list ->
daysList.value = list // передали лист который нашли
            }

        }
    }

}