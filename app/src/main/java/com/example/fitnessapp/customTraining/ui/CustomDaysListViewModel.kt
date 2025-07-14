package com.example.fitnessapp.customTraining.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.customTraining.adapter.CustomDaysAdapter
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.MainDb
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class CustomDaysListViewModel @Inject constructor(
    private val mainDb: MainDb
):ViewModel() {

    val daysListData = mainDb.daysDao.getAllDaysByDifficulty("custom").asLiveData(Dispatchers.Main)

    fun insertDay(dayModel: DayModel) = viewModelScope.launch {
        mainDb.daysDao.insertDay(dayModel)
    }

    fun deleteDay(day:DayModel)= viewModelScope.launch {
        mainDb.daysDao.deleteDay(day)
    }
}