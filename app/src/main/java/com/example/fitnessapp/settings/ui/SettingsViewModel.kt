package com.example.fitnessapp.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.exercises.domain.DaysInteractor
import com.example.fitnessapp.exercises.domain.models.ThemeMode
import com.example.fitnessapp.settings.domain.SettingsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val daysInteractor: DaysInteractor
): ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsInteractor.getThemeMode().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val voiceTipsEnabled: StateFlow<Boolean> = settingsInteractor.getVoiceTipsEnabled().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    // LiveData для отслеживания прогресса открытия тренировок
    private val _openTrainingsProgress = MutableLiveData<Float>(0f)
    val openTrainingsProgress: LiveData<Float> = _openTrainingsProgress
    
    private val _isOpeningTrainings = MutableLiveData<Boolean>(false)
    val isOpeningTrainings: LiveData<Boolean> = _isOpeningTrainings



    fun clearData() = viewModelScope.launch {
        settingsInteractor.clearData()
    }

    fun switchTheme(mode: ThemeMode) = viewModelScope.launch {
        settingsInteractor.setThemeMode(mode)
    }

    fun openAllTrainings() = viewModelScope.launch {
        _isOpeningTrainings.value = true
        _openTrainingsProgress.value = 0f
        
        settingsInteractor.openAllTrainings { progress ->
            _openTrainingsProgress.value = progress
        }
        
        // Небольшая задержка чтобы Room обновил данные
        delay(100)
        _isOpeningTrainings.value = false
        _openTrainingsProgress.value = 1f
    }

    fun toggleVoiceTips(enabled: Boolean) = viewModelScope.launch {
        settingsInteractor.setVoiceTipsEnabled(enabled)
    }


}