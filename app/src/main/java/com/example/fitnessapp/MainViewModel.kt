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

    private val _trainingDaysTitle = MutableLiveData<String>("")
    val trainingDaysTitle: LiveData<String> = _trainingDaysTitle

    private val _exerciseTitle = MutableLiveData<String>("")
    val exerciseTitle: LiveData<String> = _exerciseTitle

    fun loadStreak() {
        viewModelScope.launch {
            val streak = statisticInteractor.getCurrentStreak()
            _streakCount.postValue(streak)
        }
    }

    fun updateTrainingDaysTitle(title: String) {
        _trainingDaysTitle.value = title
    }

    fun updateExerciseTitle(title: String) {
        _exerciseTitle.value = title
    }

}