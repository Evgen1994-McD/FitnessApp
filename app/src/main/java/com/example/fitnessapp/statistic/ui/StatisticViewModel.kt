package com.example.fitnessapp.statistic.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.data.DateSelectorModel
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val mainDb: MainDb
) : ViewModel() {
    var year = -1
    var month = Calendar.getInstance().get(Calendar.MONTH)
    val eventListData = MutableLiveData<List<EventDay>>()
    val statisticData = MutableLiveData<StatisticModel>()
    val yearListData = MutableLiveData<List<DateSelectorModel>>()

    val weightListData = MutableLiveData<List<WeightModel>>()

    fun getStatisticEvents() = viewModelScope.launch {
        val eventList = ArrayList<EventDay>()
        val statisticList = mainDb.statisticDao.getStatistic()
        statisticList.forEach { statisticModel ->
eventList.add(
    EventDay(
        TimeUtils.getCalendarFromDate(statisticModel.date),
        R.drawable.dotsonround
    )
)
            /*
            Здесь получаем статистику и она уходит по обсерверу на фрагмент
             */
        }
eventListData.value = eventList

    }



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

    fun getYearList() = viewModelScope.launch {
        val tempYearList = ArrayList<DateSelectorModel>()
        val weightList = mainDb.weightDao.getAllWeightList()
       weightList.forEach { weightModel ->

           if(!tempYearList.any{ it.text.toInt() == weightModel.year }){
               tempYearList.add(DateSelectorModel(
                   weightModel.year.toString()
               ))
           }


/*
Будем перебирать все записи. Как только наткнемся на год например 2020 -
записываем, остальные года 2020 пропускаем пока не дойдём до 2021 и так далее
!tempYearList.any{ it.text.toInt() == weightModel.year } - с помощью any проверяем содержится данный год или нет
 */
       }
        if(!tempYearList.isNullOrEmpty()){
yearListData.value = tempYearList
       } else {
           tempYearList.add(DateSelectorModel(Calendar.getInstance().get(Calendar.YEAR).toString()
           ))
           yearListData.value = tempYearList

    }
    }

    fun getWeightByYearAndMonth() = viewModelScope.launch {
        weightListData.value = mainDb.weightDao.getMonthWeightList(
            year,
            month
        )
    }

    fun saveWeight(weight:Int) = viewModelScope.launch {
        val cv =Calendar.getInstance()
        mainDb.weightDao.insertWeight(
            WeightModel(
                null,
                weight,
                cv.get(Calendar.DAY_OF_MONTH),
                cv.get(Calendar.MONTH),
                cv.get(Calendar.YEAR)
            )
        )
        getWeightByYearAndMonth()
    }

    fun updateWeight(weightModel: WeightModel) = viewModelScope.launch {
        mainDb.weightDao.insertWeight(weightModel)
        getWeightByYearAndMonth()

    }

}