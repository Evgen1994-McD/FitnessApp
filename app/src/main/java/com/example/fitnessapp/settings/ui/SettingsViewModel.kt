package com.example.fitnessapp.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.MainDb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mainDb: MainDb
): ViewModel() {

    fun clearData() = viewModelScope.launch {
val daysList = mainDb.daysDao.getAllDays()
        daysList.forEach { day ->
            mainDb.daysDao.insertDay(day.copy(
                doneExerciseCounter = 0,
                isDone = false
            ))

        }
        mainDb.statisticDao.clearStatistic()
    }
}