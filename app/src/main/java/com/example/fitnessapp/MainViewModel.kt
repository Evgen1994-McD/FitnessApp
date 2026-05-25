package com.example.fitnessapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.settings.domain.SettingsInteractor
import com.example.fitnessapp.statistic.domain.StatisticInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val statisticInteractor: StatisticInteractor
) : ViewModel() {

    private val _streakCount = MutableLiveData<Int>(0)
    val streakCount: LiveData<Int> = _streakCount

    fun loadStreak() {
        viewModelScope.launch {
            val streak = statisticInteractor.getCurrentStreak()
            _streakCount.postValue(streak)
        }
    }

}