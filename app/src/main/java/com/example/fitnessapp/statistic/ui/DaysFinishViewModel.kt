package com.example.fitnessapp.statistic.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.db.StatisticModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DaysFinishViewModel @Inject constructor(
    private val mainDb: MainDb
)
    : ViewModel() {



    val statisticData = MutableLiveData<StatisticModel>()


    fun getStatisticByDate(date: String) = viewModelScope.launch {
        statisticData.value = mainDb.statisticDao
            .getStatisticByDate(date) ?: StatisticModel(
            null,
            date,
            0,
            "0"
        )
        /*

        Получить статистику по дате.
        Даже если статистики нет ( null) - мы создадим пустой и отправим (значит пользователь
        ещё не занимался)

         */
    }

}