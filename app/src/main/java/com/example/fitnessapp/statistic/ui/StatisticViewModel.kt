package com.example.fitnessapp.statistic.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val mainDb: MainDb
) : ViewModel() {
    val eventListData = MutableLiveData<List<EventDay>>()

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

}