package com.example.fitnessapp.statistic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.applandeo.materialcalendarview.EventDay
import com.example.fitnessapp.R
import com.example.fitnessapp.db.MainDb
import com.example.fitnessapp.db.StatisticModel
import com.example.fitnessapp.db.WeightModel
import com.example.fitnessapp.statistic.domain.StatisticInteractor
import com.example.fitnessapp.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val statisticInteractor: StatisticInteractor
) : ViewModel() {
    var year = -1
    var month = Calendar.getInstance().get(Calendar.MONTH)

    private val _eventListData = MutableStateFlow<List<EventDay>>(emptyList())
    val eventListData: StateFlow<List<EventDay>> = _eventListData.asStateFlow()
    
    private val _statisticData = MutableStateFlow<StatisticModel?>(null)
    val statisticData: StateFlow<StatisticModel?> = _statisticData.asStateFlow()
    
    private val _weightListData = MutableStateFlow<List<WeightModel>>(emptyList())
    val weightListData: StateFlow<List<WeightModel>> = _weightListData.asStateFlow()

    fun getStatisticEvents() = viewModelScope.launch {
        val eventList = ArrayList<EventDay>()
        val statisticList = statisticInteractor.getStatistic()
        statisticList.forEach { statisticModel ->
eventList.add(
    EventDay(
        TimeUtils.getCalendarFromDate(statisticModel.date),
        R.drawable.star
    )
)
            /*
            Здесь получаем статистику и она уходит по обсерверу на фрагмент
             */
        }
_eventListData.value = eventList

    }



    fun getStatisticByDate(date: String) = viewModelScope.launch {
_statisticData.value = statisticInteractor.getStatisticByDate(date)

/*

Получить статистику по дате.
Даже если статистики нет ( null) - мы создадим пустой и отправим (значит пользователь
ещё не занимался)

 */
    }




    fun getWeightByYearAndMonth() = viewModelScope.launch {
_weightListData.value = statisticInteractor.getWeightByYearAndMonth(
            year,
            month
        )
    }

    fun saveWeight(weight: Double) = viewModelScope.launch {
        val cv =Calendar.getInstance()
        statisticInteractor.insertWeight(
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
       statisticInteractor.insertWeight(weightModel)
        getWeightByYearAndMonth()

    }

}