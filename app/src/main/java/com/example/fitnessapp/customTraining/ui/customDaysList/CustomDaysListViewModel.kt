package com.example.fitnessapp.customTraining.ui.customDaysList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.customTraining.domain.CustomInteractor
import com.example.fitnessapp.db.DayModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomDaysListViewModel @Inject constructor(
    private val customInteractor: CustomInteractor
):ViewModel() {
    companion object{
        const val customDifficulty = "custom"
    }

    val daysListData = customInteractor.getAllDaysByDifficulty(customDifficulty).asLiveData(Dispatchers.Main)

    fun insertDay(dayModel: DayModel) = viewModelScope.launch {
       customInteractor.insertDay(dayModel)
    }

    fun deleteDay(day:DayModel)= viewModelScope.launch {
        customInteractor.deleteDay(day)
    }
}