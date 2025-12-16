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
    var year = Calendar.getInstance().get(Calendar.YEAR)
    var month = Calendar.getInstance().get(Calendar.MONTH)

    private val _eventListData = MutableStateFlow<List<EventDay>>(emptyList())
    val eventListData: StateFlow<List<EventDay>> = _eventListData.asStateFlow()
    
    private val _statisticData = MutableStateFlow<StatisticModel?>(null)
    val statisticData: StateFlow<StatisticModel?> = _statisticData.asStateFlow()
    
    private val _weightListData = MutableStateFlow<List<WeightModel>>(emptyList())
    val weightListData: StateFlow<List<WeightModel>> = _weightListData.asStateFlow()
    
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

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
        val cv = Calendar.getInstance()
        val day = cv.get(Calendar.DAY_OF_MONTH)
        val month = cv.get(Calendar.MONTH)
        val year = cv.get(Calendar.YEAR)
        
        // Проверяем, есть ли уже запись веса на сегодня
        val existingWeight = statisticInteractor.getWeightToday(year, month, day)
        
        if (existingWeight != null) {
            // Если запись существует, обновляем её
            statisticInteractor.insertWeight(
                existingWeight.copy(weight = weight)
            )
        } else {
            // Если записи нет, создаём новую
            statisticInteractor.insertWeight(
                WeightModel(
                    null,
                    weight,
                    day,
                    month,
                    year
                )
            )
        }
        getWeightByYearAndMonth()
    }

    fun updateWeight(weightModel: WeightModel) = viewModelScope.launch {
       statisticInteractor.insertWeight(weightModel)
        getWeightByYearAndMonth()
    }

    fun updateYear(newYear: Int) {
        year = newYear
        _selectedYear.value = newYear
        getWeightByYearAndMonth()
    }

    fun updateMonth(newMonth: Int) {
        month = newMonth
        _selectedMonth.value = newMonth
        getWeightByYearAndMonth()
    }

}