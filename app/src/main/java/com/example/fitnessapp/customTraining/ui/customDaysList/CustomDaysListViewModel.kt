package com.example.fitnessapp.customTraining.ui.customDaysList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun insertDay(dayModel: DayModel) = viewModelScope.launch {
       customInteractor.insertDay(dayModel)
    }

    fun deleteDay(day:DayModel)= viewModelScope.launch {
        customInteractor.deleteDay(day)
    }
    
    fun setLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
}