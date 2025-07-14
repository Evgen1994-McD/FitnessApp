package com.example.fitnessapp.customTraining.selectedExerciseList.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.DayModel
import com.example.fitnessapp.db.MainDb
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SelectedExerciseListViewModel @Inject constructor(
    private val mainDb: MainDb
) : ViewModel() {
val dayData = MutableLiveData<DayModel>()

    fun getDayById(id: Int) = viewModelScope.launch {
        dayData.value = mainDb.daysDao.getDay(id)
    }
}